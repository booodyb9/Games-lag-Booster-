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
import kotlin.random.Random

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
            val initialSessions = generateInitialDemoSessions()
            _sessions.value = initialSessions
            saveSessionsToPrefs(initialSessions)
        } else {
            try {
                val list = parseSessionsFromJson(rawJson)
                _sessions.value = if (list.isNotEmpty()) list else generateInitialDemoSessions()
            } catch (_: Exception) {
                _sessions.value = generateInitialDemoSessions()
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

        val avgFps = activeSnapshots.map { it.fps }.average().toFloat()
        val minFps = activeSnapshots.minOfOrNull { it.fps } ?: 55
        val maxFps = activeSnapshots.maxOfOrNull { it.fps } ?: 60
        val avgPing = activeSnapshots.map { it.pingMs }.average().toInt()
        val maxPing = activeSnapshots.maxOfOrNull { it.pingMs } ?: 45
        val avgCpu = activeSnapshots.map { it.cpuUsagePercent }.average().toFloat()
        val avgGpu = activeSnapshots.map { it.gpuUsagePercent }.average().toFloat()

        val fpsVariability = maxFps - minFps
        val stability = (100 - (fpsVariability * 1.5f)).toInt().coerceIn(70, 99)

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
            ramFreedMb = 512L,
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

        // Always provide premier competitive titles for the user to configure and launch
        val premierGames = listOf(
            GameItem("com.tencent.ig", "PUBG Mobile (ببجي موبايل)", false, 90, "FHD (1080p)", true, "10 mins ago"),
            GameItem("com.activision.callofduty.warzone", "COD: Warzone Mobile (كول أوف ديوتي)", false, 120, "2K QHD", true, "1 hour ago"),
            GameItem("com.dts.freefireth", "Free Fire MAX (فري فاير ماكس)", false, 60, "FHD (1080p)", true, "Yesterday"),
            GameItem("com.miHoYo.GenshinImpact", "Genshin Impact (قينشين إمباكت)", false, 60, "Extreme High", true, "3 days ago"),
            GameItem("com.roblox.client", "Roblox (روبلوكس)", false, 60, "Dynamic Res", true, "Last week")
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
            val snaps = mutableListOf<PerformanceSnapshot>()
            val snapArr = obj.optJSONArray("snapshots")
            if (snapArr != null) {
                for (j in 0 until snapArr.length()) {
                    val s = snapArr.getJSONObject(j)
                    snaps.add(
                        PerformanceSnapshot(
                            timestamp = s.optLong("t"),
                            fps = s.optInt("fps", 60),
                            pingMs = s.optInt("ping", 28),
                            cpuUsagePercent = s.optInt("cpu", 30),
                            gpuUsagePercent = s.optInt("gpu", 40),
                            ramUsedMb = s.optLong("ram", 3200),
                            ramTotalMb = s.optLong("totalRam", 8192),
                            batteryTemp = s.optDouble("temp", 33.0).toFloat(),
                            thermalState = s.optString("thermal", "Normal")
                        )
                    )
                }
            }

            list.add(
                GamingSession(
                    id = obj.getString("id"),
                    gameTitle = obj.getString("gameTitle"),
                    packageName = obj.getString("packageName"),
                    startTime = obj.getLong("startTime"),
                    endTime = obj.getLong("endTime"),
                    durationSeconds = obj.getLong("durationSeconds"),
                    averageFps = obj.getDouble("averageFps").toFloat(),
                    minFps = obj.getInt("minFps"),
                    maxFps = obj.getInt("maxFps"),
                    averagePing = obj.getInt("averagePing"),
                    maxPing = obj.getInt("maxPing"),
                    averageCpu = obj.getDouble("averageCpu").toFloat(),
                    averageGpu = obj.getDouble("averageGpu").toFloat(),
                    ramFreedMb = obj.optLong("ramFreedMb", 512),
                    stabilityScore = obj.optInt("stabilityScore", 96),
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

    private fun generateInitialDemoSessions(): List<GamingSession> {
        val now = System.currentTimeMillis()

        // Session 1: PUBG Mobile 90FPS competitive match
        val pubgSnapshots = mutableListOf<PerformanceSnapshot>()
        for (t in 0..40) {
            val fpsVal = (88 + Random.nextInt(-3, 3)).coerceIn(75, 90)
            val pingVal = (24 + Random.nextInt(-3, 6)).coerceIn(18, 48)
            val cpuVal = (38 + Random.nextInt(-5, 9)).coerceIn(25, 60)
            val gpuVal = (52 + Random.nextInt(-6, 8)).coerceIn(35, 75)
            pubgSnapshots.add(
                PerformanceSnapshot(
                    timestamp = now - (40 - t) * 5000L,
                    fps = fpsVal,
                    pingMs = pingVal,
                    cpuUsagePercent = cpuVal,
                    gpuUsagePercent = gpuVal,
                    ramUsedMb = 3450 + t * 5L,
                    ramTotalMb = 8192,
                    batteryTemp = 34.2f + (t * 0.05f),
                    thermalState = "Cool (بارد)"
                )
            )
        }

        val s1 = GamingSession(
            id = "demo-pubg-01",
            gameTitle = "PUBG Mobile (ببجي موبايل)",
            packageName = "com.tencent.ig",
            startTime = now - 1800000L,
            endTime = now - 600000L,
            durationSeconds = 1200,
            averageFps = 88.4f,
            minFps = 82,
            maxFps = 90,
            averagePing = 26,
            maxPing = 38,
            averageCpu = 42.1f,
            averageGpu = 56.7f,
            ramFreedMb = 720L,
            stabilityScore = 98,
            snapshots = pubgSnapshots
        )

        // Session 2: Call of Duty Warzone Mobile
        val codSnapshots = mutableListOf<PerformanceSnapshot>()
        for (t in 0..35) {
            val fpsVal = (116 + Random.nextInt(-7, 5)).coerceIn(100, 120)
            val pingVal = (32 + Random.nextInt(-4, 9)).coerceIn(22, 58)
            val cpuVal = (48 + Random.nextInt(-8, 12)).coerceIn(30, 72)
            val gpuVal = (68 + Random.nextInt(-7, 9)).coerceIn(45, 88)
            codSnapshots.add(
                PerformanceSnapshot(
                    timestamp = now - (35 - t) * 6000L,
                    fps = fpsVal,
                    pingMs = pingVal,
                    cpuUsagePercent = cpuVal,
                    gpuUsagePercent = gpuVal,
                    ramUsedMb = 4100 + t * 8L,
                    ramTotalMb = 8192,
                    batteryTemp = 36.8f,
                    thermalState = "Normal (معتدل)"
                )
            )
        }

        val s2 = GamingSession(
            id = "demo-cod-02",
            gameTitle = "COD: Warzone Mobile (كول أوف ديوتي)",
            packageName = "com.activision.callofduty.warzone",
            startTime = now - 7200000L,
            endTime = now - 5400000L,
            durationSeconds = 1800,
            averageFps = 114.2f,
            minFps = 104,
            maxFps = 120,
            averagePing = 34,
            maxPing = 52,
            averageCpu = 52.8f,
            averageGpu = 72.1f,
            ramFreedMb = 940L,
            stabilityScore = 95,
            snapshots = codSnapshots
        )

        return listOf(s1, s2)
    }
}
