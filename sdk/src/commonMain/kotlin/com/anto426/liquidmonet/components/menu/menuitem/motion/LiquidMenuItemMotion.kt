package com.anto426.liquidmonet.components.menu.menuitem.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState

/** Motion owned by LiquidMenuItem; no rendering or application state. */
internal object LiquidMenuItemMotion {
    @Composable
    fun animateFloatPrimary(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
        isActive: Boolean,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.spring(
                    performance = performance,
                    dampingRatio = if (isActive) LiquidMotion.PressDampingRatio else 0.52f,
                    stiffness = if (isActive) LiquidMotion.PressStiffness else 360f,
                )
            },
            label,
        )

    @Composable
    fun animateFloatSecondary(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) {
                LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)
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
            remember(performance) {
                LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)
            },
            label,
        )
}
