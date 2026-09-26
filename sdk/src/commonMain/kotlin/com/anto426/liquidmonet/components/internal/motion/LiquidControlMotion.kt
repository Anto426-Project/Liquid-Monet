package com.anto426.liquidmonet.components.internal.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.ui.geometry.Offset
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion

/** Shared motion rules for press-driven Liquid controls. */
internal object LiquidControlMotion {
    fun pressProgress(
        isPressed: Boolean,
        performance: LiquidGlassPerformanceState,
    ): AnimationSpec<Float> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio =
                if (isPressed) LiquidMotion.PressDampingRatio else LiquidMotion.SnappyDampingRatio,
            stiffness =
                if (isPressed) LiquidMotion.PressStiffness else LiquidMotion.SnappyStiffness,
            visibilityThreshold = 0.001f,
        )

    fun pointerPosition(performance: LiquidGlassPerformanceState): AnimationSpec<Offset> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = LiquidMotion.SnappyDampingRatio,
            stiffness = LiquidMotion.SnappyStiffness,
            visibilityThreshold = Offset.VisibilityThreshold,
        )
}
