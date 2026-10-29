package com.example

import android.content.Context
import android.provider.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.ProcessCleanerScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class MemoryManagementEntryTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyRestrictedProcessListStillOffersRealAndroidAppManagement() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        compose.setContent {
            MaterialTheme {
                ProcessCleanerScreen(
                    processes = emptyList(), isScanning = false, isKilling = false,
                    usedRamMb = 2048, totalRamMb = 4096, isArabic = true,
                    onRefreshScan = {}, onKillAllHeavy = {}, onKillSingle = {}
                )
            }
        }
        compose.onNodeWithText("إدارة التطبيقات المثبتة").performScrollTo().performClick()
        assertEquals(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS,
            shadowOf(context as android.app.Application).nextStartedActivity.action)
    }
}
