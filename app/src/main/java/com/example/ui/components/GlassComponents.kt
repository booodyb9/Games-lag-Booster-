package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PerformanceSnapshot
import com.example.ui.theme.CardGlassBorder
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.DeepVoidBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformancePurple
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.SlateBlueMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.max

/**
 * Luxury Frosted Glass Card with specular gradient border.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = NeonCyan.copy(alpha = 0.25f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = ElectricBlue.copy(alpha = 0.12f),
                spotColor = NeonCyan.copy(alpha = 0.18f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CardGlassSurface,
                        DeepGlassNavy.copy(alpha = 0.88f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        PureWhite.copy(alpha = 0.08f),
                        RoyalGold.copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        content()
    }
}

/**
 * Primary Action Button (زر أساسي):
 * Electric Cyan & Electric Blue Gradient Pill Button with white/black bold typography.
 */
@Composable
fun GlassBoostPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val gradientBrush = if (enabled) {
        Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
    } else {
        Brush.horizontalGradient(listOf(DarkSlate, Color.DarkGray))
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 12.dp else 0.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = NeonCyan
            )
            .clip(RoundedCornerShape(24.dp))
            .background(gradientBrush)
            .border(
                width = 1.dp,
                color = if (enabled) PureWhite.copy(alpha = 0.35f) else Color.Transparent,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ObsidianBlack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (enabled) ObsidianBlack else SlateBlueMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Secondary Action Button (زر ثانوي):
 * Translucent glass button with subtle cyan stroke.
 */
@Composable
fun GlassBoostSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DeepVoidBlack.copy(alpha = 0.7f))
            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = PureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Shimmering luxury action button.
 */
@Composable
fun LuxuryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isGold: Boolean = true,
    enabled: Boolean = true
) {
    val bgBrush = if (isGold) {
        Brush.horizontalGradient(
            colors = listOf(RoyalGoldDark, RoyalGold, RoyalGoldLight)
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(DeepGlassNavy, ElectricBlue, NeonCyan)
        )
    }

    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = if (isGold) RoyalGold else ElectricBlue)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) bgBrush else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isGold) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Circular Luxury Gauge with glow arc.
 */
@Composable
fun MetricCircularGauge(
    value: Float,
    maxValue: Float,
    unit: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "gauge_progress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val radius = (this.size.minDimension - strokeWidth) / 2
                val center = Offset(this.size.width / 2, this.size.height / 2)

                // Background track
                drawCircle(
                    color = DarkSlate.copy(alpha = 0.4f),
                    radius = radius,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )

                // Active glowing sweep
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(color.copy(alpha = 0.4f), color, RoyalGold, color)
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${value.toInt()}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Search Bar Glass from Screen 3 of the design preview.
 */
@Composable
fun SearchBarGlass(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "بحث عن لعبة...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardGlassSurface)
            .border(1.dp, CardGlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = SlateBlueMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 13.sp,
                        color = SlateBlueMuted
                    )
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = SlateBlueMuted,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onQueryChange("") }
                )
            }
        }
    }
}

/**
 * Boost Success Dialog matching Screen 11 from GlassBoost UI Design System.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoostSuccessDialog(
    result: com.example.data.model.BoostResult,
    onDismiss: () -> Unit,
    isArabic: Boolean = true
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isArabic) "نتيجة التحضير المقاسة" else "Measured preparation result") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (isArabic) "كاش GlassBoost المحذوف: ${result.cacheDeletedBytes} بايت" else "GlassBoost cache deleted: ${result.cacheDeletedBytes} bytes")
                Text(if (isArabic) "RAM المتاح قبل/بعد: ${result.availableRamBeforeMb} / ${result.availableRamAfterMb} MB" else "Available RAM before/after: ${result.availableRamBeforeMb} / ${result.availableRamAfterMb} MB")
                Text(if (isArabic) "تعذر حذف: ${result.cleanupFailures}" else "Failed deletions: ${result.cleanupFailures}")
                val before = if (result.initialPingMs >= 0) "${result.initialPingMs}ms" else "—"
                val after = if (result.optimizedPingMs >= 0) "${result.optimizedPingMs}ms" else "—"
                if (result.networkTarget.isNotEmpty()) {
                    Text(if (isArabic) "TCP ${result.networkTarget} قبل/بعد: $before / $after" else "TCP ${result.networkTarget} before/after: $before / $after")
                }
                Text(if (isArabic) "هذه قياسات للنظام واتصال اختبار. لا تثبت تسريع اللعبة أو إنهاء تطبيقات، وقد تكون النتيجة صفرًا أو يسوء زمن الاتصال."
                    else "These are system and test-connection measurements, not proof of game acceleration or app termination. Cleanup can be zero and connection time can worsen.")
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(if (isArabic) "إغلاق" else "Close") } }
    )
}

/**
 * Loads and displays the real installed app icon from Android PackageManager
 * or renders a stylized esports fallback icon.
 */
@Composable
fun AppIconImage(
    packageName: String,
    modifier: Modifier = Modifier,
    fallbackIcon: ImageVector = Icons.Default.Gamepad
) {
    val context = LocalContext.current
    val bitmapState = remember(packageName) {
        try {
            val drawable = context.packageManager.getApplicationIcon(packageName)
            if (drawable is BitmapDrawable) {
                drawable.bitmap.asImageBitmap()
            } else {
                val bmp = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(bmp)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bmp.asImageBitmap()
            }
        } catch (_: Exception) {
            null
        }
    }

    if (bitmapState != null) {
        Image(
            bitmap = bitmapState,
            contentDescription = packageName,
            modifier = modifier
        )
    } else {
        Icon(
            imageVector = fallbackIcon,
            contentDescription = packageName,
            tint = RoyalGold,
            modifier = modifier
        )
    }
}

/**
 * Interactive Performance Telemetry Chart
 */
@Composable
fun InteractivePerformanceChart(
    snapshots: List<PerformanceSnapshot>,
    selectedMetric: String,
    onSelectMetric: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var touchX by remember { mutableStateOf<Float?>(null) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TELEMETRY METRICS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("FPS", "PING", "CPU", "GPU", "ALL").forEach { metric ->
                    val isSelected = selectedMetric == metric
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonCyan else Color.Transparent)
                            .border(1.dp, if (isSelected) NeonCyan else SlateBlueMuted.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .clickable { onSelectMetric(metric) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = metric,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ObsidianBlack else SlateBlueMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CardGlassSurface)
                .border(1.dp, CardGlassBorder, RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        touchX = offset.x
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = (canvasHeight / gridLines) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val count = snapshots.size
                if (count < 2) return@Canvas

                val stepX = canvasWidth / (count - 1)

                fun drawCurve(
                    values: List<Float>,
                    maxVal: Float,
                    minVal: Float,
                    lineColor: Color,
                    fillColor: Color
                ) {
                    val range = max(1f, maxVal - minVal)
                    val path = Path()
                    var connected = false
                    values.forEachIndexed { index, value ->
                        if (value < 0f) {
                            connected = false
                        } else {
                            val x = index * stepX
                            val y = canvasHeight * (1f - ((value - minVal) / range).coerceIn(0f, 1f))
                            if (connected) path.lineTo(x, y) else path.moveTo(x, y)
                            connected = true
                        }
                    }
                    drawPath(path, lineColor, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
                }

                when (selectedMetric) {
                    "FPS" -> {
                        val fpsValues = snapshots.map { it.fps.toFloat() }
                        val minF = (fpsValues.filter { it >= 0 }.minOrNull() ?: 30f) - 5f
                        val maxF = (fpsValues.filter { it >= 0 }.maxOrNull() ?: 90f) + 5f
                        drawCurve(fpsValues, maxF, minF, PerformanceGreen, PerformanceGreen)
                    }
                    "PING" -> {
                        val pingValues = snapshots.map { it.pingMs.toFloat() }
                        val minP = max(0f, (pingValues.filter { it >= 0 }.minOrNull() ?: 15f) - 5f)
                        val maxP = (pingValues.filter { it >= 0 }.maxOrNull() ?: 80f) + 10f
                        drawCurve(pingValues, maxP, minP, GlassBlue, GlassBlue)
                    }
                    "CPU" -> {
                        val cpuValues = snapshots.map { it.cpuUsagePercent.toFloat() }
                        drawCurve(cpuValues, 100f, 0f, RoyalGold, RoyalGold)
                    }
                    "GPU" -> {
                        val gpuValues = snapshots.map { it.gpuUsagePercent.toFloat() }
                        drawCurve(gpuValues, 100f, 0f, PerformancePurple, PerformancePurple)
                    }
                    "ALL" -> {
                        val fpsNorm = snapshots.map { (it.fps / 120f) * 100f }
                        val pingNorm = snapshots.map { (it.pingMs / 100f) * 100f }
                        val cpuNorm = snapshots.map { it.cpuUsagePercent.toFloat() }
                        drawCurve(fpsNorm, 100f, 0f, PerformanceGreen, PerformanceGreen)
                        drawCurve(pingNorm, 100f, 0f, GlassBlue, GlassBlue)
                        drawCurve(cpuNorm, 100f, 0f, RoyalGold, RoyalGold)
                    }
                }
            }
        }
    }
}
