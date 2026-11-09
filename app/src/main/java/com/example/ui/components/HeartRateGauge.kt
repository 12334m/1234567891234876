package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HeartRateData
import com.example.model.HeartRateZone
import com.example.ui.theme.LocalAppColors
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HeartRateGauge(
    data: HeartRateData,
    maxHr: Int = 190,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val bpm = data.bpm
    val zone = data.getZone(maxHr)
    val zoneColor = Color(zone.colorHex)

    val scale = remember { Animatable(1f) }

    LaunchedEffect(data.isBeat, bpm) {
        if (bpm > 0) {
            scale.animateTo(
                targetValue = 1.28f,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
            )
            scale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        zoneColor.copy(alpha = 0.15f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        zoneColor.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
            .testTag("heart_rate_gauge")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Row: Status badge & RR interval
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zone Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(zoneColor.copy(alpha = 0.2f))
                        .border(1.dp, zoneColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(zoneColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${zone.shortName} · ${zone.label}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (data.rrIntervalMs != null && data.rrIntervalMs > 0) {
                    Text(
                        text = "RR ${data.rrIntervalMs} ms",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = if (bpm > 0) "REAL-TIME" else "STANDBY",
                        color = if (bpm > 0) zoneColor else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Center Heart & Big BPM Number
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .scale(scale.value),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing glow circle
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(zoneColor.copy(alpha = 0.2f))
                    )
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Rate Beat",
                        tint = if (bpm > 0) zoneColor else TextMuted,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (bpm > 0) bpm.toString() else "--",
                        color = appColors.textPrimary,
                        fontSize = 68.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        lineHeight = 68.sp,
                        modifier = Modifier.testTag("current_bpm_text")
                    )
                    Text(
                        text = "BEATS PER MINUTE",
                        color = appColors.textMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ECG Waveform Line inside Card
            EcgWaveform(
                bpm = bpm,
                isBeating = data.isBeat,
                lineColor = zoneColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (appColors.isPureBlack) Color(0xFF000000) else appColors.background)
                    .border(1.dp, appColors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            )
        }
    }
}
