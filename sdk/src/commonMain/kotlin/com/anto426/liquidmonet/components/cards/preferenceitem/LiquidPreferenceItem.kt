package com.anto426.liquidmonet.components.cards.preferenceitem

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.preferenceitem.motion.LiquidPreferenceItemMotion
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
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

/** LiquidPreferenceGroup - Liquid Glass Settings Group Container. */
@Composable
fun LiquidPreferenceGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    size: LiquidSectionHeaderSize = LiquidSectionHeaderSize.Small,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrBlank()) {
            LiquidSectionHeader(
                title = title,
                subtitle = subtitle,
                size = size,
                titleColor = titleColor,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
            )
        }
        LiquidCard(
            modifier = Modifier.fillMaxWidth(),
            backdropState = backdropState,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                content()
            }
        }
    }
}

/** LiquidPreferenceItem - Modern Liquid Glass Setting Item Row with touch feedback. */
@Composable
fun LiquidPreferenceItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop(),
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
) {
    val isInteractive = onClick != null
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val performance = LocalLiquidGlassPerformance.current
    val rowShape = remember { RoundedRectangle(16.dp) }
    val iconShape = remember { RoundedRectangle(12.dp) }
    val iconContainerColor by
        LiquidPreferenceItemMotion.animateColor(
            targetValue =
                if (isPressed && isInteractive) glassColors.accentContainer
                else glassColors.neutralContainer,
            performance = performance,
            label = "preferenceIconContainer",
        )

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null)
                        Modifier.liquidControlInteractive(
                            enabled = true,
                            interactiveHighlight = interactiveHighlight,
                            shape = rowShape,
                            interactionSource = interactionSource,
                            onClick = onClick,
                        )
                    else Modifier
                )
                .padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingContent != null) {
            leadingContent()
            Spacer(modifier = Modifier.width(12.dp))
        } else if (icon != null) {
            Box(
                modifier =
                    Modifier.size(40.dp)
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = iconShape,
                            role = LiquidGlassRole.Control,
                            containerColor = iconContainerColor,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight =
                    if (isPressed && onClick != null) FontWeight.SemiBold else FontWeight.Medium,
                color = glassColors.content,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = glassColors.secondaryContent,
                )
            }
        }

        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailingContent()
        } else if (onClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = glassColors.secondaryContent.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/**
 * LiquidPreferenceDropdown - Unified Settings Preference Item integrated with LiquidDropdownMenu.
 *
 * Provides a clean, modern preference row that reveals a bouncy, refractive liquid glass dropdown
 * menu upon selection.
 */
@Composable
fun <T> LiquidPreferenceDropdown(
    title: String,
    selectedItem: T,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    itemLabel: (T) -> String = { it.toString() },
    itemSubtitle: ((T) -> String?)? = null,
    itemIcon: ((T) -> ImageVector?)? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val performance = LocalLiquidGlassPerformance.current
    var isExpanded by remember { mutableStateOf(false) }
    val anchorState = rememberLiquidGlassOverlayAnchorState()
    val chevronRotation by
        LiquidPreferenceItemMotion.animateFloat(
            targetValue = if (isExpanded) 180f else 0f,
            performance = performance,
            label = "prefChevronRotation",
        )

    val activeLabel = itemLabel(selectedItem)
    val displaySubtitle = subtitle ?: activeLabel

    Box(modifier = modifier.fillMaxWidth().liquidGlassOverlayAnchor(anchorState)) {
        LiquidPreferenceItem(
            title = title,
            subtitle = displaySubtitle,
            icon = icon ?: itemIcon?.invoke(selectedItem),
            onClick = if (enabled) ({ isExpanded = !isExpanded }) else null,
            backdropState = backdropState,
            trailingContent = {
                Icon(
                    imageVector = LiquidIcons.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Chiudi" else "Espandi",
                    tint = LiquidGlassTheme.colors.secondaryContent,
                    modifier = Modifier.size(20.dp).graphicsLayer { rotationZ = chevronRotation },
                )
            },
        )

        LiquidDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            anchorState = anchorState,
            offset = DpOffset(0.dp, 4.dp),
            backdropState = backdropState,
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
