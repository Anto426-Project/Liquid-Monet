package com.anto426.liquidmonet.components.feedback.loading.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

internal object LiquidLoadingMotion {
    @Composable
    fun rememberDot1(
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
                    animation =
                        tween(durationMillis = 800, delayMillis = 0, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            },
            label = label,
        )

    @Composable
    fun rememberDot2(
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
                    animation =
                        tween(
                            durationMillis = 800,
                            delayMillis = 200,
                            easing = FastOutSlowInEasing,
                        ),
                    repeatMode = RepeatMode.Reverse,
                )
            },
            label = label,
        )

    @Composable
    fun rememberDot3(
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
                    animation =
                        tween(
                            durationMillis = 800,
                            delayMillis = 400,
                            easing = FastOutSlowInEasing,
                        ),
                    repeatMode = RepeatMode.Reverse,
                )
            },
            label = label,
        )

    @Composable
    fun rememberPulseProgress(
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
                    animation = tween(durationMillis = 1600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )
}
