package com.example

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.GameModeController
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GameModeControllerTest {
    private lateinit var context: Context
    private lateinit var manager: NotificationManager

    @Before fun setup() {
        context = ApplicationProvider.getApplicationContext()
        manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        context.getSharedPreferences("glassboost_game_mode", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun deniedAccessDoesNotReportEnabled() {
        shadowOf(manager).setNotificationPolicyAccessGranted(false)
        val mode = GameModeController(context)
        assertFalse(mode.setEnabled(true))
        assertFalse(mode.isActive)
    }

    @Test fun enableAndDisableRestoreThePreviousInterruptionFilter() {
        shadowOf(manager).setNotificationPolicyAccessGranted(true)
        manager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        val mode = GameModeController(context)
        assertTrue(mode.setEnabled(true))
        assertEquals(NotificationManager.INTERRUPTION_FILTER_PRIORITY, manager.currentInterruptionFilter)
        // Re-enabling must not overwrite the original filter.
        assertTrue(mode.setEnabled(true))
        assertTrue(GameModeController(context).isActive)
        assertTrue(GameModeController(context).setEnabled(false))
        assertEquals(NotificationManager.INTERRUPTION_FILTER_ALL, manager.currentInterruptionFilter)
        assertFalse(mode.isActive)
    }
}
