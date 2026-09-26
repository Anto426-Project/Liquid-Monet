package com.anto426.liquidmonet.components.charts.linechart.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidLineChart; no rendering or application state. */
internal object LiquidLineChartMotion {
    @Composable
    fun animateFloatPrimary(
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
    fun animateFloatSecondary(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) { LiquidMotion.interactiveSpring(performance) },
            label,
        )

    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance) {
            {
                LiquidMotion.slideUpFadeEnter(performance) { h -> h / 3 } togetherWith
                    LiquidMotion.slideDownFadeExit(performance) { h -> -h / 3 }
            }
        }

    @Composable
    fun rememberWavePhase(
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
                    animation = tween(durationMillis = 3800, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )

    @Composable
    fun rememberShimmerOffset(
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
                    animation = tween(durationMillis = 3200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )
}
