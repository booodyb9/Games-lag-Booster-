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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.example.data.model.HeavyProcessItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PerformanceRed
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

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
    val heavyList = processes.filter { it.isHeavy }
    val totalHeavyRam = heavyList.sumOf { it.memoryUsageMb }
    val ramRatio = if (totalRamMb > 0) (usedRamMb.toFloat() / totalRamMb.toFloat()).coerceIn(0f, 1f) else 0.5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "إدارة موارد النظام والذاكرة" else "MEMORY & RESOURCE OPTIMIZER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = RoyalGold
                )
                Text(
                    text = if (isArabic) "كشف وإغلاق العمليات الثقيلة" else "Resource Hog Process Killer",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            IconButton(onClick = onRefreshScan) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Scan",
                    tint = GlassCyan
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System RAM Usage Bar Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "RAM",
                            tint = GlassCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "حالة الذاكرة العشوائية (RAM)" else "System Physical Memory",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Text(
                        text = "${(ramRatio * 100).toInt()}% Used",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (ramRatio > 0.8f) PerformanceRed else PerformanceGreen
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { ramRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (ramRatio > 0.8f) PerformanceRed else GlassCyan,
                    trackColor = Color.Black.copy(alpha = 0.5f),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isArabic) "المستخدم: $usedRamMb MB" else "Active: $usedRamMb MB",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isArabic) "الإجمالي: $totalRamMb MB" else "Total: $totalRamMb MB",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Auto Kill All Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (heavyList.isNotEmpty())
                        Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGold, RoyalGoldLight))
                    else
                        Brush.horizontalGradient(listOf(CardGlassSurface, DeepGlassNavy))
                )
                .clickable(enabled = heavyList.isNotEmpty() && !isKilling, onClick = onKillAllHeavy)
                .padding(vertical = 14.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isKilling) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isArabic) "جاري إغلاق العمليات وتحرير الذاكرة..." else "Closing processes & freeing RAM...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = "Clean",
                        tint = if (heavyList.isNotEmpty()) Color.Black else TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic)
                            "إغلاق جميع التطبيقات الثقيلة (${heavyList.size}) • توفير $totalHeavyRam MB"
                        else
                            "Terminate All Heavy Apps (${heavyList.size}) • Free $totalHeavyRam MB",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (heavyList.isNotEmpty()) Color.Black else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detected Process List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isArabic) "العمليات والتطبيقات في الخلفية (${processes.size})" else "RUNNING BACKGROUND PROCESSES (${processes.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                letterSpacing = 1.sp
            )
            if (isScanning) {
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(processes, key = { it.packageName + it.pid }) { item ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (item.isHeavy) PerformanceRed.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
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
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isArabic) "مستهلك عالي" else "HEAVY",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PerformanceRed
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    text = item.appName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "RAM: ${item.memoryUsageMb} MB • CPU: ${item.cpuUsagePercent}% • PID: ${item.pid}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Single kill action button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, PerformanceRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { onKillSingle(item.packageName) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Kill",
                                    tint = PerformanceRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
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
    }
}
