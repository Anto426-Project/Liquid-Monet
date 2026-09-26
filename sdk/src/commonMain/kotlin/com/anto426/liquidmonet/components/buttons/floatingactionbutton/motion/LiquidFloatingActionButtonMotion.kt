package com.anto426.liquidmonet.components.buttons.floatingactionbutton.motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidDpAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidFloatingActionButton; no rendering or application state. */
internal object LiquidFloatingActionButtonMotion {
    @Composable
    fun animateDpPrimary(
        targetValue: Dp,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Dp> =
        animateLiquidDpAsState(
            targetValue,
            remember(performance) { LiquidMotion.spatialSpring(performance) },
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

    @Composable
    fun animateDpSecondary(
        targetValue: Dp,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Dp> =
        animateLiquidDpAsState(
            targetValue,
            remember(performance) { LiquidMotion.interactiveSpring(performance) },
            label,
        )

    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)
            },
            label,
        )

    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        fadeIn(
            animationSpec =
                LiquidMotion.tween(
                    performance,
                    LiquidMotion.FastDurationMillis,
                    LiquidMotion.EmphasizedDecelerate,
                )
        ) +
            expandHorizontally(
                animationSpec = LiquidMotion.spatialSpring(performance),
                expandFrom = Alignment.Start,
            )

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        fadeOut(
            animationSpec =
                LiquidMotion.tween(
                    performance,
                    LiquidMotion.FastDurationMillis,
                    LiquidMotion.EmphasizedAccelerate,
                )
        ) +
            shrinkHorizontally(
                animationSpec = LiquidMotion.spatialSpring(performance),
                shrinkTowards = Alignment.Start,
            )
}
