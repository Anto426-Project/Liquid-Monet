package com.anto426.liquidmonet.components.layout.animatedswitcher.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import kotlin.math.roundToInt

@Immutable
internal data class TransitionDisplacementBounds(
    val sharedHorizontalEnterPx: Int = 140,
    val sharedHorizontalExitPx: Int = 104,
    val sharedVerticalEnterPx: Int = 110,
    val sharedVerticalExitPx: Int = 80,
    val morphDriftPx: Int = 34,
    val maxSlideVerticalPx: Int = 1050,
    val tabGlideEnterPx: Int = 200,
    val tabGlideExitPx: Int = 130,
    val inlineDriftPx: Int = 70,
    val inlineExitPx: Int = 45,
)

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
        transitionFor: ((T, T) -> LiquidSwitcherTransition)? = null,
        mode: LiquidSwitcherMode = LiquidSwitcherMode.Screen,
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform {
        val latestIsForward by rememberUpdatedState(isForward)
        val latestTransitionFor by rememberUpdatedState(transitionFor)
        val zOrder = remember { TransitionZOrderState<T>() }
        val density = LocalDensity.current
        val bounds =
            remember(density) {
                with(density) {
                    TransitionDisplacementBounds(
                        sharedHorizontalEnterPx = 56.dp.roundToPx(),
                        sharedHorizontalExitPx = 42.dp.roundToPx(),
                        sharedVerticalEnterPx = 44.dp.roundToPx(),
                        sharedVerticalExitPx = 32.dp.roundToPx(),
                        morphDriftPx = 14.dp.roundToPx(),
                        maxSlideVerticalPx = 420.dp.roundToPx(),
                        tabGlideEnterPx = 80.dp.roundToPx(),
                        tabGlideExitPx = 52.dp.roundToPx(),
                        inlineDriftPx = 28.dp.roundToPx(),
                        inlineExitPx = 18.dp.roundToPx(),
                    )
                }
            }
        return remember(performance, transition, mode, isLtr, bounds) {
            {
                val selectedTransition =
                    latestTransitionFor?.invoke(initialState, targetState) ?: transition
                if (!motionEnabled(performance, selectedTransition)) {
                    (EnterTransition.None togetherWith ExitTransition.None).using(null)
                } else {
                    val forward =
                        latestIsForward?.invoke(initialState, targetState)
                            ?: inferForwardMotion(initialState, targetState)
                    val horizontalDirection = if (forward == isLtr) 1 else -1
                    val layered =
                        mode == LiquidSwitcherMode.Screen &&
                            (selectedTransition == LiquidSwitcherTransition.SlideHorizontal ||
                                selectedTransition == LiquidSwitcherTransition.SlideVertical ||
                                selectedTransition == LiquidSwitcherTransition.SharedAxisDepth)
                    val targetZ = zOrder.resolve(initialState, targetState, forward, layered)
                    val transform =
                        buildContentTransform(
                            performance = performance,
                            selectedTransition = selectedTransition,
                            forward = forward,
                            horizontalDirection = horizontalDirection,
                            targetContentZIndex = targetZ,
                            bounds = bounds,
                            mode = mode,
                        )
                    transform.using(sizeTransform(performance, mode))
                }
            }
        }
    }

    internal fun buildContentTransform(
        performance: LiquidGlassPerformanceState,
        selectedTransition: LiquidSwitcherTransition,
        forward: Boolean,
        horizontalDirection: Int,
        targetContentZIndex: Float = 0f,
        bounds: TransitionDisplacementBounds = TransitionDisplacementBounds(),
        mode: LiquidSwitcherMode = LiquidSwitcherMode.Screen,
    ): ContentTransform {
        if (selectedTransition == LiquidSwitcherTransition.None) {
            return ContentTransform(
                targetContentEnter = EnterTransition.None,
                initialContentExit = ExitTransition.None,
                targetContentZIndex = targetContentZIndex,
            )
        }

        val base = when (mode) {
            LiquidSwitcherMode.Tab -> buildTabSwitcherTransform(
                performance = performance,
                horizontalDirection = horizontalDirection,
                bounds = bounds,
            )

            LiquidSwitcherMode.Inline -> buildInlineSwitcherTransform(
                performance = performance,
                horizontalDirection = horizontalDirection,
                bounds = bounds,
            )

            LiquidSwitcherMode.Screen -> buildScreenTransform(
                performance = performance,
                selectedTransition = selectedTransition,
                forward = forward,
                horizontalDirection = horizontalDirection,
                bounds = bounds,
            )
        }

        return ContentTransform(
            targetContentEnter = base.targetContentEnter,
            initialContentExit = base.initialContentExit,
            targetContentZIndex = targetContentZIndex,
        )
    }

    /**
     * Dedicated in-screen tab switcher motion: spring-driven directional glide synchronized with
     * LiquidTabBar's droplet spring, with clean opacity overlap and no vertical center-scale jump
     * on tall scrollable panes.
     */
    private fun buildTabSwitcherTransform(
        performance: LiquidGlassPerformanceState,
        horizontalDirection: Int,
        bounds: TransitionDisplacementBounds,
    ): ContentTransform {
        val enterSlide =
            LiquidMotion.spring(
                performance = performance,
                dampingRatio = 0.92f,
                stiffness = 340f,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            )
        val exitSlide =
            LiquidMotion.spring(
                performance = performance,
                dampingRatio = 0.96f,
                stiffness = 420f,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            )
        val enter =
            slideInHorizontally(
                animationSpec = enterSlide,
                initialOffsetX = {
                    horizontalDirection * minOf((it * 0.24f).roundToInt(), bounds.tabGlideEnterPx)
                },
            ) +
                fadeIn(
                    animationSpec =
                        LiquidMotion.tween(
                            performance = performance,
                            durationMillis = 220,
                            delayMillis = 20,
                            easing = LiquidMotion.EmphasizedDecelerate,
                        )
                )
        val exit =
            slideOutHorizontally(
                animationSpec = exitSlide,
                targetOffsetX = {
                    -horizontalDirection * minOf((it * 0.16f).roundToInt(), bounds.tabGlideExitPx)
                },
            ) +
                fadeOut(
                    animationSpec =
                        LiquidMotion.tween(
                            performance = performance,
                            durationMillis = 135,
                            easing = LiquidMotion.EmphasizedAccelerate,
                        )
                )
        return enter togetherWith exit
    }

    /**
     * Dedicated inline content switcher motion: crisp spring-driven micro-glide and clean opacity
     * handoff tailored for filter chips, segmented sub-controls, and loading-to-content swaps.
     */
    private fun buildInlineSwitcherTransform(
        performance: LiquidGlassPerformanceState,
        horizontalDirection: Int,
        bounds: TransitionDisplacementBounds,
    ): ContentTransform {
        val enter =
            slideInHorizontally(
                animationSpec =
                    LiquidMotion.spring(
                        performance = performance,
                        dampingRatio = 0.95f,
                        stiffness = 460f,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    ),
                initialOffsetX = {
                    horizontalDirection * minOf((it * 0.10f).roundToInt(), bounds.inlineDriftPx)
                },
            ) +
                fadeIn(
                    animationSpec =
                        LiquidMotion.tween(
                            performance = performance,
                            durationMillis = 180,
                            delayMillis = 15,
                            easing = LiquidMotion.EmphasizedDecelerate,
                        )
                )
        val exit =
            slideOutHorizontally(
                animationSpec =
                    LiquidMotion.spring(
                        performance = performance,
                        dampingRatio = 1f,
                        stiffness = 520f,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    ),
                targetOffsetX = {
                    -horizontalDirection * minOf((it * 0.07f).roundToInt(), bounds.inlineExitPx)
                },
            ) +
                fadeOut(
                    animationSpec =
                        LiquidMotion.tween(
                            performance = performance,
                            durationMillis = 110,
                            easing = LiquidMotion.EmphasizedAccelerate,
                        )
                )
        return enter togetherWith exit
    }

    /**
     * Full-viewport screen-to-screen transitions tuned specifically for translucent glass surfaces:
     * fast outgoing clear prevents double-vision glass overlap while incoming glides smoothly.
     */
    private fun buildScreenTransform(
        performance: LiquidGlassPerformanceState,
        selectedTransition: LiquidSwitcherTransition,
        forward: Boolean,
        horizontalDirection: Int,
        bounds: TransitionDisplacementBounds,
    ): ContentTransform =
        when (selectedTransition) {
            LiquidSwitcherTransition.DirectionalHorizontal -> {
                val enter =
                    slideInHorizontally(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 300,
                                easing = LiquidMotion.EmphasizedDecelerate,
                            ),
                        initialOffsetX = {
                            horizontalDirection * minOf(it / 5, bounds.sharedHorizontalEnterPx)
                        },
                    ) +
                        fadeIn(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 210,
                                    delayMillis = 65,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                )
                        )
                val exit =
                    slideOutHorizontally(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 120,
                                easing = LiquidMotion.EmphasizedAccelerate,
                            ),
                        targetOffsetX = {
                            -horizontalDirection * minOf(it / 6, bounds.sharedHorizontalExitPx)
                        },
                    ) +
                        fadeOut(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 95,
                                    easing = LiquidMotion.EmphasizedAccelerate,
                                )
                        )
                enter togetherWith exit
            }

            LiquidSwitcherTransition.DirectionalVertical -> {
                val enter =
                    slideInVertically(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 300,
                                easing = LiquidMotion.EmphasizedDecelerate,
                            ),
                        initialOffsetY = {
                            (if (forward) 1 else -1) * minOf(it / 6, bounds.sharedVerticalEnterPx)
                        },
                    ) +
                        fadeIn(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 210,
                                    delayMillis = 65,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                )
                        )
                val exit =
                    slideOutVertically(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 120,
                                easing = LiquidMotion.EmphasizedAccelerate,
                            ),
                        targetOffsetY = {
                            (if (forward) -1 else 1) * minOf(it / 7, bounds.sharedVerticalExitPx)
                        },
                    ) +
                        fadeOut(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 95,
                                    easing = LiquidMotion.EmphasizedAccelerate,
                                )
                        )
                enter togetherWith exit
            }

            LiquidSwitcherTransition.LiquidMorph -> {
                val enter =
                    scaleIn(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 300,
                                delayMillis = 55,
                                easing = LiquidMotion.EmphasizedDecelerate,
                            ),
                        initialScale = if (forward) 0.95f else 1.04f,
                    ) +
                        slideInVertically(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 300,
                                    delayMillis = 55,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                ),
                            initialOffsetY = {
                                if (forward) bounds.morphDriftPx else -bounds.morphDriftPx
                            },
                        ) +
                        fadeIn(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 220,
                                    delayMillis = 55,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                )
                        )
                val exit =
                    scaleOut(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 110,
                                easing = LiquidMotion.EmphasizedAccelerate,
                            ),
                        targetScale = if (forward) 1.03f else 0.96f,
                    ) +
                        fadeOut(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 95,
                                    easing = LiquidMotion.EmphasizedAccelerate,
                                )
                        )
                enter togetherWith exit
            }

            LiquidSwitcherTransition.None -> EnterTransition.None togetherWith ExitTransition.None

            LiquidSwitcherTransition.Crossfade -> {
                fadeIn(
                    animationSpec =
                        LiquidMotion.tween(
                            performance = performance,
                            durationMillis = 200,
                            easing = LiquidMotion.EmphasizedDecelerate,
                        )
                ) togetherWith
                    fadeOut(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 140,
                                easing = LiquidMotion.EmphasizedAccelerate,
                            )
                    )
            }

            LiquidSwitcherTransition.FadeThrough -> {
                val enter =
                    fadeIn(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 210,
                                delayMillis = 70,
                                easing = LiquidMotion.EmphasizedDecelerate,
                            )
                    ) +
                        scaleIn(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 260,
                                    delayMillis = 70,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                ),
                            initialScale = 0.94f,
                        )
                val exit =
                    fadeOut(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 90,
                                easing = LiquidMotion.EmphasizedAccelerate,
                            )
                    ) +
                        scaleOut(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 90,
                                    easing = LiquidMotion.EmphasizedAccelerate,
                                ),
                            targetScale = 0.97f,
                        )
                enter togetherWith exit
            }

            LiquidSwitcherTransition.SlideHorizontal -> {
                val slideSpec =
                    LiquidMotion.spring(
                        performance = performance,
                        dampingRatio = 0.98f,
                        stiffness = 380f,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    )
                if (forward) {
                    val enter =
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { horizontalDirection * it },
                        ) +
                            fadeIn(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 80,
                                        easing = LiquidMotion.EmphasizedDecelerate,
                                    ),
                                initialAlpha = 0.85f,
                            )
                    val exit =
                        slideOutHorizontally(
                            animationSpec = slideSpec,
                            targetOffsetX = { -horizontalDirection * (it * 0.30f).roundToInt() },
                        ) +
                            scaleOut(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 200,
                                        easing = LiquidMotion.EmphasizedAccelerate,
                                    ),
                                targetScale = 0.96f,
                            ) +
                            fadeOut(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 140,
                                        easing = LiquidMotion.EmphasizedAccelerate,
                                    )
                            )
                    enter togetherWith exit
                } else {
                    val enter =
                        slideInHorizontally(
                            animationSpec = slideSpec,
                            initialOffsetX = { horizontalDirection * (it * 0.28f).roundToInt() },
                        ) +
                            scaleIn(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 240,
                                        easing = LiquidMotion.EmphasizedDecelerate,
                                    ),
                                initialScale = 0.96f,
                            ) +
                            fadeIn(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 180,
                                        delayMillis = 30,
                                        easing = LiquidMotion.EmphasizedDecelerate,
                                    )
                            )
                    val exit =
                        slideOutHorizontally(
                            animationSpec = slideSpec,
                            targetOffsetX = { -horizontalDirection * it },
                        ) +
                            fadeOut(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 150,
                                        easing = LiquidMotion.EmphasizedAccelerate,
                                    )
                            )
                    enter togetherWith exit
                }
            }

            LiquidSwitcherTransition.SlideVertical -> {
                val slideSpec =
                    LiquidMotion.spring(
                        performance = performance,
                        dampingRatio = 0.98f,
                        stiffness = 380f,
                        visibilityThreshold = IntOffset.VisibilityThreshold,
                    )
                if (forward) {
                    val enter =
                        slideInVertically(
                            animationSpec = slideSpec,
                            initialOffsetY = { minOf(it, bounds.maxSlideVerticalPx) },
                        ) +
                            fadeIn(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 110,
                                        easing = LiquidMotion.EmphasizedDecelerate,
                                    ),
                                initialAlpha = 0.78f,
                            )
                    val exit =
                        slideOutVertically(
                            animationSpec = slideSpec,
                            targetOffsetY = {
                                -(minOf(it, bounds.maxSlideVerticalPx) * 0.14f).roundToInt()
                            },
                        ) +
                            scaleOut(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 180,
                                        easing = LiquidMotion.EmphasizedAccelerate,
                                    ),
                                targetScale = 0.96f,
                            ) +
                            fadeOut(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 120,
                                        easing = LiquidMotion.EmphasizedAccelerate,
                                    )
                            )
                    enter togetherWith exit
                } else {
                    val enter =
                        slideInVertically(
                            animationSpec = slideSpec,
                            initialOffsetY = {
                                -(minOf(it, bounds.maxSlideVerticalPx) * 0.12f).roundToInt()
                            },
                        ) +
                            scaleIn(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 240,
                                        easing = LiquidMotion.EmphasizedDecelerate,
                                    ),
                                initialScale = 0.96f,
                            ) +
                            fadeIn(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 180,
                                        delayMillis = 35,
                                        easing = LiquidMotion.EmphasizedDecelerate,
                                    )
                            )
                    val exit =
                        slideOutVertically(
                            animationSpec = slideSpec,
                            targetOffsetY = { minOf(it, bounds.maxSlideVerticalPx) },
                        ) +
                            fadeOut(
                                animationSpec =
                                    LiquidMotion.tween(
                                        performance = performance,
                                        durationMillis = 150,
                                        easing = LiquidMotion.EmphasizedAccelerate,
                                    )
                            )
                    enter togetherWith exit
                }
            }

            LiquidSwitcherTransition.SharedAxisDepth -> {
                val enter =
                    scaleIn(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 300,
                                delayMillis = 65,
                                easing = LiquidMotion.EmphasizedDecelerate,
                            ),
                        initialScale = if (forward) 0.90f else 1.06f,
                    ) +
                        fadeIn(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 210,
                                    delayMillis = 65,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                )
                        )
                val exit =
                    scaleOut(
                        animationSpec =
                            LiquidMotion.tween(
                                performance = performance,
                                durationMillis = 110,
                                easing = LiquidMotion.EmphasizedAccelerate,
                            ),
                        targetScale = if (forward) 1.06f else 0.92f,
                    ) +
                        fadeOut(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 95,
                                    easing = LiquidMotion.EmphasizedAccelerate,
                                )
                        )
                enter togetherWith exit
            }
        }

    private fun sizeTransform(
        performance: LiquidGlassPerformanceState,
        mode: LiquidSwitcherMode,
    ): SizeTransform =
        SizeTransform(clip = false) { _, _ ->
            when (mode) {
                LiquidSwitcherMode.Tab,
                LiquidSwitcherMode.Inline ->
                    LiquidMotion.spring(
                        performance = performance,
                        dampingRatio = 0.98f,
                        stiffness = 520f,
                        visibilityThreshold = IntSize.VisibilityThreshold,
                    )

                LiquidSwitcherMode.Screen ->
                    LiquidMotion.spring(
                        performance = performance,
                        dampingRatio = LiquidMotion.ScreenSettleDampingRatio,
                        stiffness = LiquidMotion.ScreenSettleStiffness,
                        visibilityThreshold = IntSize.VisibilityThreshold,
                    )
            }
        }

    fun <T> returnSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.74f,
            stiffness = 440f,
        )
}

/**
 * Tracks monotonic, idempotent Z-ordering across transitions so forward pushes always layer above
 * outgoing screens and backward pops always peel off above the returning screen.
 */
internal class TransitionZOrderState<T> {
    private var lastInitial: Any? = Unset
    private var lastTarget: Any? = Unset
    private var lastZIndex: Float = 0f

    fun resolve(initial: T, target: T, forward: Boolean, layered: Boolean): Float {
        if (!layered) return lastZIndex
        if (initial == lastInitial && target == lastTarget) return lastZIndex
        val next = if (forward) lastZIndex + 1f else lastZIndex - 1f
        lastInitial = initial
        lastTarget = target
        lastZIndex = next
        return next
    }

    private companion object {
        val Unset = Any()
    }
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
