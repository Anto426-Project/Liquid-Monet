package com.anto426.liquidmonet.components.feedback.shimmer.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

internal object LiquidShimmerMotion {
    private val OpticalCausticEasing = CubicBezierEasing(0.36f, 0.0f, 0.24f, 1.0f)

    @Composable
    fun rememberShimmerTranslation(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1480, easing = OpticalCausticEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )
}
