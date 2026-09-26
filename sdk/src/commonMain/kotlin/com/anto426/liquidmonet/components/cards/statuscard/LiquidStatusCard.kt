package com.anto426.liquidmonet.components.cards.statuscard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayer
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

enum class LiquidStatusType {
    Info,
    Success,
    Warning,
    Error,
}

/**
 * LiquidStatusCard - Crystal Liquid Glass Informative Status Card. Uses pure optical refraction
 * with dedicated liquid glass icon bubble pods.
 */
@Composable
fun LiquidStatusCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    statusType: LiquidStatusType = LiquidStatusType.Info,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    supportingText: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    titleMaxLines: Int = Int.MAX_VALUE,
    descriptionMaxLines: Int = Int.MAX_VALUE,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val glassColors = LiquidGlassTheme.colors
    val statusColor =
        when (statusType) {
            LiquidStatusType.Info -> MaterialTheme.colorScheme.primary
            LiquidStatusType.Success -> glassColors.success
            LiquidStatusType.Warning -> glassColors.warning
            LiquidStatusType.Error -> glassColors.error
        }

    val contentColor = glassColors.content
    val iconHighlight = rememberLiquidControlHighlight()
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    LiquidCard(
        modifier = modifier.fillMaxWidth(),
        backdropState = effectiveBackdrop,
        shape = RoundedRectangle(22.dp),
        contentPadding = 14.dp,
        onClick = onClick,
    ) {
        val podBackdrop = resolveLiquidGlassBackdrop(effectiveBackdrop)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Optical Liquid Glass Icon Bubble Pod with Jelly Bounce
            Box(
                modifier =
                    Modifier.size(42.dp)
                        .liquidControlLayer(true, iconHighlight)
                        .liquidControlPressFeedback(
                            enabled = true,
                            interactiveHighlight = iconHighlight,
                            shape = Capsule(),
                            drawHighlightOverlay = true,
                        )
                        .liquidGlass(
                            backdrop = podBackdrop,
                            shape = Capsule(),
                            role = LiquidGlassRole.Control,
                            containerColor = statusColor.copy(alpha = 0.14f),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector =
                        icon
                            ?: when (statusType) {
                                LiquidStatusType.Info -> LiquidIcons.Info
                                LiquidStatusType.Success -> LiquidIcons.Check
                                LiquidStatusType.Warning -> LiquidIcons.Warning
                                LiquidStatusType.Error -> LiquidIcons.Close
                            },
                    contentDescription = statusType.name,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = titleMaxLines,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.secondaryContent,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = descriptionMaxLines,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                if (!supportingText.isNullOrBlank()) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                if (actionLabel != null && onAction != null) {
                    LiquidButton(
                        text = actionLabel,
                        onClick = onAction,
                        backdropState = effectiveBackdrop,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}
