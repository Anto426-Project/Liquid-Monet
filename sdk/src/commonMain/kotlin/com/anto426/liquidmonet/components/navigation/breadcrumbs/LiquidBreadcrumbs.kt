package com.anto426.liquidmonet.components.navigation.breadcrumbs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/** Data item representing a step in breadcrumbs. */
data class LiquidBreadcrumbItem(
    val label: String,
    val onClick: (() -> Unit)? = null,
)

/**
 * LiquidBreadcrumbs - Optical Liquid Glass Navigation Breadcrumbs Trail. Minimalist, elegant
 * scrollable glass trail with fluid tactile bounce on clickable items.
 */
@Composable
fun LiquidBreadcrumbs(
    items: List<LiquidBreadcrumbItem>,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Row(
        modifier =
            modifier
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = Capsule(),
                    role = LiquidGlassRole.Navigation,
                )
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.size - 1

            LiquidBreadcrumbItemView(
                item = item,
                isLast = isLast,
            )

            if (!isLast) {
                Icon(
                    imageVector = LiquidIcons.ChevronRight,
                    contentDescription = null,
                    tint = LiquidGlassTheme.colors.secondaryContent,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

@Composable
private fun LiquidBreadcrumbItemView(
    item: LiquidBreadcrumbItem,
    isLast: Boolean,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isClickable = item.onClick != null && !isLast
    val highlight = rememberLiquidControlHighlight()

    Box(
        modifier =
            modifier
                .then(
                    liquidControlLayerBlock(isClickable, highlight)?.let {
                        Modifier.graphicsLayer(it)
                    } ?: Modifier
                )
                .liquidControlPressFeedback(
                    enabled = isClickable,
                    interactiveHighlight = highlight,
                    shape = Capsule(),
                    drawHighlightOverlay = true,
                )
                .then(
                    if (isClickable) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = { item.onClick.invoke() },
                        )
                    } else Modifier
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = item.label,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.5.sp,
                ),
            color = if (isLast) colorScheme.primary else LiquidGlassTheme.colors.secondaryContent,
        )
    }
}
