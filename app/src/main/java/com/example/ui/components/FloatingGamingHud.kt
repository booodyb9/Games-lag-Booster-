package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Luxury Transparent Floating Gaming HUD with:
 * 1. Compact Floating Circle Bubble mode (دائرة صغيرة شفافة).
 * 2. Tap to expand/collapse into full stats HUD.
 * 3. Drag to side & edge docking (إخفاء وسحب في جانب الشاشة).
 * 4. Real-time FPS, Ping, Thermals, One-tap Cache Cleaner, and Speed Boost.
 */
@Composable
fun FloatingGamingHud(
    fps: Int,
    pingMs: Int,
    cpuPercent: Int,
    ramUsedMb: Long = 3400L,
    batteryTemp: Float = 33.5f,
    thermalSafetyThreshold: Float = 40.0f,
    opacity: Float = 0.85f,
    cachedProcessesCount: Int = 4,
    cachedMemoryMb: Long = 480L,
    initialBubbleMode: Boolean = true,
    onSpeedBoost: () -> Unit,
    onClearCache: () -> Unit = onSpeedBoost,
    onCoolDown: () -> Unit = onClearCache,
    onClose: () -> Unit,
    onDragDelta: (Float, Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isBubbleMode by remember { mutableStateOf(initialBubbleMode) }
    var isExpandedDetails by remember { mutableStateOf(false) }
    var isBoosting by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }
    var isCoolingDown by remember { mutableStateOf(false) }
    var feedbackText by remember { mutableStateOf<String?>(null) }
    var isThermalAlertDismissed by remember { mutableStateOf(false) }

    val isOverheating = batteryTemp >= thermalSafetyThreshold
    val shouldShowThermalAlert = isOverheating && !isThermalAlertDismissed

    val pulseTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.09f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hud_scale"
    )

    val ringScale by pulseTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )

    val fpsColor = when {
        fps >= 55 -> PerformanceGreen
        fps >= 40 -> GlassBlue
        else -> PerformanceRed
    }

    val pingColor = when {
        pingMs < 35 -> PerformanceGreen
        pingMs < 65 -> GlassBlue
        else -> PerformanceRed
    }

    val tempColor = when {
        batteryTemp >= thermalSafetyThreshold -> PerformanceRed
        batteryTemp >= 37.0f -> Color(0xFFF59E0B)
        else -> PerformanceGreen
    }

    val safeOpacity = opacity.coerceIn(0.20f, 0.95f)

    AnimatedContent(
        targetState = isBubbleMode,
        transitionSpec = {
            (fadeIn(tween(250)) + expandHorizontally()) togetherWith
                    (fadeOut(tween(200)) + shrinkHorizontally())
        },
        label = "hud_mode_animation",
        modifier = modifier
    ) { bubbleState ->
        if (bubbleState) {
            // MODE A: COMPACT TRANSPARENT CIRCLE BUBBLE (دائرة صغيرة شفافة قابلة للسحب والإخفاء)
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        spotColor = if (isOverheating) PerformanceRed else GlassCyan.copy(alpha = 0.4f)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isOverheating) Color(0x99991B1B) else DeepGlassNavy).copy(alpha = safeOpacity),
                                ObsidianBlack.copy(alpha = (safeOpacity + 0.05f).coerceAtMost(1f))
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                if (isOverheating) PerformanceRed else RoyalGold,
                                GlassCyan,
                                if (isOverheating) PerformanceRed else RoyalGoldLight,
                                if (isOverheating) PerformanceRed else RoyalGold
                            )
                        ),
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onDragEnd() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDragDelta(dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                    .clickable { isBubbleMode = false }, // Tap to expand into full HUD
                contentAlignment = Alignment.Center
            ) {
                // Subtle glowing background ring
                Box(
                    modifier = Modifier
                        .scale(ringScale)
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(
                            1.dp,
                            (if (isOverheating) PerformanceRed else GlassCyan).copy(alpha = 0.3f),
                            CircleShape
                        )
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOverheating) Icons.Default.LocalFireDepartment else Icons.Default.Bolt,
                            contentDescription = "Stats",
                            tint = if (isOverheating) PerformanceRed else GoldGlow,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = "$fps",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = fpsColor,
                        lineHeight = 14.sp
                    )
                    Text(
                        text = "FPS",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        lineHeight = 9.sp
                    )
                }
            }
        } else {
            // MODE B: EXPANDED TRANSLUCENT HUD PANEL (لوحة شفافة أنيقة قابلة للطي للدائرة)
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = if (isOverheating) PerformanceRed.copy(alpha = 0.5f) else GlassCyan.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ObsidianBlack.copy(alpha = safeOpacity),
                                DeepGlassNavy.copy(alpha = safeOpacity)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GlassCyan.copy(alpha = 0.7f),
                                Color.White.copy(alpha = 0.12f * safeOpacity),
                                RoyalGold.copy(alpha = 0.5f * safeOpacity)
                            )
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Column {
                    // Thermal Overheat Alert Banner
                    AnimatedVisibility(
                        visible = shouldShowThermalAlert,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PerformanceRed.copy(alpha = 0.4f))
                                .border(1.dp, PerformanceRed, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Alert",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🔥 ${batteryTemp}°C OVERHEAT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(GlassCyan)
                                        .clickable {
                                            coroutineScope.launch {
                                                isCoolingDown = true
                                                onCoolDown()
                                                delay(1200)
                                                isCoolingDown = false
                                                isThermalAlertDismissed = true
                                            }
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("COOL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(13.dp)
                                        .clickable { isThermalAlertDismissed = true }
                                )
                            }
                        }
                    }

                    // Main HUD Control Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Collapse to Circle Bubble Button (⚪ زر التصغير للدائرة)
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(RoyalGold.copy(alpha = 0.25f))
                                .border(1.dp, RoyalGold, CircleShape)
                                .clickable { isBubbleMode = true }, // Click to collapse into circle bubble
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Collapse to circle",
                                tint = RoyalGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Drag Handle
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = { onDragEnd() },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            onDragDelta(dragAmount.x, dragAmount.y)
                                        }
                                    )
                                }
                        )

                        // FPS Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.Black.copy(alpha = 0.45f * safeOpacity))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .scale(pulseScale)
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(fpsColor)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$fps",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = fpsColor
                            )
                            Text(
                                text = " FPS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        // Ping Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.Black.copy(alpha = 0.45f * safeOpacity))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(pingColor)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$pingMs",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = pingColor
                            )
                            Text(
                                text = "ms",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        // Temp Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(Color.Black.copy(alpha = 0.45f * safeOpacity))
                                .padding(horizontal = 5.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = if (isOverheating) Icons.Default.LocalFireDepartment else Icons.Default.Thermostat,
                                contentDescription = "Temp",
                                tint = tempColor,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${batteryTemp.toInt()}°C",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = tempColor
                            )
                        }

                        // ONE-TAP CLEAR CACHE BUTTON
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (isClearingCache)
                                            listOf(PerformanceGreen, Color(0xFF059669))
                                        else
                                            listOf(DeepGlassNavy, GlassBlue.copy(alpha = 0.85f))
                                    )
                                )
                                .border(1.dp, GlassCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable(enabled = !isClearingCache && !isBoosting) {
                                    coroutineScope.launch {
                                        isClearingCache = true
                                        feedbackText = "-${cachedMemoryMb}MB"
                                        onClearCache()
                                        delay(1200)
                                        isClearingCache = false
                                        feedbackText = null
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isClearingCache) {
                                Text(
                                    text = feedbackText ?: "OK",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CleaningServices,
                                        contentDescription = "Clear",
                                        tint = GlassCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "CACHE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // ONE-TAP SPEED BOOST BUTTON
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        if (isBoosting)
                                            listOf(PerformanceGreen, GlassCyan)
                                        else
                                            listOf(RoyalGold, Color(0xFFFFD54F))
                                    )
                                )
                                .clickable(enabled = !isBoosting && !isClearingCache) {
                                    coroutineScope.launch {
                                        isBoosting = true
                                        feedbackText = "TURBO"
                                        onSpeedBoost()
                                        delay(1200)
                                        isBoosting = false
                                        feedbackText = null
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isBoosting) {
                                Text(
                                    text = "BOOSTED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Boost",
                                        tint = Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "BOOST",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        // Toggle Expand Details
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { isExpandedDetails = !isExpandedDetails },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isExpandedDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Dismiss/Close Button
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f))
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = PerformanceRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    // Expanded Secondary Info Row
                    AnimatedVisibility(
                        visible = isExpandedDetails,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.1f))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CPU: $cpuPercent% • RAM: ${ramUsedMb}MB",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Dock to Side ➔",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassCyan,
                                    modifier = Modifier.clickable { isBubbleMode = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
