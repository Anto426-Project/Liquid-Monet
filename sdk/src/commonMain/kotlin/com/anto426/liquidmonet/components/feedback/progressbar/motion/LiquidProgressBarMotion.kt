package com.anto426.liquidmonet.components.feedback.progressbar.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidProgressBar; no rendering or application state. */
internal object LiquidProgressBarMotion {
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
                    animation = tween(durationMillis = 1800, easing = LinearEasing),
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
                    animation = tween(durationMillis = 2200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )

    @Composable
    fun rememberIndeterminatePulse(
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
                    animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            },
            label = label,
        )

    @Composable
    fun rememberRotation(
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
                    animation = tween(durationMillis = 1300, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )

    @Composable
    fun rememberSweepAngle(
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
                    animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            },
            label = label,
        )
}
