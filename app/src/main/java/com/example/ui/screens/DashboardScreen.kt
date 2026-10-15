package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BoostResult
import com.example.data.model.GameItem
import com.example.data.model.PerformanceSnapshot
import com.example.ui.components.FloatingGamingHud
import com.example.ui.components.GlassCard
import com.example.ui.components.MetricCircularGauge
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformancePurple
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    currentMetrics: PerformanceSnapshot,
    isOptimizing: Boolean,
    optimizationStep: String,
    lastBoostResult: BoostResult?,
    games: List<GameItem>,
    isFloatingHudActive: Boolean,
    hudOpacity: Float = 0.90f,
    isArabic: Boolean,
    onTriggerAutoBoost: () -> Unit,
    onToggleFloatingHud: (Boolean) -> Unit,
    onHudOpacityChanged: (Float) -> Unit = {},
    onLaunchGame: (GameItem) -> Unit,
    onNavigateToSessions: () -> Unit
) {
    val scrollState = rememberScrollState()

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Luxury Architectural Glass Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = RoyalGold.copy(alpha = 0.4f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Glass Emblem
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, RoyalGold, CircleShape)
                        .shadow(8.dp, CircleShape, spotColor = RoyalGold)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_glass_boost_logo),
                        contentDescription = "GlassBoost Emblem",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "جلاس بوست • مُعزز الألعاب الخارق" else "GLASSBOOST • LUXURY ENGINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = RoyalGold
                    )
                    Text(
                        text = if (isArabic) "وضع الألعاب عالي الأداء جاهز" else "Esports Ultra Latency Mode",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(PerformanceGreen)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isArabic) "النظام مستقر • صفر لاغ" else "0% Packet Jitter • High Priority",
                            fontSize = 12.sp,
                            color = PerformanceGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Big Cinematic "AUTO-BOOST" Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(if (isOptimizing) pulseScale else 1f)
                    .size(190.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                RoyalGold.copy(alpha = 0.25f),
                                DeepGlassNavy.copy(alpha = 0.95f),
                                Color.Black
                            )
                        )
                    )
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(
                            listOf(RoyalGold, GlassCyan, ElectricBlue, RoyalGoldLight, RoyalGold)
                        ),
                        shape = CircleShape
                    )
                    .clickable(enabled = !isOptimizing, onClick = onTriggerAutoBoost),
                contentAlignment = Alignment.Center
            ) {
                if (isOptimizing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(175.dp),
                        color = GlassCyan,
                        strokeWidth = 3.dp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Boost",
                        tint = if (isOptimizing) GlassCyan else GoldGlow,
                        modifier = Modifier.size(46.dp)
                    )
                    Text(
                        text = if (isOptimizing) {
                            if (isArabic) "جاري التسريع..." else "OPTIMIZING..."
                        } else {
                            if (isArabic) "تسريع توربو" else "AUTO BOOST"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (isArabic) "إزالة اللاغ وتنظيف الرام" else "Kill Lags & Free RAM",
                        fontSize = 11.sp,
                        color = RoyalGoldLight,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Animated Optimization Step Progress Banner
        AnimatedVisibility(
            visible = isOptimizing && optimizationStep.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                borderColor = GlassCyan
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = GlassCyan
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = optimizationStep,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Last Boost Result Toast Card
        if (lastBoostResult != null && !isOptimizing) {
            Spacer(modifier = Modifier.height(14.dp))
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = PerformanceGreen.copy(alpha = 0.5f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = PerformanceGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isArabic) "اكتمل تسريع النظام بنجاح!" else "System Turbo Optimization Complete!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PerformanceGreen
                        )
                        Text(
                            text = if (isArabic)
                                "تم تحرير ${lastBoostResult.ramFreedMb} ميجابايت رام • خفض البنج بمقدار ${lastBoostResult.initialPingMs - lastBoostResult.optimizedPingMs}ms"
                            else
                                "Freed ${lastBoostResult.ramFreedMb} MB RAM • Ping dropped by ${lastBoostResult.initialPingMs - lastBoostResult.optimizedPingMs}ms",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Real-Time Hardware Telemetry Gauges Grid
        Text(
            text = if (isArabic) "مؤشرات الأداء اللحظية (Real-Time Metrics)" else "REAL-TIME TELEMETRY",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalGold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    MetricCircularGauge(
                        value = currentMetrics.fps.toFloat(),
                        maxValue = 120f,
                        unit = "FPS",
                        label = if (isArabic) "معدل الإطارات" else "Frame Rate",
                        color = PerformanceGreen
                    )
                    MetricCircularGauge(
                        value = currentMetrics.pingMs.toFloat(),
                        maxValue = 120f,
                        unit = "ms",
                        label = if (isArabic) "زمن الاستجابة" else "Ping Latency",
                        color = GlassBlue
                    )
                    MetricCircularGauge(
                        value = currentMetrics.cpuUsagePercent.toFloat(),
                        maxValue = 100f,
                        unit = "%",
                        label = if (isArabic) "المعالج" else "CPU Load",
                        color = RoyalGold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // RAM & Temperature Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "RAM",
                            tint = GlassCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic)
                                "الرام: ${currentMetrics.ramUsedMb} / ${currentMetrics.ramTotalMb} MB"
                            else
                                "RAM: ${currentMetrics.ramUsedMb} / ${currentMetrics.ramTotalMb} MB",
                            fontSize = 12.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (currentMetrics.batteryTemp < 38f) PerformanceGreen else RoyalGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${currentMetrics.batteryTemp}°C (${currentMetrics.thermalState})",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Floating HUD Overlay Toggle Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isFloatingHudActive) GlassCyan else CardGlassSurface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DeepGlassNavy)
                            .border(1.dp, GlassCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "HUD",
                            tint = GlassCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isArabic) "الشاشة العائمة فوق الألعاب (Floating HUD)" else "In-Game Floating HUD Overlay",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isArabic) "عرض الـ FPS والبنج الحقيقي داخل ألعابك" else "Live FPS & Ping overlay during gameplay",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Switch(
                    checked = isFloatingHudActive,
                    onCheckedChange = onToggleFloatingHud,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GlassCyan,
                        checkedTrackColor = DeepGlassNavy,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Color.Black.copy(alpha = 0.5f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // HUD OPACITY SLIDER
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "شفافية اللوحة العائمة (HUD Opacity)" else "Floating HUD Opacity",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isArabic) "تقليل الشفافية لجعل اللوحة غير مزعجة أثناء اللعب" else "Make HUD less intrusive during gameplay",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RoyalGold.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${(hudOpacity * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = RoyalGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Slider(
                    value = hudOpacity,
                    onValueChange = onHudOpacityChanged,
                    valueRange = 0.30f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = RoyalGold,
                        activeTrackColor = GlassCyan,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Opacity Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Pair(0.40f, if (isArabic) "خفي (40%)" else "Stealth 40%"),
                        Pair(0.70f, if (isArabic) "متوازن (70%)" else "Subtle 70%"),
                        Pair(0.90f, if (isArabic) "قياسي (90%)" else "Default 90%"),
                        Pair(1.00f, if (isArabic) "صلب (100%)" else "Solid 100%")
                    ).forEach { (presetVal, presetLabel) ->
                        val isSelected = Math.abs(hudOpacity - presetVal) < 0.05f
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GlassCyan.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) GlassCyan else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onHudOpacityChanged(presetVal) }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = presetLabel,
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GlassCyan else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Live Floating HUD Preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "معاينة حية للوحة العائمة في الألعاب" else "LIVE COMPOSE HUD PREVIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassCyan,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = if (isArabic) "المس الدائرة للتوسيع • واسحبها للجانب" else "Tap circle to expand • Drag to dock",
                        fontSize = 10.sp,
                        color = RoyalGoldLight
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                FloatingGamingHud(
                    fps = currentMetrics.fps,
                    pingMs = currentMetrics.pingMs,
                    cpuPercent = currentMetrics.cpuUsagePercent,
                    ramUsedMb = currentMetrics.ramUsedMb,
                    batteryTemp = currentMetrics.batteryTemp,
                    thermalSafetyThreshold = 40.0f,
                    opacity = hudOpacity,
                    initialBubbleMode = true,
                    onSpeedBoost = onTriggerAutoBoost,
                    onClearCache = onTriggerAutoBoost,
                    onCoolDown = onTriggerAutoBoost,
                    onClose = { onToggleFloatingHud(false) },
                    onDragDelta = { _, _ -> },
                    onDragEnd = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Game Launch & Boost Carousel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "ألعابي المعززة (Launch with Turbo)" else "BOOSTED GAMES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                letterSpacing = 1.sp
            )
            Text(
                text = if (isArabic) "عرض سجل الجلسات ←" else "View Session Analytics →",
                fontSize = 12.sp,
                color = GlassCyan,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onNavigateToSessions)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(games) { game ->
                GlassCard(
                    modifier = Modifier
                        .width(170.dp)
                        .clickable { onLaunchGame(game) }
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepGlassNavy)
                                    .border(1.dp, RoyalGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Game",
                                    tint = RoyalGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PerformanceGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${game.targetFps} FPS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PerformanceGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = game.appName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = game.resolutionPreset,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGold)))
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "تسريع وتشغيل" else "Turbo Play",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
