package com.anto426.liquidmonet.glass.background

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope

/** Reuses the gradient program; only its local matrix changes as the field moves. */
internal class LiquidRadialGradient(colors: List<Color>) {
    private val brush =
        Brush.radialGradient(colors, center = Offset.Zero, radius = 1f) as ShaderBrush
    private val matrix = Matrix()

    fun draw(scope: DrawScope, center: Offset, radius: Float, blendMode: BlendMode) {
        if (radius <= 0f || !radius.isFinite()) return
        matrix[0, 0] = radius
        matrix[1, 1] = radius
        matrix[3, 0] = center.x
        matrix[3, 1] = center.y
        brush.transform = matrix
        with(scope) { drawCircle(brush, radius, center, blendMode = blendMode) }
    }
}

/** Unit gradient mapped onto the moving line without rebuilding colors, brush or shader. */
internal class LiquidLinearGradient(colors: List<Color>) {
    private val brush =
        Brush.linearGradient(colors, start = Offset.Zero, end = Offset(1f, 0f)) as ShaderBrush
    private val matrix = Matrix()

    fun draw(scope: DrawScope, start: Offset, end: Offset, blendMode: BlendMode) {
        mapLiquidGradientLine(matrix, start, end)
        brush.transform = matrix
        with(scope) { drawRect(brush, blendMode = blendMode) }
    }
}

internal fun mapLiquidGradientLine(matrix: Matrix, start: Offset, end: Offset) {
    val dx = end.x - start.x
    val dy = end.y - start.y
    matrix[0, 0] = dx
    matrix[0, 1] = dy
    matrix[1, 0] = -dy
    matrix[1, 1] = dx
    matrix[3, 0] = start.x
    matrix[3, 1] = start.y
}
