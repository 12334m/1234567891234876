package com.example.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val NeonCrimson = Color(0xFFFF2A55)
val NeonCyan = Color(0xFF00E5FF)
val NeonPurple = Color(0xFF8B5CF6)
val NeonGreen = Color(0xFF10B981)
val NeonYellow = Color(0xFFFBBF24)

// Zone Colors
val Zone1Color = Color(0xFF38BDF8) // Warm Up
val Zone2Color = Color(0xFF4ADE80) // Fat Burn
val Zone3Color = Color(0xFFFACC15) // Aerobic
val Zone4Color = Color(0xFFFB923C) // Anaerobic
val Zone5Color = Color(0xFFF43F5E) // Peak / Max

// Standard Dark Palette Fallback
val DarkBackground = Color(0xFF090D16)
val DarkSurface = Color(0xFF101626)
val DarkSurfaceVariant = Color(0xFF182238)
val DarkBorder = Color(0xFF22304D)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

enum class DarkThemeMode(
    val title: String,
    val subtitle: String,
    val powerBadge: String
) {
    OLED_BLACK(
        "OLED Pure Black",
        "Turns off black pixels (0% power) for maximum battery & zero VR lens flare",
        "0% OLED Power"
    ),
    MIDNIGHT_CYBER(
        "Midnight Cyber",
        "Deep slate dark mode with cyberpunk blue-gray contrast",
        "Balanced Dark"
    ),
    WARM_NIGHT(
        "Night Shift (Anti-Blue)",
        "Amber & warm crimson low-fatigue palette for dark VR rooms",
        "Low Eye Strain"
    )
}

data class AppThemeColors(
    val background: Color,
    val cardSurface: Color,
    val surfaceVariant: Color,
    val border: Color,
    val topBarBg: Color,
    val navBarBg: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val secondary: Color,
    val isPureBlack: Boolean
)

val OledBlackColors = AppThemeColors(
    background = Color(0xFF000000),      // True 0% emission black
    cardSurface = Color(0xFF08090D),     // Ultra deep charcoal
    surfaceVariant = Color(0xFF10131A),
    border = Color(0xFF1A1F2B),
    topBarBg = Color(0xFF000000),
    navBarBg = Color(0xFF000000),
    textPrimary = Color(0xFFF1F5F9),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    primary = NeonCrimson,
    secondary = NeonCyan,
    isPureBlack = true
)

val MidnightCyberColors = AppThemeColors(
    background = Color(0xFF090D16),
    cardSurface = Color(0xFF101626),
    surfaceVariant = Color(0xFF182238),
    border = Color(0xFF22304D),
    topBarBg = Color(0xFF090D16),
    navBarBg = Color(0xFF0C101A),
    textPrimary = Color(0xFFF8FAFC),
    textSecondary = Color(0xFF94A3B8),
    textMuted = Color(0xFF64748B),
    primary = NeonCrimson,
    secondary = NeonCyan,
    isPureBlack = false
)

val WarmNightColors = AppThemeColors(
    background = Color(0xFF070405),      // Pitch dark with slight warm ember
    cardSurface = Color(0xFF120A0B),     // Ultra deep ember surface
    surfaceVariant = Color(0xFF1E1012),
    border = Color(0xFF33161A),
    topBarBg = Color(0xFF070405),
    navBarBg = Color(0xFF070405),
    textPrimary = Color(0xFFFEF2F2),
    textSecondary = Color(0xFFFCA5A5),
    textMuted = Color(0xFFB91C1C),
    primary = Color(0xFFFF334B),
    secondary = Color(0xFFFFB020),      // Warm amber gold (anti-blue light)
    isPureBlack = true
)

val LocalAppColors = compositionLocalOf { OledBlackColors }
