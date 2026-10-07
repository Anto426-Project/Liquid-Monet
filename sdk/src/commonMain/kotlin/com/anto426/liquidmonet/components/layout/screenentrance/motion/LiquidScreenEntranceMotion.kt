package com.anto426.liquidmonet.components.layout.screenentrance.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal data class LiquidScreenEntranceTransitions(
    val enter: EnterTransition,
    val exit: ExitTransition,
)

internal object LiquidScreenEntranceMotion {
    fun motionEnabled(
        performance: LiquidGlassPerformanceState,
        animation: LiquidScreenEntranceAnimation,
    ): Boolean =
        animation != LiquidScreenEntranceAnimation.None &&
            !(performance.motionScale.isFinite() && performance.motionScale <= 0f)

    @Composable
    fun transitions(
        performance: LiquidGlassPerformanceState,
        animation: LiquidScreenEntranceAnimation,
        delayMillis: Int,
    ): LiquidScreenEntranceTransitions {
        val distancePx = with(LocalDensity.current) { 24.dp.roundToPx() }
        return remember(performance, animation, delayMillis, distancePx) {
            if (!motionEnabled(performance, animation)) {
                LiquidScreenEntranceTransitions(EnterTransition.None, ExitTransition.None)
            } else {
                val fadeEnter = fadeIn(
                    animationSpec = LiquidMotion.tween(performance, 280, delayMillis = delayMillis)
                )
                val fadeExit = fadeOut(animationSpec = LiquidMotion.tween(performance, 140))
                when (animation) {
                    LiquidScreenEntranceAnimation.None ->
                        LiquidScreenEntranceTransitions(EnterTransition.None, ExitTransition.None)
                    LiquidScreenEntranceAnimation.Fade ->
                        LiquidScreenEntranceTransitions(fadeEnter, fadeExit)
                    LiquidScreenEntranceAnimation.FadeUp,
                    LiquidScreenEntranceAnimation.FadeDown -> {
                        val direction = if (animation == LiquidScreenEntranceAnimation.FadeUp) 1 else -1
                        LiquidScreenEntranceTransitions(
                            slideInVertically(
                                animationSpec = LiquidMotion.tween(performance, 320, delayMillis = delayMillis),
                                initialOffsetY = { direction * distancePx },
                            ) + fadeEnter,
                            slideOutVertically(
                                animationSpec = LiquidMotion.tween(performance, 140),
                                targetOffsetY = { direction * distancePx },
                            ) + fadeExit,
                        )
                    }
                    LiquidScreenEntranceAnimation.Scale ->
                        LiquidScreenEntranceTransitions(
                            scaleIn(
                                animationSpec = LiquidMotion.tween(performance, 320, delayMillis = delayMillis),
                                initialScale = 0.94f,
                            ) + fadeEnter,
                            scaleOut(
                                animationSpec = LiquidMotion.tween(performance, 140),
                                targetScale = 0.94f,
                            ) + fadeExit,
                        )
                }
            }
        }
    }
}
