package com.anto426.liquidmonet.components.menu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.navigation.LiquidTopBarAction
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.LiquidIconButtonVariant
import com.anto426.liquidmonet.glass.overlay.LiquidGlassDropdownPlacement
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.LiquidMotion
import com.kyant.backdrop.Backdrop

/** A top-bar action using the same dropdown lifecycle and interaction as every other menu. */
@Composable
internal fun LiquidMorphingAction(
    action: LiquidTopBarAction,
    isLastItem: Boolean,
    backdropState: Backdrop,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val anchorState = rememberLiquidGlassOverlayAnchorState()
    val performance = LocalLiquidGlassPerformance.current
    val isMorphing = expanded && action.subItems.isNotEmpty()
    val iconRotation by animateFloatAsState(
        targetValue = action.iconRotation + if (expanded) 90f else 0f,
        animationSpec = LiquidMotion.snappySpring(performance),
        label = "iconRotation"
    )

    Box(modifier = modifier.padding(start = 4.dp, end = if (isLastItem) 12.dp else 0.dp)) {
        LiquidIconButton(
            icon = action.icon,
            contentDescription = action.label,
            onClick = {
                if (action.subItems.isEmpty()) action.onClick() else expanded = !expanded
            },
            modifier = Modifier.liquidGlassOverlayAnchor(anchorState),
            iconSize = 20.dp,
            iconModifier = Modifier.graphicsLayer { rotationZ = iconRotation },
            variant = if (action.subItems.isEmpty()) LiquidIconButtonVariant.TopBarAction else LiquidIconButtonVariant.DropdownAnchor,
            backdropState = backdropState
        )
        LiquidDropdownMenu(
            expanded = isMorphing,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 190.dp, max = 260.dp),
            anchorState = anchorState,
            placement = LiquidGlassDropdownPlacement.AnchorTopEnd,
            offset = DpOffset.Zero,
            backdropState = backdropState
        ) {
            action.subItems.forEach { item ->
                LiquidMenuItem(
                    text = item.label,
                    icon = item.icon,
                    selected = item.selected,
                    onClick = {
                        expanded = false
                        item.onClick()
                    }
                )
            }
        }
    }
}
