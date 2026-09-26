package com.anto426.liquidmonet.components.feedback.sheet.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidSheetMotion {
    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        fadeIn(LiquidMotion.tween(performance, 180))

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        fadeOut(LiquidMotion.tween(performance, 160))

    fun <T> entranceSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.72f,
            stiffness = 360f,
        )

    fun <T> fade(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.tween(performance, 150)

    fun <T> exitSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.95f,
            stiffness = 450f,
        )

    fun <T> returnSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.76f,
            stiffness = 420f,
        )

    fun surfaceEnter(performance: LiquidGlassPerformanceState): EnterTransition =
        slideInVertically(animationSpec = entranceSpring(performance), initialOffsetY = { it }) +
            fadeIn(fade(performance))

    fun surfaceExit(performance: LiquidGlassPerformanceState): ExitTransition =
        slideOutVertically(animationSpec = exitSpring(performance), targetOffsetY = { it }) +
            fadeOut(fade(performance))
}
