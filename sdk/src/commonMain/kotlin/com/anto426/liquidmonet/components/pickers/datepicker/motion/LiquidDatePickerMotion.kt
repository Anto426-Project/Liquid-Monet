package com.anto426.liquidmonet.components.pickers.datepicker.motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState

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
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance) {
            {
                fadeIn(
                    LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)
                ) togetherWith
                    fadeOut(LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis))
            }
        }
}
