package com.anto426.liquidmonet.components.selection.checkbox

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.selection.checkbox.motion.LiquidCheckboxMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidCheckbox - Liquid Glass Checkbox with Snell Lensing, Vibrant Monet Transitions, and Tactile
 * Bounce.
 */
@Composable
fun LiquidCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val performance = LocalLiquidGlassPerformance.current
    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val glassColors = LiquidGlassTheme.colors

    val shape = RoundedRectangle(8.dp)

    val animatedContainerColor by
        LiquidCheckboxMotion.animateColor(
            targetValue =
                if (checked) {
                    glassColors.accentContainer
                } else {
                    glassColors.neutralContainer
                },
            performance = performance,
            label = "checkboxContainerColor",
        )

    val animatedScale by
        LiquidCheckboxMotion.animateFloat(
            targetValue = if (checked) 1.05f else 1.0f,
            performance = performance,
            label = "checkboxScalePop",
        )

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = animatedScale
                    scaleY = animatedScale
                }
                .size(24.dp)
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Control,
                    containerColor = animatedContainerColor,
                    layerBlock = liquidControlLayerBlock(enabled, interactiveHighlight),
                )
                .then(
                    if (onCheckedChange != null) {
                        Modifier.clickable(
                                interactionSource = null,
                                indication = null,
                                role = Role.Checkbox,
                                enabled = enabled,
                                onClick = { onCheckedChange(!checked) },
                            )
                            .liquidControlPressFeedback(
                                enabled,
                                interactiveHighlight,
                                shape = shape,
                            )
                    } else Modifier
                ),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = checked,
            enter = remember(performance) { LiquidCheckboxMotion.enter(performance) },
            exit = remember(performance) { LiquidCheckboxMotion.exit(performance) },
        ) {
            Canvas(modifier = Modifier.size(14.dp)) {
                val w = size.width
                val h = size.height
                val checkPath =
                    Path().apply {
                        moveTo(w * 0.15f, h * 0.52f)
                        lineTo(w * 0.42f, h * 0.80f)
                        lineTo(w * 0.88f, h * 0.22f)
                    }
                drawPath(
                    path = checkPath,
                    color = primaryColor,
                    style =
                        Stroke(
                            width = 2.4.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )
            }
        }
    }
}
