package com.anto426.liquidmonet.components.menu.dropdownmenu.internal

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.menu.internal.LiquidMenuDragSelection
import com.anto426.liquidmonet.components.menu.internal.LocalLiquidMenuDragSelection
import com.anto426.liquidmonet.components.menu.internal.liquidMenuDragSelection
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/** Menu material with the same press, drag and return response as buttons and clickable cards. */
@Composable
internal fun LiquidGlassMenuSurface(
    backdropState: Backdrop = LocalLiquidGlassContentBackdrop.current ?: emptyBackdrop(),
    modifier: Modifier = Modifier,
    minWidth: androidx.compose.ui.unit.Dp = 180.dp,
    maxWidth: androidx.compose.ui.unit.Dp = 280.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val isLightSurface = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val dragSelection = remember { LiquidMenuDragSelection() }
    val menuShape = remember { RoundedRectangle(22.dp) }
    val interactiveHighlight = rememberLiquidControlHighlight()
    val menuLayerBlock =
        liquidControlLayerBlock(
            enabled = true,
            interactiveHighlight = interactiveHighlight,
        )

    val glassRimBrush =
        remember(isLightSurface) {
            Brush.linearGradient(
                colors =
                    listOf(
                        Color.White.copy(alpha = if (isLightSurface) 0.38f else 0.20f),
                        Color.White.copy(alpha = if (isLightSurface) 0.10f else 0.05f),
                        Color.White.copy(alpha = if (isLightSurface) 0.24f else 0.12f),
                        Color.White.copy(alpha = if (isLightSurface) 0.06f else 0.03f),
                    ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
            )
        }

    Box(
        modifier =
            modifier
                .graphicsLayer(clip = false)
                .widthIn(min = minWidth, max = maxWidth)
                .width(IntrinsicSize.Max)
                .liquidMenuDragSelection(dragSelection, interactiveHighlight)
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = menuShape,
                    role = LiquidGlassRole.Menu,
                    layerBlock = menuLayerBlock,
                )
                .border(
                    width = 1.dp,
                    brush = glassRimBrush,
                    shape = menuShape,
                )
                .then(interactiveHighlight.modifier(clipShape = menuShape))
                .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        CompositionLocalProvider(
            LocalLiquidMenuDragSelection provides dragSelection,
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                content = content,
            )
        }
    }
}
