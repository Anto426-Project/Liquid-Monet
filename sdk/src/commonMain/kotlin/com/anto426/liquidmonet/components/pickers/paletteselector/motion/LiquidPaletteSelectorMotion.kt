package com.anto426.liquidmonet.components.pickers.paletteselector.motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidPaletteSelector; no rendering or application state. */
internal object LiquidPaletteSelectorMotion {
    @Composable
    fun animateFloat(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) { LiquidMotion.interactiveSpring(performance) },
            label,
        )

    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        LiquidMotion.popEnter(performance)

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        LiquidMotion.popExit(performance)
}
