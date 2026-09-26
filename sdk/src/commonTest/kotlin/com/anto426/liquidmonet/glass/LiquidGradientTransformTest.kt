package com.anto426.liquidmonet.glass

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix
import com.anto426.liquidmonet.glass.background.mapLiquidGradientLine
import kotlin.test.*

class LiquidGradientTransformTest {
    @Test
    fun reusedTransformMapsBothEndpointsWithoutAccumulatingOldMovement() {
        val matrix = Matrix()
        val pairs =
            listOf(
                Offset(10f, 20f) to Offset(90f, 200f),
                Offset(-15f, 5f) to Offset(100f, 0f),
                Offset.Zero to Offset(0f, 500f),
            )
        repeat(20) {
            for ((start, end) in pairs) {
                mapLiquidGradientLine(matrix, start, end)
                assertEquals(start, matrix.map(Offset.Zero))
                assertEquals(end, matrix.map(Offset(1f, 0f)))
            }
        }
    }
}
