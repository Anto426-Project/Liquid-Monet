package com.anto426.liquidmonet.components.feedback.banner.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

/** Motion owned by LiquidBanner; no rendering or application state. */
internal object LiquidBannerMotion {
    fun enter(performance: LiquidGlassPerformanceState): EnterTransition {
        if (performance.motionScale <= 0f) {
            return fadeIn(LiquidMotion.tween(performance, 0))
        }
        return expandVertically(LiquidMotion.spatialSpring(performance)) +
            fadeIn(LiquidMotion.tween(performance, 180))
    }

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition {
        if (performance.motionScale <= 0f) {
            return fadeOut(LiquidMotion.tween(performance, 0))
        }
        return shrinkVertically(LiquidMotion.spatialSpring(performance)) +
            fadeOut(LiquidMotion.tween(performance, 140))
    }
}
