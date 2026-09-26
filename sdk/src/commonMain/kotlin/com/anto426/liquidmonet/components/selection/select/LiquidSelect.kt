package com.anto426.liquidmonet.components.selection.select

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.components.selection.select.motion.LiquidSelectMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * Canonical liquid-glass selection field. It supports both the optional-value dropdown use case and
 * the fully selected picker use case that previously had separate implementations.
 */
@Composable
fun <T> LiquidSelect(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Seleziona",
    itemLabel: (T) -> String = { it.toString() },
    itemSubtitle: ((T) -> String?)? = null,
    itemIcon: ((T) -> ImageVector?)? = null,
    leadingIcon: ImageVector? = null,
    shape: Shape = RoundedRectangle(18.dp),
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val performance = LocalLiquidGlassPerformance.current
    var isExpanded by remember { mutableStateOf(false) }
    val anchorState = rememberLiquidGlassOverlayAnchorState()
    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val chevronRotation by
        LiquidSelectMotion.animateFloat(
            targetValue = if (isExpanded) 180f else 0f,
            performance = performance,
            label = "dropdownChevron",
        )

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .height(56.dp)
                    .liquidGlassOverlayAnchor(anchorState)
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Control,
                        containerColor =
                            if (isExpanded) colorScheme.primary.copy(alpha = 0.08f) else null,
                        layerBlock = liquidControlLayerBlock(enabled, interactiveHighlight),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        enabled = enabled,
                        onClick = { isExpanded = !isExpanded },
                    )
                    .liquidControlPressFeedback(enabled, interactiveHighlight)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
            ) {
                val selectedIcon = selectedItem?.let { itemIcon?.invoke(it) }
                val icon = leadingIcon ?: selectedIcon
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Column {
                    if (selectedItem != null) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = LiquidGlassTheme.colors.secondaryContent,
                            fontSize = 11.sp,
                        )
                        Text(
                            text = itemLabel(selectedItem),
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                            color = colorScheme.onSurface,
                            fontSize = 14.sp,
                        )
                    } else {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlassTheme.colors.secondaryContent,
                        )
                    }
                }
            }

            Icon(
                imageVector = LiquidIcons.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Chiudi" else "Espandi",
                tint = LiquidGlassTheme.colors.secondaryContent,
                modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = chevronRotation },
            )
        }

        LiquidDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            anchorState = anchorState,
            offset = DpOffset(0.dp, 6.dp),
            backdropState = effectiveBackdrop,
        ) {
            items.forEach { item ->
                LiquidMenuItem(
                    text = itemLabel(item),
                    supportingText = itemSubtitle?.invoke(item),
                    icon = itemIcon?.invoke(item),
                    selected = item == selectedItem,
                    onClick = {
                        onItemSelected(item)
                        isExpanded = false
                    },
                )
            }
        }
    }
}
