package com.example.engine

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.SystemClock
import com.example.data.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Performs app-cache cleanup and public TCP connectivity diagnostics.
 * Does not change another app's FPS, CPU policy, DNS configuration or network route.
 */
class AutoBoostEngine(private val context: Context) {
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val cleanupMutex = Mutex()
    private val networkMutex = Mutex()
    private val _isOptimizing = MutableStateFlow(false)
    val isOptimizing = _isOptimizing.asStateFlow()
    private val _optimizationStep = MutableStateFlow("")
    val optimizationStep = _optimizationStep.asStateFlow()
    private val _lastBoostResult = MutableStateFlow<BoostResult?>(null)
    val lastBoostResult = _lastBoostResult.asStateFlow()
    private val _pingServers = MutableStateFlow<List<PingServerTarget>>(DEFAULT_PING_SERVERS)
    val pingServers = _pingServers.asStateFlow()
    private val _isTestingNetwork = MutableStateFlow(false)
    val isTestingNetwork = _isTestingNetwork.asStateFlow()
    private val _isTestingSingleServer = MutableStateFlow(false)
    val isTestingSingleServer = _isTestingSingleServer.asStateFlow()

    fun setServerCategory(category: String) {
        _pingServers.value = if (category == "regional") REGIONAL_PING_SERVERS else ULTRA_LOW_PING_SERVERS
    }

    private fun applyProbe(target: PingServerTarget, result: DetailedPingResult) = target.copy(
        currentPingMs = result.pingMs,
        jitterMs = result.jitterMs,
        packetLossPercent = result.packetLossPercent,
        isFailed = !result.isSuccess,
        status = result.statusText,
        lastTestedTimestamp = System.currentTimeMillis()
    )

    suspend fun testSingleServer(target: PingServerTarget) = networkMutex.withLock {
        _isTestingSingleServer.value = true
        try {
            val result = measureDetailedServerPing(target.host, target.port)
            // Update only matching targets; switching category during a test must not undo the switch.
            _pingServers.update { current -> current.map {
                if (it.id == target.id) applyProbe(it, result) else it
            } }
        } finally {
            _isTestingSingleServer.value = false
        }
    }

    suspend fun testAllServers() = networkMutex.withLock {
        _isTestingNetwork.value = true
        try {
            val targets = _pingServers.value
            val tested = coroutineScope {
                targets.map { target -> async { applyProbe(target, measureDetailedServerPing(target.host, target.port)) } }.awaitAll()
            }.associateBy { it.id }
            _pingServers.update { current -> current.map { tested[it.id] ?: it } }
        } finally {
            _isTestingNetwork.value = false
        }
    }

    suspend fun scanRunningProcesses(): List<HeavyProcessItem> = withContext(Dispatchers.IO) {
        val excluded = setOf(context.packageName, "com.android.systemui", "android")
        val running = try { activityManager.runningAppProcesses.orEmpty() } catch (_: SecurityException) { emptyList() }
        running.mapNotNull { info ->
            val pkg = info.pkgList?.firstOrNull() ?: info.processName
            if (pkg in excluded) return@mapNotNull null
            val app = try { context.packageManager.getApplicationInfo(pkg, 0) } catch (_: Exception) { null }
            val pss = try {
                activityManager.getProcessMemoryInfo(intArrayOf(info.pid)).firstOrNull()?.totalPss ?: 0
            } catch (_: Exception) { 0 }
            val mb = pss.toLong() / 1024
            HeavyProcessItem(
                packageName = pkg,
                appName = app?.let { context.packageManager.getApplicationLabel(it).toString() } ?: pkg,
                pid = info.pid,
                memoryUsageMb = mb,
                cpuUsagePercent = -1f,
                isHeavy = mb >= 100,
                isSystem = app != null && (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                category = "Visible to Android public APIs"
            )
        }.sortedByDescending { it.memoryUsageMb }
    }

    private fun availableMemory(): Long {
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return info.availMem
    }

    /** Deletes only this app's disposable cache files; never game data or system caches. */
    private suspend fun deleteOwnCache(): Pair<Long, Int> = withContext(Dispatchers.IO) {
        var bytes = 0L
        var failures = 0
        val cacheRoot = context.cacheDir
        cacheRoot.walkBottomUp().onFail { _, _ -> failures++ }.forEach { file ->
            currentCoroutineContext().ensureActive()
            if (file != cacheRoot) {
                try {
                    val length = if (file.isFile) file.length() else 0L
                    if (file.delete()) bytes += length else failures++
                } catch (_: SecurityException) {
                    failures++
                }
            }
        }
        bytes to failures
    }

    suspend fun executeAutoBoost(isArabic: Boolean = false, cleanCache: Boolean = true, checkNetwork: Boolean = true): BoostResult = cleanupMutex.withLock {
        _isOptimizing.value = true
        try {
            withContext(Dispatchers.IO) {
                _optimizationStep.value = if (isArabic) "قياس الذاكرة وفحص اتصال TCP..." else "Measuring memory and TCP connectivity..."
                val before = availableMemory()
                val beforePing = if (checkNetwork) measureRealSocketLatency("1.1.1.1", 53) else -1
                _optimizationStep.value = if (isArabic) "حذف كاش GlassBoost القابل للحذف..." else "Deleting disposable GlassBoost cache..."
                val (deleted, failures) = if (cleanCache) deleteOwnCache() else (0L to 0)
                val after = availableMemory()
                val afterPing = if (checkNetwork) measureRealSocketLatency("1.1.1.1", 53) else -1
                BoostResult(
                    // Observed system available-memory increase, not proof of memory reclaimed.
                    ramFreedMb = ((after - before) / (1024 * 1024)).coerceAtLeast(0),
                    processesKilledCount = 0,
                    killedProcessNames = emptyList(),
                    initialPingMs = beforePing,
                    optimizedPingMs = afterPing,
                    networkJitterReducedMs = 0,
                    cacheDeletedBytes = deleted,
                    cleanupFailures = failures
                ).also { _lastBoostResult.value = it }
            }
        } finally {
            _optimizationStep.value = ""
            _isOptimizing.value = false
        }
    }

    /**
     * Android 14+ only allows killing one's own processes. A successful request on older
     * versions still does not prove termination. UI uses App Info for manual management.
     */
    suspend fun killSingleProcess(packageName: String): Boolean = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= 34 || packageName == context.packageName) return@withContext false
        try {
            activityManager.killBackgroundProcesses(packageName)
        } catch (_: SecurityException) {}
        false
    }

    suspend fun clearCachedBackgroundMemory(): Long = executeAutoBoost().ramFreedMb

    /** Optional warm-up for this app only; each disabled option really skips its operation. */
    suspend fun optimizeNetworkRouting(
        resolveDns: Boolean = true,
        warmTcp: Boolean = true
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val before = measureRealSocketLatency("1.1.1.1", 53)
        if (resolveDns) coroutineScope {
            listOf("dns.google", "cloudflare-dns.com", "store.steampowered.com", "epicgames.com").map { host ->
                async(Dispatchers.IO) {
                    try { InetAddress.getAllByName(host) } catch (_: java.net.UnknownHostException) { null }
                }
            }.awaitAll()
        }
        if (warmTcp) measureRealSocketLatency("1.1.1.1", 53)
        before to measureRealSocketLatency("1.1.1.1", 53)
    }

    suspend fun measureRealSocketLatency(host: String, port: Int): Int = withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        val start = SystemClock.elapsedRealtime()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), 1500)
                (SystemClock.elapsedRealtime() - start).toInt()
            }
        } catch (_: java.io.IOException) { -1 }
        catch (_: SecurityException) { -1 }
        catch (_: IllegalArgumentException) { -1 }
    }

    suspend fun measureDetailedServerPing(host: String, port: Int, probeCount: Int = 3): DetailedPingResult =
        withContext(Dispatchers.IO) {
            require(probeCount in 1..10)
            val samples = mutableListOf<Int>()
            repeat(probeCount) {
                currentCoroutineContext().ensureActive()
                val measured = measureRealSocketLatency(host, port)
                if (measured >= 0) samples.add(measured)
                if (it < probeCount - 1) delay(80)
            }
            summarizeTcpProbes(samples, probeCount)
        }
}

/** Failed TCP connection attempts are not game packet loss. */
internal fun summarizeTcpProbes(latencies: List<Int>, attempts: Int): DetailedPingResult {
    require(attempts > 0 && latencies.size <= attempts && latencies.all { it >= 0 })
    val jitter = if (latencies.size >= 2) latencies.zipWithNext { a, b ->
        kotlin.math.abs(a.toLong() - b.toLong())
    }.average().toInt() else null
    return DetailedPingResult(
        pingMs = latencies.takeIf { it.isNotEmpty() }?.average()?.toInt(),
        jitterMs = jitter,
        packetLossPercent = ((attempts - latencies.size) * 100 / attempts),
        isSuccess = latencies.isNotEmpty(),
        statusText = if (latencies.isEmpty()) "تعذر اتصال TCP" else "TCP connect time"
    )
}

data class DetailedPingResult(
    val pingMs: Int?,
    val jitterMs: Int?,
    val packetLossPercent: Int,
    val isSuccess: Boolean,
    val statusText: String
)
