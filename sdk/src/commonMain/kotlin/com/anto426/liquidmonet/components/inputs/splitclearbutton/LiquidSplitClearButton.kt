package com.anto426.liquidmonet.components.inputs.splitclearbutton

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.inputs.splitclearbutton.internal.liquidDropletBridge
import com.anto426.liquidmonet.components.inputs.splitclearbutton.motion.LiquidSplitClearButtonMotion.rememberSeparation
import com.anto426.liquidmonet.components.inputs.splitclearbutton.motion.liquidDropletReveal
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/**
 * A clear control that separates from its field as a droplet; press belongs to LiquidIconButton.
 */
@Composable
fun LiquidSplitClearButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    shape: Shape = Capsule(),
    backdropState: Backdrop = emptyBackdrop(),
    contentDescription: String = "Cancella",
    enabled: Boolean = true,
) {
    val performance = LocalLiquidGlassPerformance.current
    val colors = LiquidGlassTheme.colors
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val gap = 8.dp
    AnimatedVisibility(
        visible = visible,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
    ) {
        val progress = rememberSeparation(performance)
        Box(
            Modifier.liquidDropletReveal(progress, rtl)
                .size(width = size + gap, height = size)
                .liquidDropletBridge(progress, gap, rtl, colors.neutralContainer, colors.outline),
            contentAlignment = Alignment.CenterEnd,
        ) {
            LiquidIconButton(
                LiquidIcons.Close,
                onClick,
                modifier = modifier,
                size = size,
                iconSize = size * 0.38f,
                shape = shape,
                backdropState = backdropState,
                contentDescription = contentDescription,
                enabled = enabled && visible,
            )
        }
    }
}
