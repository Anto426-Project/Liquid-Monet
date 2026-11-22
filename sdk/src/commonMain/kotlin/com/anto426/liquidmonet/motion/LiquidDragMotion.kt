package com.anto426.liquidmonet.motion

import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState

/**
 * Elastic control physics adapted from Kyant0/AndroidLiquidGlass's catalog (Apache-2.0).
 * Keep tracking critically damped and deformation bouncy; one spring for both loses that feel.
 */
internal object LiquidDragMotion {
    const val NavigationPressedScale = 78f / 56f
    const val NavigationContentScale = 1.2f

    fun tracking(performance: LiquidGlassPerformanceState, threshold: Float) =
        LiquidMotion.spring(performance, 1f, 1000f, threshold)

    fun selectionGlide(performance: LiquidGlassPerformanceState, threshold: Float) =
        LiquidMotion.spring(performance, 0.88f, 320f, threshold)

    fun toggleGlide(performance: LiquidGlassPerformanceState, threshold: Float) =
        LiquidMotion.spring(performance, 0.85f, 420f, threshold)

    fun velocity(performance: LiquidGlassPerformanceState, threshold: Float) =
        LiquidMotion.spring(performance, 0.5f, 300f, threshold)

    fun scaleX(performance: LiquidGlassPerformanceState) =
        LiquidMotion.spring(performance, 0.6f, 250f, 0.001f)

    fun scaleY(performance: LiquidGlassPerformanceState) =
        LiquidMotion.spring(performance, 0.7f, 250f, 0.001f)

    fun panelReturn(performance: LiquidGlassPerformanceState) =
        LiquidMotion.spring(performance, 1f, 300f, 0.5f)
}
