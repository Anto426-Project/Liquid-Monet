package com.anto426.liquidmonet.components.layout.animatedswitcher

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.anto426.liquidmonet.components.layout.animatedswitcher.motion.LiquidAnimatedSwitcherMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance

/**
 * The same presets as [LiquidAnimatedSwitcher], for an existing navigation/content host.
 * Pass the returned lambda to that host's transition spec; this does not create another host.
 * [transitionFor] chooses a preset for a state pair, for example `None` across a session boundary.
 * Application route policy stays with the caller; timings, transforms, RTL and reduced motion
 * remain owned by the SDK. Use separate forward/pop specs when direction is not inferred.
 */
@Composable
fun <T> rememberLiquidContentTransition(
    transition: LiquidSwitcherTransition = LiquidSwitcherTransition.LiquidMorph,
    isForward: ((T, T) -> Boolean)? = null,
    transitionFor: ((T, T) -> LiquidSwitcherTransition)? = null,
): AnimatedContentTransitionScope<T>.() -> ContentTransform =
    LiquidAnimatedSwitcherMotion.contentTransition(
        performance = LocalLiquidGlassPerformance.current,
        transition = transition,
        isForward = isForward,
        isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr,
        transitionFor = transitionFor,
    )
