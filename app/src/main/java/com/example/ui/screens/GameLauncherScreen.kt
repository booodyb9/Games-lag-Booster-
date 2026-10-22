package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GameItem
import com.example.ui.components.AppIconImage
import com.example.ui.components.GlassCard
import com.example.ui.theme.*

/**
 * Game Library Screen designed with luxury horizontal slider cards.
 * Each card is compact, displaying ONLY the game logo, name, and play/boost button.
 */
@Composable
fun GameLauncherScreen(
    games: List<GameItem>,
    deviceInstalledApps: List<GameItem> = emptyList(),
    isArabic: Boolean,
    onLaunchGame: (GameItem) -> Unit,
    onSaveProfile: (GameItem) -> Unit = {},
    onAddGameFromDevice: (GameItem) -> Unit,
    onRemoveGame: (packageName: String) -> Unit = {},
    onAddCustomGame: (name: String, pkg: String) -> Unit
) {
    var showDeviceAppsDialog by remember { mutableStateOf(false) }
    var showCustomAddDialog by remember { mutableStateOf(false) }
    var tuningGame by remember { mutableStateOf<GameItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf("ALL") }

    var customGameName by remember { mutableStateOf("") }
    var customGamePkg by remember { mutableStateOf("") }

    val filteredDisplayGames = remember(games, deviceInstalledApps, searchQuery, activeFilter) {
        val baseList = when (activeFilter) {
            "FAV" -> games.take(3)
            "INSTALLED" -> if (deviceInstalledApps.isNotEmpty()) deviceInstalledApps else games
            else -> games
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                        it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Screen Header & Action Buttons (Screen 3: مكتبة الألعاب)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) "مكتبة الألعاب" else "Game Library",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = if (isArabic) "ألعابك المعززة بالأداء الفائق (${games.size})" else "Esports Boosted Games (${games.size})",
                        fontSize = 11.sp,
                        color = SlateBlueMuted
                    )
                }

                // Add from Device Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue)))
                        .clickable { showDeviceAppsDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneAndroid,
                            contentDescription = "Add from device",
                            tint = ObsidianBlack,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "+ إضافة من الجهاز" else "+ Add from Device",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ObsidianBlack,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Pills Row (Screen 3: الكل | المفضلة | الألعاب المثبتة)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("ALL", if (isArabic) "الكل" else "All"),
                    Pair("FAV", if (isArabic) "المفضلة" else "Favorites"),
                    Pair("INSTALLED", if (isArabic) "الألعاب المثبتة" else "Installed")
                ).forEach { (key, label) ->
                    val isSelected = activeFilter == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isSelected)
                                    Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                                else
                                    Brush.horizontalGradient(listOf(DarkSlate.copy(alpha = 0.5f), DarkSlate.copy(alpha = 0.5f)))
                            )
                            .clickable { activeFilter = key }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) ObsidianBlack else SlateBlueMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar (Screen 3: ابحث عن لعبة...)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isArabic) "ابحث عن لعبة..." else "Search games...",
                        fontSize = 12.sp,
                        color = com.example.ui.theme.SlateBlueMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = com.example.ui.theme.NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = com.example.ui.theme.NeonCyan,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                    focusedContainerColor = com.example.ui.theme.CardGlassSurface,
                    unfocusedContainerColor = com.example.ui.theme.CardGlassSurface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Section 1: Boosted Games Horizontal Slider
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "الألعاب المعززة (سلايدر الألعاب)" else "BOOSTED GAMES CAROUSEL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalGold,
                    letterSpacing = 0.8.sp
                )
                if (games.isNotEmpty()) {
                    Text(
                        text = if (isArabic) "اسحب للتنقل ➔" else "Swipe horizontally ➔",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (games.isEmpty()) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = Color.White.copy(alpha = 0.12f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Gamepad,
                            contentDescription = "No games",
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "لا توجد ألعاب مضافة حالياً" else "No boosted games added yet",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isArabic) "اضغط على زر '+ إضافة من الجهاز' بالأعلى لاختيار ألعابك" else "Tap '+ Add from Device' to select your installed games",
                            color = TextMuted,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Horizontal Slider for Boosted Games
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(games, key = { it.packageName }) { game ->
                        SmallGameSliderCard(
                            game = game,
                            isArabic = isArabic,
                            onLaunch = { onLaunchGame(game) },
                            onTune = { tuningGame = game },
                            onRemove = { onRemoveGame(game.packageName) }
                        )
                    }

                    // Add Game Quick Card in Slider
                    item {
                        AddGamePlaceholderCard(
                            isArabic = isArabic,
                            onClick = { showDeviceAppsDialog = true }
                        )
                    }
                }
            }
        }

        // Section 2: Installed on Device Quick Add Horizontal Slider
        if (deviceInstalledApps.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "ألعاب وتطبيقات مثبتة على جهازك" else "INSTALLED ON YOUR DEVICE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassCyan,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${deviceInstalledApps.size} ${if (isArabic) "تطبيق" else "Apps"}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(deviceInstalledApps, key = { "dev_" + it.packageName }) { app ->
                        val isAlreadyAdded = games.any { it.packageName == app.packageName }
                        SmallDeviceAppSliderCard(
                            app = app,
                            isAlreadyAdded = isAlreadyAdded,
                            isArabic = isArabic,
                            onAdd = { onAddGameFromDevice(app) }
                        )
                    }
                }
            }
        }

        // Section 3: Screen 3 Vertical Games List ("قائمة الألعاب")
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "قائمة الألعاب" else "GAMES LIST",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${filteredDisplayGames.size} ${if (isArabic) "ألعاب" else "Games"}",
                    fontSize = 11.sp,
                    color = SlateBlueMuted
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(filteredDisplayGames, key = { "vert_item_" + it.packageName }) { game ->
            GameListItemRow(
                game = game,
                isArabic = isArabic,
                onLaunch = { onLaunchGame(game) },
                onTune = { tuningGame = game }
            )
        }

        item {
            // Quick Custom Add Option
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isArabic) "أو كتابة اسم اللعبة والحزمة يدوياً ➔" else "Or add custom package manually ➔",
                    fontSize = 12.sp,
                    color = GlassCyan,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showCustomAddDialog = true }
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Modal: Device Apps Picker Dialog with Search
    if (showDeviceAppsDialog) {
        val filteredApps = remember(searchQuery, deviceInstalledApps) {
            if (searchQuery.isBlank()) {
                deviceInstalledApps
            } else {
                deviceInstalledApps.filter {
                    it.appName.contains(searchQuery, ignoreCase = true) ||
                            it.packageName.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        AlertDialog(
            onDismissRequest = {
                showDeviceAppsDialog = false
                searchQuery = ""
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isArabic) "اختر ألعاب من جهازك" else "Select Device Games",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = RoyalGold
                    )
                    IconButton(
                        onClick = { showDeviceAppsDialog = false },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
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
                        placeholder = {
                            Text(
                                text = if (isArabic) "ابحث عن لعبة أو تطبيق..." else "Search installed apps...",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = GlassCyan,
                                modifier = Modifier.size(18.dp)
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
                                        .border(
                                            1.dp,
                                            if (isAlreadyAdded) RoyalGold.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable {
                                            if (!isAlreadyAdded) {
                                                onAddGameFromDevice(app)
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(DeepGlassNavy),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppIconImage(
                                                packageName = app.packageName,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.appName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = app.packageName,
                                                fontSize = 10.sp,
                                                color = TextMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

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
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
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
                                                .padding(horizontal = 9.dp, vertical = 5.dp)
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
                }
            },
            confirmButton = {},
            containerColor = DeepGlassNavy
        )
    }

    // Modal: GFX Tuning Dialog
    if (tuningGame != null) {
        val game = tuningGame!!
        var targetFps by remember(game.packageName) { mutableIntStateOf(game.targetFps) }
        var resPreset by remember(game.packageName) { mutableStateOf(game.resolutionPreset) }

        AlertDialog(
            onDismissRequest = { tuningGame = null },
            title = {
                Text(
                    text = if (isArabic) "إعدادات الرسوميات لـ ${game.appName}" else "GFX & FPS Tuning: ${game.appName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalGold
                )
            },
            text = {
                Column {
                    Text(
                        text = if (isArabic) "هذا ملف تفضيلات محفوظ. طبّق القيم من إعدادات اللعبة؛ Android لا يسمح لهذا التطبيق بفرضها على لعبة أخرى."
                        else "Saved reference profile. Apply these values in the game's own settings; Android does not let this app force them on another game.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "معدل الإطارات المستهدف (Target FPS):" else "Preferred FPS (reference):",
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
                            val isSelected = targetFps == fps
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) RoyalGold else DeepGlassNavy)
                                    .border(1.dp, if (isSelected) RoyalGold else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { targetFps = fps }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$fps FPS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else TextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isArabic) "دقة العرض (Resolution Preset):" else "Display Resolution:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    listOf("FHD (1080p)", "2K Quad HD (1440p)", "Smooth Performance (720p)").forEach { res ->
                        val isSelected = resPreset == res
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { resPreset = res }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, if (isSelected) RoyalGold else Color.Gray, CircleShape)
                                    .background(if (isSelected) RoyalGold else Color.Transparent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = res,
                                fontSize = 12.sp,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSaveProfile(game.copy(targetFps = targetFps, resolutionPreset = resPreset))
                    tuningGame = null
                }) {
                    Text(
                        text = if (isArabic) "حفظ التعديلات" else "Save Settings",
                        color = RoyalGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            containerColor = DeepGlassNavy
        )
    }

    // Modal: Custom Add Dialog
    if (showCustomAddDialog) {
        AlertDialog(
            onDismissRequest = { showCustomAddDialog = false },
            title = {
                Text(
                    text = if (isArabic) "إضافة لعبة يدوياً" else "Add Custom Game",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = RoyalGold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = customGameName,
                        onValueChange = { customGameName = it },
                        label = { Text(if (isArabic) "اسم اللعبة" else "Game Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customGamePkg,
                        onValueChange = { customGamePkg = it },
                        label = { Text(if (isArabic) "اسم الحزمة (Package Name)" else "Package Name (e.g. com.game)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (customGameName.isNotBlank() && customGamePkg.isNotBlank()) {
                            onAddCustomGame(customGameName.trim(), customGamePkg.trim())
                            customGameName = ""
                            customGamePkg = ""
                            showCustomAddDialog = false
                        }
                    }
                ) {
                    Text(
                        text = if (isArabic) "إضافة" else "Add",
                        color = RoyalGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomAddDialog = false }) {
                    Text(
                        text = if (isArabic) "إلغاء" else "Cancel",
                        color = TextSecondary
                    )
                }
            },
            containerColor = DeepGlassNavy
        )
    }
}

/**
 * Compact Horizontal Slider Card for Boosted Games:
 * Contains ONLY:
 * 1. Logo of the game
 * 2. Name of the game
 * 3. Play / Boost button
 */
@Composable
fun SmallGameSliderCard(
    game: GameItem,
    isArabic: Boolean,
    onLaunch: () -> Unit,
    onTune: () -> Unit,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(132.dp)
            .height(172.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardGlassSurface)
            .border(
                1.dp,
                Brush.linearGradient(listOf(RoyalGold.copy(alpha = 0.45f), Color.White.copy(alpha = 0.08f))),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top mini row: Subtle options (remove or tune)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Tune",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(onClick = onTune)
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable(onClick = onRemove)
                )
            }

            // 1. Game Logo
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepGlassNavy)
                    .border(1.dp, RoyalGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppIconImage(
                    packageName = game.packageName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            // 2. Game Name
            Text(
                text = game.appName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Play / Boost Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(RoyalGoldDark, RoyalGold)))
                    .clickable(onClick = onLaunch),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isArabic) "تشغيل" else "Play",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Small Horizontal Card for Discovered Device Apps.
 */
@Composable
fun SmallDeviceAppSliderCard(
    app: GameItem,
    isAlreadyAdded: Boolean,
    isArabic: Boolean,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(132.dp)
            .height(172.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardGlassSurface)
            .border(
                1.dp,
                if (isAlreadyAdded) RoyalGold.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isAlreadyAdded) (if (isArabic) "مضاف" else "Added") else (if (isArabic) "مثبت" else "Installed"),
                    fontSize = 9.sp,
                    color = if (isAlreadyAdded) PerformanceGreen else GlassCyan,
                    fontWeight = FontWeight.Bold
                )
            }

            // 1. App Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DeepGlassNavy),
                contentAlignment = Alignment.Center
            ) {
                AppIconImage(
                    packageName = app.packageName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }

            // 2. App Name
            Text(
                text = app.appName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // 3. Add Button
            if (isAlreadyAdded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PerformanceGreen.copy(alpha = 0.2f))
                        .border(1.dp, PerformanceGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
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
                            text = if (isArabic) "مضاف للقائمة" else "Added",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PerformanceGreen,
                            maxLines = 1
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(DeepGlassNavy, ElectricBlue, GlassCyan)))
                        .clickable(onClick = onAdd),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isArabic) "+ إضافة" else "+ Add",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Placeholder card at the end of the slider to prompt adding more games.
 */
@Composable
fun AddGamePlaceholderCard(
    isArabic: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(132.dp)
            .height(172.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(DeepGlassNavy)
                    .border(1.dp, RoyalGold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Game",
                    tint = RoyalGold,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isArabic) "إضافة لعبة +" else "Add Game +",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isArabic) "من الجهاز" else "From device",
                fontSize = 10.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Screen 3 Vertical Game List Row strictly matching "شاشة الألعاب" in UI Design Preview:
 * Game Icon + Name + "أداء محسّن" + "تشغيل" Electric Blue Pill Button.
 */
@Composable
fun GameListItemRow(
    game: GameItem,
    isArabic: Boolean,
    onLaunch: () -> Unit,
    onTune: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardGlassSurface)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(NeonCyan.copy(alpha = 0.28f), Color.White.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onTune)
            .padding(horizontal = 14.dp, vertical = 10.dp)
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
                // Game Square Icon with rounded corners
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepGlassNavy)
                        .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AppIconImage(
                        packageName = game.packageName,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = game.appName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isArabic) "ملف تفضيلات محفوظ" else "Saved preference profile",
                        fontSize = 11.sp,
                        color = SlateBlueMuted
                    )
                }
            }

            // Right Action Button: "تشغيل" (Electric Blue Pill Button - Screen 3)
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .background(
                        Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                    )
                    .clickable(onClick = onLaunch),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isArabic) "تشغيل" else "Play",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = ObsidianBlack
                )
            }
        }
    }
}
