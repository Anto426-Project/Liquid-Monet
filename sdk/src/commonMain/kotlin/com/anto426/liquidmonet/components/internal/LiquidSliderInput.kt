package com.anto426.liquidmonet.components.internal

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun LiquidSliderInput(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    interactionSource: MutableInteractionSource,
)

@Composable
internal expect fun LiquidRangeSliderInput(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    startInteractionSource: MutableInteractionSource,
    endInteractionSource: MutableInteractionSource,
)
