package com.anto426.liquidmonet.components.internal

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidControlDefaults
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.kyant.backdrop.Backdrop

/**
 * LiquidGlassButton - Radiant Optical Liquid Glass Button with Animated Interactive States.
 * Balances vibrant saturated Monet colors with Snell lens refraction,
 * chromatic prismatic dispersion, and ambient specular highlights.
 */
@Composable
internal fun LiquidGlassButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    isInteractive: Boolean = true,
    enabled: Boolean = isInteractive,
    shape: Shape = LiquidControlDefaults.shape,
    height: Dp = 48.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp),
    border: BorderStroke? = null,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable RowScope.() -> Unit
) {
    val interactiveHighlight = rememberLiquidControlHighlight()
    val performance = LocalLiquidGlassPerformance.current

    val targetContainerColor = when {
        surfaceColor.isSpecified -> surfaceColor
        tint.isSpecified -> tint
        else -> null
    }

    val animatedContainerColor = if (targetContainerColor != null) {
        animateColorAsState(
            targetValue = if (enabled) {
                targetContainerColor
            } else {
                targetContainerColor.copy(alpha = targetContainerColor.alpha * 0.45f)
            },
            animationSpec = LiquidMotion.tween(performance, 240),
            label = "buttonContainerColor"
        ).value
    } else null

    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.50f,
        animationSpec = LiquidMotion.tween(performance, 240),
        label = "buttonAlpha"
    )

    val glassModifier = modifier
        .liquidControlInteractive(
            enabled = isInteractive && enabled,
            interactiveHighlight = interactiveHighlight,
            backdrop = backdrop,
            shape = shape,
            containerColor = animatedContainerColor,
            onClick = onClick
        )
        .let { if (border != null) it.border(border, shape) else it }
        .height(height)
        .padding(contentPadding)
        .graphicsLayer { alpha = animatedAlpha }

    Row(
        modifier = glassModifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}
