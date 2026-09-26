package com.anto426.liquidmonet.components.selection.slider

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidGlassSlider
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import kotlin.math.roundToInt

/**
 * Canonical liquid-glass slider.
 *
 * [steps] follows the Material convention: zero is continuous; a positive value is the number of
 * selectable positions between the two endpoints.
 */
@Composable
fun LiquidSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    trackHeight: Dp = 8.dp,
    thumbSize: Dp = 24.dp,
    tint: Color = Color.Unspecified,
    backdropState: Backdrop = emptyBackdrop(),
) {
    requireLiquidSliderRange(valueRange)
    require(steps >= 0) { "LiquidSlider steps must be zero or positive." }
    require(value.isFinite()) { "LiquidSlider value must be finite." }
    LiquidInputNormalization.positive(trackHeight, "LiquidSlider trackHeight")
    LiquidInputNormalization.positive(thumbSize, "LiquidSlider thumbSize")
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    LiquidGlassSlider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps,
        backdrop = effectiveBackdrop,
        modifier = modifier,
        enabled = enabled,
        tint = tint,
        trackHeight = trackHeight,
        thumbSize = thumbSize,
    )
}

internal fun requireLiquidSliderRange(valueRange: ClosedFloatingPointRange<Float>) {
    require(
        valueRange.start.isFinite() &&
            valueRange.endInclusive.isFinite() &&
            valueRange.endInclusive > valueRange.start
    ) {
        "LiquidSlider valueRange must contain two finite, ordered endpoints."
    }
}

internal fun snapLiquidSliderValue(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
): Float {
    val start = valueRange.start
    val end = valueRange.endInclusive
    val boundedValue = if (value.isFinite()) value.coerceIn(start, end) else start
    if (steps <= 0) return boundedValue

    val intervalCount = steps.toFloat() + 1f
    val stepSize = (end - start) / intervalCount
    if (!stepSize.isFinite() || stepSize <= 0f) return boundedValue

    return (start + ((boundedValue - start) / stepSize).roundToInt() * stepSize).coerceIn(
        start,
        end,
    )
}
