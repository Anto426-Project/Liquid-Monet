package com.anto426.liquidmonet.components.cards.card.motion

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.components.internal.InteractiveHighlight

/** Animate only presses owned by the card's clickable, never a nested control's gestures. */
@Composable
internal fun rememberLiquidCardInteractionSource(
    enabled: Boolean,
    highlight: InteractiveHighlight,
): MutableInteractionSource {
    val source = remember { MutableInteractionSource() }
    LaunchedEffect(source, enabled, highlight) {
        if (!enabled) return@LaunchedEffect
        var activePress: PressInteraction.Press? = null
        try {
            source.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        activePress = interaction
                        highlight.press(interaction.pressPosition)
                    }
                    is PressInteraction.Release -> if (interaction.press == activePress) {
                        activePress = null
                        highlight.release()
                    }
                    is PressInteraction.Cancel -> if (interaction.press == activePress) {
                        activePress = null
                        highlight.release()
                    }
                }
            }
        } finally {
            highlight.release()
        }
    }
    return source
}
