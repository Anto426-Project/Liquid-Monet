package com.anto426.liquidmonet.components.navigation.topbar.motion

import androidx.compose.animation.*
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidTopBarMotion {
    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        expandVertically(animationSpec = LiquidMotion.interactiveSpring(performance)) +
            fadeIn(LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis))

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        shrinkVertically(animationSpec = LiquidMotion.snappySpring(performance)) +
            fadeOut(LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis))
}
