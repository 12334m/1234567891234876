package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    darkThemeMode: DarkThemeMode = DarkThemeMode.OLED_BLACK,
    content: @Composable () -> Unit
) {
    val appColors = when (darkThemeMode) {
        DarkThemeMode.OLED_BLACK -> OledBlackColors
        DarkThemeMode.MIDNIGHT_CYBER -> MidnightCyberColors
        DarkThemeMode.WARM_NIGHT -> WarmNightColors
    }

    val colorScheme = darkColorScheme(
        primary = appColors.primary,
        onPrimary = Color.White,
        primaryContainer = Color(0xFF3F0B18),
        onPrimaryContainer = Color(0xFFFFD9E2),

        secondary = appColors.secondary,
        onSecondary = Color(0xFF00363D),
        secondaryContainer = Color(0xFF004F58),
        onSecondaryContainer = Color(0xFF97F0FF),

        tertiary = NeonPurple,
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF321957),
        onTertiaryContainer = Color(0xFFEADBFF),

        background = appColors.background,
        onBackground = appColors.textPrimary,

        surface = appColors.cardSurface,
        onSurface = appColors.textPrimary,
        surfaceVariant = appColors.surfaceVariant,
        onSurfaceVariant = appColors.textSecondary,

        outline = appColors.border
    )

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
