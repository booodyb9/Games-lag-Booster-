package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BoostResult
import com.example.data.model.GameItem
import com.example.data.model.PerformanceSnapshot
import com.example.data.model.PingServerTarget
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.ui.components.AppIconImage
import com.example.ui.components.BoostSuccessDialog
import com.example.ui.components.FloatingGamingHud
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.data.model.DEFAULT_PING_SERVERS

/**
 * GlassBoost Dashboard Screen.
 * Faithful implementation of the GlassBoost UI Design Preview (Screen 2: الواجهة الرئيسية and Screen 9: شاشة نجاح التحسين).
 */
@Composable
fun DashboardScreen(
    currentMetrics: PerformanceSnapshot,
    isOptimizing: Boolean,
    optimizationStep: String,
    lastBoostResult: BoostResult?,
    showResultSummary: Boolean = true,
    games: List<GameItem>,
    isFloatingHudActive: Boolean,
    hudOpacity: Float = 0.70f,
    isArabic: Boolean,
    pingServers: List<PingServerTarget> = DEFAULT_PING_SERVERS,
    onTriggerAutoBoost: () -> Unit,
    onToggleFloatingHud: (Boolean) -> Unit,
    onHudOpacityChanged: (Float) -> Unit = {},
    onLaunchGame: (GameItem) -> Unit,
    onNavigateToSessions: () -> Unit,
    onNavigateToNetwork: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    var showSuccessModal by remember { mutableStateOf(false) }

    // Trigger success dialog on optimization completion
    androidx.compose.runtime.LaunchedEffect(isOptimizing, lastBoostResult) {
        if (!isOptimizing && lastBoostResult != null) {
            showSuccessModal = true
        }
    }

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

    // Calculate real RAM usage
    val ramUsedGb = (currentMetrics.ramUsedMb / 1024.0).let { String.format("%.1f", it) }
    val ramTotalGb = (currentMetrics.ramTotalMb / 1024.0).let { String.format("%.0f", it) }
    val ramRatioPercent = if (currentMetrics.ramTotalMb > 0) {
        ((currentMetrics.ramUsedMb.toFloat() / currentMetrics.ramTotalMb.toFloat()) * 100).toInt()
    } else -1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Brand Header Row with Crown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_glass_boost_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "GlassBoost",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = PureWhite
                )
            }

            // Royal Gold Crown Badge
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, RoyalGold.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = "VIP Crown",
                    tint = RoyalGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Center Performance Boost Gauge (Screen 2 Arc)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Arc Gauge Canvas
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedPercentage by animateFloatAsState(
                        targetValue = if (isOptimizing) 98f else 85f,
                        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                        label = "gauge_arc"
                    )

                    Canvas(modifier = Modifier.size(190.dp)) {
                        val strokeWidth = 12.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                        val arcSize = Size(diameter, diameter)

                        // Background Track
                        drawArc(
                            color = DarkSlate.copy(alpha = 0.5f),
                            startAngle = 140f,
                            sweepAngle = 260f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )

                        // Glowing Gradient Arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    ElectricBlue,
                                    NeonCyan,
                                    RoyalGold,
                                    ElectricBlue
                                )
                            ),
                            startAngle = 140f,
                            sweepAngle = (animatedPercentage / 100f) * 260f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    // Inside Gauge Text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isOptimizing) "98%" else "85%",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "تحسين الأداء" else "Performance Boost",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateBlueMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Electric Cyan Pill Button: ⚡ Boost
                Box(
                    modifier = Modifier
                        .scale(if (isOptimizing) pulseScale else 1f)
                        .width(180.dp)
                        .height(48.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = NeonCyan)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(NeonCyan, ElectricBlue)
                            )
                        )
                        .clickable(enabled = !isOptimizing, onClick = onTriggerAutoBoost),
                    contentAlignment = Alignment.Center
                ) {
                    if (isOptimizing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = ObsidianBlack,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "جاري التحضير..." else "Boosting...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = ObsidianBlack
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Boost",
                                tint = ObsidianBlack,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "⚡ تحضير وقياس" else "⚡ Prepare",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = ObsidianBlack
                            )
                        }
                    }
                }

                // Optimization Step Progress Text
                AnimatedVisibility(visible = isOptimizing && optimizationStep.isNotBlank()) {
                    Text(
                        text = optimizationStep,
                        fontSize = 11.sp,
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2x2 Telemetry Cards (Matching Screen 2 in the design preview)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: RAM
            TelemetryGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Memory,
                label = "RAM",
                mainValue = if (ramRatioPercent >= 0) "$ramRatioPercent%" else "—",
                subtitle = if (currentMetrics.ramTotalMb > 0) "$ramUsedGb GB / $ramTotalGb GB" else if (isArabic) "القياس غير متاح" else "Unavailable",
                isPositive = ramRatioPercent in 0..79
            )

            // Card 2: CPU
            TelemetryGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Thermostat,
                label = if (isArabic) "حرارة البطارية" else "Battery temp",
                mainValue = if (currentMetrics.batteryTemp >= 0f) "${currentMetrics.batteryTemp.toInt()}°C" else "—",
                subtitle = currentMetrics.thermalState,
                isPositive = currentMetrics.batteryTemp in 0f..40f
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 3: Network & Ping Servers
            TelemetryGridCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigateToNetwork() },
                icon = Icons.Default.Wifi,
                label = if (isArabic) "الشبكة والخوادم" else "Network & Ping",
                mainValue = if (currentMetrics.pingMs >= 0) "${currentMetrics.pingMs}ms" else "—",
                subtitle = if (isArabic) "اتصال Cloudflare TCP ➔" else "Cloudflare TCP connect ➔",
                isPositive = currentMetrics.pingMs in 0..49
            )

            // Card 4: Battery
            TelemetryGridCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.BatteryChargingFull,
                label = if (isArabic) "البطارية" else "Battery",
                mainValue = if (currentMetrics.batteryLevelPercent >= 0) "${currentMetrics.batteryLevelPercent}%" else "—",
                subtitle = if (isArabic) "قراءة Android" else "Reported by Android",
                isPositive = currentMetrics.batteryLevelPercent >= 20
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Regional Gaming Servers Direct Access Card (Screen 4 Gateway)
        val effectivePingServers = if (pingServers.isEmpty()) DEFAULT_PING_SERVERS else pingServers
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToNetwork() },
            borderColor = NeonCyan.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Ping Nodes",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "خوادم الألعاب واختبار Ping (8 مواقع)" else "Gaming Servers & Ping (8 Nodes)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                    }

                    Text(
                        text = if (isArabic) "فحص واختبار ➔" else "Test All ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal preview chips of all 8 locations
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(effectivePingServers) { server ->
                            val ping = server.currentPingMs
                            val isFailed = server.isFailed
                            val chipColor = when {
                                isFailed -> PerformanceRed
                                ping == null -> SlateBlueMuted
                                ping < 45 -> PerformanceGreen
                                ping < 90 -> NeonCyan
                                else -> PerformanceAmber
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.45f))
                                    .border(1.dp, chipColor.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToNetwork() }
                                    .padding(horizontal = 9.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = server.iconEmoji, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isArabic) server.nameAr.substringAfter("— ") else server.nameEn.substringAfter("— "),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PureWhite
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when {
                                            isFailed -> if (isArabic) "خطأ" else "ERR"
                                            ping != null -> "${ping}ms"
                                            else -> "--"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = chipColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

        Spacer(modifier = Modifier.height(16.dp))

        // In-Game Floating HUD Overlay Card with Opacity Slider (Screen 6)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isFloatingHudActive) NeonCyan else CardGlassBorder
        ) {
            Column {
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DeepGlassNavy)
                                .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "HUD",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "لوحة الألعاب العائمة (Floating HUD)" else "In-Game Floating HUD",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = if (isArabic) "عرض الـ FPS والبنج والتحكم أثناء اللعب" else "Live FPS & Ping overlay during gameplay",
                                fontSize = 11.sp,
                                color = SlateBlueMuted
                            )
                        }
                    }

                    Switch(
                        checked = isFloatingHudActive,
                        onCheckedChange = onToggleFloatingHud,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = DeepGlassNavy,
                            uncheckedThumbColor = SlateBlueMuted,
                            uncheckedTrackColor = Color.Black.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Opacity Slider (Matching Screen 6: سطوع 70%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "سطوع / شفافية اللوحة:" else "HUD Opacity:",
                        fontSize = 11.sp,
                        color = SlateBlueMuted
                    )
                    Text(
                        text = "${(hudOpacity * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }

                Slider(
                    value = hudOpacity,
                    onValueChange = onHudOpacityChanged,
                    valueRange = 0.30f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = ElectricBlue,
                        inactiveTrackColor = DarkSlate
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Boosted Games Horizontal Slider (Matching Screen 3 / Design System)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "ألعابي المعززة (Turbo Games)" else "BOOSTED GAMES",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = if (isArabic) "سجل الجلسات ←" else "Session History →",
                fontSize = 11.sp,
                color = NeonCyan,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onNavigateToSessions)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(games, key = { it.packageName }) { game ->
                Box(
                    modifier = Modifier
                        .width(132.dp)
                        .height(168.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardGlassSurface)
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.45f), Color.White.copy(alpha = 0.08f))),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 1. Logo
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DeepGlassNavy)
                                .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AppIconImage(
                                packageName = game.packageName,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }

                        // 2. Name
                        Text(
                            text = game.appName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 3. Play Button (Screen 3 style)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue)))
                                .clickable { onLaunchGame(game) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = ObsidianBlack,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (isArabic) "تشغيل" else "Play",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ObsidianBlack,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Boost Success Modal (Faithful implementation of Screen 11 in design preview)
    if (showResultSummary && showSuccessModal && lastBoostResult != null) {
        BoostSuccessDialog(
            result = lastBoostResult,
            onDismiss = { showSuccessModal = false },
            isArabic = isArabic
        )
    }
}

/**
 * 2x2 Telemetry Grid Card strictly matching Screen 2 in the design preview.
 */
@Composable
fun TelemetryGridCard(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    mainValue: String,
    subtitle: String,
    isPositive: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardGlassSurface)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(NeonCyan.copy(alpha = 0.35f), Color.White.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SlateBlueMuted
                )

                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mainValue,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isPositive) PerformanceGreen else RoyalGold,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
