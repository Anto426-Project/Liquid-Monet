package com.anto426.liquidmonet.components.cards.mediacontroller.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

internal object LiquidMediaControllerMotion {
    @Composable
    fun rememberPlayPulse(
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
                    animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                )
            },
            label = label,
        )
}
