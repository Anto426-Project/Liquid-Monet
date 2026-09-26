package com.anto426.liquidmonet.components.display.swipetodismiss.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidDragMotion
import com.anto426.liquidmonet.motion.LiquidElasticDrag
import com.anto426.liquidmonet.motion.liquidRubberBand
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.CoroutineScope

internal class LiquidSwipeMotion(
    scope: CoroutineScope,
    threshold: Float,
    maximumDrag: Float,
    private val performance: () -> LiquidGlassPerformanceState,
) {
    private val drag =
        LiquidElasticDrag(scope, maximumDrag * 8f) { raw ->
            if (abs(raw) <= threshold) raw
            else {
                val limit = maximumDrag - threshold
                sign(raw) *
                    (threshold +
                        if (limit > 0f) abs(liquidRubberBand(abs(raw) - threshold, limit)) else 0f)
            }
        }
    val offset: Float
        get() = drag.offset

    val motionEnabled: Boolean
        get() = performance().motionScale > 0f

    fun begin() = drag.dragBy(0f)

    fun move(delta: Float, allowLeft: Boolean, allowRight: Boolean) {
        val restricted =
            (delta < 0f && !allowLeft && offset <= 0f) ||
                (delta > 0f && !allowRight && offset >= 0f)
        drag.dragBy(if (restricted) delta * 0.1f else delta)
    }

    fun release() = drag.release(LiquidDragMotion.panelReturn(performance()))
}

internal object LiquidSwipeToDismissMotion {
    @Composable
    fun rememberSwipe(
        threshold: Float,
        maximumDrag: Float,
        performance: LiquidGlassPerformanceState,
    ): LiquidSwipeMotion {
        val scope = rememberCoroutineScope()
        val currentPerformance = rememberUpdatedState(performance)
        return remember(scope, threshold, maximumDrag) {
            LiquidSwipeMotion(scope, threshold, maximumDrag) { currentPerformance.value }
        }
    }
}
