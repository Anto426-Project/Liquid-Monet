package com.anto426.liquidmonet.components.buttons.button.motion

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState

/** Motion owned by LiquidButton; no rendering or application state. */
internal object LiquidButtonMotion {
    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue,
            remember(performance) { LiquidMotion.tween(performance, 220) },
            label,
        )

    @Composable
    fun <T> contentTransition(
        performance: LiquidGlassPerformanceState
    ): AnimatedContentTransitionScope<T>.() -> ContentTransform =
        remember(performance) {
            {
                (scaleIn(
                        LiquidMotion.spring(
                            performance = performance,
                            dampingRatio = 0.72f,
                            stiffness = 420f,
                        ),
                        initialScale = 0.65f,
                    ) + fadeIn(LiquidMotion.tween(performance, 180)))
                    .togetherWith(
                        scaleOut(
                            LiquidMotion.spring(
                                performance = performance,
                                dampingRatio = 0.85f,
                                stiffness = 480f,
                            ),
                            targetScale = 0.65f,
                        ) + fadeOut(LiquidMotion.tween(performance, 140))
                    )
            }
        }
}
