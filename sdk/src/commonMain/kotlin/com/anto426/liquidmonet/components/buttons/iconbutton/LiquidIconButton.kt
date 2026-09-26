package com.anto426.liquidmonet.components.buttons.iconbutton

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.iconbutton.motion.LiquidIconButtonMotion
import com.anto426.liquidmonet.components.internal.LiquidControlDefaults
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

enum class LiquidIconButtonVariant {
    Standard,
    TopBarAction,
    Ghost,
    DropdownAnchor,
}

/** Canonical icon control, including icons used as dropdown anchors. */
@Composable
fun LiquidIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    iconSize: Dp = 24.dp,
    shape: Shape = LiquidControlDefaults.shape,
    backdropState: Backdrop = emptyBackdrop(),
    variant: LiquidIconButtonVariant = LiquidIconButtonVariant.Standard,
    contentColor: Color = Color.Unspecified,
    iconModifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
) {
    LiquidInputNormalization.positive(size, "LiquidIconButton size")
    LiquidInputNormalization.positive(iconSize, "LiquidIconButton iconSize")
    val performance = LocalLiquidGlassPerformance.current
    val interactiveHighlight = rememberLiquidControlHighlight()
    val glassColors = LiquidGlassTheme.colors
    val animatedContentColor by
        LiquidIconButtonMotion.animateColor(
            targetValue =
                if (enabled) {
                    if (contentColor.isSpecified) contentColor else glassColors.content
                } else {
                    glassColors.disabledContent
                },
            performance = performance,
            label = "iconButtonContentColor",
        )
    val resolvedContainerColor =
        if (containerColor.isSpecified) containerColor else glassColors.neutralContainer
    val animatedContainerColor by
        LiquidIconButtonMotion.animateColor(
            targetValue =
                when {
                    variant != LiquidIconButtonVariant.Standard -> Color.Transparent
                    enabled -> resolvedContainerColor
                    else ->
                        resolvedContainerColor.copy(alpha = resolvedContainerColor.alpha * 0.45f)
                },
            performance = performance,
            label = "iconButtonContainerColor",
        )

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Box(
        modifier =
            modifier
                .size(size)
                .liquidControlInteractive(
                    enabled = enabled,
                    interactiveHighlight = interactiveHighlight,
                    shape = shape,
                    backdrop =
                        if (variant == LiquidIconButtonVariant.Ghost) null else effectiveBackdrop,
                    containerColor = animatedContainerColor,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = animatedContentColor,
            modifier = iconModifier.size(iconSize),
        )
    }
}
