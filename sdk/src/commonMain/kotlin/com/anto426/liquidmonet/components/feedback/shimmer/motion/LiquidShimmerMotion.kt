package com.anto426.liquidmonet.components.feedback.shimmer.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

internal object LiquidShimmerMotion {
    @Composable
    fun rememberShimmerTranslation(
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
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )
}
