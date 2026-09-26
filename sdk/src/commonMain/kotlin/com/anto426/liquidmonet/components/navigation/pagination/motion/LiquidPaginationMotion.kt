package com.anto426.liquidmonet.components.navigation.pagination.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.DampedDragAnimation
import com.anto426.liquidmonet.motion.createLiquidNavigationSelectionMotion

internal object LiquidPaginationMotion {
    @Composable
    fun rememberIndicator(
        page: Int,
        pageCount: Int,
        performance: LiquidGlassPerformanceState,
    ): DampedDragAnimation {
        val scope = rememberCoroutineScope()
        val currentPerformance = rememberUpdatedState(performance)
        val motion =
            remember(scope, pageCount) {
                createLiquidNavigationSelectionMotion(
                    scope,
                    { currentPerformance.value },
                    page,
                    pageCount,
                )
            }
        LaunchedEffect(page, motion) {
            if (motion.targetValue != page.toFloat()) motion.animateToValue(page.toFloat())
        }
        return motion
    }
}
