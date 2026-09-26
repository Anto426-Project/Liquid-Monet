package com.anto426.liquidmonet.components.feedback.snackbar.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidSnackbarMotion {

    fun <T> returnSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.interactiveSpring(performance)

    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        slideInVertically(
            animationSpec =
                LiquidMotion.spring(performance, dampingRatio = 0.84f, stiffness = 420f),
            initialOffsetY = { it / 2 },
        ) +
            scaleIn(
                animationSpec =
                    LiquidMotion.spring(performance, dampingRatio = 0.86f, stiffness = 440f),
                initialScale = 0.96f,
            ) +
            fadeIn(animationSpec = LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis))

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        slideOutVertically(
            animationSpec =
                LiquidMotion.spring(performance, dampingRatio = 0.92f, stiffness = 500f),
            targetOffsetY = { it / 3 },
        ) +
            scaleOut(
                animationSpec =
                    LiquidMotion.spring(performance, dampingRatio = 0.92f, stiffness = 500f),
                targetScale = 0.98f,
            ) +
            fadeOut(
                animationSpec = LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)
            )
}
