package com.anto426.liquidmonet.components.pickers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

@Composable
fun rememberLiquidTimePickerState(
    initialHour: Int = 12,
    initialMinute: Int = 0
): LiquidTimePickerState {
    return remember { LiquidTimePickerState(initialHour, initialMinute) }
}

/**
 * LiquidTimePicker - Inline Liquid Glass Time Picker.
 */
@Composable
fun LiquidTimePicker(
    state: LiquidTimePickerState,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop()
) {
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Hours Column
        LiquidTimeNumberColumn(
            value = state.hour,
            onIncrement = { state.incrementHour() },
            onDecrement = { state.decrementHour() },
            label = "Ore",
            backdropState = effectiveBackdrop
        )

        Text(
            text = ":",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // Minutes Column
        LiquidTimeNumberColumn(
            value = state.minute,
            onIncrement = { state.incrementMinute() },
            onDecrement = { state.decrementMinute() },
            label = "Minuti",
            backdropState = effectiveBackdrop
        )
    }
}

@Composable
private fun LiquidTimeNumberColumn(
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    label: String,
    backdropState: Backdrop
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LiquidIconButton(
            icon = LiquidIcons.KeyboardArrowUp,
            onClick = onIncrement,
            backdropState = backdropState
        )

        Box(
            modifier = Modifier
                .size(width = 68.dp, height = 56.dp)
                .liquidGlass(
                    backdrop = backdropState,
                    shape = RoundedRectangle(16.dp),
                    role = LiquidGlassRole.Control,
                    containerColor = LiquidGlassTheme.colors.accentContainer
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.twoDigits(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        LiquidIconButton(
            icon = LiquidIcons.KeyboardArrowDown,
            onClick = onDecrement,
            backdropState = backdropState
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LiquidGlassTheme.colors.secondaryContent
        )
    }
}


/**
 * LiquidTimePickerField - Liquid Glass Time Input Trigger Field.
 */
@Composable
fun LiquidTimePickerField(
    selectedHour: Int?,
    selectedMinute: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Seleziona Ora",
    placeholder: String = "HH:MM",
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop()
) {
    val formattedTime = remember(selectedHour, selectedMinute) {
        if (selectedHour != null && selectedMinute != null) {
            "${selectedHour.twoDigits()}:${selectedMinute.twoDigits()}"
        } else null
    }

    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .liquidGlass(
                backdrop = effectiveBackdrop,
                shape = RoundedRectangle(18.dp),
                role = LiquidGlassRole.Control,
                layerBlock = liquidControlLayerBlock(enabled, interactiveHighlight)
            )
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.Button,
                enabled = enabled,
                onClick = onClick
            )
            .liquidControlPressFeedback(enabled, interactiveHighlight, shape = RoundedRectangle(18.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = LiquidIcons.Time,
                    contentDescription = label,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )

                Column {
                    if (formattedTime != null) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = LiquidGlassTheme.colors.secondaryContent
                        )
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlassTheme.colors.secondaryContent.copy(alpha = 0.72f)
                        )
                    }
                }
            }

            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = LiquidGlassTheme.colors.secondaryContent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
