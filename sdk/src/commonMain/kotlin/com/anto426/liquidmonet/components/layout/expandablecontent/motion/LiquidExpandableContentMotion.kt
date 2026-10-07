package com.anto426.liquidmonet.components.layout.expandablecontent.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidExpandableContentMotion {
    @Composable
    fun transitions(performance: LiquidGlassPerformanceState): Pair<EnterTransition, ExitTransition> =
        remember(performance) {
            if (performance.motionScale.isFinite() && performance.motionScale <= 0f) {
                EnterTransition.None to ExitTransition.None
            } else {
                (fadeIn(LiquidMotion.tween(performance)) + expandVertically(LiquidMotion.spatialSpring(performance), clip = false)) to
                    (fadeOut(LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)) + shrinkVertically(LiquidMotion.spatialSpring(performance), clip = false))
            }
        }
}
