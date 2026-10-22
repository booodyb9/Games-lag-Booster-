package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FloatingPanelServiceTest {
    @Test fun opacityWhileOffIsSavedWithoutStartingAService() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        FloatingPanelService.updateOpacity(app, 0.5f)
        assertEquals(0.5f, app.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE).getFloat("hud_opacity", 0f), 0f)
        assertNull(shadowOf(app).nextStartedService)
    }
}
