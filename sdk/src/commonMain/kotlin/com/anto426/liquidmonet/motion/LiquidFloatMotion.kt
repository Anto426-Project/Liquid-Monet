package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animate
import androidx.compose.runtime.Stable

/** Internal scalar controller. Rendering reads values; owners supply only finite motion plans. */
@Stable
internal class LiquidFloatMotion(initialValue: Float) {
    private val animation = Animatable(initialValue.takeIf { it.isFinite() } ?: 0f)
    val value: Float
        get() = animation.value

    suspend fun snapTo(targetValue: Float) =
        animation.snapTo(targetValue.takeIf { it.isFinite() } ?: value)

    suspend fun animateTo(
        targetValue: Float,
        animationSpec: FiniteAnimationSpec<Float>,
        onFrame: LiquidFloatMotion.() -> Unit = {},
    ) {
        animation.animateTo(targetValue.takeIf { it.isFinite() } ?: value, animationSpec) {
            this@LiquidFloatMotion.onFrame()
        }
    }
}

internal suspend fun animateLiquidFloat(
    initialValue: Float,
    targetValue: Float,
    animationSpec: FiniteAnimationSpec<Float>,
    block: (Float, Float) -> Unit,
) = animate(initialValue, targetValue, animationSpec = animationSpec, block = block)
