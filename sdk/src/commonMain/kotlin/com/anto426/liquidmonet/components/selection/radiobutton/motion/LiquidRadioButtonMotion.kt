package com.anto426.liquidmonet.components.selection.radiobutton.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidRadioButton; no rendering or application state. */
internal object LiquidRadioButtonMotion {
    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween(performance, 200) },
            label,
        )

    @Composable
    fun animateFloatPrimary(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.spring(
                    performance = performance,
                    dampingRatio = 0.54f,
                    stiffness = 460f,
                )
            },
            label,
        )

    @Composable
    fun animateFloatSecondary(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.spring(
                    performance = performance,
                    dampingRatio = 0.52f,
                    stiffness = 520f,
                )
            },
            label,
        )
}
