package com.anto426.liquidmonet.components.feedback.toast.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidToastMotion {
    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance) {
            {
                enter(performance)
                    .togetherWith(exit(performance))
                    .using(SizeTransform(clip = false))
            }
        }

    fun <T> returnSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.interactiveSpring(performance)

    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        slideInVertically(
            animationSpec =
                LiquidMotion.spring(performance, dampingRatio = 0.82f, stiffness = 420f),
            initialOffsetY = { -it / 2 },
        ) +
            scaleIn(
                animationSpec =
                    LiquidMotion.spring(performance, dampingRatio = 0.84f, stiffness = 440f),
                initialScale = 0.90f,
            ) +
            fadeIn(
                animationSpec =
                    LiquidMotion.tween(
                        performance,
                        LiquidMotion.FastDurationMillis,
                        LiquidMotion.EmphasizedDecelerate,
                    )
            )

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        slideOutVertically(
            animationSpec =
                LiquidMotion.spring(performance, dampingRatio = 0.90f, stiffness = 500f),
            targetOffsetY = { -it / 3 },
        ) +
            scaleOut(
                animationSpec =
                    LiquidMotion.spring(performance, dampingRatio = 0.90f, stiffness = 500f),
                targetScale = 0.95f,
            ) +
            fadeOut(
                animationSpec =
                    LiquidMotion.tween(
                        performance,
                        LiquidMotion.FastDurationMillis,
                        LiquidMotion.EmphasizedAccelerate,
                    )
            )
}
