package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.selectedPingTarget
import com.example.engine.tcpConnectMillis
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.net.ServerSocket

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TcpDiagnosticsTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    @Before fun resetSelection() {
        context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun savedRegionalSelectionResolvesSameActualAddressAfterRecreation() {
        context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE).edit()
            .putString("selected_ping_category", "regional").putString("selected_ping_server_id", "frankfurt").commit()
        val selected = selectedPingTarget(context)
        assertEquals("ec2.eu-central-1.amazonaws.com", selected.host)
        assertEquals(443, selected.port)
    }

    @Test fun invalidOrStaleSelectionFallsBackToVisibleCategory() {
        val prefs = context.getSharedPreferences("glassboost_storage", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_ping_category", "regional")
            .putString("selected_ping_server_id", "ultra_google_core").commit()
        assertEquals("riyadh", selectedPingTarget(context).id)
        prefs.edit().putString("selected_ping_category", "invalid")
            .putString("selected_ping_server_id", "missing").commit()
        assertEquals("8.8.8.8", selectedPingTarget(context).host)
    }

    @Test fun tcpProbePerformsAnActualHandshakeWithLocalListener() = runBlocking {
        ServerSocket(0).use { listener ->
            listener.soTimeout = 2000
            assertTrue(tcpConnectMillis("127.0.0.1", listener.localPort) >= 0)
            listener.accept().use { accepted -> assertTrue(accepted.isConnected) }
        }
    }

    @Test fun refusedAndInvalidPortsNeverProduceSuccessfulLatency() = runBlocking {
        val closedPort = ServerSocket(0).use { it.localPort }
        assertEquals(-1, tcpConnectMillis("127.0.0.1", closedPort))
        assertEquals(-1, tcpConnectMillis("127.0.0.1", -1))
    }
}
