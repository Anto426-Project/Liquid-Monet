package com.anto426.liquidmonet.components.interaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayer
import com.anto426.liquidmonet.components.internal.motion.rememberLiquidOwnedPressInteractionSource
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight

/**
 * Applies the same elastic press and drag response as [com.anto426.liquidmonet.components.cards.card.LiquidCard].
 * Only the element that owns the tap animates, so nested controls stay independent. [shape]
 * bounds the press highlight. Set [clipContent] to clip content to the moving shape as well.
 * A null [onClick] leaves this element available as a container for its own controls.
 */
@Composable
fun Modifier.liquidClickable(
    shape: Shape,
    enabled: Boolean = true,
    role: Role = Role.Button,
    clipContent: Boolean = false,
    interactionSource: MutableInteractionSource? = null,
    onClick: (() -> Unit)?,
): Modifier {
    val contentClip = if (clipContent) Modifier.clip(shape) else Modifier
    if (onClick == null) return then(contentClip)

    val highlight = rememberLiquidControlHighlight()
    val source = rememberLiquidOwnedPressInteractionSource(enabled, highlight, interactionSource)
    return liquidControlLayer(enabled, highlight)
        .then(contentClip)
        .then(
            if (enabled) highlight.modifier(clipShape = shape).then(highlight.pointerTrackingModifier)
            else Modifier,
        )
        .clickable(
            enabled = enabled,
            interactionSource = source,
            indication = null,
            role = role,
            onClick = onClick,
        )
}
