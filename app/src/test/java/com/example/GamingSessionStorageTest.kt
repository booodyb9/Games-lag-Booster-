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

    @Test fun legacyAggregatesAreRecomputedFromValidSamples() {
        val storage = GamingSessionStorage(context)
        storage.startRecording("Legacy", "test.legacy")
        storage.appendSnapshot(sample(-1, -1))
        storage.appendSnapshot(sample(-1, 50, 20, 40))
        storage.stopRecording()
        val prefs = context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE)
        val json = org.json.JSONArray(prefs.getString("saved_sessions", "[]"))
        json.getJSONObject(0).apply {
            remove("measurementVersion")
            put("averagePing", 24)
            put("averageCpu", 9.0)
            put("averageGpu", 19.0)
            put("averageFps", 89.0)
            put("ramFreedMb", 512)
            put("stabilityScore", 98)
        }
        prefs.edit().putString("saved_sessions", json.toString()).commit()
        val loaded = GamingSessionStorage(context).sessions.value.first()
        assertEquals(50, loaded.averagePing)
        assertEquals(20f, loaded.averageCpu, 0f)
        assertEquals(40f, loaded.averageGpu, 0f)
        assertEquals(-1f, loaded.averageFps, 0f)
        assertEquals(0L, loaded.ramFreedMb)
        assertEquals(-1, loaded.stabilityScore)
    }
}
