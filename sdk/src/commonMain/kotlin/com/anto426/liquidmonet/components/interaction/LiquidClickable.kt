package com.anto426.liquidmonet.components.interaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import com.anto426.liquidmonet.components.internal.motion.LiquidCardMotion
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayer
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
): Modifier = liquidClickable(
    shape = shape,
    enabled = enabled,
    role = role,
    clipContent = clipContent,
    interactionSource = interactionSource,
    cardMotion = null,
    onClick = onClick,
)

@Composable
internal fun Modifier.liquidClickable(
    shape: Shape,
    enabled: Boolean,
    role: Role = Role.Button,
    clipContent: Boolean,
    interactionSource: MutableInteractionSource? = null,
    cardMotion: LiquidCardMotion?,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)? = null,
): Modifier {
    val contentClip = if (clipContent) Modifier.clip(shape) else Modifier
    val action = if (onLongClick == null) {
        if (onClick == null) return then(contentClip)
        clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            role = role,
            onClick = onClick,
        )
    } else {
        combinedClickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null,
            role = role,
            hapticFeedbackEnabled = false,
            onClick = onClick ?: {},
            onLongClick = onLongClick,
        )
    }
    val highlight = rememberLiquidControlHighlight()
    return action
        .then(if (enabled) highlight.ownedGestureModifier() else Modifier)
        .liquidControlLayer(enabled, highlight, cardMotion)
        .then(contentClip)
        .then(if (enabled) highlight.modifier(clipShape = shape) else Modifier)
}
