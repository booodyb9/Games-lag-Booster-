package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PingServerTarget
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "مُحسّن استجابة الشبكة والـ PING" else "NETWORK & PING LATENCY STABILIZER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = RoyalGold
                )
                Text(
                    text = if (isArabic) "خوادم الألعاب وتثبيت البنج" else "Multi-Region Game Servers",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            IconButton(onClick = onTestAllServers) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Test",
                    tint = GlassCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Lag Optimizer Action Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (networkOptimizationDone) PerformanceGreen else GlassBlue
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(DeepGlassNavy)
                            .border(1.dp, GlassCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "DNS",
                            tint = GlassCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "تقليل اللاغ وحظر فقدان الحزم" else "Anti-Lag & Packet Stabilizer",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isArabic)
                                "توجيه الاتصال لأقرب نقطة ألعاب في الرياض • خفض التذبذب (Jitter)"
                            else
                                "Optimized routing to nearest Saudi ME cluster • Jitter: ±${overallJitterMs}ms",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(DeepGlassNavy, ElectricBlue, GlassCyan)
                            )
                        )
                        .clickable(enabled = !isOptimizingNetwork, onClick = onOptimizeNetworkRouting)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOptimizingNetwork) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "جاري ضبط الحزم وخوادم DNS..." else "Optimizing packet routes & DNS buffers...",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (networkOptimizationDone) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                contentDescription = "Optimize",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic)
                                    (if (networkOptimizationDone) "تم تفعيل مسار تقليل اللاغ بنجاح" else "تحسين إعدادات الشبكة ومسار DNS")
                                else
                                    (if (networkOptimizationDone) "Network Optimized & Latency Locked" else "Optimize Network & Gaming DNS"),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Regional Gaming Servers List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "قياس سرعة استجابة الخوادم (Live Ping)" else "REGIONAL GAMING NODES PING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                letterSpacing = 1.sp
            )
            if (isTesting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = GlassCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(servers) { server ->
                val ping = server.currentPingMs
                val pingColor = when {
                    ping == null -> TextSecondary
                    ping < 35 -> PerformanceGreen
                    ping < 65 -> GlassBlue
                    else -> PerformanceRed
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (server.id == "riyadh") RoyalGold.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = server.iconEmoji,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (isArabic) server.nameAr else server.nameEn,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    if (server.id == "riyadh") {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(RoyalGold.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isArabic) "الأسرع" else "FASTEST",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = RoyalGold
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${server.location} • ${server.host}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Ping readout badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, pingColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (ping != null) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$ping ms",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = pingColor
                                    )
                                    Text(
                                        text = "±${server.jitterMs ?: 2}ms jitter",
                                        fontSize = 9.sp,
                                        color = TextSecondary
                                    )
                                }
                            } else {
                                Text(
                                    text = "Testing...",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
