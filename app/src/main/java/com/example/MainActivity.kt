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
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.draw.scale
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import com.example.ui.screens.GameModeScreen
import com.example.ui.screens.NetworkOptimizerScreen
import com.example.ui.screens.ProcessCleanerScreen
import com.example.ui.screens.SessionHistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.DeepVoidBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBlueMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ScreenTab(val titleAr: String, val titleEn: String, val icon: ImageVector) {
    DASHBOARD("الرئيسية", "Home", Icons.Default.Home),
    GAMES("الألعاب", "Games", Icons.Default.Gamepad),
    TOOLS("الأدوات", "Tools", Icons.Default.Bolt),
    SESSIONS("الجلسات", "Sessions", Icons.Default.ShowChart),
    SETTINGS("المزيد", "Settings", Icons.Default.Settings)
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
                        try {
                            val launchIntent = packageManager.getLaunchIntentForPackage(game.packageName)
                            if (launchIntent != null) {
                                startActivity(launchIntent)
                            } else {
                                Toast.makeText(
                                    this,
                                    "تم تشغيل وضع التوربو لـ ${game.appName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(
                                this,
                                "تم تشغيل وضع التوربو لـ ${game.appName}",
                                Toast.LENGTH_SHORT
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
            // Keep state synced
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
    var hudOpacity by remember { mutableStateOf(sharedPrefs.getFloat("hud_opacity", 0.70f)) }
    val coroutineScope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }
    var isArabic by remember { mutableStateOf(true) }
    var showSplashScreen by remember { mutableStateOf(true) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1400)
        showSplashScreen = false
    }

    // Tools sub-tab (0: Process Cleaner - Screen 5, 1: Network Optimizer - Screen 4, 2: Game Mode - Screen 8)
    var selectedToolIndex by remember { mutableStateOf(0) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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

    // 8 Regional Gaming Ping Benchmark Destinations as requested
    var pingServers by remember {
        mutableStateOf(
            listOf(
                PingServerTarget(
                    id = "riyadh",
                    nameAr = "السعودية — الرياض",
                    nameEn = "Saudi Arabia — Riyadh",
                    location = "Riyadh (KSA Hub)",
                    host = "stc.com.sa",
                    port = 443,
                    iconEmoji = "🇸🇦"
                ),
                PingServerTarget(
                    id = "dubai",
                    nameAr = "الإمارات — دبي",
                    nameEn = "UAE — Dubai",
                    location = "Dubai (Middle East Cloud)",
                    host = "ec2.me-central-1.amazonaws.com",
                    port = 443,
                    iconEmoji = "🇦🇪"
                ),
                PingServerTarget(
                    id = "cairo",
                    nameAr = "مصر — القاهرة",
                    nameEn = "Egypt — Cairo",
                    location = "Cairo (North Africa)",
                    host = "www.orange.eg",
                    port = 443,
                    iconEmoji = "🇪🇬"
                ),
                PingServerTarget(
                    id = "us_east",
                    nameAr = "أمريكا — شرق الولايات المتحدة",
                    nameEn = "USA — US East",
                    location = "N. Virginia (US East)",
                    host = "ec2.us-east-1.amazonaws.com",
                    port = 443,
                    iconEmoji = "🇺🇸"
                ),
                PingServerTarget(
                    id = "us_west",
                    nameAr = "أمريكا — غرب الولايات المتحدة",
                    nameEn = "USA — US West",
                    location = "Oregon (US West)",
                    host = "ec2.us-west-2.amazonaws.com",
                    port = 443,
                    iconEmoji = "🇺🇸"
                ),
                PingServerTarget(
                    id = "london",
                    nameAr = "بريطانيا — لندن",
                    nameEn = "UK — London",
                    location = "London (West Europe)",
                    host = "ec2.eu-west-2.amazonaws.com",
                    port = 443,
                    iconEmoji = "🇬🇧"
                ),
                PingServerTarget(
                    id = "frankfurt",
                    nameAr = "ألمانيا — فرانكفورت",
                    nameEn = "Germany — Frankfurt",
                    location = "Frankfurt (Central Europe)",
                    host = "ec2.eu-central-1.amazonaws.com",
                    port = 443,
                    iconEmoji = "🇩🇪"
                ),
                PingServerTarget(
                    id = "singapore",
                    nameAr = "سنغافورة",
                    nameEn = "Singapore",
                    location = "Singapore (Asia Pacific)",
                    host = "ec2.ap-southeast-1.amazonaws.com",
                    port = 443,
                    iconEmoji = "🇸🇬"
                )
            )
        )
    }

    var selectedPingServerId by remember {
        mutableStateOf(sharedPrefs.getString("selected_ping_server_id", "riyadh") ?: "riyadh")
    }
    var isTestingNetwork by remember { mutableStateOf(false) }
    var isTestingSingleServer by remember { mutableStateOf(false) }
    var isOptimizingNetwork by remember { mutableStateOf(false) }
    var networkOptimizationDone by remember { mutableStateOf(false) }

    fun testSingleServer(target: PingServerTarget) {
        coroutineScope.launch {
            isTestingSingleServer = true
            val res = autoBoostEngine.measureDetailedServerPing(target.host, target.port, probeCount = 3)
            pingServers = pingServers.map { s ->
                if (s.id == target.id) {
                    s.copy(
                        currentPingMs = res.pingMs,
                        jitterMs = res.jitterMs,
                        packetLossPercent = res.packetLossPercent,
                        isFailed = !res.isSuccess,
                        status = res.statusText,
                        lastTestedTimestamp = System.currentTimeMillis()
                    )
                } else s
            }
            isTestingSingleServer = false
        }
    }

    fun testAllServers() {
        coroutineScope.launch {
            isTestingNetwork = true
            val updated = pingServers.map { s ->
                val res = autoBoostEngine.measureDetailedServerPing(s.host, s.port, probeCount = 2)
                s.copy(
                    currentPingMs = res.pingMs,
                    jitterMs = res.jitterMs,
                    packetLossPercent = res.packetLossPercent,
                    isFailed = !res.isSuccess,
                    status = res.statusText,
                    lastTestedTimestamp = System.currentTimeMillis()
                )
            }
            pingServers = updated
            isTestingNetwork = false
        }
    }

    // BackHandler: returns to dashboard if on sub-screen
    BackHandler(enabled = currentTab != ScreenTab.DASHBOARD || drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            currentTab = ScreenTab.DASHBOARD
        }
    }

    // Navigation Drawer (Screen 12 from GlassBoost UI Design System)
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepVoidBlack,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Drawer Header (Screen 12)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Logo",
                                tint = NeonCyan,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GlassBoost",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Play Better • Faster • Smoother",
                                    fontSize = 10.sp,
                                    color = SlateBlueMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Drawer Navigation Items (Screen 12)
                        val drawerItems = listOf(
                            Triple(ScreenTab.DASHBOARD, if (isArabic) "الصفحة الرئيسية" else "Dashboard", Icons.Default.Home),
                            Triple(ScreenTab.GAMES, if (isArabic) "الألعاب" else "Games Library", Icons.Default.Gamepad),
                            Triple(ScreenTab.TOOLS, if (isArabic) "الأدوات" else "Boost Tools", Icons.Default.Bolt),
                            Triple(ScreenTab.SESSIONS, if (isArabic) "سجل الجلسات" else "Session History", Icons.Default.ShowChart),
                            Triple(ScreenTab.SETTINGS, if (isArabic) "الإعدادات" else "Settings", Icons.Default.Settings)
                        )

                        drawerItems.forEach { (tab, title, icon) ->
                            val isSelected = currentTab == tab
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) DeepGlassNavy else Color.Transparent)
                                    .clickable {
                                        currentTab = tab
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) NeonCyan else SlateBlueMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) PureWhite else SlateBlueMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Direct Entry: خوادم الألعاب واختبار Ping (8 مواقع)
                        val isNetworkTabActive = currentTab == ScreenTab.TOOLS && selectedToolIndex == 1
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isNetworkTabActive) DeepGlassNavy else Color.Transparent)
                                .clickable {
                                    currentTab = ScreenTab.TOOLS
                                    selectedToolIndex = 1
                                    coroutineScope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = "Network & Ping Servers",
                                tint = if (isNetworkTabActive) NeonCyan else SlateBlueMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isArabic) "خوادم الألعاب واختبار Ping (8 مواقع)" else "Gaming Servers & Ping (8 Nodes)",
                                fontSize = 14.sp,
                                fontWeight = if (isNetworkTabActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isNetworkTabActive) PureWhite else SlateBlueMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Screen 12: حول التطبيق
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showAboutDialog = true
                                    coroutineScope.launch { drawerState.close() }
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "About",
                                tint = SlateBlueMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isArabic) "حول التطبيق" else "About GlassBoost",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateBlueMuted
                            )
                        }
                    }

                    // Drawer Footer: تسجيل الخروج (Screen 12)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable {
                                (context as? ComponentActivity)?.finish()
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit App",
                            tint = PerformanceRed.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isArabic) "تسجيل الخروج" else "Exit Application",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PerformanceRed.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBlack,
            topBar = {
                // GlassBoost Top App Bar with Menu & Crown
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Menu Drawer Toggle Icon
                            IconButton(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Open Drawer",
                                    tint = PureWhite,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { currentTab = ScreenTab.DASHBOARD }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Logo",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "GlassBoost",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = PureWhite
                                )
                            }
                        }

                        // Top Bar Actions
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Language switcher
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CardGlassSurface)
                                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { isArabic = !isArabic }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "EN" else "العربية",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Quick Turbo Boost Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                                    )
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
                                        tint = ObsidianBlack,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (isArabic) "تسريع" else "BOOST",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ObsidianBlack
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                // Glassmorphic Bottom Navigation Bar (Matching the 5 tabs in Design Preview)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepVoidBlack.copy(alpha = 0.95f))
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                listOf(NeonCyan.copy(alpha = 0.35f), Color.White.copy(alpha = 0.08f), RoyalGold.copy(alpha = 0.2f))
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 6.dp, horizontal = 4.dp)
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
                                    tint = if (isSelected) NeonCyan else SlateBlueMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isArabic) tab.titleAr else tab.titleEn,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) NeonCyan else SlateBlueMuted
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
                                onNavigateToSessions = { currentTab = ScreenTab.SESSIONS },
                                pingServers = pingServers,
                                onNavigateToNetwork = {
                                    currentTab = ScreenTab.TOOLS
                                    selectedToolIndex = 1
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

                        ScreenTab.TOOLS -> {
                            // Sub-navigation between Tools screens: Cleaner (Screen 5), Network (Screen 4), Game Mode (Screen 8)
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        Pair(0, if (isArabic) "منظف العمليات" else "Cleaner"),
                                        Pair(1, if (isArabic) "محسن الشبكة" else "Network"),
                                        Pair(2, if (isArabic) "وضع الألعاب" else "Game Mode")
                                    ).forEach { (idx, label) ->
                                        val isSelected = selectedToolIndex == idx
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) NeonCyan else CardGlassSurface)
                                                .clickable { selectedToolIndex = idx }
                                                .padding(vertical = 7.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) ObsidianBlack else SlateBlueMuted
                                            )
                                        }
                                    }
                                }

                                when (selectedToolIndex) {
                                    0 -> {
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
                                    1 -> {
                                        NetworkOptimizerScreen(
                                            servers = pingServers,
                                            selectedServerId = selectedPingServerId,
                                            isTestingAll = isTestingNetwork,
                                            isTestingSingle = isTestingSingleServer,
                                            isOptimizingNetwork = isOptimizingNetwork,
                                            networkOptimizationDone = networkOptimizationDone,
                                            overallJitterMs = 2,
                                            isArabic = isArabic,
                                            onSelectServer = { newId ->
                                                selectedPingServerId = newId
                                                sharedPrefs.edit().putString("selected_ping_server_id", newId).apply()
                                            },
                                            onTestSelectedServer = { server ->
                                                testSingleServer(server)
                                            },
                                            onTestAllServers = { testAllServers() },
                                            onOptimizeNetworkRouting = {
                                                coroutineScope.launch {
                                                    isOptimizingNetwork = true
                                                    autoBoostEngine.optimizeNetworkRouting()
                                                    networkOptimizationDone = true
                                                    isOptimizingNetwork = false
                                                    testAllServers()
                                                }
                                            }
                                        )
                                    }
                                    else -> {
                                        GameModeScreen(
                                            isArabic = isArabic,
                                            onActivate = {
                                                coroutineScope.launch {
                                                    autoBoostEngine.executeAutoBoost(isArabic)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
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

                        ScreenTab.SETTINGS -> {
                            SettingsScreen(isArabic = isArabic)
                        }
                    }
                }
            }
        }
    }

    if (showAboutDialog) {
        AboutGlassBoostDialog(
            onDismiss = { showAboutDialog = false },
            isArabic = isArabic
        )
    }

    if (showSplashScreen) {
        GlassBoostSplashScreen(
            onDismiss = { showSplashScreen = false }
        )
    }
}

/**
 * Screen 1 from GlassBoost UI Design System:
 * شاشة البداية (Splash / Onboarding Screen)
 * Dark luxury backdrop, central glowing lightning bolt emblem, typography, and progress indicator.
 */
@Composable
fun GlassBoostSplashScreen(
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ObsidianBlack,
                        DeepVoidBlack,
                        DeepGlassNavy.copy(alpha = 0.6f)
                    )
                )
            )
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Screen 1: Centerpiece Glowing Lightning Bolt
            Box(
                modifier = Modifier
                    .scale(glowScale)
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.35f),
                                ElectricBlue.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "GlassBoost Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(76.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GlassBoost",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Play Better • Faster • Smoother",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                color = SlateBlueMuted
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Sleek Progress Pill Bar at bottom
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(DarkSlate)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(45.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                        )
                )
            }
        }
    }
}

/**
 * About GlassBoost Dialog (from Screen 12 Drawer)
 */
@Composable
fun AboutGlassBoostDialog(
    onDismiss: () -> Unit,
    isArabic: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Logo",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GlassBoost • Pro Edition",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PureWhite
                )
            }
        },
        text = {
            Column {
                Text(
                    text = if (isArabic)
                        "تطبيق GlassBoost لتسريع وتحسين أداء الألعاب، خفض البنج، مراقبة الإطارات (FPS)، وتنظيف الذاكرة بلوحة عائمة شفافة."
                    else
                        "GlassBoost delivers esports-grade performance optimization, real socket ping tuning, live FPS & thermal HUD, and hardware unleash.",
                    fontSize = 13.sp,
                    color = SlateBlueMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Version 3.4 • Hybrid iOS Glassmorphic Design",
                    fontSize = 11.sp,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text(
                    text = if (isArabic) "إغلاق" else "Close",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = DeepVoidBlack,
        shape = RoundedCornerShape(20.dp)
    )
}
