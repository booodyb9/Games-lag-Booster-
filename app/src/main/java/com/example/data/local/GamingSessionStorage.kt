package com.example.data.local

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.example.data.model.GameItem
import com.example.data.model.GamingSession
import com.example.data.model.PerformanceSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Handles persistent storage of gaming sessions, telemetry snapshots,
 * active recording state, and installed games catalogue.
 */
class GamingSessionStorage(private val context: Context) {

    private val prefs = context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE)

    private val _sessions = MutableStateFlow<List<GamingSession>>(emptyList())
    val sessions: StateFlow<List<GamingSession>> = _sessions.asStateFlow()

    private val _games = MutableStateFlow<List<GameItem>>(emptyList())
    val games: StateFlow<List<GameItem>> = _games.asStateFlow()

    // Active in-game recording state
    private var isRecording = false
    private var activeSessionId: String? = null
    private var activeGameTitle: String = ""
    private var activePackageName: String = ""
    private var activeStartTime: Long = 0L
    private val activeSnapshots = mutableListOf<PerformanceSnapshot>()

    private val _isRecordingSession = MutableStateFlow(false)
    val isRecordingSession: StateFlow<Boolean> = _isRecordingSession.asStateFlow()

    private val _activeRecordingGame = MutableStateFlow("")
    val activeRecordingGame: StateFlow<String> = _activeRecordingGame.asStateFlow()

    init {
        loadSessions()
        loadGames()
    }

    private fun loadSessions() {
        val rawJson = prefs.getString("saved_sessions", null)
        if (rawJson.isNullOrEmpty()) {
            _sessions.value = emptyList()
        } else {
            try {
                val list = parseSessionsFromJson(rawJson)
                _sessions.value = list.filterNot { it.id.startsWith("demo-") }
            } catch (_: Exception) {
                _sessions.value = emptyList()
            }
        }
    }

    private fun loadGames() {
        val rawJson = prefs.getString("saved_games", null)
        if (!rawJson.isNullOrEmpty()) {
            try {
                val list = parseGamesFromJson(rawJson)
                if (list.isNotEmpty()) {
                    _games.value = list
                    return
                }
            } catch (_: Exception) {}
        }

        // Scan installed launchable apps and games
        val discoveredGames = scanInstalledGames()
        _games.value = discoveredGames
        saveGamesToPrefs(discoveredGames)
    }

    fun startRecording(gameTitle: String, packageName: String) {
        isRecording = true
        activeSessionId = UUID.randomUUID().toString()
        activeGameTitle = gameTitle
        activePackageName = packageName
        activeStartTime = System.currentTimeMillis()
        activeSnapshots.clear()
        _isRecordingSession.value = true
        _activeRecordingGame.value = gameTitle
    }

    fun appendSnapshot(snapshot: PerformanceSnapshot) {
        if (!isRecording) return
        activeSnapshots.add(snapshot)
    }

    fun stopRecording(): GamingSession? {
        if (!isRecording || activeSnapshots.isEmpty()) {
            isRecording = false
            _isRecordingSession.value = false
            _activeRecordingGame.value = ""
            return null
        }

        val endTime = System.currentTimeMillis()
        val duration = ((endTime - activeStartTime) / 1000).coerceAtLeast(1)

        val fpsValues = activeSnapshots.map { it.fps }.filter { it >= 0 }
        fun average(values: List<Int>): Float =
            values.filter { it >= 0 }.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: -1f
        val avgFps = average(fpsValues)
        val minFps = fpsValues.minOrNull() ?: -1
        val maxFps = fpsValues.maxOrNull() ?: -1
        val avgPing = average(activeSnapshots.map { it.pingMs }).toInt()
        val maxPing = activeSnapshots.map { it.pingMs }.filter { it >= 0 }.maxOrNull() ?: -1
        val avgCpu = average(activeSnapshots.map { it.cpuUsagePercent })
        val avgGpu = average(activeSnapshots.map { it.gpuUsagePercent })
        val stability = if (fpsValues.size >= 2) {
            (100 - ((maxFps - minFps) * 1.5f)).toInt().coerceIn(0, 100)
        } else -1

        val session = GamingSession(
            id = activeSessionId ?: UUID.randomUUID().toString(),
            gameTitle = activeGameTitle,
            packageName = activePackageName,
            startTime = activeStartTime,
            endTime = endTime,
            durationSeconds = duration,
            averageFps = (avgFps * 10).toInt() / 10f,
            minFps = minFps,
            maxFps = maxFps,
            averagePing = avgPing,
            maxPing = maxPing,
            averageCpu = (avgCpu * 10).toInt() / 10f,
            averageGpu = (avgGpu * 10).toInt() / 10f,
            ramFreedMb = 0L,
            stabilityScore = stability,
            snapshots = activeSnapshots.toList()
        )

        val updated = listOf(session) + _sessions.value
        _sessions.value = updated
        saveSessionsToPrefs(updated)

        isRecording = false
        _isRecordingSession.value = false
        _activeRecordingGame.value = ""
        activeSnapshots.clear()
        return session
    }

    fun deleteSession(sessionId: String) {
        val updated = _sessions.value.filterNot { it.id == sessionId }
        _sessions.value = updated
        saveSessionsToPrefs(updated)
    }

    fun addCustomGame(name: String, packageName: String) {
        val item = GameItem(
            packageName = packageName,
            appName = name,
            isCustom = true,
            targetFps = 90,
            resolutionPreset = "FHD (1080p)",
            isTurboEnabled = true,
            lastPlayedDate = "Added from device"
        )
        if (_games.value.none { it.packageName == packageName }) {
            val updated = listOf(item) + _games.value
            _games.value = updated
            saveGamesToPrefs(updated)
        }
    }

    fun addGameFromDevice(game: GameItem) {
        if (_games.value.none { it.packageName == game.packageName }) {
            val updated = listOf(game) + _games.value
            _games.value = updated
            saveGamesToPrefs(updated)
        }
    }

    fun updateGameProfile(game: GameItem) {
        require(game.targetFps in listOf(60, 90, 120))
        val updated = if (_games.value.any { it.packageName == game.packageName }) {
            _games.value.map { if (it.packageName == game.packageName) game else it }
        } else _games.value + game
        _games.value = updated
        saveGamesToPrefs(updated)
    }

    fun removeGame(packageName: String) {
        val updated = _games.value.filterNot { it.packageName == packageName }
        _games.value = updated
        saveGamesToPrefs(updated)
    }

    fun getInstalledLaunchableApps(): List<GameItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val list = mutableListOf<GameItem>()
        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) continue
            val label = info.loadLabel(pm).toString()
            list.add(
                GameItem(
                    packageName = pkg,
                    appName = label,
                    isCustom = true,
                    targetFps = 90,
                    resolutionPreset = "FHD (1080p)",
                    isTurboEnabled = true,
                    lastPlayedDate = "Installed on Device"
                )
            )
        }
        return list.sortedBy { it.appName.lowercase() }
    }

    private fun scanInstalledGames(): List<GameItem> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
        val list = mutableListOf<GameItem>()

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            if (pkg == context.packageName) continue
            val label = info.loadLabel(pm).toString()

            // Detect typical games or popular game keywords
            val isLikelyGame = label.contains("game", ignoreCase = true) ||
                    label.contains("play", ignoreCase = true) ||
                    label.contains("craft", ignoreCase = true) ||
                    label.contains("racing", ignoreCase = true) ||
                    label.contains("pubg", ignoreCase = true) ||
                    label.contains("cod", ignoreCase = true)

            if (isLikelyGame) {
                list.add(
                    GameItem(
                        packageName = pkg,
                        appName = label,
                        isCustom = false,
                        targetFps = 90,
                        resolutionPreset = "FHD (1080p)",
                        isTurboEnabled = true
                    )
                )
            }
        }

        // Always provide premier competitive titles matching Screen 3 in design preview
        val premierGames = listOf(
            GameItem("com.tencent.ig", "PUBG Mobile", false, 90, "FHD (1080p)", true, "10 mins ago"),
            GameItem("com.activision.callofduty.warzone", "Call of Duty Mobile", false, 120, "2K QHD", true, "1 hour ago"),
            GameItem("com.dts.freefireth", "Free Fire", false, 60, "FHD (1080p)", true, "Yesterday"),
            GameItem("com.miHoYo.GenshinImpact", "Genshin Impact", false, 60, "Extreme High", true, "3 days ago"),
            GameItem("jp.konami.pesam", "eFootball™", false, 90, "FHD (1080p)", true, "Recently")
        )

        val merged = (premierGames + list).distinctBy { it.packageName }
        return merged
    }

    private fun saveSessionsToPrefs(list: List<GamingSession>) {
        val jsonArray = JSONArray()
        for (session in list) {
            val obj = JSONObject().apply {
                put("id", session.id)
                put("gameTitle", session.gameTitle)
                put("packageName", session.packageName)
                put("startTime", session.startTime)
                put("endTime", session.endTime)
                put("durationSeconds", session.durationSeconds)
                put("measurementVersion", 2)
                put("averageFps", session.averageFps.toDouble())
                put("minFps", session.minFps)
                put("maxFps", session.maxFps)
                put("averagePing", session.averagePing)
                put("maxPing", session.maxPing)
                put("averageCpu", session.averageCpu.toDouble())
                put("averageGpu", session.averageGpu.toDouble())
                put("ramFreedMb", session.ramFreedMb)
                put("stabilityScore", session.stabilityScore)

                val snapArr = JSONArray()
                for (s in session.snapshots) {
                    val sObj = JSONObject().apply {
                        put("t", s.timestamp)
                        put("fps", s.fps)
                        put("ping", s.pingMs)
                        put("cpu", s.cpuUsagePercent)
                        put("gpu", s.gpuUsagePercent)
                        put("ram", s.ramUsedMb)
                        put("totalRam", s.ramTotalMb)
                        put("temp", s.batteryTemp.toDouble())
                        put("thermal", s.thermalState)
                        put("battery", s.batteryLevelPercent)
                    }
                    snapArr.put(sObj)
                }
                put("snapshots", snapArr)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_sessions", jsonArray.toString()).apply()
    }

    private fun parseSessionsFromJson(raw: String): List<GamingSession> {
        val list = mutableListOf<GamingSession>()
        val jsonArray = JSONArray(raw)
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val currentMeasurements = obj.optInt("measurementVersion", 0) >= 2
            val snaps = mutableListOf<PerformanceSnapshot>()
            val snapArr = obj.optJSONArray("snapshots")
            if (snapArr != null) {
                for (j in 0 until snapArr.length()) {
                    val s = snapArr.getJSONObject(j)
                    snaps.add(
                        PerformanceSnapshot(
                            timestamp = s.optLong("t"),
                            fps = if (currentMeasurements) s.optInt("fps", -1) else -1,
                            pingMs = s.optInt("ping", -1),
                            cpuUsagePercent = s.optInt("cpu", -1),
                            gpuUsagePercent = s.optInt("gpu", -1),
                            ramUsedMb = s.optLong("ram", 0),
                            ramTotalMb = s.optLong("totalRam", 0),
                            batteryTemp = s.optDouble("temp", -1.0).toFloat(),
                            thermalState = s.optString("thermal", "Unknown"),
                            batteryLevelPercent = s.optInt("battery", -1)
                        )
                    )
                }
            }

            fun legacyAverage(values: List<Int>): Float =
                values.filter { it >= 0 }.takeIf { it.isNotEmpty() }?.average()?.toFloat() ?: -1f
            list.add(
                GamingSession(
                    id = obj.getString("id"),
                    gameTitle = obj.getString("gameTitle"),
                    packageName = obj.getString("packageName"),
                    startTime = obj.getLong("startTime"),
                    endTime = obj.getLong("endTime"),
                    durationSeconds = obj.getLong("durationSeconds"),
                    averageFps = if (currentMeasurements) obj.getDouble("averageFps").toFloat() else -1f,
                    minFps = if (currentMeasurements) obj.getInt("minFps") else -1,
                    maxFps = if (currentMeasurements) obj.getInt("maxFps") else -1,
                    averagePing = if (currentMeasurements) obj.getInt("averagePing") else legacyAverage(snaps.map { it.pingMs }).toInt(),
                    maxPing = if (currentMeasurements) obj.getInt("maxPing") else snaps.map { it.pingMs }.filter { it >= 0 }.maxOrNull() ?: -1,
                    averageCpu = if (currentMeasurements) obj.getDouble("averageCpu").toFloat() else legacyAverage(snaps.map { it.cpuUsagePercent }),
                    averageGpu = if (currentMeasurements) obj.getDouble("averageGpu").toFloat() else legacyAverage(snaps.map { it.gpuUsagePercent }),
                    ramFreedMb = if (currentMeasurements) obj.optLong("ramFreedMb", 0) else 0L,
                    stabilityScore = if (currentMeasurements) obj.optInt("stabilityScore", -1) else -1,
                    snapshots = snaps
                )
            )
        }
        return list
    }

    private fun saveGamesToPrefs(list: List<GameItem>) {
        val jsonArray = JSONArray()
        for (g in list) {
            val obj = JSONObject().apply {
                put("pkg", g.packageName)
                put("name", g.appName)
                put("custom", g.isCustom)
                put("targetFps", g.targetFps)
                put("res", g.resolutionPreset)
                put("turbo", g.isTurboEnabled)
                put("lastPlayed", g.lastPlayedDate)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString("saved_games", jsonArray.toString()).apply()
    }

    private fun parseGamesFromJson(raw: String): List<GameItem> {
        val list = mutableListOf<GameItem>()
        val arr = JSONArray(raw)
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                GameItem(
                    packageName = o.getString("pkg"),
                    appName = o.getString("name"),
                    isCustom = o.optBoolean("custom", false),
                    targetFps = o.optInt("targetFps", 90),
                    resolutionPreset = o.optString("res", "FHD (1080p)"),
                    isTurboEnabled = o.optBoolean("turbo", true),
                    lastPlayedDate = o.optString("lastPlayed", "Today")
                )
            )
        }
        return list
    }

}
