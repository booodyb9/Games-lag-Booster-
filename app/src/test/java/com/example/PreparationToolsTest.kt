package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.engine.AutoBoostEngine
import com.example.engine.summarizeTcpProbes
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PreparationToolsTest {
    @Test fun jitterIsNeverClampedToThreeMilliseconds() {
        val result = summarizeTcpProbes(listOf(10, 100, 10), 3)
        assertEquals(90, result.jitterMs)
        assertEquals(0, result.packetLossPercent)
    }

    @Test fun unreachableEndpointHasNoFabricatedMeasurements() {
        val result = summarizeTcpProbes(emptyList(), 3)
        assertNull(result.pingMs)
        assertNull(result.jitterMs)
        assertEquals(100, result.packetLossPercent)
        assertFalse(result.isSuccess)
    }

    @Test fun oneSuccessfulProbeCannotEstimateJitter() {
        val result = summarizeTcpProbes(listOf(20), 3)
        assertNull(result.jitterMs)
        assertEquals(66, result.packetLossPercent)
    }

    @Test fun preparationSettingsSurviveRecreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val options = PreparationOptions(false, false, false, false, true)
        PreparationPreferences(context).save(options)
        assertEquals(options, PreparationPreferences(context).read())
    }

    @Test fun gameProfileSurvivesRecreation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE).edit().clear().commit()
        val storage = GamingSessionStorage(context)
        storage.addCustomGame("Test", "test.profile")
        val edited = storage.games.value.first { it.packageName == "test.profile" }.copy(targetFps = 120, resolutionPreset = "Smooth Performance (720p)")
        storage.updateGameProfile(edited)
        assertEquals(edited, GamingSessionStorage(context).games.value.first { it.packageName == "test.profile" })
    }

    @Test fun cleanupCountsDeletedBytesAndPreservesUserData() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.cacheDir.deleteRecursively()
        context.cacheDir.mkdirs()
        val cached = java.io.File(context.cacheDir, "discardable")
        cached.writeBytes(ByteArray(1024))
        val document = java.io.File(context.filesDir, "keep-me")
        document.writeText("user data")
        val engine = AutoBoostEngine(context)
        val result = engine.executeAutoBoost(checkNetwork = false)
        assertEquals(1024L, result.cacheDeletedBytes)
        assertFalse(cached.exists())
        assertEquals("user data", document.readText())
        assertEquals(0, result.processesKilledCount)
        assertEquals(-1, result.initialPingMs)
        assertFalse(engine.isOptimizing.value)
    }

    @Test fun disabledCleanupDoesNotDeleteCache() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cached = java.io.File(context.cacheDir, "disabled-cleanup").apply { parentFile!!.mkdirs(); writeText("keep") }
        val result = AutoBoostEngine(context).executeAutoBoost(cleanCache = false, checkNetwork = false)
        assertTrue(cached.exists())
        assertEquals(0L, result.cacheDeletedBytes)
    }

    @Test @Config(sdk = [34]) fun modernAndroidCannotClaimAnotherProcessWasKilled() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertFalse(AutoBoostEngine(context).killSingleProcess("test.other.app"))
    }
}
