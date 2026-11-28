package com.anto426.liquidmonet.components.selection.segmentedcontrol.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastCoerceIn
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.DampedDragAnimation
import com.anto426.liquidmonet.motion.LiquidDragMotion
import com.anto426.liquidmonet.motion.createLiquidNavigationSelectionMotion
import com.anto426.liquidmonet.motion.liquidNavigationSelectionLayer
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope

/** Motion owned by LiquidSegmentedControl; mirrors LiquidTabBar's elastic droplet physics. */
internal object LiquidSegmentedControlMotion {
    const val ContentPressedScale: Float = LiquidDragMotion.NavigationContentScale

    fun createSelectionMotion(
        scope: CoroutineScope,
        performance: () -> LiquidGlassPerformanceState,
        initialIndex: Int,
        itemCount: Int,
        onDragStarted: DampedDragAnimation.(Offset) -> Unit = {},
        onDragStopped: DampedDragAnimation.() -> Unit = {},
        onDragCancelled: DampedDragAnimation.() -> Unit = {},
        onDrag: DampedDragAnimation.(IntSize, Offset) -> Unit = { _, _ -> },
    ): DampedDragAnimation =
        createLiquidNavigationSelectionMotion(
            scope = scope,
            performance = performance,
            initialIndex = initialIndex,
            itemCount = itemCount,
            onDragStarted = onDragStarted,
            onDragStopped = onDragStopped,
            onDragCancelled = onDragCancelled,
            onDrag = onDrag,
        )

    fun dropletInteractionProgress(motion: DampedDragAnimation): Float {
        val velocityProgress = (abs(motion.velocity) / 4f).fastCoerceIn(0f, 1f)
        return maxOf(motion.pressProgress, velocityProgress).fastCoerceIn(0f, 1f)
    }

    fun panelReturnSpec(performance: LiquidGlassPerformanceState): FiniteAnimationSpec<Float> =
        LiquidDragMotion.panelReturn(performance)

    fun GraphicsLayerScope.applyDropletLayer(motion: DampedDragAnimation) {
        liquidNavigationSelectionLayer(motion)
    }
}
