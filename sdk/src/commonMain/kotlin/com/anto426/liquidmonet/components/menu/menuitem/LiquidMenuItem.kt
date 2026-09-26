package com.anto426.liquidmonet.components.menu.menuitem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.menu.internal.LocalLiquidMenuDragSelection
import com.anto426.liquidmonet.components.menu.internal.liquidMenuDragTarget
import com.anto426.liquidmonet.components.menu.menu.LiquidMenuItemType
import com.anto426.liquidmonet.components.menu.menuitem.motion.LiquidMenuItemMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidMenuItem - Clean, borderless menu item row with dynamic touch illumination. Deforms
 * seamlessly together with the outer menu container with interactive spotlight glow.
 */
@Composable
fun LiquidMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: LiquidMenuItemType = LiquidMenuItemType.Action,
    icon: ImageVector? = null,
    supportingText: String? = null,
    trailingText: String? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    contentColor: Color = Color.Unspecified,
    selected: Boolean = false,
    destructive: Boolean = false,
) {
    val performance = LocalLiquidGlassPerformance.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()
    val dragSelection = LocalLiquidMenuDragSelection.current
    val dragKey = remember { Any() }
    val isDragTarget by
        remember(dragSelection, dragKey) {
            derivedStateOf { dragSelection?.activeKey == dragKey }
        }
    val isActive = enabled && (isPressed || isDragTarget)
    val isSelectable = type == LiquidMenuItemType.Selectable || selected
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val accentColor = if (destructive) colorScheme.error else colorScheme.primary
    val defaultColor =
        when {
            destructive -> colorScheme.error
            contentColor != Color.Unspecified -> contentColor
            else -> colorScheme.onSurface
        }
    val resolvedColor = if (isActive || isHovered || selected) accentColor else defaultColor

    val itemScale by
        LiquidMenuItemMotion.animateFloatPrimary(
            targetValue = if (isActive && performance.motionScale > 0f) 0.94f else 1f,
            performance = performance,
            label = "menuItemScale",
            isActive = isActive,
        )

    val animatedAlpha by
        LiquidMenuItemMotion.animateFloatSecondary(
            targetValue = if (enabled) 1f else 0.40f,
            performance = performance,
            label = "menuItemAlpha",
        )

    val backgroundColor by
        LiquidMenuItemMotion.animateColor(
            targetValue =
                when {
                    isActive -> accentColor.copy(alpha = glassColors.selectedContainer.alpha)
                    isHovered -> accentColor.copy(alpha = glassColors.accentContainer.alpha)
                    selected -> accentColor.copy(alpha = glassColors.accentContainer.alpha * 0.80f)
                    else -> Color.Transparent
                },
            performance = performance,
            label = "menuItemBg",
        )

    val borderColor by
        LiquidMenuItemMotion.animateColor(
            targetValue =
                when {
                    isActive -> accentColor.copy(alpha = glassColors.focusIndicator.alpha)
                    isHovered -> accentColor.copy(alpha = glassColors.focusIndicator.alpha * 0.62f)
                    selected -> accentColor.copy(alpha = glassColors.focusIndicator.alpha * 0.50f)
                    else -> Color.Transparent
                },
            performance = performance,
            label = "menuItemBorder",
        )

    val itemShape = remember { RoundedRectangle(14.dp) }

    Row(
        modifier =
            modifier
                .liquidMenuDragTarget(dragSelection, dragKey, enabled, onClick)
                .fillMaxWidth()
                .semantics { if (isSelectable) this.selected = selected }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick,
                )
                .graphicsLayer {
                    // Keep the highlight and border attached to the label while the item bounces.
                    scaleX = itemScale
                    scaleY = itemScale
                    alpha = animatedAlpha
                    clip = false
                }
                .background(
                    color = backgroundColor,
                    shape = itemShape,
                )
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = itemShape,
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint =
                    resolvedColor.copy(
                        alpha = if (isActive || isHovered || selected) 1f else 0.90f
                    ),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive || selected) FontWeight.SemiBold else FontWeight.Medium,
                color = resolvedColor,
                fontSize = 14.5.sp,
            )
            if (!supportingText.isNullOrBlank()) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.labelSmall,
                    color = resolvedColor.copy(alpha = 0.62f),
                    fontSize = 11.5.sp,
                )
            }
        }

        if (!trailingText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodySmall,
                color = resolvedColor.copy(alpha = 0.65f),
                fontSize = 12.sp,
            )
        }
        if (selected) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = LiquidIcons.Check,
                contentDescription = "Selezionato",
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
        } else if (trailingIcon != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = defaultColor.copy(alpha = 0.60f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
