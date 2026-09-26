package com.anto426.liquidmonet.components.inputs.otpinput.motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidDpAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidOtpInput; no rendering or application state. */
internal object LiquidOtpInputMotion {
    @Composable
    fun animateColorPrimary(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween(performance, 200) },
            label,
        )

    @Composable
    fun animateColorSecondary(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween(performance, 180) },
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
                LiquidMotion.spring(
                    performance = performance,
                    dampingRatio = 0.62f,
                    stiffness = 440f,
                )
            },
            label,
        )

    @Composable
    fun animateDpPrimary(
        targetValue: Dp,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Dp> =
        animateLiquidDpAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.spring(
                    performance = performance,
                    dampingRatio = 0.72f,
                    stiffness = 420f,
                )
            },
            label,
        )

    @Composable
    fun animateDpSecondary(
        targetValue: Dp,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Dp> =
        animateLiquidDpAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween(performance, 160) },
            label,
        )

    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance) {
            {
                (scaleIn(
                        animationSpec =
                            LiquidMotion.spring(
                                performance = performance,
                                dampingRatio = 0.55f,
                                stiffness = 520f,
                            ),
                        initialScale = 0.35f,
                    ) + fadeIn(LiquidMotion.tween(performance, 120)))
                    .togetherWith(
                        scaleOut(
                            animationSpec =
                                LiquidMotion.tween(
                                    performance,
                                    100,
                                ),
                            targetScale = 1.35f,
                        ) + fadeOut(LiquidMotion.tween(performance, 90))
                    )
            }
        }
}
