package com.anto426.liquidmonet.components.pickers.timepicker.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState

/** Motion owned by LiquidTimePicker; no rendering or application state. */
internal object LiquidTimePickerMotion {
    @Composable
    fun animatePresetColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue = targetValue,
            animationSpec = remember(performance) { LiquidMotion.tween(performance, 150) },
            label = label,
        )

    @Composable
    fun animateDrumColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue = targetValue,
            animationSpec = remember(performance) { LiquidMotion.tween(performance, 140) },
            label = label,
        )

    @Composable
    fun drumDigitTransition(
        performance: LiquidGlassPerformanceState,
        direction: Int,
    ): AnimatedContentTransitionScope<Int>.() -> ContentTransform =
        remember(performance, direction) {
            {
                val enterDuration = LiquidMotion.FastDurationMillis
                val exitDuration = 110
                val dir = if (direction >= 0) 1 else -1
                (slideInVertically(
                    animationSpec =
                        LiquidMotion.tween(
                            performance,
                            enterDuration,
                            LiquidMotion.EmphasizedDecelerate,
                        ),
                    initialOffsetY = { fullHeight -> dir * (fullHeight / 2) },
                ) +
                    fadeIn(
                        LiquidMotion.tween(
                            performance,
                            enterDuration,
                            LiquidMotion.EmphasizedDecelerate,
                        ),
                    )) togetherWith
                    (slideOutVertically(
                        animationSpec =
                            LiquidMotion.tween(
                                performance,
                                exitDuration,
                                LiquidMotion.EmphasizedAccelerate,
                            ),
                        targetOffsetY = { fullHeight -> -dir * (fullHeight / 2) },
                    ) +
                        fadeOut(
                            LiquidMotion.tween(
                                performance,
                                exitDuration,
                                LiquidMotion.EmphasizedAccelerate,
                            ),
                        )) using SizeTransform(clip = false)
            }
        }
}
