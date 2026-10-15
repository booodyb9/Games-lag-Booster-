package com.example

import com.example.data.model.GamingSession
import com.example.data.model.PerformanceSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testStabilityCalculation() {
        val snapshots = listOf(
            PerformanceSnapshot(0, 60, 25, 30, 40, 3000, 8000, 33f),
            PerformanceSnapshot(1000, 58, 26, 32, 42, 3000, 8000, 33f),
            PerformanceSnapshot(2000, 59, 24, 31, 41, 3000, 8000, 33f)
        )
        val minFps = snapshots.minOf { it.fps }
        val maxFps = snapshots.maxOf { it.fps }
        val delta = maxFps - minFps
        assertTrue("FPS delta should be minimal for stable session", delta <= 5)
    }

    @Test
    fun testGamingSessionProperties() {
        val session = GamingSession(
            id = "test-session",
            gameTitle = "PUBG Mobile",
            packageName = "com.tencent.ig",
            startTime = 1000L,
            endTime = 5000L,
            durationSeconds = 4L,
            averageFps = 59.5f,
            minFps = 58,
            maxFps = 60,
            averagePing = 25,
            maxPing = 30,
            averageCpu = 35f,
            averageGpu = 45f,
            ramFreedMb = 512L,
            stabilityScore = 98
        )
        assertEquals("PUBG Mobile", session.gameTitle)
        assertEquals(98, session.stabilityScore)
        assertTrue(session.averageFps >= 50f)
    }
}
