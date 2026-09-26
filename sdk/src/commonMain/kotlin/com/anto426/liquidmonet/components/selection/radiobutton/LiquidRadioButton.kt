package com.anto426.liquidmonet.components.selection.radiobutton

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.selection.radiobutton.motion.LiquidRadioButtonMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/** LiquidRadioButton - Liquid Glass Radio Button with Animated Concentric Dot. */
@Composable
fun LiquidRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val interactiveHighlight = rememberLiquidControlHighlight()
    val hapticFeedback = LocalHapticFeedback.current
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val glassColors = LiquidGlassTheme.colors

    val shape = Capsule()

    val animatedContainerColor by
        LiquidRadioButtonMotion.animateColor(
            targetValue =
                if (selected) {
                    glassColors.accentContainer
                } else {
                    glassColors.neutralContainer
                },
            performance = performance,
            label = "radioContainerColor",
        )

    val animatedScale by
        LiquidRadioButtonMotion.animateFloatPrimary(
            targetValue = if (selected) 1.04f else 1.0f,
            performance = performance,
            label = "radioScalePop",
        )
    val dotProgress by
        LiquidRadioButtonMotion.animateFloatSecondary(
            targetValue = if (selected) 1f else 0f,
            performance = performance,
            label = "radioLiquidDot",
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
                    if (onClick != null) {
                        Modifier.clickable(
                                interactionSource = null,
                                indication = null,
                                role = Role.RadioButton,
                                enabled = enabled,
                                onClick = {
                                    hapticFeedback.performLiquidHaptic(LiquidHapticCue.Selection)
                                    onClick()
                                },
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
        Box(
            modifier =
                Modifier.size(10.dp)
                    .graphicsLayer {
                        alpha = dotProgress.coerceIn(0f, 1f)
                        // Slightly asymmetric scaling makes selection feel like a settling droplet.
                        scaleX = (0.2f + 0.8f * dotProgress).coerceAtLeast(0f)
                        scaleY = (0.1f + 0.9f * dotProgress).coerceAtLeast(0f)
                    }
                    .background(primaryColor, CircleShape)
        )
    }
}
