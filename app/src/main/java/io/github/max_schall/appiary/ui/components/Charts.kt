package io.github.max_schall.appiary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.max_schall.appiary.ui.theme.Spacing

/** One labelled bar in [BarChart]. */
data class BarDatum(val label: String, val value: Double)

/**
 * Minimal column chart drawn on a Canvas (no charting dependency). Bars share a
 * single [color]; x-labels render as evenly weighted text below. Heights are
 * scaled to the largest value in the set.
 */
@Composable
fun BarChart(
    bars: List<BarDatum>,
    color: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 140.dp,
) {
    if (bars.isEmpty()) return
    val maxValue = bars.maxOf { it.value }.coerceAtLeast(1e-6)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height),
        ) {
            val n = bars.size
            val gap = size.width * 0.015f
            val barWidth = ((size.width - gap * (n + 1)) / n).coerceAtLeast(1f)
            val radius = CornerRadius(barWidth * 0.18f, barWidth * 0.18f)
            bars.forEachIndexed { i, bar ->
                val x = gap + i * (barWidth + gap)
                // Faint full-height track so empty/short bars stay legible.
                drawRoundRect(
                    color = trackColor,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = radius,
                    alpha = 0.35f,
                )
                val barHeight = (bar.value / maxValue * size.height).toFloat()
                if (barHeight > 0f) {
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, size.height - barHeight),
                        size = Size(barWidth, barHeight),
                        cornerRadius = radius,
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = Spacing.xs)) {
            bars.forEach { bar ->
                Text(
                    text = bar.label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** A varroa measurement plotted on [TrendScatter]: %-load at a moment in time. */
data class ScatterPoint(val timeMs: Long, val value: Double, val color: Color)

/** A dashed horizontal reference line (e.g. a treatment threshold). */
data class ThresholdLine(val value: Double, val color: Color)

/**
 * Time-series scatter with a faint connecting line and optional dashed threshold
 * lines. Used for the varroa-load trend; x is time, y is infestation %.
 */
@Composable
fun TrendScatter(
    points: List<ScatterPoint>,
    modifier: Modifier = Modifier,
    thresholds: List<ThresholdLine> = emptyList(),
    height: androidx.compose.ui.unit.Dp = 160.dp,
) {
    if (points.isEmpty()) return
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val gridColor = MaterialTheme.colorScheme.surfaceVariant
    val sorted = points.sortedBy { it.timeMs }
    val minT = sorted.first().timeMs
    val maxT = sorted.last().timeMs
    val span = (maxT - minT).coerceAtLeast(1L)
    val yMax = (sorted.maxOf { it.value }.coerceAtLeast(
        thresholds.maxOfOrNull { it.value } ?: 0.0,
    ) * 1.15).coerceAtLeast(1.0)

    Box(modifier.fillMaxWidth()) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(height),
        ) {
            fun x(t: Long) = ((t - minT).toFloat() / span * size.width)
            fun y(v: Double) = (size.height - (v / yMax * size.height)).toFloat()

            // Baseline.
            drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2f)

            // Threshold reference lines (dashed).
            val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
            thresholds.forEach { t ->
                val yy = y(t.value)
                drawLine(t.color, Offset(0f, yy), Offset(size.width, yy), strokeWidth = 2.5f, pathEffect = dash, alpha = 0.7f)
            }

            // Connecting line.
            if (sorted.size > 1) {
                for (i in 0 until sorted.size - 1) {
                    drawLine(
                        lineColor,
                        Offset(x(sorted[i].timeMs), y(sorted[i].value)),
                        Offset(x(sorted[i + 1].timeMs), y(sorted[i + 1].value)),
                        strokeWidth = 3f,
                    )
                }
            }

            // Points.
            sorted.forEach { p ->
                drawCircle(p.color, radius = 7f, center = Offset(x(p.timeMs), y(p.value)))
            }
        }
        Text(
            text = formatLoad(yMax),
            modifier = Modifier.align(Alignment.TopStart),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatLoad(v: Double): String = "${"%.1f".format(v)}%"
