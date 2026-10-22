package com.example.engine

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.os.SystemClock
import com.example.data.model.PerformanceSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.max
import kotlin.math.min

/**
 * Real-time hardware performance monitor that tracks FPS, network ping,
 * CPU, GPU, RAM, and thermal metrics.
 */
class PerformanceMonitorEngine(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val _currentMetrics = MutableStateFlow(
        PerformanceSnapshot(
            timestamp = System.currentTimeMillis(),
            fps = -1,
            pingMs = -1,
            cpuUsagePercent = -1,
            gpuUsagePercent = -1,
            ramUsedMb = 0,
            ramTotalMb = 0,
            batteryTemp = -1f,
            thermalState = "Unknown"
        )
    )
    val currentMetrics: StateFlow<PerformanceSnapshot> = _currentMetrics.asStateFlow()

    private var monitoringJob: Job? = null
    private var lastCpuTotal: Long = 0
    private var lastCpuIdle: Long = 0

    fun start(scope: CoroutineScope) {
        if (monitoringJob != null && monitoringJob?.isActive == true) return

        monitoringJob = scope.launch(Dispatchers.Default) {
            while (isActive) {
                val ping = measureSocketPing("1.1.1.1", 53)
                val (usedRam, totalRam) = getRamInfo()
                val cpuUsage = readCpuUsage()
                val gpuUsage = readGpuUsagePercent()
                val (temp, thermal) = readBatteryAndThermal()

                val snapshot = PerformanceSnapshot(
                    timestamp = System.currentTimeMillis(),
                    // No public Android API exposes another app's game FPS.
                    fps = -1,
                    pingMs = ping,
                    cpuUsagePercent = cpuUsage,
                    gpuUsagePercent = gpuUsage,
                    ramUsedMb = usedRam,
                    ramTotalMb = totalRam,
                    batteryTemp = temp,
                    thermalState = thermal
                )

                _currentMetrics.value = snapshot
                delay(1000)
            }
        }
    }

    fun stop() {
        monitoringJob?.cancel()
        monitoringJob = null
    }

    private fun getRamInfo(): Pair<Long, Long> {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalMb = memoryInfo.totalMem / (1024 * 1024)
        val availMb = memoryInfo.availMem / (1024 * 1024)
        val usedMb = max(0L, totalMb - availMb)
        return Pair(usedMb, totalMb)
    }

    private suspend fun measureSocketPing(host: String, port: Int): Int = withContext(Dispatchers.IO) {
        val startTime = SystemClock.elapsedRealtime()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 800)
                val latency = (SystemClock.elapsedRealtime() - startTime).toInt()
                latency
            }
        } catch (_: Exception) {
            -1 // Unreachable/blocked is unavailable, never fabricate a ping.
        }
    }

    private fun readCpuUsage(): Int {
        try {
            val reader = RandomAccessFile("/proc/stat", "r")
            val load = reader.readLine()
            reader.close()
            if (load != null) {
                val tokens = load.split("\\s+".toRegex())
                if (tokens.size >= 5) {
                    val user = tokens[1].toLong()
                    val nice = tokens[2].toLong()
                    val system = tokens[3].toLong()
                    val idle = tokens[4].toLong()
                    val iowait = if (tokens.size > 5) tokens[5].toLong() else 0L
                    val irq = if (tokens.size > 6) tokens[6].toLong() else 0L
                    val softirq = if (tokens.size > 7) tokens[7].toLong() else 0L

                    val total = user + nice + system + idle + iowait + irq + softirq
                    val totalDiff = total - lastCpuTotal
                    val idleDiff = idle - lastCpuIdle

                    lastCpuTotal = total
                    lastCpuIdle = idle

                    if (totalDiff > 0) {
                        val usage = ((totalDiff - idleDiff) * 100 / totalDiff).toInt()
                        return usage.coerceIn(0, 100)
                    }
                }
            }
        } catch (_: Exception) {}

        return -1 // /proc/stat is unavailable or unreadable; do not invent CPU usage.
    }

    private fun readGpuUsagePercent(): Int {
        // Only accept kernel nodes that explicitly report GPU busy percentage.
        val gpuPaths = listOf(
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
            "/sys/kernel/gpu/gpu_busy"
        )
        for (path in gpuPaths) {
            try {
                val file = File(path)
                if (file.exists() && file.canRead()) {
                    val content = file.readText().trim().removeSuffix("%").trim()
                    val parsed = content.toIntOrNull()
                    if (parsed != null && parsed in 0..100) return parsed
                }
            } catch (_: Exception) {}
        }
        return -1 // Most retail Android devices do not expose GPU utilization to apps.
    }

    private fun readBatteryAndThermal(): Pair<Float, String> {
        var tempC = -1f
        try {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            if (intent != null) {
                val rawTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
                if (rawTemp >= 0) tempC = rawTemp / 10.0f
            }
        } catch (_: Exception) {}

        val thermalState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Cool (بارد)"
                PowerManager.THERMAL_STATUS_LIGHT -> "Normal (معتدل)"
                PowerManager.THERMAL_STATUS_MODERATE -> "Warm (دافئ)"
                PowerManager.THERMAL_STATUS_SEVERE -> "Throttling (ساخن)"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Critical (حرج)"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency (طوارئ)"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown imminent (إيقاف وشيك)"
                else -> "Unknown (غير متاح)"
            }
        } else {
            when {
                tempC < 0f -> "Unknown (غير متاح)"
                tempC < 35f -> "Cool (بارد)"
                tempC < 40f -> "Normal (معتدل)"
                tempC < 44f -> "Warm (دافئ)"
                else -> "Hot (ساخن)"
            }
        }

        return Pair(tempC, thermalState)
    }
}
