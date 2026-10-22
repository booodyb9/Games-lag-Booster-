package com.example.engine

import android.app.NotificationManager
import android.content.Context
import android.os.Build

/** User-approved interruption control. No CPU/FPS or network-routing claims. */
class GameModeController(context: Context) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val prefs = context.getSharedPreferences("glassboost_game_mode", Context.MODE_PRIVATE)
    val hasAccess: Boolean get() = manager.isNotificationPolicyAccessGranted
    val isActive: Boolean get() = hasAccess && prefs.getBoolean("active", false) &&
        manager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

    fun setEnabled(enabled: Boolean): Boolean {
        if (!hasAccess) return false
        try {
            if (enabled) {
                if (isActive) return true
                val previous = manager.currentInterruptionFilter
                manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                prefs.edit().putInt("previous_filter", previous).putBoolean("active", true).apply()
            } else {
                if (!prefs.getBoolean("active", false)) return true
                // Android 15+ uses an app-owned implicit Zen rule; ALL deactivates only that rule.
                val restore = if (Build.VERSION.SDK_INT >= 35) NotificationManager.INTERRUPTION_FILTER_ALL
                    else prefs.getInt("previous_filter", NotificationManager.INTERRUPTION_FILTER_ALL)
                manager.setInterruptionFilter(restore)
                prefs.edit().putBoolean("active", false).apply()
            }
            return true
        } catch (_: SecurityException) {
            return false
        }
    }
}
