package com.anto426.liquidmonet.components.inputs.splitclearbutton.internal

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Transient neck between the field and droplet. Geometry is read and reused during drawing. */
internal fun Modifier.liquidDropletBridge(
    progress: State<Float>,
    gap: Dp,
    rtl: Boolean,
    fill: Color,
    outline: Color,
): Modifier = drawWithCache {
    val neck = Path()
    val gapPixels = gap.toPx()
    val stroke = Stroke(0.7.dp.toPx())
    onDrawBehind {
        val phase = progress.value.coerceIn(0f, 1f)
        val remaining = 1f - phase
        val opening = 1f - remaining * remaining * remaining
        val edge = size.width * (1f - opening)
        if (edge < gapPixels && phase < 0.94f && phase > 0f) {
            val halfNeck = size.height * 0.12f * ((0.94f - phase) / 0.44f).coerceIn(0f, 1f)
            val center = size.height / 2f
            fun x(value: Float): Float = if (rtl) size.width - value else value
            neck.reset()
            neck.moveTo(x(edge - 1f), center - halfNeck)
            neck.cubicTo(
                x(edge + gapPixels * 0.35f),
                center - halfNeck * 0.15f,
                x(gapPixels * 0.8f),
                center - halfNeck * 0.15f,
                x(gapPixels + 1f),
                center - halfNeck * 1.6f,
            )
            neck.lineTo(x(gapPixels + 1f), center + halfNeck * 1.6f)
            neck.cubicTo(
                x(gapPixels * 0.8f),
                center + halfNeck * 0.15f,
                x(edge + gapPixels * 0.35f),
                center + halfNeck * 0.15f,
                x(edge - 1f),
                center + halfNeck,
            )
            neck.close()
            drawPath(neck, fill)
            drawPath(neck, outline, style = stroke)
        }
    }
}
