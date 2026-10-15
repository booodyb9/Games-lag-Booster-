package com.example

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat
import com.example.engine.PerformanceMonitorEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground Service that renders a draggable, luxury glass floating HUD overlay
 * over running games to display live FPS, Ping latency, CPU load, and Quick Boost.
 */
class FloatingPanelService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var monitorEngine: PerformanceMonitorEngine? = null

    private var fpsTextView: TextView? = null
    private var pingTextView: TextView? = null
    private var cpuTextView: TextView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceNotification()
        monitorEngine = PerformanceMonitorEngine(applicationContext)
        monitorEngine?.start(serviceScope)

        if (Settings.canDrawOverlays(this)) {
            setupFloatingView()
        }

        observeMetrics()
    }

    private fun startForegroundServiceNotification() {
        val channelId = "glassboost_hud_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "GlassBoost Gaming Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows real-time FPS, Ping, and system metrics during games"
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
            .setContentTitle("GlassBoost • Active Gaming Monitor")
            .setContentText("FPS: 60 • Ping: 24ms • Turbo Engine Active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupFloatingView() {
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
            y = 180
        }

        // Luxury Dark Frosted Glass Container
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 18, 32, 18)
            elevation = 16f

            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 48f
                setColor(Color.parseColor("#E60B0F19")) // 90% obsidian glass
                setStroke(3, Color.parseColor("#4D00D2FF")) // Cyan glass specular border
            }
            background = bg
        }

        // Logo indicator
        val logoDot = View(this).apply {
            val size = 20
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_VERTICAL
                setMargins(0, 0, 16, 0)
            }
            val dotBg = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor("#D4AF37")) // Royal gold
            }
            background = dotBg
        }
        container.addView(logoDot)

        // FPS Badge
        fpsTextView = TextView(this).apply {
            text = "60 FPS"
            setTextColor(Color.parseColor("#10B981")) // Performance Green
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 20, 0)
        }
        container.addView(fpsTextView)

        // Ping Badge
        pingTextView = TextView(this).apply {
            text = "24ms"
            setTextColor(Color.parseColor("#00D2FF")) // Glass Cyan
            textSize = 13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 20, 0)
        }
        container.addView(pingTextView)

        // CPU Badge
        cpuTextView = TextView(this).apply {
            text = "CPU 28%"
            setTextColor(Color.parseColor("#F8FAFC"))
            textSize = 12f
            setPadding(0, 0, 16, 0)
        }
        container.addView(cpuTextView)

        // Close pill button
        val closeBtn = TextView(this).apply {
            text = "✕"
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 13f
            setPadding(12, 0, 4, 0)
            setOnClickListener {
                stopSelf()
            }
        }
        container.addView(closeBtn)

        // Touch & Drag Handling
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        container.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(container, params)
                    true
                }
                else -> false
            }
        }

        floatingView = container
        try {
            windowManager?.addView(floatingView, params)
        } catch (_: Exception) {}
    }

    private fun observeMetrics() {
        serviceScope.launch {
            monitorEngine?.currentMetrics?.collectLatest { snapshot ->
                fpsTextView?.text = "${snapshot.fps} FPS"
                pingTextView?.text = "${snapshot.pingMs}ms"
                cpuTextView?.text = "CPU ${snapshot.cpuUsagePercent}%"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        monitorEngine?.stop()
        serviceScope.cancel()
        if (floatingView != null && windowManager != null) {
            try {
                windowManager?.removeView(floatingView)
            } catch (_: Exception) {}
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FloatingPanelService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingPanelService::class.java)
            context.stopService(intent)
        }
    }
}
