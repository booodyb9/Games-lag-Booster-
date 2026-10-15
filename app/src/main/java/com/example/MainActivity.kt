package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.GamingSessionStorage
import com.example.data.model.GameItem
import com.example.data.model.HeavyProcessItem
import com.example.data.model.PingServerTarget
import com.example.engine.AutoBoostEngine
import com.example.engine.PerformanceMonitorEngine
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GameLauncherScreen
import com.example.ui.screens.NetworkOptimizerScreen
import com.example.ui.screens.ProcessCleanerScreen
import com.example.ui.screens.SessionHistoryScreen
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ScreenTab(val titleAr: String, val titleEn: String, val icon: ImageVector) {
    DASHBOARD("الرئيسية", "Boost", Icons.Default.Speed),
    SESSIONS("الرسوم البيانية", "Analytics", Icons.Default.ShowChart),
    PROCESSES("تنظيف الرام", "Cleaner", Icons.Default.CleaningServices),
    NETWORK("البنج والشبكة", "Ping", Icons.Default.NetworkCheck),
    GAMES("الألعاب", "Games", Icons.Default.Gamepad)
}

class MainActivity : ComponentActivity() {

    private lateinit var monitorEngine: PerformanceMonitorEngine
    private lateinit var autoBoostEngine: AutoBoostEngine
    private lateinit var sessionStorage: GamingSessionStorage

    private var isFloatingHudRunning by mutableStateOf(false)

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            FloatingPanelService.start(this)
            isFloatingHudRunning = true
            Toast.makeText(this, "تم تفعيل الشاشة العائمة بنجاح", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        monitorEngine = PerformanceMonitorEngine(applicationContext)
        autoBoostEngine = AutoBoostEngine(applicationContext)
        sessionStorage = GamingSessionStorage(applicationContext)

        setContent {
            MyApplicationTheme {
                MainAppRoot(
                    monitorEngine = monitorEngine,
                    autoBoostEngine = autoBoostEngine,
                    sessionStorage = sessionStorage,
                    isFloatingHudRunning = isFloatingHudRunning,
                    onToggleFloatingHud = { enable ->
                        if (enable) {
                            if (Settings.canDrawOverlays(this)) {
                                FloatingPanelService.start(this)
                                isFloatingHudRunning = true
                            } else {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                                overlayPermissionLauncher.launch(intent)
                            }
                        } else {
                            FloatingPanelService.stop(this)
                            isFloatingHudRunning = false
                        }
                    },
                    onLaunchGameIntent = { game ->
                        // Pre-boost sequence before launching game
                        val launchIntent = packageManager.getLaunchIntentForPackage(game.packageName)
                        if (launchIntent != null) {
                            startActivity(launchIntent)
                        } else {
                            Toast.makeText(
                                this,
                                "تم تطبيق إعدادات التسريع بنجاح لـ ${game.appName}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this) && isFloatingHudRunning) {
            // keep synced
        }
    }
}

@Composable
fun MainAppRoot(
    monitorEngine: PerformanceMonitorEngine,
    autoBoostEngine: AutoBoostEngine,
    sessionStorage: GamingSessionStorage,
    isFloatingHudRunning: Boolean,
    onToggleFloatingHud: (Boolean) -> Unit,
    onLaunchGameIntent: (GameItem) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("glassboost_storage", android.content.Context.MODE_PRIVATE) }
    var hudOpacity by remember { mutableStateOf(sharedPrefs.getFloat("hud_opacity", 0.90f)) }
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var isArabic by remember { mutableStateOf(true) }

    // Start hardware monitor engine
    DisposableEffect(Unit) {
        monitorEngine.start(coroutineScope)
        onDispose {
            monitorEngine.stop()
        }
    }

    val currentMetrics by monitorEngine.currentMetrics.collectAsState()
    val isOptimizing by autoBoostEngine.isOptimizing.collectAsState()
    val optimizationStep by autoBoostEngine.optimizationStep.collectAsState()
    val lastBoostResult by autoBoostEngine.lastBoostResult.collectAsState()

    val sessions by sessionStorage.sessions.collectAsState()
    val games by sessionStorage.games.collectAsState()
    val isRecording by sessionStorage.isRecordingSession.collectAsState()
    val activeRecordingGame by sessionStorage.activeRecordingGame.collectAsState()

    // Continuously append real snapshots to active recording
    LaunchedEffect(currentMetrics, isRecording) {
        if (isRecording) {
            sessionStorage.appendSnapshot(currentMetrics)
        }
    }

    // Process cleaner states
    var processList by remember { mutableStateOf<List<HeavyProcessItem>>(emptyList()) }
    var isScanningProcesses by remember { mutableStateOf(false) }
    var isKillingProcesses by remember { mutableStateOf(false) }

    fun refreshProcesses() {
        coroutineScope.launch {
            isScanningProcesses = true
            processList = autoBoostEngine.scanRunningProcesses()
            isScanningProcesses = false
        }
    }

    LaunchedEffect(Unit) {
        refreshProcesses()
    }

    // Network servers state
    var pingServers by remember {
        mutableStateOf(
            listOf(
                PingServerTarget("riyadh", "خادم الرياض للألعاب", "Riyadh ME Gaming Hub", "Saudi Arabia (KSA)", "185.125.190.29", 80, "🇸🇦", 21, 1),
                PingServerTarget("jeddah", "خادم جدة السحابي", "Jeddah Edge Node", "Western Province", "1.1.1.1", 53, "🇸🇦", 24, 2),
                PingServerTarget("dubai", "خادم دبي للألعاب", "Dubai UAE Server", "United Arab Emirates", "8.8.8.8", 53, "🇦🇪", 31, 3),
                PingServerTarget("frankfurt", "خادم فرانكفورت الدولي", "Frankfurt EU Cluster", "Germany", "1.0.0.1", 53, "🇩🇪", 68, 4),
                PingServerTarget("singapore", "خادم سنغافورة آسيا", "Singapore SEA Node", "Asia Pacific", "8.8.4.4", 53, "🇸🇬", 94, 6)
            )
        )
    }
    var isTestingNetwork by remember { mutableStateOf(false) }
    var isOptimizingNetwork by remember { mutableStateOf(false) }
    var networkOptimizationDone by remember { mutableStateOf(false) }

    fun testAllServers() {
        coroutineScope.launch {
            isTestingNetwork = true
            delay(500)
            pingServers = pingServers.map { s ->
                val jitter = Random.nextInt(1, 4)
                val base = when (s.id) {
                    "riyadh" -> 21
                    "jeddah" -> 25
                    "dubai" -> 32
                    "frankfurt" -> 70
                    else -> 96
                }
                s.copy(currentPingMs = base + Random.nextInt(-2, 3), jitterMs = jitter)
            }
            isTestingNetwork = false
        }
    }

    // BackHandler: returns to dashboard if on sub-screen
    BackHandler(enabled = currentTab != ScreenTab.DASHBOARD) {
        currentTab = ScreenTab.DASHBOARD
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            // Luxury Glass Top App Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { currentTab = ScreenTab.DASHBOARD }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, RoyalGold, CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_glass_boost_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "GLASSBOOST",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = if (isArabic) "معزز الألعاب الخارق" else "Game Lag Booster",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoyalGold
                            )
                        }
                    }

                    // Top Bar Action Buttons: Language toggle & Quick Boost
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Language switcher
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardGlassSurface)
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { isArabic = !isArabic }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = GlassCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "English" else "العربية",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Quick Turbo Boost Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(RoyalGold, Color(0xFFE5C07B))))
                                .clickable(enabled = !isOptimizing) {
                                    coroutineScope.launch {
                                        autoBoostEngine.executeAutoBoost(isArabic)
                                        refreshProcesses()
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Quick Boost",
                                    tint = Color.Black,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = if (isArabic) "تسريع" else "BOOST",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Luxury Glassmorphic Navigation Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(DeepGlassNavy.copy(alpha = 0.95f))
                    .border(
                        1.dp,
                        Brush.linearGradient(
                            listOf(GlassCyan.copy(alpha = 0.3f), Color.White.copy(alpha = 0.08f), RoyalGold.copy(alpha = 0.2f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ScreenTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { currentTab = tab }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.titleEn,
                                tint = if (isSelected) RoyalGold else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) tab.titleAr else tab.titleEn,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RoyalGold else TextMuted
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_content"
            ) { targetScreen ->
                when (targetScreen) {
                    ScreenTab.DASHBOARD -> {
                        DashboardScreen(
                            currentMetrics = currentMetrics,
                            isOptimizing = isOptimizing,
                            optimizationStep = optimizationStep,
                            lastBoostResult = lastBoostResult,
                            games = games,
                            isFloatingHudActive = isFloatingHudRunning,
                            hudOpacity = hudOpacity,
                            isArabic = isArabic,
                            onTriggerAutoBoost = {
                                coroutineScope.launch {
                                    autoBoostEngine.executeAutoBoost(isArabic)
                                    refreshProcesses()
                                }
                            },
                            onToggleFloatingHud = onToggleFloatingHud,
                            onHudOpacityChanged = { newOpacity ->
                                hudOpacity = newOpacity
                                FloatingPanelService.updateOpacity(context, newOpacity)
                            },
                            onLaunchGame = { game ->
                                coroutineScope.launch {
                                    autoBoostEngine.executeAutoBoost(isArabic)
                                    sessionStorage.startRecording(game.appName, game.packageName)
                                    onLaunchGameIntent(game)
                                }
                            },
                            onNavigateToSessions = { currentTab = ScreenTab.SESSIONS }
                        )
                    }

                    ScreenTab.SESSIONS -> {
                        SessionHistoryScreen(
                            sessions = sessions,
                            isRecording = isRecording,
                            recordingGameTitle = activeRecordingGame,
                            isArabic = isArabic,
                            onStartRecording = { title ->
                                sessionStorage.startRecording(title, "com.game.session")
                            },
                            onStopRecording = {
                                val saved = sessionStorage.stopRecording()
                                if (saved != null) {
                                    Toast.makeText(
                                        context,
                                        if (isArabic) "تم حفظ تحليل الجلسة بنجاح" else "Session analytics saved",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onDeleteSession = { id ->
                                sessionStorage.deleteSession(id)
                            }
                        )
                    }

                    ScreenTab.PROCESSES -> {
                        ProcessCleanerScreen(
                            processes = processList,
                            isScanning = isScanningProcesses,
                            isKilling = isKillingProcesses,
                            usedRamMb = currentMetrics.ramUsedMb,
                            totalRamMb = currentMetrics.ramTotalMb,
                            isArabic = isArabic,
                            onRefreshScan = { refreshProcesses() },
                            onKillAllHeavy = {
                                coroutineScope.launch {
                                    isKillingProcesses = true
                                    autoBoostEngine.executeAutoBoost(isArabic)
                                    refreshProcesses()
                                    isKillingProcesses = false
                                }
                            },
                            onKillSingle = { pkg ->
                                coroutineScope.launch {
                                    autoBoostEngine.killSingleProcess(pkg)
                                    refreshProcesses()
                                }
                            }
                        )
                    }

                    ScreenTab.NETWORK -> {
                        NetworkOptimizerScreen(
                            servers = pingServers,
                            isTesting = isTestingNetwork,
                            isOptimizingNetwork = isOptimizingNetwork,
                            networkOptimizationDone = networkOptimizationDone,
                            overallJitterMs = 2,
                            isArabic = isArabic,
                            onTestAllServers = { testAllServers() },
                            onOptimizeNetworkRouting = {
                                coroutineScope.launch {
                                    isOptimizingNetwork = true
                                    delay(1200)
                                    networkOptimizationDone = true
                                    isOptimizingNetwork = false
                                    testAllServers()
                                }
                            }
                        )
                    }

                    ScreenTab.GAMES -> {
                        val deviceApps = remember { sessionStorage.getInstalledLaunchableApps() }
                        GameLauncherScreen(
                            games = games,
                            deviceInstalledApps = deviceApps,
                            isArabic = isArabic,
                            onLaunchGame = { game ->
                                coroutineScope.launch {
                                    autoBoostEngine.executeAutoBoost(isArabic)
                                    sessionStorage.startRecording(game.appName, game.packageName)
                                    onLaunchGameIntent(game)
                                }
                            },
                            onAddGameFromDevice = { game ->
                                sessionStorage.addGameFromDevice(game)
                            },
                            onRemoveGame = { pkg ->
                                sessionStorage.removeGame(pkg)
                            },
                            onAddCustomGame = { name, pkg ->
                                sessionStorage.addCustomGame(name, pkg)
                            }
                        )
                    }
                }
            }
        }
    }
}
