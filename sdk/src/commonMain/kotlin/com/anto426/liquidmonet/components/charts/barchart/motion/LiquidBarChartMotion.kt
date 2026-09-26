package com.anto426.liquidmonet.components.charts.barchart.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidBarChart; no rendering or application state. */
internal object LiquidBarChartMotion {
    @Composable
    fun animateFloat(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) { LiquidMotion.fluidSpring(performance) },
            label,
        )

    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance) {
            {
                LiquidMotion.slideUpFadeEnter(performance) { it / 2 } togetherWith
                    LiquidMotion.slideDownFadeExit(performance) { -it / 2 }
            }
        }

    @Composable
    fun rememberBarShimmerPhase(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue,
            targetValue,
            remember() {
                infiniteRepeatable(
                    animation = tween(durationMillis = 2600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )
}
