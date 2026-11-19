package com.anto426.liquidmonet.components.internal

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
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
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
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
    RangeSlider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        startInteractionSource = startInteractionSource,
        endInteractionSource = endInteractionSource,
        startThumb = { Box(Modifier.size(48.dp)) },
        endThumb = { Box(Modifier.size(48.dp)) },
        track = { Box(Modifier.fillMaxWidth().height(1.dp)) },
    )
}
