package com.anto426.liquidmonet.components.layout.lazyfooter.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.TransformOrigin
import com.anto426.liquidmonet.components.layout.lazyfooter.LiquidLazyFooterOrientation
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidLazyFooterMotion {
    fun <T> impulseSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.65f,
            stiffness = 320f,
        )

    fun <T> entranceFade(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.tween(performance, 200)

    fun <T> entranceScale(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.72f,
            stiffness = 320f,
        )

    fun <T> expansionSpring(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.75f,
            stiffness = 320f,
        )

    fun <T> exitFade(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.tween(performance, 150)

    fun <T> exitScale(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.80f,
            stiffness = 380f,
        )

    fun <T> collapse(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<T> =
        LiquidMotion.tween(performance, 180)

    fun enter(
        performance: LiquidGlassPerformanceState,
        orientation: LiquidLazyFooterOrientation,
    ): EnterTransition =
        fadeIn(entranceFade(performance)) +
            scaleIn(
                animationSpec = entranceScale(performance),
                initialScale = 0.95f,
                transformOrigin = TransformOrigin.Center,
            ) +
            when (orientation) {
                LiquidLazyFooterOrientation.Vertical ->
                    expandVertically(
                        animationSpec = expansionSpring(performance),
                        expandFrom = Alignment.Top,
                        clip = false,
                    )
                LiquidLazyFooterOrientation.Horizontal ->
                    expandHorizontally(
                        animationSpec = expansionSpring(performance),
                        expandFrom = Alignment.Start,
                        clip = false,
                    )
            }

    fun exit(
        performance: LiquidGlassPerformanceState,
        orientation: LiquidLazyFooterOrientation,
    ): ExitTransition =
        fadeOut(exitFade(performance)) +
            scaleOut(
                animationSpec = exitScale(performance),
                targetScale = 0.95f,
                transformOrigin = TransformOrigin.Center,
            ) +
            when (orientation) {
                LiquidLazyFooterOrientation.Vertical ->
                    shrinkVertically(
                        animationSpec = collapse(performance),
                        shrinkTowards = Alignment.Top,
                        clip = false,
                    )
                LiquidLazyFooterOrientation.Horizontal ->
                    shrinkHorizontally(
                        animationSpec = collapse(performance),
                        shrinkTowards = Alignment.Start,
                        clip = false,
                    )
            }
}
