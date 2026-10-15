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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBlueMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * Screen 11 from GlassBoost UI Design System:
 * إعدادات التحسين (Optimization Settings) with Performance Mode selection (Max, Balanced, Saver)
 * and auto-optimization toggles.
 */
@Composable
fun SettingsScreen(
    isArabic: Boolean
) {
    var selectedPerformanceMode by remember { mutableStateOf("MAX") }
    var autoBoostEnabled by remember { mutableStateOf(true) }
    var autoOptimizeGamesEnabled by remember { mutableStateOf(true) }
    var performanceNotificationsEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isArabic) "إعدادات التحسين" else "Optimization Settings",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = PureWhite
                    )
                    Text(
                        text = if (isArabic) "تخصيص نمط التسريع والأداء التلقائي" else "Configure engine presets & auto-tuning",
                        fontSize = 11.sp,
                        color = SlateBlueMuted
                    )
                }
            }
        }

        // Section 1: وضع الأداء (Performance Mode Radio Group - Screen 11)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = if (isArabic) "وضع الأداء (Performance Mode)" else "Performance Mode",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val modes = listOf(
                        Triple("MAX", if (isArabic) "أقصى أداء" else "Max Performance", if (isArabic) "استهلاك كامل لقدرات المعالج مع صفر لاغ" else "Full hardware unleash with zero throttling"),
                        Triple("BALANCED", if (isArabic) "متوازن" else "Balanced", if (isArabic) "توازن ممتاز بين السلاسة وعمر البطارية" else "Smooth frame rates with thermal control"),
                        Triple("SAVER", if (isArabic) "توفير الطاقة" else "Battery Saver", if (isArabic) "تقليل استهلاك البطارية والحرارة أثناء اللعب" else "Low heat & extended gaming runtime")
                    )

                    modes.forEach { (modeKey, title, desc) ->
                        val isSelected = selectedPerformanceMode == modeKey

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DeepGlassNavy else Color.Transparent)
                                .border(1.dp, if (isSelected) NeonCyan.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(10.dp))
                                .clickable { selectedPerformanceMode = modeKey }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedPerformanceMode = modeKey },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = NeonCyan,
                                    unselectedColor = SlateBlueMuted
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else SlateBlueMuted
                                )
                                Text(
                                    text = desc,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }

        // Section 2: Auto Toggles (Screen 11 Switches)
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (isArabic) "خيارات التشغيل الذكي" else "Smart Automation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )

                    // Toggle 1: تفعيل التحسين التلقائي
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "تفعيل التحسين التلقائي" else "Auto-Boost Engine",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = if (isArabic) "تسريع المعالج والرام تلقائياً عند فتح الألعاب" else "Trigger optimization sequence upon game launch",
                                fontSize = 10.sp,
                                color = SlateBlueMuted
                            )
                        }

                        Switch(
                            checked = autoBoostEnabled,
                            onCheckedChange = { autoBoostEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = DeepGlassNavy,
                                uncheckedThumbColor = SlateBlueMuted,
                                uncheckedTrackColor = DarkSlate
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSlate.copy(alpha = 0.5f)))

                    // Toggle 2: تحسين الألعاب تلقائياً
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "تحسين الألعاب تلقائياً" else "Auto Optimize Games",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = if (isArabic) "تطبيق دقة الشاشة وتثبيت الإطارات لكل لعبة" else "Apply per-game target FPS locks automatically",
                                fontSize = 10.sp,
                                color = SlateBlueMuted
                            )
                        }

                        Switch(
                            checked = autoOptimizeGamesEnabled,
                            onCheckedChange = { autoOptimizeGamesEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = DeepGlassNavy,
                                uncheckedThumbColor = SlateBlueMuted,
                                uncheckedTrackColor = DarkSlate
                            )
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(DarkSlate.copy(alpha = 0.5f)))

                    // Toggle 3: إشعارات الأداء
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "إشعارات الأداء (HUD)" else "Performance Notifications",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Text(
                                text = if (isArabic) "تنبيه عند ارتفاع حرارة المعالج أو ارتفاع البنج" else "Alert when CPU temperature spikes above safety limit",
                                fontSize = 10.sp,
                                color = SlateBlueMuted
                            )
                        }

                        Switch(
                            checked = performanceNotificationsEnabled,
                            onCheckedChange = { performanceNotificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = DeepGlassNavy,
                                uncheckedThumbColor = SlateBlueMuted,
                                uncheckedTrackColor = DarkSlate
                            )
                        )
                    }
                }
            }
        }

        // Section 3: About GlassBoost
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardGlassSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = RoyalGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "GlassBoost • Hybrid Esports Engine v2.4",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PureWhite
                        )
                        Text(
                            text = "Play Better • Faster • Smoother",
                            fontSize = 10.sp,
                            color = SlateBlueMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
