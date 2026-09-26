package com.anto426.liquidmonet.components.feedback.dialog.motion

import androidx.compose.animation.*
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

internal object LiquidDialogMotion {
    fun enter(performance: LiquidGlassPerformanceState): EnterTransition =
        fadeIn(LiquidMotion.tween(performance, 180))

    fun exit(performance: LiquidGlassPerformanceState): ExitTransition =
        fadeOut(LiquidMotion.tween(performance, 140))

    fun panelEnter(performance: LiquidGlassPerformanceState): EnterTransition =
        scaleIn(
            animationSpec =
                LiquidMotion.spring(
                    performance,
                    LiquidMotion.SpatialDampingRatio,
                    LiquidMotion.SpatialStiffness,
                ),
            initialScale = 0.92f,
        ) +
            fadeIn(
                animationSpec =
                    LiquidMotion.tween(
                        performance,
                        LiquidMotion.StandardDurationMillis,
                        LiquidMotion.EmphasizedDecelerate,
                    )
            )

    fun panelExit(performance: LiquidGlassPerformanceState): ExitTransition =
        scaleOut(
            animationSpec =
                LiquidMotion.spring(
                    performance,
                    LiquidMotion.SnappyDampingRatio,
                    LiquidMotion.SnappyStiffness,
                ),
            targetScale = 0.96f,
        ) +
            fadeOut(
                animationSpec =
                    LiquidMotion.tween(
                        performance,
                        LiquidMotion.FastDurationMillis,
                        LiquidMotion.EmphasizedAccelerate,
                    )
            )
}
