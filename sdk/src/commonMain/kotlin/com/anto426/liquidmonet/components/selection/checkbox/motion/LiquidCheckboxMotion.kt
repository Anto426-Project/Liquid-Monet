package com.anto426.liquidmonet.components.selection.checkbox.motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidCheckbox; no rendering or application state. */
internal object LiquidCheckboxMotion {
    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.tween(performance, 220, LiquidMotion.FastOutSlow)
            },
            label,
        )

    @Composable
    fun animateFloat(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) { LiquidMotion.snappySpring(performance) },
            label,
        )

    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        LiquidMotion.popEnter(performance)

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        LiquidMotion.popExit(performance)
}
