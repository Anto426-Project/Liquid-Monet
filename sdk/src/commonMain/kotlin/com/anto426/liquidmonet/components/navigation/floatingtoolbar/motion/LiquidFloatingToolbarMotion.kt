package com.anto426.liquidmonet.components.navigation.floatingtoolbar.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.components.navigation.floatingtoolbar.LiquidFloatingToolbarOrientation
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidColorAsState

/** Motion owned by LiquidFloatingToolbar; no rendering or application state. */
internal object LiquidFloatingToolbarMotion {
    fun enter(
        performance: LiquidGlassPerformanceState,
        orientation: LiquidFloatingToolbarOrientation,
    ): EnterTransition {
        if (performance.motionScale <= 0f) {
            return fadeIn(LiquidMotion.tween(performance, 0))
        }
        val spatial = LiquidMotion.spatialSpring<androidx.compose.ui.unit.IntOffset>(performance)
        val slide =
            when (orientation) {
                LiquidFloatingToolbarOrientation.Horizontal ->
                    slideInVertically(animationSpec = spatial) { it / 2 }
                LiquidFloatingToolbarOrientation.Vertical ->
                    slideInHorizontally(animationSpec = spatial) { it / 2 }
            }
        return slide +
            fadeIn(LiquidMotion.tween(performance, 180)) +
            scaleIn(
                initialScale = 0.92f,
                animationSpec = LiquidMotion.snappySpring(performance),
            )
    }

    fun exit(
        performance: LiquidGlassPerformanceState,
        orientation: LiquidFloatingToolbarOrientation,
    ): ExitTransition {
        if (performance.motionScale <= 0f) {
            return fadeOut(LiquidMotion.tween(performance, 0))
        }
        val spatial = LiquidMotion.spatialSpring<androidx.compose.ui.unit.IntOffset>(performance)
        val slide =
            when (orientation) {
                LiquidFloatingToolbarOrientation.Horizontal ->
                    slideOutVertically(animationSpec = spatial) { it / 2 }
                LiquidFloatingToolbarOrientation.Vertical ->
                    slideOutHorizontally(animationSpec = spatial) { it / 2 }
            }
        return slide +
            fadeOut(LiquidMotion.tween(performance, 140)) +
            scaleOut(
                targetScale = 0.92f,
                animationSpec = LiquidMotion.snappySpring(performance),
            )
    }

    @Composable
    fun animateColor(
        targetValue: Color,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Color> =
        animateLiquidColorAsState(
            targetValue = targetValue,
            animationSpec = remember(performance) { LiquidMotion.interactiveSpring(performance) },
            label = label,
        )
}
