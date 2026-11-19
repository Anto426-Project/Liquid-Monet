package com.anto426.liquidmonet.components.internal

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.RangeSliderState
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal actual fun LiquidSliderInput(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    interactionSource: MutableInteractionSource,
) {
    val state = remember(steps, valueRange) { SliderState(value, steps, valueRange) }
    state.value = value
    Slider(
        state = state,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = { Box(Modifier.size(48.dp)) },
        track = { Box(Modifier.fillMaxWidth().height(1.dp)) },
    )
}

@Composable
internal actual fun LiquidRangeSliderInput(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    startInteractionSource: MutableInteractionSource,
    endInteractionSource: MutableInteractionSource,
) {
    val state = remember(steps, valueRange) {
        RangeSliderState(value.start, value.endInclusive, steps, valueRange)
    }
    // Each setter clamps against the other thumb. Move the leading thumb first.
    if (value.start > state.endValue) {
        state.endValue = value.endInclusive
        state.startValue = value.start
    } else {
        state.startValue = value.start
        state.endValue = value.endInclusive
    }
    RangeSlider(
        state = state,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        startThumbInteractionSource = startInteractionSource,
        endThumbInteractionSource = endInteractionSource,
        startThumb = { Box(Modifier.size(48.dp)) },
        endThumb = { Box(Modifier.size(48.dp)) },
        track = { Box(Modifier.fillMaxWidth().height(1.dp)) },
    )
}
