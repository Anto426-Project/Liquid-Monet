package com.anto426.liquidmonet.components.inputs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidMotion
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/** Animated clear affordance; press feedback belongs solely to LiquidIconButton. */
@Composable
fun LiquidSplitClearButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    shape: Shape = Capsule(),
    backdropState: Backdrop = emptyBackdrop(),
    contentDescription: String = "Cancella",
    enabled: Boolean = true
) {
    val performance = LocalLiquidGlassPerformance.current
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(
            animationSpec = LiquidMotion.snappySpring(performance),
            expandFrom = Alignment.Start
        ) + fadeIn(animationSpec = LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis)),
        exit = shrinkHorizontally(
            animationSpec = LiquidMotion.snappySpring(performance),
            shrinkTowards = Alignment.Start
        ) + fadeOut(animationSpec = LiquidMotion.tween(performance, LiquidMotion.FastDurationMillis))
    ) {
        LiquidIconButton(
            icon = LiquidIcons.Close,
            onClick = onClick,
            modifier = modifier.padding(start = 8.dp),
            size = size,
            iconSize = size * 0.38f,
            shape = shape,
            backdropState = backdropState,
            contentDescription = contentDescription,
            enabled = enabled && visible
        )
    }
}
