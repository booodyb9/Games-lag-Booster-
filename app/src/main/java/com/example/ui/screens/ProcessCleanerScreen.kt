package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeavyProcessItem
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
 * Screen 5 from GlassBoost UI Design System:
 * شاشة تنظيف العمليات (Process & Memory Cleaner) featuring circular cleaned memory gauge,
 * checklist breakdown (Cache, Junk, Background Processes), and "تنظيف الآن" primary action.
 */
@Composable
fun ProcessCleanerScreen(
    processes: List<HeavyProcessItem>,
    isScanning: Boolean,
    isKilling: Boolean,
    usedRamMb: Long,
    totalRamMb: Long,
    isArabic: Boolean,
    onRefreshScan: () -> Unit,
    onKillAllHeavy: () -> Unit,
    onKillSingle: (packageName: String) -> Unit
) {
    var checkCache by remember { mutableStateOf(true) }
    var checkJunk by remember { mutableStateOf(true) }
    var checkBgProcesses by remember { mutableStateOf(true) }

    val heavyList = processes.filter { it.isHeavy }
    val totalHeavyRam = heavyList.sumOf { it.memoryUsageMb }

    // This screen has no measured-cleanup input yet; never show a fabricated cleaned-memory total.
    val displayCleanedGb = "— MB"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "منظف العمليات" else "Process Cleaner",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = if (isArabic) "تفريغ الذاكرة وإغلاق التطبيقات الخلفية" else "Purge cached memory & free RAM",
                        fontSize = 11.sp,
                        color = SlateBlueMuted
                    )
                }

                IconButton(onClick = onRefreshScan) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan",
                        tint = NeonCyan
                    )
                }
            }
        }

        // Circular gauge: show unavailable until a measured cleanup result is supplied.
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(160.dp)) {
                        val strokeWidth = 10.dp.toPx()
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

                        // Glowing Arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                colors = listOf(NeonCyan, ElectricBlue, NeonCyan)
                            ),
                            startAngle = 140f,
                            sweepAngle = 210f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayCleanedGb,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isArabic) "تم التنظيف" else "Cleaned",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateBlueMuted
                        )
                    }
                }
            }
        }

        // Checklist labels are options, not measurements; avoid presenting invented file/cache sizes.
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Item 1: الذاكرة المؤقتة
                ChecklistRowItem(
                    title = if (isArabic) "الذاكرة المؤقتة" else "Cache Memory",
                    sizeText = if (isArabic) "غير مقاس" else "Not measured",
                    isChecked = checkCache,
                    onToggle = { checkCache = !checkCache }
                )

                // Item 2: الملفات غير الضرورية
                ChecklistRowItem(
                    title = if (isArabic) "الملفات غير الضرورية" else "Junk Files",
                    sizeText = if (isArabic) "غير مقاس" else "Not measured",
                    isChecked = checkJunk,
                    onToggle = { checkJunk = !checkJunk }
                )

                // Item 3: العمليات الخلفية
                ChecklistRowItem(
                    title = if (isArabic) "العمليات الخلفية" else "Background Processes",
                    sizeText = if (isArabic) "غير مقاس" else "Not measured",
                    isChecked = checkBgProcesses,
                    onToggle = { checkBgProcesses = !checkBgProcesses },
                    hasChevron = true
                )
            }
        }

        // Full-Width Primary Action Button: 🗑️ تنظيف الآن (Screen 5)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                    )
                    .clickable(enabled = !isKilling, onClick = onKillAllHeavy),
                contentAlignment = Alignment.Center
            ) {
                if (isKilling) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = ObsidianBlack,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "جاري التنظيف وتفريغ الذاكرة..." else "Purging memory...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBlack
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clean",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "تنظيف الآن" else "Clean Now",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = ObsidianBlack
                        )
                    }
                }
            }
        }

        // Running Background Processes List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "العمليات والتطبيقات في الخلفية (${processes.size})" else "RUNNING BACKGROUND APPS (${processes.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold,
                    letterSpacing = 0.8.sp
                )
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = NeonCyan
                    )
                }
            }
        }

        items(processes, key = { it.packageName + it.pid }) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardGlassSurface)
                    .border(
                        1.dp,
                        if (item.isHeavy) PerformanceRed.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.isHeavy) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PerformanceRed.copy(alpha = 0.2f))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "مستهلك" else "HEAVY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PerformanceRed
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = item.appName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (item.cpuUsagePercent >= 0f) "RAM: ${item.memoryUsageMb} MB • CPU: ${item.cpuUsagePercent}%" else "RAM: ${item.memoryUsageMb} MB • CPU: " + (if (isArabic) "غير متاح" else "Unavailable"),
                            fontSize = 11.sp,
                            color = SlateBlueMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.4f))
                            .border(1.dp, PerformanceRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onKillSingle(item.packageName) }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isArabic) "إغلاق" else "Close",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PerformanceRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * Checklist item row strictly matching Screen 5 in the design preview.
 */
@Composable
private fun ChecklistRowItem(
    title: String,
    sizeText: String,
    isChecked: Boolean,
    onToggle: () -> Unit,
    hasChevron: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardGlassSurface)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Checkbox Box
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (isChecked) NeonCyan else Color.Transparent)
                        .border(1.5.dp, if (isChecked) NeonCyan else DarkSlate, RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isChecked) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Checked",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = PureWhite
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sizeText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PerformanceGreen
                )

                if (hasChevron) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = SlateBlueMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
