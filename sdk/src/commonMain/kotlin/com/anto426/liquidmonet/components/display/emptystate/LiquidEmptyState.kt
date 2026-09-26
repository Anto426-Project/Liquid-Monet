package com.anto426.liquidmonet.components.display.emptystate

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayer
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
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
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidEmptyState - Optical Liquid Glass Empty State Placeholder. Fully customizable for title,
 * description, custom icon, colors, actions, and custom content.
 */
@Composable
fun LiquidEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = LiquidIcons.Info,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    iconContainerColor: Color? = null,
    iconSize: Dp = 32.dp,
    iconBubbleSize: Dp = 68.dp,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    secondaryActionButtonText: String? = null,
    onSecondaryActionClick: (() -> Unit)? = null,
    onIconClick: (() -> Unit)? = null,
    shape: Shape = RoundedRectangle(24.dp),
    backdropState: Backdrop = emptyBackdrop(),
    customIcon: (@Composable () -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val iconHighlight = rememberLiquidControlHighlight()

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Surface,
                )
                .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Clean Optical Liquid Glass Icon Bubble
                if (customIcon != null) {
                    customIcon()
                    Spacer(modifier = Modifier.height(14.dp))
                } else if (icon != null) {
                    Box(
                        modifier =
                            Modifier.size(iconBubbleSize)
                                .liquidControlLayer(true, iconHighlight)
                                .liquidControlPressFeedback(
                                    enabled = true,
                                    interactiveHighlight = iconHighlight,
                                    shape = Capsule(),
                                    drawHighlightOverlay = true,
                                )
                                .then(
                                    if (onIconClick != null) {
                                        Modifier.clickable(
                                            interactionSource =
                                                remember { MutableInteractionSource() },
                                            indication = null,
                                            role = Role.Button,
                                            onClick = onIconClick,
                                        )
                                    } else Modifier
                                )
                                .liquidGlass(
                                    backdrop = effectiveBackdrop,
                                    shape = Capsule(),
                                    role = LiquidGlassRole.Control,
                                    containerColor =
                                        iconContainerColor ?: iconColor.copy(alpha = 0.12f),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(iconSize),
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Title
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        ),
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                // Subtitle / Description
                if (description != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                        color = LiquidGlassTheme.colors.secondaryContent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }

                // Extra Content Slot
                if (extraContent != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    extraContent()
                }

                // Actions Row
                if (
                    (actionButtonText != null && onActionClick != null) ||
                        (secondaryActionButtonText != null && onSecondaryActionClick != null)
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (secondaryActionButtonText != null && onSecondaryActionClick != null) {
                            LiquidButton(
                                text = secondaryActionButtonText,
                                onClick = onSecondaryActionClick,
                                variant = LiquidButtonVariant.Secondary,
                                backdropState = effectiveBackdrop,
                            )
                        }

                        if (actionButtonText != null && onActionClick != null) {
                            LiquidButton(
                                text = actionButtonText,
                                onClick = onActionClick,
                                variant = LiquidButtonVariant.Primary,
                                backdropState = effectiveBackdrop,
                            )
                        }
                    }
                }
            }
        }
    }
}
