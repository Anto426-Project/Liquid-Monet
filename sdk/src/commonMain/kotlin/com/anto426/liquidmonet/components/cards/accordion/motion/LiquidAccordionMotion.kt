package com.anto426.liquidmonet.components.cards.accordion.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState
import kotlin.math.PI
import kotlin.math.sin

/** Expansion, deformation and chevron motion for an expandable card. */
internal object LiquidAccordionMotion {
    @Composable
    fun animateOpeningProgress(
        expanded: Boolean,
        performance: LiquidGlassPerformanceState,
    ): State<Float> = animateLiquidFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = remember(performance) {
            LiquidMotion.spring(performance, dampingRatio = 0.58f, stiffness = 300f)
        },
        label = "AccordionOpening",
    )

    @Composable
    fun animateChevronRotation(
        expanded: Boolean,
        performance: LiquidGlassPerformanceState,
    ): State<Float> = animateLiquidFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = remember(performance) {
            LiquidMotion.spring(performance, dampingRatio = 0.54f, stiffness = 320f)
        },
        label = "AccordionChevron",
    )

    fun deformation(progress: Float): Float = sin(progress.coerceIn(0f, 1f) * PI.toFloat())

    @Composable
    fun transitions(performance: LiquidGlassPerformanceState): Pair<EnterTransition, ExitTransition> =
        remember(performance) {
            if (performance.motionScale.isFinite() && performance.motionScale <= 0f) {
                EnterTransition.None to ExitTransition.None
            } else {
                enter(performance) to exit(performance)
            }
        }

    private fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        expandVertically(
            animationSpec =
                LiquidMotion.spring(
                    performance = performance,
                    dampingRatio = LiquidMotion.MenuBounceDampingRatio,
                    stiffness = LiquidMotion.MenuBounceStiffness,
                ),
            expandFrom = Alignment.Top,
        ) +
            scaleIn(
                animationSpec =
                    LiquidMotion.spring(
                        performance = performance,
                        dampingRatio = LiquidMotion.MenuBounceDampingRatio,
                        stiffness = LiquidMotion.MenuBounceStiffness,
                    ),
                initialScale = 0.90f,
                transformOrigin = TransformOrigin(0.5f, 0f),
            ) +
            fadeIn(
                animationSpec =
                    LiquidMotion.tween(
                        performance = performance,
                        durationMillis = LiquidMotion.FastDurationMillis,
                        easing = LiquidMotion.EmphasizedDecelerate,
                    )
            )

    private fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        shrinkVertically(
            animationSpec = LiquidMotion.snappySpring(performance),
            shrinkTowards = Alignment.Top,
        ) +
            scaleOut(
                animationSpec =
                    LiquidMotion.tween(
                        performance = performance,
                        durationMillis = LiquidMotion.FastDurationMillis,
                        easing = LiquidMotion.EmphasizedAccelerate,
                    ),
                targetScale = 0.92f,
                transformOrigin = TransformOrigin(0.5f, 0f),
            ) +
            fadeOut(
                animationSpec =
                    LiquidMotion.tween(
                        performance = performance,
                        durationMillis = LiquidMotion.FastDurationMillis,
                        easing = LiquidMotion.EmphasizedAccelerate,
                    )
            )
}
