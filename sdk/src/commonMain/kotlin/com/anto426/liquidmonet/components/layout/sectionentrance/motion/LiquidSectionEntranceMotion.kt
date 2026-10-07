package com.anto426.liquidmonet.components.layout.sectionentrance.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidSectionEntranceMotion {
    fun motionEnabled(performance: LiquidGlassPerformanceState, animation: LiquidScreenEntranceAnimation): Boolean =
        animation != LiquidScreenEntranceAnimation.None &&
            !(performance.motionScale.isFinite() && performance.motionScale <= 0f)

    @Composable
    fun progress(
        performance: LiquidGlassPerformanceState,
        animation: LiquidScreenEntranceAnimation,
        revealed: Boolean,
        delayMillis: Int,
        replayKey: Any?,
    ): State<Float> {
        val enabled = motionEnabled(performance, animation)
        val progress = remember { Animatable(if (enabled) 0f else 1f) }
        LaunchedEffect(revealed, animation, replayKey, enabled) {
            if (!enabled) progress.snapTo(1f)
            else if (revealed) {
                progress.snapTo(0f)
                progress.animateTo(
                    1f,
                    LiquidMotion.tween(performance, durationMillis = 320, delayMillis = delayMillis, easing = LiquidMotion.EmphasizedDecelerate),
                )
            }
        }
        return progress.asState()
    }
}
