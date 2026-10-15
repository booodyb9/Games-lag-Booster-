package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PingServerTarget
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBlueMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Screen 4 from GlassBoost UI Design System:
 * شاشة تحسين الشبكة (Network Optimizer) with concentric radar pulse, connection status,
 * gaming DNS toggles, and regional server live ping tests.
 */
@Composable
fun NetworkOptimizerScreen(
    servers: List<PingServerTarget>,
    isTesting: Boolean,
    isOptimizingNetwork: Boolean,
    networkOptimizationDone: Boolean,
    overallJitterMs: Int,
    isArabic: Boolean,
    onTestAllServers: () -> Unit,
    onOptimizeNetworkRouting: () -> Unit
) {
    var dnsOptimizationEnabled by remember { mutableStateOf(true) }
    var connectionBoostEnabled by remember { mutableStateOf(true) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "محسن الشبكة" else "Network Optimizer",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
                Text(
                    text = if (isArabic) "تثبيت البنج وإزالة اللاغ والتقطيع" else "Low Ping Lock & Jitter Free",
                    fontSize = 11.sp,
                    color = SlateBlueMuted
                )
            }

            IconButton(onClick = onTestAllServers) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Test",
                    tint = NeonCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Center Concentric Radar Circles with Wi-Fi Icon (Screen 4 Visual Centerpiece)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                val radius = size.minDimension / 2

                // Concentric circles
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

                // Animated pulsing radar wave
                drawCircle(
                    color = NeonCyan.copy(alpha = radarAlpha1),
                    radius = radius * radarScale1,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Central Glowing Wi-Fi Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
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
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Connection Status Card (Matching Screen 4: "الاتصال محسن")
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (networkOptimizationDone) PerformanceGreen else NeonCyan.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isArabic) "الاتصال محسن" else "Connection Optimized",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isArabic) "تم تحسين استقرار الشبكة وزيادة السرعة" else "Network stability locked & latency reduced",
                    fontSize = 11.sp,
                    color = SlateBlueMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

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
                            text = if (isArabic) "تحسين خوادم DNS" else "Gaming DNS Fastpath",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    Switch(
                        checked = dnsOptimizationEnabled,
                        onCheckedChange = { dnsOptimizationEnabled = it },
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
                            text = if (isArabic) "تسريع الاتصال (TCP No-Delay)" else "Connection Accelerator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    Switch(
                        checked = connectionBoostEnabled,
                        onCheckedChange = { connectionBoostEnabled = it },
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
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                        )
                        .clickable(enabled = !isOptimizingNetwork, onClick = onOptimizeNetworkRouting),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOptimizingNetwork) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "جاري ضبط المسارات..." else "Optimizing routes...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Optimize",
                                tint = ObsidianBlack,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "تفعيل تحسين الشبكة الآن" else "Activate Network Boost",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ObsidianBlack
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Regional Ping Nodes List
        Text(
            text = if (isArabic) "خوادم الألعاب الإقليمية (Live Ping)" else "REGIONAL GAMING SERVERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalGold,
            letterSpacing = 0.8.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(servers) { server ->
                val ping = server.currentPingMs
                val pingColor = when {
                    ping == null -> SlateBlueMuted
                    ping < 35 -> PerformanceGreen
                    ping < 65 -> NeonCyan
                    else -> PerformanceRed
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardGlassSurface)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = server.iconEmoji,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) server.nameAr else server.nameEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                                Text(
                                    text = server.location,
                                    fontSize = 10.sp,
                                    color = SlateBlueMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pingColor.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (ping != null) "${ping}ms" else "--",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = pingColor
                            )
                        }
                    }
                }
            }
        }
    }
}
