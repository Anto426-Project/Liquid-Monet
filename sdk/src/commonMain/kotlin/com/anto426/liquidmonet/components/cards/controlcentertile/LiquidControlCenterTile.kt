package com.anto426.liquidmonet.components.cards.controlcentertile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.controlcentertile.motion.LiquidControlCenterTileMotion
import com.anto426.liquidmonet.components.cards.internal.LiquidControlCenterDefaults
import com.anto426.liquidmonet.components.cards.internal.LiquidControlCenterIcon
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

/** Wide quick-setting tile. Its icon and label share the control's single press transform. */
@Composable
fun LiquidControlCenterTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingContent: (@Composable () -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    LiquidControlCenterTileSurface(
        active,
        enabled,
        onClick,
        modifier.fillMaxWidth(),
        backdropState,
    ) { iconBackground, iconTint ->
        Row(
            Modifier.fillMaxWidth().padding(LiquidControlCenterDefaults.spacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LiquidControlCenterDefaults.spacing),
        ) {
            LiquidControlCenterIcon(icon, iconBackground, iconTint)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color =
                        if (enabled) LiquidGlassTheme.colors.content
                        else LiquidGlassTheme.colors.disabledContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        when {
                            !enabled -> LiquidGlassTheme.colors.disabledContent
                            active -> MaterialTheme.colorScheme.primary
                            else -> LiquidGlassTheme.colors.secondaryContent
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailingContent?.invoke()
        }
    }
}

/** Compact quick-setting tile, with the same material and press physics as the wide variant. */
@Composable
fun LiquidControlCenterCompactTile(
    title: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    LiquidControlCenterTileSurface(
        active,
        enabled,
        onClick,
        modifier.heightIn(min = 104.dp),
        backdropState,
    ) { iconBackground, iconTint ->
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LiquidControlCenterIcon(icon, iconBackground, iconTint)
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color =
                    if (enabled) LiquidGlassTheme.colors.content
                    else LiquidGlassTheme.colors.disabledContent,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun LiquidControlCenterTileSurface(
    active: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
    backdrop: Backdrop,
    content: @Composable (iconBackground: Color, iconTint: Color) -> Unit,
) {
    val colors = LiquidGlassTheme.colors
    val scheme = MaterialTheme.colorScheme
    val performance = LocalLiquidGlassPerformance.current
    val background by
        LiquidControlCenterTileMotion.animateColor(
            if (active && enabled) colors.selectedContainer else colors.neutralContainer,
            performance,
            "tileBackground",
        )
    val iconBackground by
        LiquidControlCenterTileMotion.animateColor(
            if (active && enabled) scheme.primary else colors.neutralContainer,
            performance,
            "tileIconBackground",
        )
    val iconTint by
        LiquidControlCenterTileMotion.animateColor(
            when {
                !enabled -> colors.disabledContent
                active -> scheme.onPrimary
                else -> colors.content
            },
            performance,
            "tileIconTint",
        )
    Box(
        modifier
            .heightIn(min = LiquidControlCenterDefaults.minimumHeight)
            .semantics(mergeDescendants = true) {
                toggleableState = if (active) ToggleableState.On else ToggleableState.Off
            }
            .liquidControlInteractive(
                enabled,
                rememberLiquidControlHighlight(),
                LiquidControlCenterDefaults.shape,
                role = Role.Switch,
                backdrop = resolveLiquidGlassBackdrop(backdrop),
                containerColor = background,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content(iconBackground, iconTint)
    }
}
