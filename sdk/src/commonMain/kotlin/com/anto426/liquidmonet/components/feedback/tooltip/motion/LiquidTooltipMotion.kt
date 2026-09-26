package com.anto426.liquidmonet.components.feedback.tooltip.motion

import androidx.compose.animation.*
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidTooltipMotion {

    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        scaleIn(
            animationSpec = LiquidMotion.spring(performance, dampingRatio = 0.75f, stiffness = 500f)
        ) + fadeIn(animationSpec = LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis))

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        scaleOut(
            animationSpec = LiquidMotion.spring(performance, dampingRatio = 0.85f, stiffness = 500f)
        ) +
            fadeOut(
                animationSpec = LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)
            )
}
