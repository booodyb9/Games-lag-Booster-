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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CardGlassSurface
import com.example.ui.theme.DeepGlassNavy
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.PerformanceGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.SlateBlueMuted

/**
 * Screen 8 from GlassBoost UI Design System:
 * شاشة وضع الألعاب (Game Mode Screen) with large glowing controller emblem,
 * 3 core features (FPS boost, Latency lock, Network stabilization) and "تفعيل الآن" action.
 */
@Composable
fun GameModeScreen(
    isArabic: Boolean,
    onActivate: () -> Unit
) {
    var isActivated by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Glowing Game Controller Badge (Screen 8 Centerpiece)
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ElectricBlue.copy(alpha = 0.5f), DeepGlassNavy)
                        )
                    )
                    .border(2.5.dp, NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Gamepad,
                    contentDescription = "Game Mode Controller",
                    tint = NeonCyan,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isArabic) "وضع الألعاب" else "Game Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = PureWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isArabic) "أقصى أداء لأفضل تجربة لعب" else "Maximum performance for the ultimate gaming experience",
                fontSize = 13.sp,
                color = SlateBlueMuted,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3 Feature Perk Rows (Screen 8)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Perk 1: تحسين الإطارات
                GameModePerkCard(
                    icon = Icons.Default.Speed,
                    title = if (isArabic) "تحسين الإطارات (FPS Boost)" else "Frame Rate Stabilization",
                    subtitle = if (isArabic) "تثبيت معدل الإطارات عند 60/90/120 FPS بدون هبوط" else "Locks frame rates up to 120 FPS with zero drops",
                    isActive = isActivated
                )

                // Perk 2: تقليل اللاغ
                GameModePerkCard(
                    icon = Icons.Default.Timer,
                    title = if (isArabic) "تقليل اللاتج (Low Ping)" else "Ping & Lag Reduction",
                    subtitle = if (isArabic) "خفض زمن الوصول بنسبة تصل إلى 40% عبر مسارات سريعة" else "Cuts round-trip latency by up to 40% via fastpath",
                    isActive = isActivated
                )

                // Perk 3: تثبيت الشبكة
                GameModePerkCard(
                    icon = Icons.Default.Wifi,
                    title = if (isArabic) "تثبيت الشبكة (Network Lock)" else "Network Packet Lock",
                    subtitle = if (isArabic) "حظر التقطيع وفقدان الحزم لمنع الـ Lag Spikes" else "Eliminates jitter and packet loss spikes during combat",
                    isActive = isActivated
                )
            }
        }

        // Action Button: تفعيل الآن (Screen 8)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(
                    if (isActivated)
                        Brush.horizontalGradient(listOf(PerformanceGreen, ElectricBlue))
                    else
                        Brush.horizontalGradient(listOf(NeonCyan, ElectricBlue))
                )
                .clickable {
                    isActivated = !isActivated
                    onActivate()
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isActivated) Icons.Default.Check else Icons.Default.Bolt,
                    contentDescription = "Activate",
                    tint = ObsidianBlack,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isActivated) {
                        if (isArabic) "وضع الألعاب مفعّل بنجاح ✓" else "Game Mode Activated ✓"
                    } else {
                        if (isArabic) "تفعيل الآن" else "Activate Now"
                    },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = ObsidianBlack
                )
            }
        }
    }
}

@Composable
private fun GameModePerkCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardGlassSurface)
            .border(
                1.dp,
                if (isActive) NeonCyan.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DeepGlassNavy)
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = SlateBlueMuted
                )
            }
        }
    }
}
