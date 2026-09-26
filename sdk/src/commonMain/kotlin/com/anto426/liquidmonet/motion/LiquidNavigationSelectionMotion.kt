package com.anto426.liquidmonet.motion

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.IntSize
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope

/** One motion contract for navigation tabs and page selections. */
internal fun createLiquidNavigationSelectionMotion(
    scope: CoroutineScope,
    performance: () -> LiquidGlassPerformanceState,
    initialIndex: Int,
    itemCount: Int,
    onDragStarted: DampedDragAnimation.(Offset) -> Unit = {},
    onDragStopped: DampedDragAnimation.() -> Unit = {},
    onDragCancelled: DampedDragAnimation.() -> Unit = {},
    onDrag: DampedDragAnimation.(IntSize, Offset) -> Unit = { _, _ -> },
): DampedDragAnimation =
    DampedDragAnimation(
        animationScope = scope,
        performance = performance,
        initialValue = initialIndex.coerceIn(0, itemCount.coerceAtLeast(1) - 1).toFloat(),
        valueRange = 0f..(itemCount.coerceAtLeast(1) - 1).toFloat(),
        visibilityThreshold = 0.001f,
        initialScale = 1f,
        pressedScale = LiquidDragMotion.NavigationPressedScale,
        onDragStarted = onDragStarted,
        onDragStopped = onDragStopped,
        onDragCancelled = onDragCancelled,
        onDrag = onDrag,
    )

private fun navigationSpeed(motion: DampedDragAnimation): Float =
    (abs(motion.velocity / 10f) * 0.75f).coerceIn(0f, 0.2f)

internal fun liquidNavigationSelectionScaleX(motion: DampedDragAnimation): Float =
    motion.scaleX / (1f - navigationSpeed(motion))

internal fun liquidNavigationSelectionScaleY(motion: DampedDragAnimation): Float =
    motion.scaleY * (1f - navigationSpeed(motion) * 0.33f)

internal fun GraphicsLayerScope.liquidNavigationSelectionLayer(motion: DampedDragAnimation) {
    scaleX = liquidNavigationSelectionScaleX(motion)
    scaleY = liquidNavigationSelectionScaleY(motion)
}
