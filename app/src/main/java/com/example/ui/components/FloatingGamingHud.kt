package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
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
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.ElectricBlue
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
 * Pure Jetpack Compose Floating Gaming HUD that renders real-time FPS, Ping,
 * CPU/RAM metrics, thermal monitoring with safety overheat alerts,
 * and adjustable opacity support for non-intrusive gameplay.
 */
@Composable
fun FloatingGamingHud(
    fps: Int,
    pingMs: Int,
    cpuPercent: Int,
    ramUsedMb: Long = 3400L,
    batteryTemp: Float = 33.5f,
    thermalSafetyThreshold: Float = 40.0f,
    opacity: Float = 0.90f,
    cachedProcessesCount: Int = 4,
    cachedMemoryMb: Long = 480L,
    onSpeedBoost: () -> Unit,
    onClearCache: () -> Unit = onSpeedBoost,
    onCoolDown: () -> Unit = onClearCache,
    onClose: () -> Unit,
    onDragDelta: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }
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
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hud_scale"
    )

    val flamePulse by pulseTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
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

    // Border glowing animation when overheating
    val borderColor by animateColorAsState(
        targetValue = if (isOverheating) PerformanceRed else GlassCyan.copy(alpha = 0.6f),
        animationSpec = tween(500),
        label = "border_color"
    )

    val safeOpacity = opacity.coerceIn(0.25f, 1.0f)

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = if (isOverheating) PerformanceRed.copy(alpha = 0.5f) else GlassCyan.copy(alpha = 0.35f),
                ambientColor = RoyalGold.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBlack.copy(alpha = safeOpacity),
                        DeepGlassNavy.copy(alpha = safeOpacity)
                    )
                )
            )
            .border(
                width = if (isOverheating) 2.dp else 1.5.dp,
                brush = Brush.linearGradient(
                    colors = if (isOverheating) {
                        listOf(PerformanceRed, Color(0xFFFF6B6B), PerformanceRed)
                    } else {
                        listOf(
                            borderColor,
                            Color.White.copy(alpha = 0.15f * safeOpacity),
                            RoyalGold.copy(alpha = 0.4f * safeOpacity)
                        )
                    }
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            // HIGH-PRIORITY THERMAL OVERHEAT SAFETY ALERT BANNER
            AnimatedVisibility(
                visible = shouldShowThermalAlert,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(bottom = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(PerformanceRed.copy(alpha = 0.35f))
                            .border(1.dp, PerformanceRed, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Overheat",
                                tint = Color(0xFFFF4D4D),
                                modifier = Modifier
                                    .scale(flamePulse)
                                    .size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    text = "🔥 OVERHEAT ALERT: ${batteryTemp}°C",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFEAEA)
                                )
                                Text(
                                    text = "Safety threshold exceeded • Throttling risk",
                                    fontSize = 9.sp,
                                    color = Color(0xFFFFD1D1)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // COOL DOWN BUTTON
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.horizontalGradient(listOf(Color(0xFF0284C7), GlassCyan)))
                                    .clickable(enabled = !isCoolingDown) {
                                        coroutineScope.launch {
                                            isCoolingDown = true
                                            onCoolDown()
                                            delay(1500)
                                            isCoolingDown = false
                                            isThermalAlertDismissed = true
                                        }
                                    }
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCoolingDown) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(10.dp),
                                        strokeWidth = 1.5.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AcUnit,
                                            contentDescription = "Cool",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "COOL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Dismiss Alert
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .clickable { isThermalAlertDismissed = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Main Compact HUD Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                // Drag handle dot with logo styling
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isOverheating) PerformanceRed else RoyalGold)
                        .border(1.dp, if (isOverheating) Color.White else GoldGlow, CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onDragDelta(dragAmount.x, dragAmount.y)
                            }
                        }
                )

                // FPS Stat Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.45f * safeOpacity))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(fpsColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$fps",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = fpsColor
                    )
                    Text(
                        text = " FPS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                // Ping Stat Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.45f * safeOpacity))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(pingColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$pingMs",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = pingColor
                    )
                    Text(
                        text = "ms",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }

                // REAL-TIME TEMPERATURE MONITORING BADGE
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isOverheating)
                                PerformanceRed.copy(alpha = 0.35f)
                            else
                                Color.Black.copy(alpha = 0.45f * safeOpacity)
                        )
                        .border(
                            width = if (isOverheating) 1.dp else 0.dp,
                            color = if (isOverheating) PerformanceRed else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = if (isOverheating) Icons.Default.LocalFireDepartment else Icons.Default.Thermostat,
                        contentDescription = "Temperature",
                        tint = tempColor,
                        modifier = Modifier
                            .scale(if (isOverheating) flamePulse else 1f)
                            .size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${batteryTemp.toInt()}°C",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = tempColor
                    )
                }

                // ONE-TAP CLEAR CACHED MEMORY BUTTON
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = GlassCyan)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isClearingCache)
                                    listOf(PerformanceGreen, Color(0xFF059669))
                                else
                                    listOf(DeepGlassNavy, GlassBlue.copy(alpha = 0.85f))
                            )
                        )
                        .border(1.dp, GlassCyan.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable(enabled = !isClearingCache && !isBoosting) {
                            coroutineScope.launch {
                                isClearingCache = true
                                feedbackText = "-${cachedMemoryMb}MB"
                                onClearCache()
                                delay(1300)
                                isClearingCache = false
                                feedbackText = null
                            }
                        }
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isClearingCache) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = feedbackText ?: "CLEARED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clear Cache",
                                tint = GlassCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "CACHE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // SPEED BOOST BUTTON (⚡ BOOST)
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(10.dp), spotColor = RoyalGold)
                        .clip(RoundedCornerShape(10.dp))
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
                        .padding(horizontal = 7.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBoosting) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(10.dp),
                                strokeWidth = 1.5.dp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "BOOSTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Speed Boost",
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "BOOST",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Expand/Collapse Toggle
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { isExpanded = !isExpanded },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = PerformanceRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Expanded Details View with Identified Background Processes & Cached Memory info
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CPU: $cpuPercent%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalGold
                        )
                        Text(
                            text = "RAM: ${ramUsedMb}MB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassCyan
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Thermal: ${batteryTemp}°C",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = tempColor
                            )
                            if (isOverheating) {
                                Text(
                                    text = " (THROTTLE)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = PerformanceRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Background processes & cache readout
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.35f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cached Background: $cachedProcessesCount apps (~${cachedMemoryMb}MB)",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )

                        Text(
                            text = "Purge Cache ➔",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassCyan,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    isClearingCache = true
                                    onClearCache()
                                    delay(1000)
                                    isClearingCache = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
