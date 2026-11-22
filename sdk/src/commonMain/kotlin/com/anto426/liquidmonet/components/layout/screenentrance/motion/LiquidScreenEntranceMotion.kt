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
        val enterDistancePx = with(LocalDensity.current) { 18.dp.roundToPx() }
        val exitDistancePx = with(LocalDensity.current) { 12.dp.roundToPx() }
        return remember(performance, animation, delayMillis, enterDistancePx, exitDistancePx) {
            if (!motionEnabled(performance, animation)) {
                LiquidScreenEntranceTransitions(EnterTransition.None, ExitTransition.None)
            } else {
                val fadeEnter = fadeIn(
                    animationSpec = LiquidMotion.tween(
                        performance = performance,
                        durationMillis = 240,
                        delayMillis = delayMillis,
                        easing = LiquidMotion.EmphasizedDecelerate,
                    )
                )
                val fadeExit = fadeOut(
                    animationSpec = LiquidMotion.tween(
                        performance = performance,
                        durationMillis = 130,
                        easing = LiquidMotion.EmphasizedAccelerate,
                    )
                )
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
                                animationSpec = LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 300,
                                    delayMillis = delayMillis,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                ),
                                initialOffsetY = { direction * enterDistancePx },
                            ) + fadeEnter,
                            slideOutVertically(
                                animationSpec = LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 140,
                                    easing = LiquidMotion.EmphasizedAccelerate,
                                ),
                                targetOffsetY = { direction * exitDistancePx },
                            ) + fadeExit,
                        )
                    }
                    LiquidScreenEntranceAnimation.Scale ->
                        LiquidScreenEntranceTransitions(
                            scaleIn(
                                animationSpec = LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 300,
                                    delayMillis = delayMillis,
                                    easing = LiquidMotion.EmphasizedDecelerate,
                                ),
                                initialScale = 0.95f,
                            ) + fadeEnter,
                            scaleOut(
                                animationSpec = LiquidMotion.tween(
                                    performance = performance,
                                    durationMillis = 140,
                                    easing = LiquidMotion.EmphasizedAccelerate,
                                ),
                                targetScale = 0.96f,
                            ) + fadeExit,
                        )
                }
            }
        }
    }
}
