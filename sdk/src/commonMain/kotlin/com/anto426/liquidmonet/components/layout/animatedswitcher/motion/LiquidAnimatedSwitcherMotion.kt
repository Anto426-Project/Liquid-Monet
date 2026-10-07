package com.anto426.liquidmonet.components.layout.animatedswitcher.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidAnimatedSwitcherMotion {
    fun motionEnabled(
        performance: LiquidGlassPerformanceState,
        transition: LiquidSwitcherTransition,
    ): Boolean =
        transition != LiquidSwitcherTransition.None &&
            !(performance.motionScale.isFinite() && performance.motionScale <= 0f)

    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState,
        transition: LiquidSwitcherTransition,
        isForward: ((T, T) -> Boolean)?,
        isLtr: Boolean,
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance, transition, isForward, isLtr) {
            {
                if (!motionEnabled(performance, transition)) {
                    (EnterTransition.None togetherWith ExitTransition.None).using(null)
                } else {
                    val forward =
                        isForward?.invoke(initialState, targetState)
                            ?: inferForwardMotion(initialState, targetState)
                    val horizontalDirection = if (forward == isLtr) 1 else -1
                    val transform = when (transition) {
                        LiquidSwitcherTransition.DirectionalHorizontal -> {
                            val enter =
                                slideInHorizontally(
                                    animationSpec = LiquidMotion.spring(performance, 0.78f, 380f),
                                    initialOffsetX = { horizontalDirection * (it / 3) },
                                ) + fadeIn(animationSpec = LiquidMotion.tween(performance, 220))
                            val exit =
                                slideOutHorizontally(
                                    animationSpec = LiquidMotion.spring(performance, 0.78f, 380f),
                                    targetOffsetX = { -horizontalDirection * (it / 3) },
                                ) + fadeOut(animationSpec = LiquidMotion.tween(performance, 180))
                            enter togetherWith exit
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
                            enter togetherWith exit
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
                            enter togetherWith exit
                        }
                        LiquidSwitcherTransition.None -> EnterTransition.None togetherWith ExitTransition.None
                        LiquidSwitcherTransition.Crossfade -> {
                            fadeIn(animationSpec = LiquidMotion.tween(performance, 220)) togetherWith
                                fadeOut(animationSpec = LiquidMotion.tween(performance, 220))
                        }
                        LiquidSwitcherTransition.FadeThrough -> {
                            fadeIn(
                                animationSpec = LiquidMotion.tween(performance, 220, delayMillis = 100)
                            ) togetherWith fadeOut(animationSpec = LiquidMotion.tween(performance, 100))
                        }
                        LiquidSwitcherTransition.SlideHorizontal -> {
                            val direction = if (forward == isLtr) {
                                AnimatedContentTransitionScope.SlideDirection.Left
                            } else {
                                AnimatedContentTransitionScope.SlideDirection.Right
                            }
                            slideIntoContainer(direction, LiquidMotion.spatialSpring(performance)) togetherWith
                                slideOutOfContainer(direction, LiquidMotion.spatialSpring(performance))
                        }
                        LiquidSwitcherTransition.SlideVertical -> {
                            val direction = if (forward) {
                                AnimatedContentTransitionScope.SlideDirection.Up
                            } else {
                                AnimatedContentTransitionScope.SlideDirection.Down
                            }
                            slideIntoContainer(direction, LiquidMotion.spatialSpring(performance)) togetherWith
                                slideOutOfContainer(direction, LiquidMotion.spatialSpring(performance))
                        }
                        LiquidSwitcherTransition.SharedAxisDepth -> {
                            val enter = scaleIn(
                                animationSpec = LiquidMotion.tween(performance, 320),
                                initialScale = if (forward) 0.88f else 1.08f,
                            ) + fadeIn(animationSpec = LiquidMotion.tween(performance, 220, delayMillis = 80))
                            val exit = scaleOut(
                                animationSpec = LiquidMotion.tween(performance, 320),
                                targetScale = if (forward) 1.08f else 0.88f,
                            ) + fadeOut(animationSpec = LiquidMotion.tween(performance, 100))
                            enter togetherWith exit
                        }
                    }
                    transform.using(SizeTransform(clip = false) { _, _ ->
                        LiquidMotion.spatialSpring(performance)
                    })
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
