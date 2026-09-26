package com.anto426.liquidmonet.components.selection.stepper

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/** LiquidStepper / LiquidNumberInput - Optical Liquid Glass Numeric Stepper. */
@Composable
fun LiquidStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    minValue: Int = 0,
    maxValue: Int = 100,
    step: Int = 1,
    unit: String? = null,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    require(maxValue >= minValue) { "LiquidStepper maxValue must not be less than minValue." }
    require(step > 0) { "LiquidStepper step must be greater than zero." }
    val safeValue = value.coerceIn(minValue, maxValue)
    val canDecrement = safeValue.toLong() - step.toLong() >= minValue.toLong()
    val canIncrement = safeValue.toLong() + step.toLong() <= maxValue.toLong()
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = RoundedRectangle(18.dp),
                    role = LiquidGlassRole.Control,
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label != null) {
            Column {
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colorScheme.onSurface,
                )
                if (unit != null) {
                    Text(
                        text = "Unità: $unit",
                        style = MaterialTheme.typography.labelSmall,
                        color = glassColors.secondaryContent,
                    )
                }
            }
        }

        CompositionLocalProvider(
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LiquidIconButton(
                    icon = LiquidIcons.Remove,
                    onClick = {
                        if (canDecrement) {
                            onValueChange(safeValue - step)
                        }
                    },
                    enabled = enabled && canDecrement,
                    backdropState = effectiveBackdrop,
                )

                Box(
                    modifier =
                        Modifier.size(width = 54.dp, height = 40.dp)
                            .clip(RoundedRectangle(12.dp))
                            .background(glassColors.accentContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (unit != null) "$safeValue $unit" else safeValue.toString(),
                        style =
                            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface,
                    )
                }

                LiquidIconButton(
                    icon = LiquidIcons.Add,
                    onClick = {
                        if (canIncrement) {
                            onValueChange(safeValue + step)
                        }
                    },
                    enabled = enabled && canIncrement,
                    backdropState = effectiveBackdrop,
                )
            }
        }
    }
}
