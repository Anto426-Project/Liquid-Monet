package com.anto426.liquidmonet.components.layout.animatedswitcher.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidAnimatedSwitcherMotion {
    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState,
        transition: LiquidSwitcherTransition,
        isForward: ((T, T) -> Boolean)?,
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance, transition, isForward) {
            {
                val forward =
                    isForward?.invoke(initialState, targetState)
                        ?: inferForwardMotion(initialState, targetState)
                when (transition) {
                    LiquidSwitcherTransition.DirectionalHorizontal -> {
                        val enter =
                            slideInHorizontally(
                                animationSpec = LiquidMotion.spring(performance, 0.78f, 380f),
                                initialOffsetX = { if (forward) it / 3 else -it / 3 },
                            ) + fadeIn(animationSpec = LiquidMotion.tween(performance, 220))
                        val exit =
                            slideOutHorizontally(
                                animationSpec = LiquidMotion.spring(performance, 0.78f, 380f),
                                targetOffsetX = { if (forward) -it / 3 else it / 3 },
                            ) + fadeOut(animationSpec = LiquidMotion.tween(performance, 180))
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    }
                    LiquidSwitcherTransition.DirectionalVertical -> {
                        val enter =
                            slideInVertically(
                                animationSpec = LiquidMotion.spring(performance, 0.78f, 380f),
                                initialOffsetY = { if (forward) it / 3 else -it / 3 },
                            ) + fadeIn(animationSpec = LiquidMotion.tween(performance, 220))
                        val exit =
                            slideOutVertically(
                                animationSpec = LiquidMotion.spring(performance, 0.78f, 380f),
                                targetOffsetY = { if (forward) -it / 3 else it / 3 },
                            ) + fadeOut(animationSpec = LiquidMotion.tween(performance, 180))
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    }
                    LiquidSwitcherTransition.LiquidMorph -> {
                        val enter =
                            scaleIn(
                                animationSpec = LiquidMotion.spring(performance, 0.72f, 360f),
                                initialScale = 0.92f,
                            ) + fadeIn(animationSpec = LiquidMotion.tween(performance, 200))
                        val exit =
                            scaleOut(
                                animationSpec = LiquidMotion.spring(performance, 0.72f, 360f),
                                targetScale = 0.92f,
                            ) + fadeOut(animationSpec = LiquidMotion.tween(performance, 160))
                        (enter togetherWith exit).using(SizeTransform(clip = false))
                    }
                }
            }
        }

    fun <T> returnSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.58f,
            stiffness = 420f,
        )
}

private fun <T> inferForwardMotion(initialState: T, targetState: T): Boolean {
    if (initialState is Number && targetState is Number) {
        return targetState.toDouble() >= initialState.toDouble()
    }

    if (initialState is Comparable<*> && targetState is Comparable<*>) {
        return runCatching {
                @Suppress("UNCHECKED_CAST")
                (targetState as Comparable<Any?>).compareTo(initialState) >= 0
            }
            .getOrDefault(true)
    }

    return true
}
