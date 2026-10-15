package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GamingSession
import com.example.ui.components.GlassCard
import com.example.ui.components.InteractivePerformanceChart
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
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

@Composable
fun SessionHistoryScreen(
    sessions: List<GamingSession>,
    isRecording: Boolean,
    recordingGameTitle: String,
    isArabic: Boolean,
    onStartRecording: (gameTitle: String) -> Unit,
    onStopRecording: () -> Unit,
    onDeleteSession: (sessionId: String) -> Unit
) {
    var selectedSessionId by remember(sessions) {
        mutableStateOf(sessions.firstOrNull()?.id)
    }

    val selectedSession = sessions.firstOrNull { it.id == selectedSessionId } ?: sessions.firstOrNull()
    var selectedMetricTab by remember { mutableStateOf("FPS") } // "FPS", "PING", "CPU", "GPU", "ALL"

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Title & Description
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "تحليل أداء الألعاب المسجل" else "GAME PERFORMANCE TELEMETRY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = RoyalGold
                )
                Text(
                    text = if (isArabic) "سجل جلسات اللعب ومعدل الإطارات" else "Gaming Sessions & Metric Graphs",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Session Recorder Action Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = if (isRecording) PerformanceRed else GlassCyan.copy(alpha = 0.4f)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isRecording) PerformanceRed else PerformanceGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRecording) {
                                if (isArabic) "تسجيل مباشر لجلسة: $recordingGameTitle" else "Recording Live: $recordingGameTitle"
                            } else {
                                if (isArabic) "تسجيل مقاييس أداء جديدة (FPS / Ping / CPU / GPU)" else "Track & Record New Gaming Session"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(PerformanceRed.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "REC • LIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PerformanceRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isRecording) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.horizontalGradient(listOf(PerformanceRed, Color(0xFFB91C1C))))
                            .clickable { onStopRecording() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "إيقاف التسجيل وحفظ التحليل البياني" else "Stop Recording & Save Analysis",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGold)))
                                .clickable { onStartRecording("PUBG Mobile Match") }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FiberManualRecord,
                                    contentDescription = "Record",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تسجيل جلسة ببجي" else "Record PUBG",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.horizontalGradient(listOf(DeepGlassNavy, GlassBlue)))
                                .clickable { onStartRecording("COD Warzone Live") }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FiberManualRecord,
                                    contentDescription = "Record",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "تسجيل جلسة كود" else "Record COD",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Session List Selector Pills
        Text(
            text = if (isArabic) "الجلسات المسجلة" else "SAVED SESSIONS",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalGold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sessions) { session ->
                val isSelected = session.id == selectedSession?.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected)
                                Brush.horizontalGradient(listOf(DeepGlassNavy, CardGlassSurface))
                            else
                                Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.4f)))
                        )
                        .border(
                            1.dp,
                            if (isSelected) RoyalGold else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedSessionId = session.id }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gamepad,
                                contentDescription = "Game",
                                tint = if (isSelected) RoyalGold else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = session.gameTitle,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${session.averageFps} FPS • ${session.averagePing}ms • ${session.durationSeconds / 60}m",
                            fontSize = 11.sp,
                            color = if (isSelected) GlassCyan else TextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedSession != null) {
            // Selected Session Header & Delete Action
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedSession.gameTitle,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "${selectedSession.durationSeconds}s duration • ${selectedSession.snapshots.size} telemetry data points",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        IconButton(onClick = { onDeleteSession(selectedSession.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = PerformanceRed.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metric Selection Tabs
                    val tabs = listOf(
                        Triple("FPS", if (isArabic) "الإطارات" else "FPS", PerformanceGreen),
                        Triple("PING", if (isArabic) "البنج" else "Ping", GlassBlue),
                        Triple("CPU", if (isArabic) "المعالج" else "CPU", RoyalGold),
                        Triple("GPU", if (isArabic) "الرسومات" else "GPU", PerformancePurple),
                        Triple("ALL", if (isArabic) "الكل" else "Overlay", Color.White)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        tabs.forEach { (id, label, color) ->
                            val isTabSelected = selectedMetricTab == id
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isTabSelected) color.copy(alpha = 0.25f) else Color.Transparent)
                                    .border(
                                        width = if (isTabSelected) 1.dp else 0.dp,
                                        color = if (isTabSelected) color else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedMetricTab = id }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTabSelected) color else TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // The Interactive Canvas Performance Chart
                    InteractivePerformanceChart(
                        snapshots = selectedSession.snapshots,
                        selectedMetric = selectedMetricTab
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Analytics Summary Cards Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // FPS Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, PerformanceGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "معدل FPS" else "AVG FPS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PerformanceGreen
                                )
                                Text(
                                    text = "${selectedSession.averageFps}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Min: ${selectedSession.minFps} | Max: ${selectedSession.maxFps}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Ping Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, GlassBlue.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "متوسط البنج" else "AVG PING",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = GlassBlue
                                )
                                Text(
                                    text = "${selectedSession.averagePing} ms",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Peak: ${selectedSession.maxPing} ms",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // CPU Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, RoyalGold.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "استهلاك المعالج" else "CPU USAGE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalGold
                                )
                                Text(
                                    text = "${selectedSession.averageCpu}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = if (isArabic) "مستقر • لا اختناق" else "No Throttling",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // GPU Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                                .border(1.dp, PerformancePurple.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "استهلاك كرت الشاشة" else "GPU LOAD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PerformancePurple
                                )
                                Text(
                                    text = "${selectedSession.averageGpu}%",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Stability: ${selectedSession.stabilityScore}%",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
