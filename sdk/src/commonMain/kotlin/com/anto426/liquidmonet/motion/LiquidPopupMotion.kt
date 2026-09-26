package com.anto426.liquidmonet.motion

import androidx.compose.animation.*
import androidx.compose.ui.graphics.TransformOrigin
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState

/** Shared placement-aware popup transitions, used by hosted overlays and dropdown menus. */
object LiquidPopupMotion {
    /**
     * Canonical Dropdown Menu entrance transition with a pronounced, organic liquid bounce effect.
     * Scales rapidly from 84% with a spring overshoot and bouncy slide before settling gracefully.
     */
    fun enter(
        performance: LiquidGlassPerformanceState = LiquidGlassPerformanceState.Fallback,
        transformOrigin: TransformOrigin = TransformOrigin(0.92f, 0.04f),
        isAbove: Boolean = false,
    ): EnterTransition {
        val slideOffset = if (isAbove) 16 else -16
        return scaleIn(
            animationSpec = LiquidMotion.menuBounceSpring(performance),
            initialScale = 0.84f,
            transformOrigin = transformOrigin,
        ) +
            slideInVertically(
                animationSpec = LiquidMotion.menuBounceSpring(performance),
                initialOffsetY = { (slideOffset * (it / 100).coerceAtLeast(1)).coerceIn(-24, 24) },
            ) +
            fadeIn(
                animationSpec =
                    LiquidMotion.tween(
                        performance,
                        LiquidMotion.FastDurationMillis,
                        LiquidMotion.EmphasizedDecelerate,
                    )
            )
    }

    /** Canonical Dropdown Menu exit transition. Smoothly collapses with snappy dissipation. */
    fun exit(
        performance: LiquidGlassPerformanceState = LiquidGlassPerformanceState.Fallback,
        transformOrigin: TransformOrigin = TransformOrigin(0.92f, 0.04f),
        isAbove: Boolean = false,
    ): ExitTransition {
        val slideOffset = if (isAbove) 16 else -16
        return scaleOut(
            animationSpec =
                LiquidMotion.spring(
                    performance,
                    LiquidMotion.SnappyDampingRatio,
                    LiquidMotion.SnappyStiffness,
                ),
            targetScale = 0.80f,
            transformOrigin = transformOrigin,
        ) +
            slideOutVertically(
                animationSpec =
                    LiquidMotion.tween(performance, 140, LiquidMotion.EmphasizedAccelerate),
                targetOffsetY = { (slideOffset * (it / 100).coerceAtLeast(1)).coerceIn(-28, 28) },
            ) +
            fadeOut(
                animationSpec =
                    LiquidMotion.tween(performance, 140, LiquidMotion.EmphasizedAccelerate)
            )
    }
}
