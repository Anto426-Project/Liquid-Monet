package com.anto426.liquidmonet.components.internal

import com.anto426.liquidmonet.glass.liquidInteractiveZIndex

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule

internal val LocalLiquidBottomTabScale =
    staticCompositionLocalOf { { 1f } }

internal val LocalLiquidBottomTabInteractive =
    staticCompositionLocalOf { true }

@Composable
internal fun RowScope.LiquidGlassBottomTab(
    onClick: () -> Unit,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val scale = LocalLiquidBottomTabScale.current
    val interactive = LocalLiquidBottomTabInteractive.current
    Column(
        modifier
            // Scale outside the shape clip: the tab keeps its capsule mask, while the complete
            // bounced result can grow over the navigation container and adjacent tab slots.
            .graphicsLayer {
                val s = scale()
                scaleX = s
                scaleY = s
                clip = false
            }
            .liquidInteractiveZIndex()
            .clip(Capsule())
            .then(
                if (interactive) {
                    Modifier.selectable(
                        selected = selected,
                        interactionSource = null,
                        indication = null,
                        role = Role.Tab,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            )
            .fillMaxHeight()
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(2f.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}
