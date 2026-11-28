package com.anto426.liquidmonet.components.feedback.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.feedback.banner.motion.LiquidBannerMotion
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassLegibility
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LocalLiquidGlassLegibility
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

/** Semantic tone for [LiquidBanner]. */
enum class LiquidBannerTone {
    Info,
    Success,
    Warning,
    Error,
}

/**
 * LiquidBanner - Inline dismissible Liquid Glass status banner with semantic accent tint,
 * optional primary action button, and smooth expand/collapse transitions.
 */
@Composable
fun LiquidBanner(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    visible: Boolean = true,
    tone: LiquidBannerTone = LiquidBannerTone.Info,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    dismissContentDescription: String = "Chiudi",
    shape: Shape = RoundedRectangle(20.dp),
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
) {
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)

    val accentColor =
        when (tone) {
            LiquidBannerTone.Info -> colorScheme.primary
            LiquidBannerTone.Success -> glassColors.success
            LiquidBannerTone.Warning -> glassColors.warning
            LiquidBannerTone.Error -> glassColors.error
        }
    val resolvedIcon =
        icon
            ?: when (tone) {
                LiquidBannerTone.Info -> LiquidIcons.Info
                LiquidBannerTone.Success -> LiquidIcons.Check
                LiquidBannerTone.Warning -> LiquidIcons.Warning
                LiquidBannerTone.Error -> LiquidIcons.Warning
            }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = remember(performance) { LiquidBannerMotion.enter(performance) },
        exit = remember(performance) { LiquidBannerMotion.exit(performance) },
    ) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Surface,
                        containerColor = accentColor.copy(alpha = 0.12f),
                        backdropPolicy = backdropPolicy,
                        legibility = legibility,
                    )
                    .border(width = 1.dp, color = accentColor.copy(alpha = 0.28f), shape = shape)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CompositionLocalProvider(
                LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
                LocalLiquidGlassLegibility provides legibility,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier =
                            Modifier.size(36.dp)
                                .clip(Capsule())
                                .background(accentColor.copy(alpha = 0.20f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = resolvedIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = glassColors.content,
                        )
                        if (!message.isNullOrBlank()) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = glassColors.secondaryContent,
                            )
                        }
                    }

                    if (onDismiss != null) {
                        LiquidIconButton(
                            icon = LiquidIcons.Close,
                            contentDescription = dismissContentDescription,
                            onClick = onDismiss,
                            size = 32.dp,
                            iconSize = 16.dp,
                            variant = LiquidIconButtonVariant.Ghost,
                            contentColor = glassColors.secondaryContent,
                            backdrop = effectiveBackdrop,
                        )
                    }
                }

                if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        LiquidButton(
                            text = actionLabel,
                            onClick = onActionClick,
                            size = LiquidButtonSize.Small,
                            variant = LiquidButtonVariant.Tonal,
                            tint = accentColor,
                            backdrop = effectiveBackdrop,
                        )
                    }
                }
            }
        }
    }
}
