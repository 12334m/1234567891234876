package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HeartRateZone
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Zone1Color
import com.example.ui.theme.Zone2Color
import com.example.ui.theme.Zone3Color
import com.example.ui.theme.Zone4Color
import com.example.ui.theme.Zone5Color

@Composable
fun ZoneDistributionBar(
    z1Sec: Long,
    z2Sec: Long,
    z3Sec: Long,
    z4Sec: Long,
    z5Sec: Long,
    modifier: Modifier = Modifier
) {
    val total = (z1Sec + z2Sec + z3Sec + z4Sec + z5Sec).coerceAtLeast(1L).toFloat()

    val p1 = (z1Sec.toFloat() / total).coerceIn(0f, 1f)
    val p2 = (z2Sec.toFloat() / total).coerceIn(0f, 1f)
    val p3 = (z3Sec.toFloat() / total).coerceIn(0f, 1f)
    val p4 = (z4Sec.toFloat() / total).coerceIn(0f, 1f)
    val p5 = (z5Sec.toFloat() / total).coerceIn(0f, 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "HEART RATE ZONES",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Stacked Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFF1E293B))
        ) {
            if (p1 > 0f) Box(modifier = Modifier.weight(p1).height(14.dp).background(Zone1Color))
            if (p2 > 0f) Box(modifier = Modifier.weight(p2).height(14.dp).background(Zone2Color))
            if (p3 > 0f) Box(modifier = Modifier.weight(p3).height(14.dp).background(Zone3Color))
            if (p4 > 0f) Box(modifier = Modifier.weight(p4).height(14.dp).background(Zone4Color))
            if (p5 > 0f) Box(modifier = Modifier.weight(p5).height(14.dp).background(Zone5Color))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Legend with times
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ZoneLegendItem(name = "Z1", timeSec = z1Sec, color = Zone1Color)
            ZoneLegendItem(name = "Z2", timeSec = z2Sec, color = Zone2Color)
            ZoneLegendItem(name = "Z3", timeSec = z3Sec, color = Zone3Color)
            ZoneLegendItem(name = "Z4", timeSec = z4Sec, color = Zone4Color)
            ZoneLegendItem(name = "Z5", timeSec = z5Sec, color = Zone5Color)
        }
    }
}

@Composable
private fun ZoneLegendItem(
    name: String,
    timeSec: Long,
    color: Color
) {
    val mins = timeSec / 60
    val secs = timeSec % 60
    val timeStr = "${mins}m"

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = name,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = timeStr,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}
