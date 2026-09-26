package com.anto426.liquidmonet.components.navigation.navigationbar.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidDpAsState

/** Motion owned by LiquidNavigationBar; no rendering or application state. */
internal object LiquidNavigationBarMotion {
    @Composable
    fun animateDp(
        targetValue: Dp,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Dp> =
        animateLiquidDpAsState(
            targetValue,
            remember(performance) { LiquidMotion.spatialSpring(performance) },
            label,
        )
}
