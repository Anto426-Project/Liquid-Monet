package com.anto426.liquidmonet.components.display.iconcontainer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.shapes.RoundedRectangle

/**
 * Styles a regular Compose `Icon` as a compact Liquid icon container.
 *
 * Callers keep ownership of icon semantics and tint, while standard Liquid components apply one
 * consistent container geometry. Pass [contentPadding] directly for asymmetric insets, or let it
 * derive automatically from [containerSize] and [iconSize].
 */
@Composable
fun Modifier.liquidIconContainer(
    containerSize: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    containerColor: Color = Color.Unspecified,
    shape: Shape = RoundedRectangle(12.dp),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = PaddingValues(((containerSize - iconSize) / 2f).coerceAtLeast(0.dp)),
): Modifier {
    require(containerSize > 0.dp) { "Liquid icon containerSize must be positive." }
    require(iconSize > 0.dp) { "Liquid icon iconSize must be positive." }
    val fill =
        if (containerColor.isSpecified) containerColor
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    return this.size(containerSize)
        .clip(shape)
        .background(fill)
        .then(if (border != null) Modifier.border(border, shape) else Modifier)
        .padding(contentPadding)
}
