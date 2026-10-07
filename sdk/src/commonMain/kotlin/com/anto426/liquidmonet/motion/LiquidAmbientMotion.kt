package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateBackground

/**
 * A normalized 0..1 reversing phase for decorative artwork. Timings and activity are owned by
 * the SDK. No animation clock is kept when [active] is false, motion is reduced, or the current
 * current performance profile disables animated backgrounds. The paused phase is 0.5.
 * Read the returned state in drawing code rather than recomposing artwork every frame.
 */
@Composable
fun rememberLiquidAmbientPhase(
    active: Boolean = true,
    label: String = "LiquidAmbientPhase",
): State<Float> {
    val performance = LocalLiquidGlassPerformance.current
    if (!active || !performance.animateBackground) {
        return remember { mutableFloatStateOf(.5f) }
    }
    val transition = rememberLiquidInfiniteTransition(label)
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = remember { infiniteRepeatable(tween<Float>(9000, easing = LinearEasing), RepeatMode.Reverse) },
        label = label,
    )
}
