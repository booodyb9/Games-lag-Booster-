package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardGlassBorder
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.DeepVoidBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBlueMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Screen 6 & Screen 9 from GlassBoost UI Design System:
 * اللوحة العائمة أثناء اللعب (In-Game Floating HUD) & اللوحة العائمة (تصميم جديد)
 * Features:
 * 1. Mode A: Compact Floating Bubble (دائرة صغيرة شفافة يمكن سحبها وإخفاؤها في حافة الشاشة).
 * 2. Mode B: Full Gaming HUD with the 4 glowing tiles (Boost, الشبكة, الحرارة, الذاكرة),
 *    real-time telemetry, brightness/opacity slider, and instant clean cache button.
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
    isArabic: Boolean = true,
    onSpeedBoost: () -> Unit,
    onClearCache: () -> Unit = onSpeedBoost,
    onCoolDown: () -> Unit = onClearCache,
    onClose: () -> Unit,
    onOpacityChanged: (Float) -> Unit = {},
    onDragDelta: (Float, Float) -> Unit = { _, _ -> },
    onDragEnd: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isBubbleMode by remember { mutableStateOf(initialBubbleMode) }
    var isBoosting by remember { mutableStateOf(false) }
    var isClearingCache by remember { mutableStateOf(false) }
    var hudOpacitySlider by remember(opacity) { mutableStateOf(opacity) }
    var isThermalAlertDismissed by remember { mutableStateOf(false) }

    val isOverheating = batteryTemp >= thermalSafetyThreshold
    val shouldShowThermalAlert = isOverheating && !isThermalAlertDismissed

    val pulseTransition = rememberInfiniteTransition(label = "hud_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hud_scale"
    )

    val fpsColor = when {
        fps >= 55 -> PerformanceGreen
        fps >= 40 -> NeonCyan
        else -> PerformanceRed
    }

    val pingColor = when {
        pingMs < 35 -> PerformanceGreen
        pingMs < 65 -> NeonCyan
        else -> PerformanceRed
    }

    val tempColor = when {
        batteryTemp >= thermalSafetyThreshold -> PerformanceRed
        batteryTemp >= 38.0f -> RoyalGold
        else -> PerformanceGreen
    }

    val safeOpacity = hudOpacitySlider.coerceIn(0.20f, 0.98f)

    AnimatedContent(
        targetState = isBubbleMode,
        transitionSpec = {
            (fadeIn(tween(200)) + expandHorizontally()) togetherWith
                    (fadeOut(tween(180)) + shrinkHorizontally())
        },
        label = "hud_mode_animation",
        modifier = modifier
    ) { bubbleState ->
        if (bubbleState) {
            // MODE A: COMPACT FLOATING CIRCLE BUBBLE (دائرة صغيرة شفافة قابلة للسحب والإخفاء)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = CircleShape,
                        spotColor = if (isOverheating) PerformanceRed else NeonCyan.copy(alpha = 0.5f)
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
                                if (isOverheating) PerformanceRed else NeonCyan,
                                ElectricBlue,
                                RoyalGold,
                                if (isOverheating) PerformanceRed else NeonCyan
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isOverheating) Icons.Default.LocalFireDepartment else Icons.Default.Bolt,
                        contentDescription = "Stats",
                        tint = if (isOverheating) PerformanceRed else NeonCyan,
                        modifier = Modifier.size(13.dp)
                    )
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
            // MODE B: FULL GAMING HUD (Matching Screen 6 & Screen 9 in UI Design Preview)
            Box(
                modifier = Modifier
                    .width(310.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = if (isOverheating) PerformanceRed else NeonCyan.copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                DeepVoidBlack.copy(alpha = safeOpacity),
                                DeepGlassNavy.copy(alpha = safeOpacity)
                            )
                        )
                    )
                    .border(
                        width = 1.2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.65f),
                                PureWhite.copy(alpha = 0.12f),
                                RoyalGold.copy(alpha = 0.35f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header: "< لوحة الألعاب العائمة" + Close & Minimize
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isBubbleMode = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Minimize",
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isArabic) "لوحة الألعاب العائمة" else "Floating Gaming HUD",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Minimize button
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(DarkSlate.copy(alpha = 0.6f))
                                    .clickable { isBubbleMode = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Dock",
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Close button
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(DarkSlate.copy(alpha = 0.6f))
                                    .clickable { onClose() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = PerformanceRed,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Screen 9 & 6: Row of 4 Primary Floating Tiles (Boost, الشبكة, الحرارة, الذاكرة)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tile 1: Boost (⚡)
                        HudActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Bolt,
                            label = "Boost",
                            subText = if (isBoosting) "DONE" else "$fps FPS",
                            isActive = true,
                            accentColor = NeonCyan,
                            onClick = {
                                coroutineScope.launch {
                                    isBoosting = true
                                    onSpeedBoost()
                                    delay(1200)
                                    isBoosting = false
                                }
                            }
                        )

                        // Tile 2: الشبكة (WiFi)
                        HudActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Wifi,
                            label = if (isArabic) "الشبكة" else "Network",
                            subText = "${pingMs}ms",
                            isActive = pingMs < 50,
                            accentColor = pingColor,
                            onClick = { }
                        )

                        // Tile 3: الحرارة (Thermometer)
                        HudActionTile(
                            modifier = Modifier.weight(1f),
                            icon = if (isOverheating) Icons.Default.LocalFireDepartment else Icons.Default.Thermostat,
                            label = if (isArabic) "الحرارة" else "Temp",
                            subText = "${batteryTemp.toInt()}°C",
                            isActive = !isOverheating,
                            accentColor = tempColor,
                            onClick = { onCoolDown() }
                        )

                        // Tile 4: الذاكرة (Memory)
                        HudActionTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Memory,
                            label = if (isArabic) "الذاكرة" else "RAM",
                            subText = "$cpuPercent%",
                            isActive = true,
                            accentColor = ElectricBlue,
                            onClick = {
                                coroutineScope.launch {
                                    isClearingCache = true
                                    onClearCache()
                                    delay(1000)
                                    isClearingCache = false
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Screen 6: Opacity / Brightness Slider ("سطوع / شفافية")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "سطوع / شفافية" else "HUD Opacity",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateBlueMuted
                        )
                        Text(
                            text = "${(hudOpacitySlider * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }

                    Slider(
                        value = hudOpacitySlider,
                        onValueChange = {
                            hudOpacitySlider = it
                            onOpacityChanged(it)
                        },
                        valueRange = 0.30f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = DarkSlate
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Drag & Quick Clean Footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Trash / Clean Action
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CardGlassSurface)
                                .border(1.dp, CardGlassBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    coroutineScope.launch {
                                        onClearCache()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Purge Cache",
                                tint = SlateBlueMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        // Hint: "اسحب للإخفاء ⨀"
                        Text(
                            text = if (isArabic) "اسحب في الحافة للإخفاء" else "Drag to edge to hide",
                            fontSize = 10.sp,
                            color = SlateBlueMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragEnd = { onDragEnd() },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onDragDelta(dragAmount.x, dragAmount.y)
                                    }
                                )
                            }
                        )

                        // Mode indicator
                        Text(
                            text = if (isArabic) "وضع الأداء ▾" else "Ultra ▾",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalGold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Action Tile Component used in Floating HUD (Screen 9 & Screen 6)
 */
@Composable
private fun HudActionTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    subText: String,
    isActive: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) DeepGlassNavy.copy(alpha = 0.8f) else CardGlassSurface
            )
            .border(
                width = 1.dp,
                color = if (isActive) accentColor.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = SlateBlueMuted
            )
        }
    }
}
