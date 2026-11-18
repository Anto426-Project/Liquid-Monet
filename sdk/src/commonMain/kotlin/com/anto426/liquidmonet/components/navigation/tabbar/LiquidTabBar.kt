package com.anto426.liquidmonet.components.navigation.tabbar

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
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

    val scroll = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val fontSize = if (items.size <= 3) 14.sp else 12.sp
        val labelStyle = MaterialTheme.typography.labelLarge.copy(
            fontSize = fontSize, lineHeight = (fontSize.value * 1.25f).sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp,
        )
        val slotWidth = items.maxOf { item ->
            val labelWidth = with(density) { textMeasurer.measure(item.label, labelStyle).size.width.toDp() }
            val badgeWidth = item.badge?.let { with(density) { textMeasurer.measure(it, labelStyle).size.width.toDp() } + 16.dp } ?: 0.dp
            labelWidth + badgeWidth + (if (item.icon != null) 26.dp else 0.dp) + 20.dp
        }.coerceAtLeast(64.dp)
        val availableWidth = if (constraints.hasBoundedWidth) maxWidth else slotWidth * items.size + 8.dp
        val rowWidth = maxOf(availableWidth, slotWidth * items.size + 8.dp)
        LaunchedEffect(normalizedSelectedIndex, rowWidth, availableWidth) {
            val slot = with(density) { ((rowWidth - 8.dp) / items.size).toPx() }
            val viewport = with(density) { availableWidth.toPx() }
            val start = normalizedSelectedIndex * slot
            val end = start + slot
            val target = when {
                start < scroll.value -> start.toInt()
                end > scroll.value + viewport -> (end - viewport + with(density) { 8.dp.toPx() }).toInt()
                else -> scroll.value
            }
            scroll.scrollTo(target.coerceAtLeast(0))
        }
        Box(Modifier.width(availableWidth).horizontalScroll(scroll)) {
            LiquidGlassBottomTabs(
                selectedTabIndex = { normalizedSelectedIndex },
                onTabSelected = onTabSelected,
                tabsCount = items.size,
                modifier = Modifier.width(rowWidth),
                backdrop = effectiveBackdrop,
            ) {
                items.forEachIndexed { index, item ->
                    LiquidGlassBottomTab(
                        onClick = {
                            if (index != normalizedSelectedIndex) onTabSelected(index)
                        },
                        selected = index == normalizedSelectedIndex,
                    ) {
                        AdaptiveTabItem(
                            item = item,
                            tabsCount = items.size,
                            contentColor = contentColor,
                            textStyle = labelStyle,
                        )
                    }
                }
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
    textStyle: TextStyle,
) {
    val count = tabsCount.coerceAtLeast(1)
    val icon = item.icon
    val badge = item.badge?.takeIf(String::isNotEmpty)
    val fontScale = LocalDensity.current.fontScale
    BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 6.dp)) {
        val vertical = icon != null && maxWidth < (112 * fontScale).dp
        val label: @Composable () -> Unit = {
            Text(
                text = item.label,
                color = contentColor,
                style = textStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        if (vertical) {
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                ) {
                    Box(Modifier.weight(1f, fill = false)) { label() }
                    if (badge != null) CompactBadge(badge, contentColor, count)
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            ) {
                if (icon != null) Icon(icon, null, Modifier.size(20.dp), tint = contentColor)
                Box(Modifier.weight(1f, fill = false)) { label() }
                if (badge != null) CompactBadge(badge, contentColor, count)
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
    val fSize = 12.sp

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
