package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PerformanceSnapshot
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBlue
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformancePurple
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
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
    borderColor: Color = GlassCyan.copy(alpha = 0.25f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = GlassBlue.copy(alpha = 0.15f),
                spotColor = RoyalGold.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CardGlassSurface,
                        DeepGlassNavy.copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        borderColor,
                        Color.White.copy(alpha = 0.08f),
                        RoyalGold.copy(alpha = 0.2f)
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
                drawArc(
                    color = Color.White.copy(alpha = 0.08f),
                    startAngle = 140f,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active glowing arc
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to color.copy(alpha = 0.6f),
                        0.7f to color,
                        1f to RoyalGoldLight
                    ),
                    startAngle = 140f,
                    sweepAngle = 260f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${value.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Visual Canvas Chart rendering FPS, Ping, CPU, and GPU session trends
 * with cubic bezier curves, gradient fills, and touch inspector.
 */
@Composable
fun InteractivePerformanceChart(
    snapshots: List<PerformanceSnapshot>,
    selectedMetric: String, // "FPS", "PING", "CPU", "GPU", "ALL"
    modifier: Modifier = Modifier,
    height: Dp = 220.dp
) {
    if (snapshots.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No telemetry data recorded yet\nلا توجد بيانات مسجلة للجلسة",
                color = TextMuted,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val primaryColor = when (selectedMetric) {
        "FPS" -> PerformanceGreen
        "PING" -> GlassBlue
        "CPU" -> RoyalGold
        "GPU" -> PerformancePurple
        else -> PerformanceGreen
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Active point readout tooltip
        val activeSnap = selectedIndex?.let { snapshots.getOrNull(it) } ?: snapshots.lastOrNull()
        if (activeSnap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedMetric) {
                            "FPS" -> "${activeSnap.fps} FPS"
                            "PING" -> "${activeSnap.pingMs} ms Latency"
                            "CPU" -> "${activeSnap.cpuUsagePercent}% CPU Load"
                            "GPU" -> "${activeSnap.gpuUsagePercent}% GPU Load"
                            else -> "FPS: ${activeSnap.fps} | Ping: ${activeSnap.pingMs}ms"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Temp: ${activeSnap.batteryTemp}°C (${activeSnap.thermalState})",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.35f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                .pointerInput(snapshots) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        val idx = (ratio * (snapshots.size - 1)).toInt()
                        selectedIndex = idx
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 12.dp)) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw background horizontal grid lines
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
                    val points = values.mapIndexed { index, v ->
                        val norm = ((v - minVal) / range).coerceIn(0f, 1f)
                        val x = index * stepX
                        val y = canvasHeight - (norm * canvasHeight)
                        Offset(x, y)
                    }

                    // Stroke path
                    val path = Path()
                    // Gradient fill under path
                    val fillPath = Path()

                    path.moveTo(points.first().x, points.first().y)
                    fillPath.moveTo(points.first().x, canvasHeight)
                    fillPath.lineTo(points.first().x, points.first().y)

                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlX = (p1.x + p2.x) / 2
                        path.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                        fillPath.cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                    }

                    fillPath.lineTo(points.last().x, canvasHeight)
                    fillPath.close()

                    // Draw fill gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                fillColor.copy(alpha = 0.35f),
                                fillColor.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // Draw line
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Highlight selected point or last point
                    val highlightIdx = selectedIndex ?: (points.size - 1)
                    val targetPt = points.getOrNull(highlightIdx)
                    if (targetPt != null) {
                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = targetPt
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = targetPt
                        )
                    }
                }

                when (selectedMetric) {
                    "FPS" -> {
                        val fpsValues = snapshots.map { it.fps.toFloat() }
                        val minF = (fpsValues.minOrNull() ?: 30f) - 5f
                        val maxF = (fpsValues.maxOrNull() ?: 90f) + 5f
                        drawCurve(fpsValues, maxF, minF, PerformanceGreen, PerformanceGreen)
                    }
                    "PING" -> {
                        val pingValues = snapshots.map { it.pingMs.toFloat() }
                        val minP = max(0f, (pingValues.minOrNull() ?: 15f) - 5f)
                        val maxP = (pingValues.maxOrNull() ?: 80f) + 10f
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
                        // Multi-curve view
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
            colors = listOf(DeepGlassNavy, ElectricBlue, GlassCyan)
        )
    }

    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = if (isGold) RoyalGold else GlassBlue)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) bgBrush else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isGold) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
    }
}
