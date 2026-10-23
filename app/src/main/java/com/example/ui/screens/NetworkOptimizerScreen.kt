package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PingServerTarget
import com.example.data.model.DEFAULT_PING_SERVERS
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardGlassBorder
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.DeepVoidBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceAmber
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBlueMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Screen 4: public TCP connectivity benchmark
 * Includes:
 * 1. Public TCP endpoints grouped for convenient comparison; they are not game servers.
 * 2. Test buttons measure TCP connection time and failed connection attempts.
 * 3. Individual and bulk tests are available.
 * 4. Results are not in-game latency or ICMP packet-loss measurements.
 * 5. This benchmark does not reroute traffic and is not a VPN.
 */
@Composable
fun NetworkOptimizerScreen(
    servers: List<PingServerTarget> = DEFAULT_PING_SERVERS,
    selectedServerId: String = "ultra_google_core",
    isTestingAll: Boolean = false,
    isTestingSingle: Boolean = false,
    isOptimizingNetwork: Boolean = false,
    networkOptimizationDone: Boolean = false,
    overallJitterMs: Int = 2,
    isArabic: Boolean = true,
    onSelectServer: (String) -> Unit = {},
    onSelectCategory: (String) -> Unit = {},
    onTestSelectedServer: (PingServerTarget) -> Unit = {},
    onTestAllServers: () -> Unit = {},
    onOptimizeNetworkRouting: () -> Unit = {}
) {
    val effectiveServers = if (servers.isEmpty()) DEFAULT_PING_SERVERS else servers
    var selectedCategory by remember(effectiveServers) {
        mutableStateOf(if (effectiveServers.firstOrNull()?.category == "regional") "regional" else "ultra")
    }
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("glassboost_storage", android.content.Context.MODE_PRIVATE) }
    var dnsOptimizationEnabled by remember { mutableStateOf(prefs.getBoolean("dns_warmup", true)) }
    var connectionBoostEnabled by remember { mutableStateOf(prefs.getBoolean("tcp_warmup", true)) }
    var destinationDropdownExpanded by remember { mutableStateOf(false) }

    val currentSelectedServer = effectiveServers.find { it.id == selectedServerId } ?: effectiveServers.firstOrNull()

    // Automatically trigger test on entry if not tested yet
    LaunchedEffect(selectedServerId, effectiveServers) {
        if (currentSelectedServer != null && currentSelectedServer.lastTestedTimestamp == null && !isTestingSingle && !isTestingAll) {
            onTestSelectedServer(currentSelectedServer)
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "radar_pulse")
    val radarScale1 by pulseTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_1"
    )
    val radarAlpha1 by pulseTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_alpha_1"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "فحص الشبكة والوجهات" else "Network endpoint diagnostics",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = if (isArabic) "قياس زمن اتصال TCP بوجهات عامة، وليس خوادم ألعاب" else "TCP connection timing to public endpoints, not game servers",
                        fontSize = 11.sp,
                        color = SlateBlueMuted
                    )
                }

                // Test All button in header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isTestingAll) DeepGlassNavy else CardGlassSurface)
                        .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable(enabled = !isTestingAll && !isTestingSingle, onClick = onTestAllServers)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    if (isTestingAll) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = NeonCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "جاري الفحص..." else "Testing...",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Test All",
                                tint = NeonCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "اختبار الكل" else "Test All",
                                fontSize = 11.sp,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Concentric Radar Circles with Wi-Fi Icon
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(140.dp)) {
                    val radius = size.minDimension / 2

                    drawCircle(
                        color = ElectricBlue.copy(alpha = 0.15f),
                        radius = radius * 0.95f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.25f),
                        radius = radius * 0.70f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = ElectricBlue.copy(alpha = 0.35f),
                        radius = radius * 0.45f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    drawCircle(
                        color = NeonCyan.copy(alpha = radarAlpha1),
                        radius = radius * radarScale1,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(ElectricBlue, DeepGlassNavy)
                            )
                        )
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi Radar",
                        tint = PureWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Section 3: Selected Ping Destination Primary Test Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NeonCyan.copy(alpha = 0.45f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header row: Destination picker trigger
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Destination",
                                tint = RoyalGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "الوجهة النشطة للاختبار" else "ACTIVE TEST DESTINATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalGold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Dropdown selection trigger
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { destinationDropdownExpanded = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentSelectedServer?.let { "${it.iconEmoji} ${if (isArabic) it.nameAr else it.nameEn}" }
                                            ?: if (isArabic) "اختر الوجهة" else "Select Target",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PureWhite
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Dropdown",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = destinationDropdownExpanded,
                                onDismissRequest = { destinationDropdownExpanded = false },
                                modifier = Modifier
                                    .background(DeepVoidBlack)
                                    .border(1.dp, CardGlassBorder, RoundedCornerShape(8.dp))
                            ) {
                                effectiveServers.forEach { server ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = server.iconEmoji, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isArabic) server.nameAr else server.nameEn,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (server.id == selectedServerId) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (server.id == selectedServerId) NeonCyan else PureWhite
                                                )
                                            }
                                        },
                                        onClick = {
                                            onSelectServer(server.id)
                                            destinationDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (currentSelectedServer != null) {
                        val ping = currentSelectedServer.currentPingMs
                        val isFailed = currentSelectedServer.isFailed
                        val jitter = currentSelectedServer.jitterMs
                        val loss = currentSelectedServer.packetLossPercent

                        val pingColor = when {
                            isFailed -> PerformanceRed
                            ping == null -> SlateBlueMuted
                            ping < 45 -> PerformanceGreen
                            ping < 90 -> NeonCyan
                            ping < 160 -> PerformanceAmber
                            else -> PerformanceRed
                        }

                        // Display Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .border(1.dp, pingColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${currentSelectedServer.iconEmoji} ${if (isArabic) currentSelectedServer.nameAr else currentSelectedServer.nameEn}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PureWhite
                                        )
                                        Text(
                                            text = "${currentSelectedServer.location} • ${currentSelectedServer.host}",
                                            fontSize = 10.sp,
                                            color = SlateBlueMuted
                                        )
                                    }

                                    // Main Ping or Failure Readout
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(pingColor.copy(alpha = 0.18f))
                                            .border(1.dp, pingColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isFailed -> if (isArabic) "تعذر الاتصال" else "Failed"
                                                ping != null -> "${ping}ms"
                                                else -> "--"
                                            },
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = pingColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Sub-metrics: Connection Status, Jitter, Packet Loss
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CardGlassSurface)
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isArabic) "حالة الاتصال" else "STATUS",
                                                fontSize = 9.sp,
                                                color = SlateBlueMuted
                                            )
                                            Text(
                                                text = if (isFailed) {
                                                    if (isArabic) "تعذر الوصول" else "Unreachable"
                                                } else if (ping != null) {
                                                    when {
                                                        ping < 45 -> if (isArabic) "زمن اتصال TCP منخفض" else "Low TCP connect time"
                                                        ping < 90 -> if (isArabic) "زمن اتصال TCP جيد" else "Good TCP connect time"
                                                        ping < 160 -> if (isArabic) "زمن اتصال TCP متوسط" else "Moderate TCP connect time"
                                                        else -> if (isArabic) "تأخير مرتفع" else "High Latency"
                                                    }
                                                } else {
                                                    if (isArabic) "جاهز للاختبار" else "Ready to Test"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = pingColor,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    // Jitter Badge
                                    Box(
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CardGlassSurface)
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "JITTER",
                                                fontSize = 9.sp,
                                                color = SlateBlueMuted
                                            )
                                            Text(
                                                text = if (jitter != null && !isFailed) "${jitter}ms" else "--",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PureWhite
                                            )
                                        }
                                    }

                                    // Packet Loss Badge
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CardGlassSurface)
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = if (isArabic) "فشل TCP" else "TCP FAIL",
                                                fontSize = 9.sp,
                                                color = SlateBlueMuted
                                            )
                                            Text(
                                                text = if (loss != null) "${loss}%" else "—",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (loss != null && loss > 0) PerformanceRed else PerformanceGreen
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Dedicated "اختبار Ping" Button for the selected destination
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                                )
                                .clickable(
                                    enabled = !isTestingSingle && !isTestingAll,
                                    onClick = { onTestSelectedServer(currentSelectedServer) }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isTestingSingle) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = ObsidianBlack,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isArabic) "جاري قياس زمن استجابة TCP..." else "Testing TCP Connect Latency...",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ObsidianBlack
                                    )
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Ping",
                                        tint = ObsidianBlack,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) "⚡ اختبار Ping للوجهة المحددة" else "⚡ Test Selected Ping",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ObsidianBlack
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Technical Clarification Note (TCP Connect vs ICMP)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepGlassNavy.copy(alpha = 0.4f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Technical Info",
                                tint = NeonCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic)
                                    "النتيجة تمثل زمن اتصال TCP المباشر (TCP Handshake Socket) بنقاط الاختبار الفعلية لضمان الدقة وتوافق أندرويد."
                                else
                                    "Measured via real direct TCP Handshake Socket Connect to reachable endpoints, complying with Android network APIs.",
                                fontSize = 10.sp,
                                color = SlateBlueMuted,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Public TCP endpoints list with category switcher
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) {
                            if (selectedCategory == "ultra") "وجهات DNS عامة" else "وجهات اختبار متنوعة (${effectiveServers.size})"
                        } else {
                            if (selectedCategory == "ultra") "PUBLIC DNS ENDPOINTS" else "OTHER PUBLIC TEST ENDPOINTS (${effectiveServers.size})"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGold,
                        letterSpacing = 0.8.sp
                    )

                    Text(
                        text = if (isArabic) "انقر للاختبار والفحص" else "Tap to select & test",
                        fontSize = 10.sp,
                        color = SlateBlueMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Category switcher: public DNS endpoints vs other public endpoints
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepVoidBlack)
                        .border(1.dp, CardGlassBorder, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val isUltraActive = selectedCategory == "ultra"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isUltraActive) NeonCyan else Color.Transparent)
                            .clickable {
                                selectedCategory = "ultra"
                                onSelectCategory("ultra")
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⚡", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "وجهات DNS عامة" else "Public DNS endpoints",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isUltraActive) ObsidianBlack else PureWhite
                            )
                        }
                    }

                    val isRegionalActive = selectedCategory == "regional"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isRegionalActive) NeonCyan else Color.Transparent)
                            .clickable {
                                selectedCategory = "regional"
                                onSelectCategory("regional")
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🌍", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "خوادم إقليمية" else "Regional Nodes",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRegionalActive) ObsidianBlack else PureWhite
                            )
                        }
                    }
                }
            }
        }

        // All 8 Servers Items with direct "فحص" button
        items(effectiveServers, key = { it.id }) { server ->
            val isSelected = server.id == selectedServerId
            val ping = server.currentPingMs
            val isFailed = server.isFailed
            val pingColor = when {
                isFailed -> PerformanceRed
                ping == null -> SlateBlueMuted
                ping < 45 -> PerformanceGreen
                ping < 90 -> NeonCyan
                ping < 160 -> PerformanceAmber
                else -> PerformanceRed
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) DeepGlassNavy.copy(alpha = 0.5f) else CardGlassSurface)
                    .border(
                        1.dp,
                        if (isSelected) NeonCyan else Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectServer(server.id) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = server.iconEmoji,
                            fontSize = 20.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isArabic) server.nameAr else server.nameEn,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) NeonCyan else PureWhite
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${server.location} • ${server.host}",
                                fontSize = 10.sp,
                                color = SlateBlueMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Ping Result Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pingColor.copy(alpha = 0.18f))
                                .border(1.dp, pingColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = when {
                                    isFailed -> if (isArabic) "تعذر الاتصال" else "Failed"
                                    ping != null -> "${ping}ms"
                                    else -> "--"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = pingColor
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Inline "فحص" button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CardGlassSurface)
                                .border(1.dp, NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .clickable(
                                    enabled = !isTestingSingle && !isTestingAll,
                                    onClick = {
                                        onSelectServer(server.id)
                                        onTestSelectedServer(server)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isArabic) "فحص" else "Test",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }
                }
            }
        }

        // Section 5: System Network Optimization Controls (DNS, Fastpath, Boost)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = if (networkOptimizationDone) PerformanceGreen else NeonCyan.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isArabic) "فحص وتهيئة اتصال التطبيق" else "App connectivity diagnostics",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic) "تهيئة اختبارات هذا التطبيق؛ لا تغيّر اتصال اللعبة" else "Warm-up for this app only; game routes are unchanged",
                        fontSize = 11.sp,
                        color = SlateBlueMuted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Toggle 1: تحسين DNS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "DNS",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isArabic) "تهيئة DNS لهذا التطبيق فقط" else "Pre-resolve DNS for this app only",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        Switch(
                            checked = dnsOptimizationEnabled,
                            onCheckedChange = { dnsOptimizationEnabled = it; prefs.edit().putBoolean("dns_warmup", it).apply() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = DeepGlassNavy,
                                uncheckedThumbColor = SlateBlueMuted,
                                uncheckedTrackColor = DarkSlate
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Toggle 2: تسريع الاتصال
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Speed",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isArabic) "فتح اتصال TCP تجريبي" else "Open a test TCP connection",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        Switch(
                            checked = connectionBoostEnabled,
                            onCheckedChange = { connectionBoostEnabled = it; prefs.edit().putBoolean("tcp_warmup", it).apply() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = DeepGlassNavy,
                                uncheckedThumbColor = SlateBlueMuted,
                                uncheckedTrackColor = DarkSlate
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Optimize Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CardGlassSurface)
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable(enabled = !isOptimizingNetwork, onClick = onOptimizeNetworkRouting),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOptimizingNetwork) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = NeonCyan,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "جاري تهيئة اتصال التطبيق..." else "Warming this app connection...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan
                                )
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Optimize",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تنفيذ خيارات التهيئة المحددة" else "Run selected connection warm-ups",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            val best = effectiveServers.filter { !it.isFailed && it.currentPingMs != null }.minByOrNull { it.currentPingMs!! }
            androidx.compose.material3.OutlinedButton(
                enabled = best != null,
                onClick = { best?.let { onSelectServer(it.id) } }
            ) { Text(if (isArabic) "اختيار أسرع وجهة اختبار مقاسة" else "Select fastest measured test endpoint") }
            Text(
                if (isArabic) "الاختيار يحدد وجهة القياس فقط ولا يربط اللعبة بهذا الخادم. DNS وتغييرات الشبكة تتم من إعدادات الهاتف."
                else "Selection changes only the benchmark target, not game routing. Configure system DNS in phone settings.",
                color = TextSecondary, fontSize = 11.sp
            )
            androidx.compose.material3.TextButton(onClick = {
                val privateDns = android.content.Intent("android.settings.PRIVATE_DNS_SETTINGS")
                try { context.startActivity(privateDns) }
                catch (_: android.content.ActivityNotFoundException) {
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
                }
            }) { Text(if (isArabic) "فتح إعدادات DNS والشبكة" else "Open DNS and network settings") }
        }

        // Section 6: Policy & Honest Transparency Disclaimer (Rule 8)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic)
                        "تنبيه: هذا قياس لاتصال TCP بمواقع عامة وليس Ping داخل اللعبة أو قياس فقد حزم اللعبة. لا يغير مسار بيانات اللعبة ولا يعمل كـ VPN."
                    else
                        "Note: This measures TCP connection time to public endpoints, not in-game ping or game packet loss. It does not reroute game traffic or act as a VPN.",
                    fontSize = 10.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
