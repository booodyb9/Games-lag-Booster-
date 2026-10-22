package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GamingSessionStorage
import com.example.data.model.PerformanceSnapshot
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GamingSessionStorageTest {
    private lateinit var context: Context

    @Before fun reset() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE).edit().clear().commit()
    }

    private fun sample(fps: Int, ping: Int, cpu: Int = -1, gpu: Int = -1) =
        PerformanceSnapshot(System.currentTimeMillis(), fps, ping, cpu, gpu, 1000, 4000, 30f)

    @Test fun firstInstallHasNoInventedSessions() {
        assertTrue(GamingSessionStorage(context).sessions.value.isEmpty())
    }

    @Test fun unavailableSamplesDoNotLowerValidAverages() {
        val storage = GamingSessionStorage(context)
        storage.startRecording("Test", "test.game")
        storage.appendSnapshot(sample(-1, -1))
        storage.appendSnapshot(sample(-1, 50, 20, 40))
        val session = storage.stopRecording()!!
        assertEquals(50, session.averagePing)
        assertEquals(20f, session.averageCpu, 0f)
        assertEquals(40f, session.averageGpu, 0f)
        assertEquals(-1f, session.averageFps, 0f)
        assertEquals(-1, session.stabilityScore)
    }

    @Test fun sessionDoesNotInventFreedRam() {
        val storage = GamingSessionStorage(context)
        storage.startRecording("Test", "test.game")
        storage.appendSnapshot(sample(60, 50))
        assertEquals(0L, storage.stopRecording()!!.ramFreedMb)
    }

    @Test fun unstableSessionIsNotForcedToSeventyPercent() {
        val storage = GamingSessionStorage(context)
        storage.startRecording("Test", "test.game")
        storage.appendSnapshot(sample(0, 50))
        storage.appendSnapshot(sample(120, 50))
        assertTrue(storage.stopRecording()!!.stabilityScore < 70)
    }

    @Test fun deletingLastSessionDoesNotRestoreDemoData() {
        val storage = GamingSessionStorage(context)
        storage.startRecording("Test", "test.game")
        storage.appendSnapshot(sample(60, 50))
        storage.stopRecording()
        storage.sessions.value.toList().forEach { storage.deleteSession(it.id) }
        assertTrue(GamingSessionStorage(context).sessions.value.isEmpty())
    }
}
