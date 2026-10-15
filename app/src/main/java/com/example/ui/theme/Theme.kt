package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GlassBoostColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = ObsidianBlack,
    primaryContainer = RoyalGoldDark,
    onPrimaryContainer = RoyalGoldLight,
    secondary = GlassBlue,
    onSecondary = ObsidianBlack,
    secondaryContainer = DeepGlassNavy,
    onSecondaryContainer = GlassCyan,
    tertiary = GlassCyan,
    background = ObsidianBlack,
    onBackground = TextPrimary,
    surface = DeepGraphite,
    onSurface = TextPrimary,
    surfaceVariant = CardGlassSurface,
    onSurfaceVariant = TextSecondary,
    outline = CardGlassBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Luxury dark glass mode is default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ObsidianBlack.toArgb()
                window.navigationBarColor = ObsidianBlack.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = GlassBoostColorScheme,
        typography = Typography,
        content = content
    )
}
