package com.anto426.liquidmonet.components.selection.chip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayer
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.selection.chip.motion.LiquidChipMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/**
 * LiquidChip - Radiant Optical Liquid Glass Chip for filters, tags, suggestions, and categories.
 * Features vibrant Monet illumination, dynamic scale spring transitions, and lively liquid bounce.
 */
@Composable
fun LiquidChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    badge: String? = null,
    onCloseClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = Capsule(),
    backdropState: Backdrop = emptyBackdrop(),
    tint: Color = Color.Unspecified,
) {
    val interactiveHighlight = rememberLiquidControlHighlight()
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val activeMonetColor = if (tint.isSpecified) tint else colorScheme.primary

    val targetContentColor =
        if (selected) {
            if (enabled) glassColors.content else glassColors.disabledContent
        } else if (enabled) {
            glassColors.content
        } else {
            glassColors.disabledContent
        }

    val animatedContentColor by
        LiquidChipMotion.animateColor(
            targetValue = targetContentColor,
            performance = performance,
            label = "chipContentColor",
        )

    // Vibrant Radiant Monet Glow & Fill
    val targetContainerColor =
        if (selected) {
            activeMonetColor.copy(alpha = glassColors.selectedContainer.alpha)
        } else {
            glassColors.neutralContainer
        }
    val animatedContainerColor by
        LiquidChipMotion.animateColor(
            targetValue = targetContainerColor,
            performance = performance,
            label = "chipContainerColor",
        )

    Row(
        modifier =
            modifier
                .height(38.dp)
                .liquidControlLayer(enabled, interactiveHighlight)
                .liquidControlPressFeedback(
                    enabled = enabled,
                    interactiveHighlight = interactiveHighlight,
                    shape = shape,
                    drawHighlightOverlay = true,
                )
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Control,
                    containerColor = animatedContainerColor,
                )
                .clickable(
                    interactionSource =
                        remember {
                            androidx.compose.foundation.interaction.MutableInteractionSource()
                        },
                    indication = null,
                    role = Role.Button,
                    enabled = enabled,
                    onClick = onClick,
                )
                .padding(
                    start = if (leadingIcon != null) 12.dp else 14.dp,
                    end =
                        if (onCloseClick != null || trailingIcon != null || badge != null) 8.dp
                        else 14.dp,
                ),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides animatedContentColor) {
            // Leading Icon
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = animatedContentColor.copy(alpha = if (selected) 1f else 0.90f),
                    modifier = Modifier.size(16.dp),
                )
            }

            BasicText(
                text = label,
                style =
                    TextStyle(
                        color = animatedContentColor,
                        fontSize = 13.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    ),
            )

            // Radiant Count / Badge Bubble
            if (badge != null) {
                Box(
                    modifier =
                        Modifier
                            // The parent chip already owns the optical pass. A badge is a tint
                            // layer,
                            // not another piece of glass sampling the same pixels a second time.
                            .background(
                                color =
                                    if (selected) activeMonetColor.copy(alpha = 0.44f)
                                    else
                                        glassColors.neutralContainer.copy(
                                            alpha =
                                                (glassColors.neutralContainer.alpha * 1.8f)
                                                    .coerceAtMost(1f)
                                        ),
                                shape = Capsule(),
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = badge,
                        style =
                            TextStyle(
                                color = animatedContentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }

            // Close Action with Tactile Bounce
            if (onCloseClick != null) {
                LiquidIconButton(
                    icon = LiquidIcons.Close,
                    contentDescription = "Rimuovi",
                    onClick = onCloseClick,
                    enabled = enabled,
                    size = 20.dp,
                    iconSize = 14.dp,
                    variant = LiquidIconButtonVariant.Ghost,
                    contentColor = animatedContentColor.copy(alpha = 0.75f),
                )
            } else if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = animatedContentColor.copy(alpha = 0.90f),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
