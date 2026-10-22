package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.engine.AutoBoostEngine
import com.example.engine.PerformanceMonitorEngine
import com.example.ui.components.FloatingGamingHud
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Android Foreground Service hosting a pure Jetpack Compose Floating Gaming HUD
 * with real-time FPS, Ping, thermal overheat monitoring, and customizable opacity.
 */
class FloatingPanelService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var lifecycleOwner: FloatingLifecycleOwner? = null

    private var monitorEngine: PerformanceMonitorEngine? = null
    private var autoBoostEngine: AutoBoostEngine? = null


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        startForegroundServiceNotification()

        val prefs = getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE)
        _hudOpacity.value = prefs.getFloat("hud_opacity", 0.70f).coerceIn(0.2f, 1f)

        monitorEngine = PerformanceMonitorEngine(applicationContext)
        autoBoostEngine = AutoBoostEngine(applicationContext)
        monitorEngine?.start(serviceScope)

        if (Settings.canDrawOverlays(this)) {
            setupComposeFloatingOverlay()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val opacity = intent?.getFloatExtra(EXTRA_OPACITY, -1f) ?: -1f
        if (opacity in 0.2f..1.0f) {
            _hudOpacity.value = opacity
        }
        return START_STICKY
    }

    private fun startForegroundServiceNotification() {
        val channelId = "glassboost_compose_hud"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "GlassBoost Floating HUD",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Real-time Compose FPS, Ping, and Memory HUD over games"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("GlassBoost • Compose Floating HUD")
            .setContentText("Active FPS, Ping & Thermal monitor running over games")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun setupComposeFloatingOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 80
            y = 200
        }

        val owner = FloatingLifecycleOwner().apply {
            onCreate()
            onResume()
        }
        lifecycleOwner = owner

        val view = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)

            setContent {
                MyApplicationTheme {
                    val metricsState by (monitorEngine?.currentMetrics ?: return@MyApplicationTheme).collectAsState()
                    val opacityState by _hudOpacity.collectAsState()
                    val preparing by autoBoostEngine!!.isOptimizing.collectAsState()
                    val result by autoBoostEngine!!.lastBoostResult.collectAsState()

                    FloatingGamingHud(
                        fps = metricsState.fps,
                        isPreparing = preparing,
                        cleanupBytes = result?.cacheDeletedBytes,
                        pingMs = metricsState.pingMs,
                        cpuPercent = metricsState.cpuUsagePercent,
                        ramUsedMb = metricsState.ramUsedMb,
                        batteryTemp = metricsState.batteryTemp,
                        thermalSafetyThreshold = 40.0f,
                        opacity = opacityState,
                        onSpeedBoost = {
                            serviceScope.launch {
                                autoBoostEngine?.executeAutoBoost(false)
                            }
                        },
                        onClearCache = {
                            serviceScope.launch {
                                autoBoostEngine?.clearCachedBackgroundMemory()
                            }
                        },
                        onCoolDown = {
                            android.widget.Toast.makeText(this@FloatingPanelService,
                                "اترك الهاتف يبرد وأوقف اللعب عند السخونة؛ التنظيف لا يبرّد المعالج.",
                                android.widget.Toast.LENGTH_LONG).show()
                        },
                        onOpacityChanged = { updateOpacity(this@FloatingPanelService, it) },
                        onClose = {
                            stopSelf()
                        },
                        onDragDelta = { dx, dy ->
                            params.x = (params.x + dx.toInt()).coerceAtLeast(0)
                            params.y = (params.y + dy.toInt()).coerceAtLeast(0)
                            try {
                                windowManager?.updateViewLayout(this@apply, params)
                            } catch (_: Exception) {}
                        },
                        onDragEnd = {
                            val screenWidth = resources.displayMetrics.widthPixels
                            val currentW = if (this@apply.width > 0) this@apply.width else 160
                            val snapX = if (params.x < screenWidth / 2) 0 else (screenWidth - currentW).coerceAtLeast(0)
                            params.x = snapX
                            try {
                                windowManager?.updateViewLayout(this@apply, params)
                            } catch (_: Exception) {}
                        }
                    )
                }
            }
        }

        // Add touch listener for seamless edge dragging and snapping
        var initialX = 0
        var initialY = 0
        var touchStartX = 0f
        var touchStartY = 0f

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    touchStartX = event.rawX
                    touchStartY = event.rawY
                    false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchStartX).toInt()
                    val dy = (event.rawY - touchStartY).toInt()
                    if (Math.abs(dx) > 8 || Math.abs(dy) > 8) {
                        params.x = (initialX + dx).coerceAtLeast(0)
                        params.y = (initialY + dy).coerceAtLeast(0)
                        try {
                            windowManager?.updateViewLayout(view, params)
                        } catch (_: Exception) {}
                    }
                    false
                }
                MotionEvent.ACTION_UP -> {
                    val screenWidth = resources.displayMetrics.widthPixels
                    val snapX = if (params.x < screenWidth / 2) 0 else (screenWidth - view.width).coerceAtLeast(0)
                    params.x = snapX
                    try {
                        windowManager?.updateViewLayout(view, params)
                    } catch (_: Exception) {}
                    false
                }
                else -> false
            }
        }

        composeView = view

        try {
            windowManager?.addView(composeView, params)
            _running.value = true
        } catch (_: Exception) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _running.value = false
        monitorEngine?.stop()
        serviceScope.cancel()

        lifecycleOwner?.onDestroy()
        lifecycleOwner = null

        if (composeView != null && windowManager != null) {
            try {
                windowManager?.removeView(composeView)
            } catch (_: Exception) {}
            composeView = null
        }
    }

    companion object {
        const val EXTRA_OPACITY = "extra_hud_opacity"
        private val _running = MutableStateFlow(false)
        val running: kotlinx.coroutines.flow.StateFlow<Boolean> = _running
        private val _hudOpacity = MutableStateFlow(0.70f)

        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) return
            val intent = Intent(context, FloatingPanelService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun updateOpacity(context: Context, opacity: Float) {
            val clamped = opacity.coerceIn(0.2f, 1f)
            context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE)
                .edit().putFloat("hud_opacity", clamped).apply()
            // Updating a setting never starts a service or requests overlay permission.
            _hudOpacity.value = clamped
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingPanelService::class.java)
            context.stopService(intent)
        }
    }
}

/**
 * Custom LifecycleOwner, ViewModelStoreOwner, and SavedStateRegistryOwner implementation
 * for hosting Jetpack Compose inside an Android Service window.
 */
class FloatingLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val controller = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = controller.savedStateRegistry

    fun onCreate() {
        controller.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun onResume() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        store.clear()
    }
}
