package com.anto426.liquidmonet.glass.background.motion

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState

/** Motion owned by LiquidBackground; no rendering or application state. */
internal object LiquidBackgroundMotion {
    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween<Color>(performance, 850) },
            label,
        )

    @Composable
    fun rememberP1(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
        normalizedSpeed: Float,
    ): State<Float> =
        transition.animateFloat(
            initialValue,
            targetValue,
            remember(normalizedSpeed) {
                infiniteRepeatable(
                    animation =
                        tween(
                            (22000 / normalizedSpeed).toInt().coerceAtLeast(100),
                            easing = LinearEasing,
                        ),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )

    @Composable
    fun rememberP2(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
        normalizedSpeed: Float,
    ): State<Float> =
        transition.animateFloat(
            initialValue,
            targetValue,
            remember(normalizedSpeed) {
                infiniteRepeatable(
                    animation =
                        tween(
                            (29000 / normalizedSpeed).toInt().coerceAtLeast(100),
                            easing = LinearEasing,
                        ),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )

    @Composable
    fun rememberP3(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
        normalizedSpeed: Float,
    ): State<Float> =
        transition.animateFloat(
            initialValue,
            targetValue,
            remember(normalizedSpeed) {
                infiniteRepeatable(
                    animation =
                        tween(
                            (37000 / normalizedSpeed).toInt().coerceAtLeast(100),
                            easing = LinearEasing,
                        ),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )

    @Composable
    fun rememberP4(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
        normalizedSpeed: Float,
    ): State<Float> =
        transition.animateFloat(
            initialValue,
            targetValue,
            remember(normalizedSpeed) {
                infiniteRepeatable(
                    animation =
                        tween(
                            (17000 / normalizedSpeed).toInt().coerceAtLeast(100),
                            easing = LinearEasing,
                        ),
                    repeatMode = RepeatMode.Restart,
                )
            },
            label = label,
        )
}
