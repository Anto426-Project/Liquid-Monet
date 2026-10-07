package com.anto426.liquidmonet.components.layout.screenentrance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.layout.screenentrance.motion.LiquidScreenEntranceMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance

/** Entrance styles for a screen or an individual section of its content. */
enum class LiquidScreenEntranceAnimation {
    None,
    Fade,
    FadeUp,
    FadeDown,
    Scale,
}

/**
 * Animates content when it first enters composition, and whenever [visible] changes.
 *
 * Use [delayMillis] to stagger independent sections. Reduced motion removes the delay and
 * transforms. Recomposition preserves progress; wrap this component in `key(screenId)` to replay
 * the entrance for a new screen. Content leaves composition once the exit finishes.
 *
 * Place this inside the application's existing LiquidGlassScene. It does not create a scene,
 * backdrop or overlay host. Avoid combining a full-screen entrance with a spatial switcher
 * transition; animate its sections instead.
 */
@Composable
fun LiquidScreenEntrance(
    modifier: Modifier = Modifier,
    animation: LiquidScreenEntranceAnimation = LiquidScreenEntranceAnimation.FadeUp,
    visible: Boolean = true,
    delayMillis: Int = 0,
    label: String = "LiquidScreenEntrance",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    require(delayMillis >= 0) { "LiquidScreenEntrance delayMillis must be non-negative." }

    val performance = LocalLiquidGlassPerformance.current
    val transitions = LiquidScreenEntranceMotion.transitions(performance, animation, delayMillis)
    val visibleState = remember {
        MutableTransitionState(visible && !LiquidScreenEntranceMotion.motionEnabled(performance, animation))
    }
    visibleState.targetState = visible

    AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = transitions.enter,
        exit = transitions.exit,
        label = label,
        content = content,
    )
}
