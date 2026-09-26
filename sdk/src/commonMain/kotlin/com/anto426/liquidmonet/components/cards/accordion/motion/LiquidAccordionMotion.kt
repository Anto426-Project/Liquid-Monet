package com.anto426.liquidmonet.components.cards.accordion.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidAccordion; no rendering or application state. */
internal object LiquidAccordionMotion {
    fun bubbleSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<Float> =
        LiquidMotion.spring(performance, dampingRatio = 0.58f, stiffness = 300f)

    @Composable
    fun animateFloatPrimary(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> = animateLiquidFloatAsState(targetValue, bubbleSpring(performance), label)

    @Composable
    fun animateFloatSecondary(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.spring(
                    performance = performance,
                    dampingRatio = 0.54f,
                    stiffness = 320f,
                )
            },
            label,
        )

    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween(performance, 200) },
            label,
        )

    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
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

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
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
