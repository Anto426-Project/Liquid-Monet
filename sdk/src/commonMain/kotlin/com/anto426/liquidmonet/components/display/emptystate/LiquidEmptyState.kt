package com.anto426.liquidmonet.components.display.emptystate

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidEmptyState - Material 3 Expressive Empty / Error State.
 *
 * Renders a transparent Expressive illustrative hero ([LiquidExpressiveHeroFrame]) with an icon pod,
 * geometric document/signal motif, overlapping status badge, centered title/description, and
 * optional Liquid Glass action buttons. Set [showSurfaceContainer] to `true` to wrap the state in
 * an optical Liquid Glass card container.
 */
@Composable
fun LiquidEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    badgeIcon: ImageVector? = null,
    isError: Boolean = false,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    iconContainerColor: Color? = null,
    iconSize: Dp = 18.dp,
    iconBubbleSize: Dp = 34.dp,
    actionButtonText: String? = null,
    onActionClick: (() -> Unit)? = null,
    secondaryActionButtonText: String? = null,
    onSecondaryActionClick: (() -> Unit)? = null,
    onIconClick: (() -> Unit)? = null,
    showSurfaceContainer: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
    shape: Shape = RoundedRectangle(24.dp),
    backdropState: Backdrop = emptyBackdrop(),
    customIcon: (@Composable () -> Unit)? = null,
    extraContent: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val resolvedMainIcon = icon ?: if (isError) LiquidIcons.Warning else LiquidIcons.MenuBook
    val resolvedBadgeIcon = badgeIcon ?: if (isError) LiquidIcons.Refresh else LiquidIcons.Info
    val podContainerColor = iconContainerColor ?: iconColor.copy(alpha = 0.20f)

    val containerModifier = if (showSurfaceContainer) {
        modifier
            .fillMaxWidth()
            .liquidGlass(
                backdrop = effectiveBackdrop,
                shape = shape,
                role = LiquidGlassRole.Surface,
            )
            .padding(contentPadding)
    } else {
        modifier
            .fillMaxWidth()
            .padding(contentPadding)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center,
    ) {
        val body: @Composable () -> Unit = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (customIcon != null) {
                    customIcon()
                } else {
                    val heroClickModifier = if (onIconClick != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onIconClick,
                        )
                    } else {
                        Modifier
                    }

                    LiquidExpressiveHeroFrame(
                        modifier = heroClickModifier,
                        accentColor = iconColor,
                        mainContent = {
                            Box(
                                modifier = Modifier
                                    .offset(x = (-16).dp, y = (-14).dp)
                                    .size(iconBubbleSize)
                                    .background(podContainerColor, RoundedRectangle(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = resolvedMainIcon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(iconSize),
                                )
                            }

                            // Stylized signal/document geometric motif in the main ring
                            Canvas(
                                modifier = Modifier
                                    .offset(x = 12.dp, y = 4.dp)
                                    .size(34.dp),
                            ) {
                                val stroke = 2.4.dp.toPx()
                                drawRoundRect(
                                    color = iconColor.copy(alpha = 0.88f),
                                    topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
                                    size = Size(size.width * 0.72f, size.height * 0.72f),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                                    style = Stroke(width = stroke),
                                )
                                if (isError) {
                                    drawLine(
                                        color = iconColor.copy(alpha = 0.72f),
                                        start = Offset(size.width * 0.26f, size.height * 0.74f),
                                        end = Offset(size.width * 0.74f, size.height * 0.26f),
                                        strokeWidth = 2.2.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                } else {
                                    drawLine(
                                        color = iconColor.copy(alpha = 0.72f),
                                        start = Offset(size.width * 0.30f, size.height * 0.42f),
                                        end = Offset(size.width * 0.70f, size.height * 0.42f),
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                    drawLine(
                                        color = iconColor.copy(alpha = 0.56f),
                                        start = Offset(size.width * 0.30f, size.height * 0.58f),
                                        end = Offset(size.width * 0.56f, size.height * 0.58f),
                                        strokeWidth = 2.dp.toPx(),
                                        cap = StrokeCap.Round,
                                    )
                                }
                            }
                        },
                        badgeContent = {
                            Icon(
                                imageVector = resolvedBadgeIcon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(21.dp),
                            )
                        },
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                }

                if (extraContent != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    extraContent()
                }

                if (
                    (actionButtonText != null && onActionClick != null) ||
                    (secondaryActionButtonText != null && onSecondaryActionClick != null)
                ) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (secondaryActionButtonText != null && onSecondaryActionClick != null) {
                            LiquidButton(
                                text = secondaryActionButtonText,
                                onClick = onSecondaryActionClick,
                                variant = LiquidButtonVariant.Secondary,
                                size = LiquidButtonSize.Medium,
                                backdropState = effectiveBackdrop,
                            )
                        }

                        if (actionButtonText != null && onActionClick != null) {
                            LiquidButton(
                                text = actionButtonText,
                                onClick = onActionClick,
                                variant = LiquidButtonVariant.Primary,
                                size = LiquidButtonSize.Medium,
                                backdropState = effectiveBackdrop,
                            )
                        }
                    }
                }
            }
        }

        if (showSurfaceContainer) {
            CompositionLocalProvider(
                LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
            ) {
                body()
            }
        } else {
            body()
        }
    }
}
