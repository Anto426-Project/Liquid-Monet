package com.anto426.liquidmonet.components.internal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPreset
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.kyant.backdrop.Backdrop

/** Creates the shared press/highlight state at the control call site. */
@Composable
internal fun rememberLiquidControlHighlight(): InteractiveHighlight {
    val animationScope = rememberCoroutineScope()
    val performance = rememberUpdatedState(LocalLiquidGlassPerformance.current)
    return remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope, performance = { performance.value })
    }
}

/** Place before the surface so glass, content and highlight share one elastic layer. */
@Composable
internal fun Modifier.liquidControlInteractive(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
    shape: Shape,
    role: Role = Role.Button,
    stretchFactor: Float = LiquidControlDefaults.deformationFactor,
    translationFactor: Float = LiquidControlDefaults.deformationFactor,
    interactionSource: MutableInteractionSource? = null,
    backdrop: Backdrop? = null,
    containerColor: Color? = null,
    preset: LiquidGlassPreset? = null,
    onClick: () -> Unit,
): Modifier {
    val currentOnClick = rememberUpdatedState(onClick)
    val interaction =
        remember(
            enabled,
            interactiveHighlight,
            shape,
            role,
            stretchFactor,
            translationFactor,
            interactionSource,
        ) {
            val layer =
                liquidControlLayerBlock(
                    enabled,
                    interactiveHighlight,
                    stretchFactor,
                    translationFactor,
                )
            Modifier.clickable(
                    enabled = enabled,
                    role = role,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { currentOnClick.value() },
                )
                .then(if (enabled) interactiveHighlight.gestureModifier else Modifier)
                .then(if (layer != null) Modifier.graphicsLayer(layer) else Modifier)
                .then(if (enabled) interactiveHighlight.modifier(clipShape = shape) else Modifier)
        }
    val control = then(interaction)
    return if (backdrop == null) control
    else
        control.liquidGlass(
            backdrop = backdrop,
            shape = shape,
            role = LiquidGlassRole.Control,
            containerColor = containerColor,
            preset = preset,
            interactive = enabled,
        )
}

/** Adds the single press gesture and optical highlight used by Liquid controls. */
internal fun Modifier.liquidControlPressFeedback(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
    shape: Shape? = null,
    drawHighlightOverlay: Boolean = true,
    highlightColor: Color = Color.Unspecified,
): Modifier {
    if (!enabled) return this

    val interactiveModifier = this
    return if (drawHighlightOverlay) {
        interactiveModifier
            .then(interactiveHighlight.modifier(highlightColor, shape))
            .then(interactiveHighlight.gestureModifier)
    } else {
        interactiveModifier.then(interactiveHighlight.gestureModifier)
    }
}
