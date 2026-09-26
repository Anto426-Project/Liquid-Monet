package com.anto426.liquidmonet.components.navigation.navigationbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.internal.LiquidGlassBottomTab
import com.anto426.liquidmonet.components.internal.LiquidGlassBottomTabs
import com.anto426.liquidmonet.components.navigation.navigationbar.motion.LiquidNavigationBarMotion
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

data class LiquidNavigationItem(
    val label: String,
    val route: String = "",
    val icon: ImageVector? = null,
    val badge: String? = null,
)

/**
 * LiquidNavigationBar - Liquid Glass Bottom Navigation Bar with smart adaptive layout. Dynamically
 * calibrates icon size, font size, badge placement, and text truncation based on element count.
 */
@Composable
fun LiquidNavigationBar(
    items: List<LiquidNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    if (items.isEmpty()) return
    val normalizedSelectedIndex = selectedIndex.coerceIn(items.indices)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val contentColor = LiquidGlassTheme.colors.content

    LiquidNavigationBarShell(
        modifier = modifier,
        visible = visible,
    ) {
        LiquidGlassBottomTabs(
            selectedTabIndex = { normalizedSelectedIndex },
            onTabSelected = onItemSelected,
            backdrop = effectiveBackdrop,
            tabsCount = items.size,
        ) {
            for (index in items.indices) {
                val item = items[index]
                LiquidGlassBottomTab(
                    onClick = {
                        if (index != normalizedSelectedIndex) onItemSelected(index)
                    }
                ) {
                    AdaptiveNavItem(
                        item = item,
                        itemsCount = items.size,
                        contentColor = contentColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidNavigationBarShell(
    modifier: Modifier,
    visible: Boolean,
    content: @Composable () -> Unit,
) {
    val performance = LocalLiquidGlassPerformance.current
    val navBarOffsetY by
        LiquidNavigationBarMotion.animateDp(
            targetValue = if (visible) 0.dp else 120.dp,
            performance = performance,
            label = "navBarOffsetY",
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationY = navBarOffsetY.toPx()
                }
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        content()
    }
}

/** Smart adaptive navigation item for bottom bars. */
@Composable
private fun AdaptiveNavItem(
    item: LiquidNavigationItem,
    itemsCount: Int,
    contentColor: Color,
) {
    val glassColors = LiquidGlassTheme.colors
    val count = itemsCount.coerceAtLeast(1)
    val icon = item.icon
    val badge = item.badge?.takeIf(String::isNotEmpty)
    val hasIcon = icon != null
    val hasBadge = badge != null

    val iconSize =
        when {
            count <= 3 -> 22.dp
            count == 4 -> 20.dp
            else -> 18.dp
        }

    val fontSize =
        when {
            !hasIcon -> if (count <= 3) 13.5.sp else 12.sp
            count <= 3 -> 11.5.sp
            count == 4 -> 11.sp
            else -> 10.sp
        }

    val spacing =
        when {
            count <= 3 -> 2.dp
            count == 4 -> 2.dp
            else -> 1.5.dp
        }

    val horizontalPadding =
        when {
            count <= 3 -> 6.dp
            count == 4 -> 4.dp
            else -> 2.dp
        }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
    ) {
        if (icon != null) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(iconSize),
                    tint = contentColor,
                )
                if (badge != null) {
                    Box(
                        modifier =
                            Modifier.align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-3).dp)
                                .clip(Capsule())
                                .background(glassColors.neutralContainer)
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = badge,
                            color = contentColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }
            }
        }

        if (item.label.isNotEmpty()) {
            Text(
                text = item.label,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}
