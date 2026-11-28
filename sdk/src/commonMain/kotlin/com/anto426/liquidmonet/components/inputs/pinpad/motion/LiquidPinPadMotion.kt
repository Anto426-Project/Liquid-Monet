package com.anto426.liquidmonet.components.inputs.pinpad.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidPinPad; no rendering or application state. */
internal object LiquidPinPadMotion {
    @Composable
    fun animateDotScale(
        filled: Boolean,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue = if (filled) 1f else 0.72f,
            animationSpec = remember(performance) { LiquidMotion.snappySpring(performance) },
            label = label,
        )

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
