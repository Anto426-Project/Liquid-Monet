package com.anto426.liquidmonet.components.inputs.numberfield.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState

/** Motion owned by LiquidNumberField; no rendering or application state. */
internal object LiquidNumberFieldMotion {
    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue = targetValue,
            animationSpec = remember(performance) { LiquidMotion.interactiveSpring(performance) },
            label = label,
        )
}
