package com.anto426.liquidmonet.components.pickers.datepicker.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidDatePicker; no rendering or application state. */
internal object LiquidDatePickerMotion {
    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween(performance, 150) },
            label,
        )

    @Composable
    fun animateFloat(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.tween(
                    performance,
                    LiquidMotion.FastDurationMillis,
                    LiquidMotion.EmphasizedDecelerate,
                )
            },
            label,
        )

    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance) {
            {
                fadeIn(
                    LiquidMotion.tween(
                        performance,
                        LiquidMotion.FastDurationMillis,
                        LiquidMotion.EmphasizedDecelerate,
                    )
                ) togetherWith
                    fadeOut(
                        LiquidMotion.tween(
                            performance,
                            110,
                            LiquidMotion.EmphasizedAccelerate,
                        )
                    ) using SizeTransform(clip = false)
            }
        }

    @Composable
    fun monthGridTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<Int>.() -> ContentTransform =
        remember(performance) {
            {
                val direction = if (targetState >= initialState) 1 else -1
                val enterDuration = LiquidMotion.FastDurationMillis
                val exitDuration = 120
                (slideInHorizontally(
                    animationSpec =
                        LiquidMotion.tween(
                            performance,
                            enterDuration,
                            LiquidMotion.EmphasizedDecelerate,
                        ),
                    initialOffsetX = { width -> direction * (width / 6).coerceAtLeast(16) },
                ) +
                    fadeIn(
                        LiquidMotion.tween(
                            performance,
                            enterDuration,
                            LiquidMotion.EmphasizedDecelerate,
                        )
                    )) togetherWith
                    (slideOutHorizontally(
                        animationSpec =
                            LiquidMotion.tween(
                                performance,
                                exitDuration,
                                LiquidMotion.EmphasizedAccelerate,
                            ),
                        targetOffsetX = { width -> -direction * (width / 8).coerceAtLeast(12) },
                    ) +
                        fadeOut(
                            LiquidMotion.tween(
                                performance,
                                exitDuration,
                                LiquidMotion.EmphasizedAccelerate,
                            )
                        )) using SizeTransform(clip = false)
            }
        }
}
