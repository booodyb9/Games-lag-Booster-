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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.GameItem
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.GlassCyan
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.RoyalGoldDark
import com.example.ui.theme.RoyalGoldLight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GameLauncherScreen(
    games: List<GameItem>,
    isArabic: Boolean,
    onLaunchGame: (GameItem) -> Unit,
    onAddCustomGame: (name: String, pkg: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var tuningGame by remember { mutableStateOf<GameItem?>(null) }

    var customGameName by remember { mutableStateOf("") }
    var customGamePkg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isArabic) "مكتبة الألعاب ومُشغّل التوربو" else "GAME LIBRARY & GFX LAUNCHER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = RoyalGold
                )
                Text(
                    text = if (isArabic) "تشغيل الألعاب بأقصى سرعة" else "High-FPS Game Launcher",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            IconButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Game",
                    tint = RoyalGold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(games) { game ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = RoyalGold.copy(alpha = 0.25f)
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
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DeepGlassNavy)
                                    .border(1.5.dp, RoyalGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = "Game",
                                    tint = RoyalGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = game.appName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PerformanceGreen.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${game.targetFps} FPS TARGET",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PerformanceGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = game.resolutionPreset,
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { tuningGame = game }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Tune",
                                    tint = GlassCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGold)))
                                    .clickable { onLaunchGame(game) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Launch",
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "تشغيل" else "BOOST",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // GFX Tuning Modal
    if (tuningGame != null) {
        val game = tuningGame!!
        var targetFps by remember { mutableIntStateOf(game.targetFps) }
        var resPreset by remember { mutableStateOf(game.resolutionPreset) }

        AlertDialog(
            onDismissRequest = { tuningGame = null },
            title = {
                Text(
                    text = if (isArabic) "إعدادات الرسوميات لـ ${game.appName}" else "GFX & FPS Tuning: ${game.appName}",
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isArabic) "معدل الإطارات المستهدف (Target FPS):" else "Target Frame Rate Lock:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(60, 90, 120).forEach { fps ->
                            val isSel = targetFps == fps
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) PerformanceGreen.copy(alpha = 0.25f) else CardGlassSurface)
                                    .border(1.dp, if (isSel) PerformanceGreen else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { targetFps = fps }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$fps FPS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) PerformanceGreen else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isArabic) "دقة العرض (Resolution Scale):" else "Display Rendering Resolution:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf("HD (720p) - أقصى سلاسة", "FHD (1080p) - متوازن", "2K QHD - جودة سينمائية").forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { resPreset = r }
                        ) {
                            RadioButton(
                                selected = resPreset.startsWith(r.take(2)),
                                onClick = { resPreset = r },
                                colors = RadioButtonDefaults.colors(selectedColor = RoyalGold)
                            )
                            Text(text = r, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { tuningGame = null }) {
                    Text(if (isArabic) "حفظ الإعدادات" else "Save Settings", color = RoyalGold, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DeepGlassNavy
        )
    }

    // Add Custom Game Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = if (isArabic) "إضافة لعبة جديدة للمُسرّع" else "Add Game to Booster",
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = customGameName,
                        onValueChange = { customGameName = it },
                        label = { Text(if (isArabic) "اسم اللعبة" else "Game Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customGamePkg,
                        onValueChange = { customGamePkg = it },
                        label = { Text(if (isArabic) "اسم الحزمة (Package Name)" else "Package Name (e.g. com.game)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customGameName.isNotBlank()) {
                            onAddCustomGame(
                                customGameName,
                                if (customGamePkg.isNotBlank()) customGamePkg else "com.custom.${customGameName.lowercase().replace(" ", "")}"
                            )
                            customGameName = ""
                            customGamePkg = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(if (isArabic) "إضافة" else "Add", color = RoyalGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel", color = TextSecondary)
                }
            },
            containerColor = DeepGlassNavy
        )
    }
}
