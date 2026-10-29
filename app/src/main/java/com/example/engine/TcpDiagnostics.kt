package com.example.engine

import android.content.Context
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

/** Resolve the same saved destination for the dashboard, HUD and preparation probes. */
internal fun selectedPingTarget(context: Context): PingServerTarget {
    val prefs = context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE)
    val targets = if (prefs.getString("selected_ping_category", "ultra") == "regional")
        REGIONAL_PING_SERVERS else ULTRA_LOW_PING_SERVERS
    return targets.firstOrNull { it.id == prefs.getString("selected_ping_server_id", null) }
        ?: targets.first()
}

/** TCP handshake timing, including hostname resolution; never in-game latency. */
internal suspend fun tcpConnectMillis(host: String, port: Int, timeoutMs: Int = 1500): Int =
    withContext(Dispatchers.IO) {
        currentCoroutineContext().ensureActive()
        val start = System.nanoTime()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                ((System.nanoTime() - start) / 1_000_000).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            }
        } catch (_: java.io.IOException) { -1 }
        catch (_: SecurityException) { -1 }
        catch (_: IllegalArgumentException) { -1 }
    }
