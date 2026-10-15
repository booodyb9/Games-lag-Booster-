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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.GlassBlue
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
fun GameLauncherScreen(
    games: List<GameItem>,
    deviceInstalledApps: List<GameItem> = emptyList(),
    isArabic: Boolean,
    onLaunchGame: (GameItem) -> Unit,
    onAddGameFromDevice: (GameItem) -> Unit,
    onRemoveGame: (packageName: String) -> Unit = {},
    onAddCustomGame: (name: String, pkg: String) -> Unit
) {
    var showDeviceAppsDialog by remember { mutableStateOf(false) }
    var showCustomAddDialog by remember { mutableStateOf(false) }
    var tuningGame by remember { mutableStateOf<GameItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var customGameName by remember { mutableStateOf("") }
    var customGamePkg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
    ) {
        // Luxury Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isArabic) "مكتبة الألعاب ومُشغّل التوربو" else "GAME LIBRARY & GFX LAUNCHER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = RoyalGold
                )
                Text(
                    text = if (isArabic) "ألعابك المعززة بالأداء الفائق" else "Turbo Boosted Games (${games.size})",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
            }

            // Add from Device Apps Action Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGold)))
                    .clickable { showDeviceAppsDialog = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = "Add from device",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isArabic) "إضافة من الجهاز" else "Add from Device",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Games List with refined spacing and luxury cards
        if (games.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = "No games",
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isArabic) "لا توجد ألعاب مضافة حالياً" else "No boosted games added yet",
                        color = TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "اضغط على 'إضافة من الجهاز' لاختيار ألعابك" else "Tap 'Add from Device' to select your installed games",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(games, key = { it.packageName }) { game ->
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = RoyalGold.copy(alpha = 0.35f)
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
                                    // Game Icon badge
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
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(PerformanceGreen.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "${game.targetFps} FPS LOCK",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
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
                                    IconButton(
                                        onClick = { tuningGame = game },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Tune,
                                            contentDescription = "Tune",
                                            tint = GlassCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    // Turbo Launch button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(RoyalGoldDark, RoyalGold, RoyalGoldLight)
                                                )
                                            )
                                            .clickable { onLaunchGame(game) }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Launch",
                                                tint = Color.Black,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (isArabic) "تشغيل" else "BOOST",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.Black
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    IconButton(
                                        onClick = { onRemoveGame(game.packageName) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MODAL DIALOG: SELECT FROM INSTALLED DEVICE APPS & GAMES
    if (showDeviceAppsDialog) {
        val filteredApps = deviceInstalledApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }

        AlertDialog(
            onDismissRequest = { showDeviceAppsDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "إضافة لعبة من تطبيقات الجهاز" else "Add Installed Device Apps",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = RoyalGold
                        )
                        Text(
                            text = if (isArabic) "اختر أي لعبة أو تطبيق مثبت لتسريعه" else "Tap any installed app to add to booster",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = { showDeviceAppsDialog = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.height(380.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(if (isArabic) "ابحث عن لعبة أو تطبيق..." else "Search installed apps...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = GlassCyan
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            focusedContainerColor = Color.Black.copy(alpha = 0.4f),
                            unfocusedContainerColor = Color.Black.copy(alpha = 0.4f)
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isArabic) "لم يتم العثور على نتائج" else "No matching apps found",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredApps) { app ->
                                val isAlreadyAdded = games.any { it.packageName == app.packageName }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isAlreadyAdded) CardGlassSurface else Color.Black.copy(alpha = 0.35f))
                                        .border(1.dp, if (isAlreadyAdded) RoyalGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                        .clickable {
                                            if (!isAlreadyAdded) {
                                                onAddGameFromDevice(app)
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.appName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = app.packageName,
                                            fontSize = 10.sp,
                                            color = TextMuted,
                                            maxLines = 1
                                        )
                                    }

                                    if (isAlreadyAdded) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(PerformanceGreen.copy(alpha = 0.2f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Added",
                                                    tint = PerformanceGreen,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = if (isArabic) "مضاف" else "Added",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PerformanceGreen
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGold)))
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Text(
                                                text = if (isArabic) "+ إضافة" else "+ Add",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isArabic) "أو كتابة اسم اللعبة والحزمة يدوياً ➔" else "Or add custom package manually ➔",
                        fontSize = 11.sp,
                        color = GlassCyan,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {
                            showDeviceAppsDialog = false
                            showCustomAddDialog = true
                        }
                    )
                }
            },
            confirmButton = {},
            containerColor = DeepGlassNavy
        )
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

    // Manual Add Dialog
    if (showCustomAddDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAddDialog = false },
            title = {
                Text(
                    text = if (isArabic) "إضافة لعبة يدوياً" else "Add Game Manually",
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
                        label = { Text(if (isArabic) "اسم الحزمة (Package Name)" else "Package Name") },
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
                            showCustomAddDialog = false
                        }
                    }
                ) {
                    Text(if (isArabic) "إضافة" else "Add", color = RoyalGold, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAddDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel", color = TextSecondary)
                }
            },
            containerColor = DeepGlassNavy
        )
    }
}
