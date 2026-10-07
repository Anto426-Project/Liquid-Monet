package com.anto426.liquidmonet.components.layout.animatedswitcher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.layout.animatedswitcher.motion.LiquidAnimatedSwitcherMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.LiquidFloatMotion
import com.anto426.liquidmonet.motion.rememberLiquidPredictiveBackState
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Motion styles supported by [LiquidAnimatedSwitcher]. */
enum class LiquidSwitcherTransition {
    /** Horizontal shared-axis motion whose direction follows [LiquidAnimatedSwitcher.isForward]. */
    DirectionalHorizontal,

    /** Vertical shared-axis motion whose direction follows [LiquidAnimatedSwitcher.isForward]. */
    DirectionalVertical,

    /** Elastic liquid morph for content at the same hierarchy level. */
    LiquidMorph,

    /** Immediate replacement, including layout size, with no decorative motion. */
    None,

    /** Simultaneous opacity blend for peer screens without a spatial relationship. */
    Crossfade,

    /** Fades out the old screen before fading in the new one. */
    FadeThrough,

    /** Full-width page slide; forward/backward motion follows the layout direction. */
    SlideHorizontal,

    /** Full-height page slide; forward enters from below, backward from above. */
    SlideVertical,

    /** Directional scale and fade for moving into or out of a hierarchy. */
    SharedAxisDepth,
}

/**
 * Changes arbitrary content with the same adaptive motion used by Liquid Monet navigation.
 *
 * [isForward] is optional for naturally ordered states: numbers and mutually comparable values are
 * inferred automatically. Unordered states default to forward motion, and can provide their own
 * resolver when semantic order matters.
 */
@Composable
fun <T> LiquidAnimatedSwitcher(
    targetState: T,
    modifier: Modifier = Modifier,
    transition: LiquidSwitcherTransition = LiquidSwitcherTransition.LiquidMorph,
    isForward: ((initialState: T, targetState: T) -> Boolean)? = null,
    onSwipeForward: (() -> Unit)? = null,
    onSwipeBackward: (() -> Unit)? = null,
    swipeThreshold: Dp = 48.dp,
    contentAlignment: Alignment = Alignment.Center,
    label: String = "LiquidAnimatedSwitcher",
    onPredictiveBack: (() -> Unit)? = null,
    content: @Composable AnimatedContentScope.(targetState: T) -> Unit,
) {
    require(swipeThreshold > 0.dp) { "LiquidAnimatedSwitcher swipeThreshold must be positive." }

    val performance = LocalLiquidGlassPerformance.current
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val motionEnabled = LiquidAnimatedSwitcherMotion.motionEnabled(performance, transition)
    val scope = rememberCoroutineScope()
    val forwardAction by rememberUpdatedState(onSwipeForward)
    val backwardAction by rememberUpdatedState(onSwipeBackward)
    val predictiveBack = rememberLiquidPredictiveBackState(onPredictiveBack)
    val thresholdPx = with(density) { swipeThreshold.toPx() }
    var rawDragOffsetPx by remember { mutableFloatStateOf(0f) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    var settleJob: Job? by remember { mutableStateOf<Job?>(null) }

    val settleDrag: () -> Unit = {
        settleJob?.cancel()
        rawDragOffsetPx = 0f
        if (!motionEnabled) {
            dragOffsetPx = 0f
            settleJob = null
        } else {
            val start = dragOffsetPx
            settleJob = scope.launch {
                LiquidFloatMotion(start).animateTo(
                    targetValue = 0f,
                    animationSpec = LiquidAnimatedSwitcherMotion.returnSpring(performance),
                ) {
                    dragOffsetPx = value
                }
            }
        }
    }

    val touchModifier =
        if (onSwipeForward != null || onSwipeBackward != null) {
            Modifier.semantics {
                    customActions = buildList {
                        if (forwardAction != null) {
                            add(
                                CustomAccessibilityAction("Contenuto successivo") {
                                    forwardAction?.invoke()
                                    true
                                }
                            )
                        }
                        if (backwardAction != null) {
                            add(
                                CustomAccessibilityAction("Contenuto precedente") {
                                    backwardAction?.invoke()
                                    true
                                }
                            )
                        }
                    }
                }
                .pointerInput(thresholdPx, isLtr, performance, motionEnabled) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            settleJob?.cancel()
                            rawDragOffsetPx = 0f
                            dragOffsetPx = 0f
                        },
                        onDragEnd = {
                            if (abs(rawDragOffsetPx) >= thresholdPx) {
                                val forwardGesture =
                                    if (isLtr) {
                                        rawDragOffsetPx < 0f
                                    } else {
                                        rawDragOffsetPx > 0f
                                    }
                                val action = if (forwardGesture) forwardAction else backwardAction
                                if (action != null) {
                                    hapticFeedback.performLiquidHaptic(LiquidHapticCue.Selection)
                                    action()
                                }
                            }
                            settleDrag()
                        },
                        onDragCancel = settleDrag,
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            rawDragOffsetPx += dragAmount
                            dragOffsetPx =
                                if (!motionEnabled) 0f else thresholdPx *
                                    1.65f *
                                    kotlin.math.tanh(rawDragOffsetPx / (thresholdPx * 1.65f))
                        },
                    )
                }
        } else {
            Modifier
        }

    AnimatedContent(
        targetState = targetState,
        modifier =
            modifier.then(touchModifier).graphicsLayer {
                val axisSize = size.width.coerceAtLeast(1f)
                val drag = if (motionEnabled) dragOffsetPx else 0f
                val backProgress = if (motionEnabled) predictiveBack.progress else 0f
                val deformation = (abs(drag) / axisSize).coerceIn(0f, 0.22f)
                transformOrigin = TransformOrigin.Center
                translationX =
                    drag * 0.42f +
                        axisSize * 0.16f * backProgress * predictiveBack.edgeDirection
                scaleX = (1f + deformation * 0.28f) * (1f - backProgress * 0.025f)
                scaleY = (1f - deformation * 0.16f) * (1f - backProgress * 0.025f)
                alpha = 1f - backProgress * 0.08f
                clip = false
            },
        transitionSpec =
            LiquidAnimatedSwitcherMotion.contentTransition(
                performance,
                transition = transition,
                isForward = isForward,
                isLtr = isLtr,
            ),
        contentAlignment = contentAlignment,
        label = label,
        content = content,
    )
}
