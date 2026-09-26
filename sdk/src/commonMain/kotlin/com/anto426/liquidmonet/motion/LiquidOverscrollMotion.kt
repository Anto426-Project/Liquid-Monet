package com.anto426.liquidmonet.motion

import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign
import kotlinx.coroutines.CoroutineScope

/**
 * Shared edge feedback for lazy lists. Scrolling and fling remain owned by the scroll container.
 */
@Composable
fun rememberLiquidOverscrollEffect(
    orientation: Orientation,
    enabled: Boolean = true,
): OverscrollEffect? {
    val performance = rememberUpdatedState(LocalLiquidGlassPerformance.current)
    val scope = rememberCoroutineScope()
    val limit = with(LocalDensity.current) { 40.dp.toPx() }
    val effect =
        remember(orientation, scope, limit) {
            LiquidOverscrollMotion(orientation, limit, scope) { performance.value }
        }
    return effect.takeIf { enabled && performance.value.motionScale > 0f }
}

internal class LiquidOverscrollMotion(
    private val orientation: Orientation,
    private val limit: Float,
    scope: CoroutineScope,
    private val performance: () -> LiquidGlassPerformanceState,
) : OverscrollEffect {
    private val drag = LiquidElasticDrag(scope, limit * 8f) { liquidRubberBand(it, limit) }
    internal val displacement: Float
        get() = drag.offset

    override val isInProgress: Boolean
        get() = abs(displacement) > 0.5f

    private fun Offset.axis(): Float = if (orientation == Orientation.Vertical) y else x

    private fun axis(value: Float): Offset =
        if (orientation == Orientation.Vertical) Offset(0f, value) else Offset(value, 0f)

    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        if (performance().motionScale <= 0f) return performScroll(delta)
        val incoming = delta.axis()
        val returning =
            if (source == NestedScrollSource.UserInput && incoming * drag.rawOffset < 0f) {
                sign(incoming) * min(abs(incoming), abs(drag.rawOffset))
            } else 0f
        if (returning != 0f) drag.dragBy(returning)
        val remaining = delta - axis(returning)
        val consumed = performScroll(remaining)
        val edge = (remaining - consumed).axis()
        if (source == NestedScrollSource.UserInput && edge != 0f) {
            drag.dragBy(edge)
            return axis(returning + edge) + consumed
        }
        return axis(returning) + consumed
    }

    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        try {
            performFling(velocity)
        } finally {
            drag.release(LiquidDragMotion.panelReturn(performance()))
        }
    }

    override val node: DelegatableNode =
        object : Modifier.Node(), LayoutModifierNode {
            override fun MeasureScope.measure(
                measurable: Measurable,
                constraints: Constraints,
            ): MeasureResult {
                val child = measurable.measure(constraints)
                return layout(child.width, child.height) {
                    child.placeWithLayer(0, 0) {
                        val amount = displacement
                        val stretch = (abs(amount) / limit).coerceIn(0f, 1f)
                        if (orientation == Orientation.Vertical) {
                            translationY = amount
                            scaleY = 1f + stretch * 0.018f
                            scaleX = 1f - stretch * 0.009f
                            transformOrigin = TransformOrigin(0.5f, if (amount >= 0f) 0f else 1f)
                        } else {
                            translationX = amount
                            scaleX = 1f + stretch * 0.018f
                            scaleY = 1f - stretch * 0.009f
                            transformOrigin = TransformOrigin(if (amount >= 0f) 0f else 1f, 0.5f)
                        }
                    }
                }
            }

            override fun onDetach() {
                drag.reset()
            }
        }
}
