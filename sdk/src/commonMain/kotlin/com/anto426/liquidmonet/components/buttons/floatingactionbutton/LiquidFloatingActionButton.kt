package com.anto426.liquidmonet.components.buttons.floatingactionbutton

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.floatingactionbutton.motion.LiquidFloatingActionButtonMotion
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidFloatingActionButton - Pure Crystal Liquid Glass Floating Action Button. Uses the exact
 * same 1:1 optical liquid glass pipeline as LiquidDialog & LiquidSheet with Snell lens refraction,
 * chromatic dispersion, specular highlights, and 3D shadows.
 */
@Composable
fun LiquidFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    visible: Boolean = true,
    expanded: Boolean = true,
    label: (@Composable () -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop(),
    enabled: Boolean = true,
    containerColor: Color? = null,
    shape: Shape = RoundedRectangle(16.dp),
    content: @Composable () -> Unit,
) {
    LiquidInputNormalization.positive(size, "LiquidFloatingActionButton size")
    val performance = LocalLiquidGlassPerformance.current
    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val fabOffsetY by
        LiquidFloatingActionButtonMotion.animateDpPrimary(
            targetValue = if (visible) 0.dp else 160.dp,
            performance = performance,
            label = "fabOffsetY",
        )
    val fabVisibility by
        LiquidFloatingActionButtonMotion.animateFloat(
            targetValue = if (visible) 1f else 0f,
            performance = performance,
            label = "fabVisibility",
        )
    val horizontalPadding by
        LiquidFloatingActionButtonMotion.animateDpSecondary(
            targetValue = if (label != null && expanded) 20.dp else 0.dp,
            performance = performance,
            label = "fabHorizontalPadding",
        )
    val resolvedContainerColor = containerColor ?: glassColors.accentContainer
    val animatedContainerColor by
        LiquidFloatingActionButtonMotion.animateColor(
            targetValue =
                if (enabled) {
                    resolvedContainerColor
                } else {
                    resolvedContainerColor.copy(alpha = resolvedContainerColor.alpha * 0.45f)
                },
            performance = performance,
            label = "fabContainerColor",
        )
    val animatedContentColor by
        LiquidFloatingActionButtonMotion.animateColor(
            targetValue =
                if (enabled) {
                    LiquidGlassDefaults.contentColorFor(resolvedContainerColor, colorScheme)
                } else {
                    glassColors.disabledContent
                },
            performance = performance,
            label = "fabContentColor",
        )

    Row(
        modifier =
            modifier
                .graphicsLayer {
                    translationY = fabOffsetY.toPx()
                    alpha = fabVisibility
                    scaleX = 0.84f + 0.16f * fabVisibility
                    scaleY = 0.84f + 0.16f * fabVisibility
                }
                .height(size)
                .defaultMinSize(minWidth = size)
                .liquidControlInteractive(
                    enabled = enabled && visible,
                    interactiveHighlight = interactiveHighlight,
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    containerColor = animatedContainerColor,
                    onClick = onClick,
                )
                .padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides animatedContentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                content()
                if (label != null) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter =
                            remember(performance) {
                                LiquidFloatingActionButtonMotion.enter(performance)
                            },
                        exit =
                            remember(performance) {
                                LiquidFloatingActionButtonMotion.exit(performance)
                            },
                    ) {
                        label()
                    }
                }
            }
        }
    }
}
