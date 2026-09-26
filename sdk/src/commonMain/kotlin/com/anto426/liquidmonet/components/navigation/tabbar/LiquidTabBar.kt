package com.anto426.liquidmonet.components.navigation.tabbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.internal.LiquidGlassBottomTab
import com.anto426.liquidmonet.components.internal.LiquidGlassBottomTabs
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/**
 * LiquidTabBar - Shared Liquid Glass segmented control with a sliding droplet indicator.
 * Automatically adapts typography, spacing, icon sizing, and layout based on element count.
 */
@Composable
fun LiquidTabBar(
    items: List<LiquidNavigationItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    if (items.isEmpty()) return
    val normalizedSelectedIndex = selectedIndex.coerceIn(items.indices)
    val contentColor = LiquidGlassTheme.colors.content
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    LiquidGlassBottomTabs(
        selectedTabIndex = { normalizedSelectedIndex },
        onTabSelected = onTabSelected,
        tabsCount = items.size,
        modifier = modifier,
        backdrop = effectiveBackdrop,
    ) {
        items.forEachIndexed { index, item ->
            LiquidGlassBottomTab(
                onClick = {
                    if (index != normalizedSelectedIndex) onTabSelected(index)
                }
            ) {
                AdaptiveTabItem(
                    item = item,
                    tabsCount = items.size,
                    contentColor = contentColor,
                )
            }
        }
    }
}

/**
 * Smart adaptive tab item that dynamically chooses font size, icon size, spacing, and layout
 * (horizontal vs compact vertical) depending on tabs count and text length.
 */
@Composable
private fun AdaptiveTabItem(
    item: LiquidNavigationItem,
    tabsCount: Int,
    contentColor: Color,
) {
    val count = tabsCount.coerceAtLeast(1)
    val icon = item.icon
    val badge = item.badge?.takeIf(String::isNotEmpty)
    val hasIcon = icon != null
    val hasBadge = badge != null
    val isLongText = item.label.length > 7

    val fontSize =
        when {
            count <= 2 -> 14.5.sp
            count == 3 -> 13.5.sp
            count == 4 -> if (hasIcon || isLongText) 12.sp else 13.sp
            else -> if (hasIcon) 11.sp else 12.sp
        }

    val iconSize =
        when {
            count <= 2 -> 18.dp
            count == 3 -> 17.dp
            count == 4 -> 15.dp
            else -> 14.dp
        }

    val spacing =
        when {
            count <= 2 -> 7.dp
            count == 3 -> 5.dp
            count == 4 -> 4.dp
            else -> 3.dp
        }

    val horizontalPadding =
        when {
            count <= 2 -> 8.dp
            count == 3 -> 6.dp
            count == 4 -> 4.dp
            else -> 2.dp
        }

    val useVerticalLayout = count >= 4 && hasIcon && isLongText

    if (useVerticalLayout) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = item.label,
                tint = contentColor,
                modifier = Modifier.size(iconSize),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
            ) {
                Text(
                    text = item.label,
                    color = contentColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                if (hasBadge) {
                    CompactBadge(badge = badge, contentColor = contentColor, count = count)
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
        ) {
            if (hasIcon) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize),
                )
            }
            Text(
                text = item.label,
                color = contentColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            if (hasBadge) {
                CompactBadge(badge = badge, contentColor = contentColor, count = count)
            }
        }
    }
}

/** Compact high-contrast badge adapted to available space. */
@Composable
private fun CompactBadge(
    badge: String,
    contentColor: Color,
    count: Int,
) {
    val glassColors = LiquidGlassTheme.colors
    val hPad = if (count <= 3) 6.dp else 4.dp
    val vPad = if (count <= 3) 2.dp else 1.dp
    val fSize = if (count <= 3) 10.sp else 9.sp

    Box(
        modifier =
            Modifier.clip(Capsule())
                .background(glassColors.neutralContainer)
                .padding(horizontal = hPad, vertical = vPad),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = badge,
            color = contentColor,
            fontSize = fSize,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
        )
    }
}
