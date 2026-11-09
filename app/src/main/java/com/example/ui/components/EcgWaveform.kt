package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan

@Composable
fun EcgWaveform(
    bpm: Int,
    isBeating: Boolean,
    modifier: Modifier = Modifier,
    lineColor: Color = NeonCrimson
) {
    val durationMs = if (bpm > 30) (60_000 / bpm).coerceIn(350, 1500) else 1000
    val infiniteTransition = rememberInfiniteTransition(label = "ecg_phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw faint grid lines
        val gridSpacing = 24.dp.toPx()
        var gx = 0f
        while (gx < width) {
            drawLine(
                color = Color.White.copy(alpha = 0.04f),
                start = Offset(gx, 0f),
                end = Offset(gx, height),
                strokeWidth = 1f
            )
            gx += gridSpacing
        }
        var gy = 0f
        while (gy < height) {
            drawLine(
                color = Color.White.copy(alpha = 0.04f),
                start = Offset(0f, gy),
                end = Offset(width, gy),
                strokeWidth = 1f
            )
            gy += gridSpacing
        }

        if (bpm <= 0) {
            // Flatline with subtle idle noise
            val path = Path()
            path.moveTo(0f, midY)
            path.lineTo(width, midY)
            drawPath(
                path = path,
                color = Color.Gray.copy(alpha = 0.5f),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
            return@Canvas
        }

        // Draw dynamic ECG trace
        val path = Path()
        val numPoints = 80
        val stepX = width / (numPoints - 1)

        for (i in 0 until numPoints) {
            val normX = i.toFloat() / (numPoints - 1)
            // Local position within cycle (0..1)
            val cyclePos = (normX * 2.5f - phase + 10f) % 1f

            // Typical P-Q-R-S-T wave pattern
            val waveY = when {
                cyclePos in 0.15f..0.22f -> {
                    // P wave (small bump)
                    val p = (cyclePos - 0.15f) / 0.07f
                    -height * 0.12f * kotlin.math.sin(p * Math.PI.toFloat())
                }
                cyclePos in 0.25f..0.28f -> {
                    // Q dip
                    height * 0.08f
                }
                cyclePos in 0.28f..0.33f -> {
                    // R peak (tall spike)
                    val r = (cyclePos - 0.28f) / 0.05f
                    -height * 0.42f * kotlin.math.sin(r * Math.PI.toFloat())
                }
                cyclePos in 0.33f..0.36f -> {
                    // S dip
                    height * 0.14f
                }
                cyclePos in 0.42f..0.52f -> {
                    // T wave (moderate bump)
                    val t = (cyclePos - 0.42f) / 0.10f
                    -height * 0.16f * kotlin.math.sin(t * Math.PI.toFloat())
                }
                else -> 0f
            }

            val px = i * stepX
            val py = (midY + waveY).coerceIn(4f, height - 4f)

            if (i == 0) {
                path.moveTo(px, py)
            } else {
                path.lineTo(px, py)
            }
        }

        // Draw glowing background glow
        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.3f),
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw crisp foreground line
        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.4f),
                    lineColor,
                    NeonCyan
                )
            ),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
