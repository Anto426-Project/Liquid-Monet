package com.anto426.liquidmonet.components.cards.card

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidCard - Liquid Glass Card with Snell optical refraction.
 *
 * Interaction is inferred from [onClick]. Clickable cards receive the shared Liquid press response;
 * container cards remain stable so nested controls and gestures do not compete with their parent.
 */
@Composable
fun LiquidCard(
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
    shape: Shape = RoundedRectangle(24.dp),
    colors: LiquidCardColors = LiquidCardDefaults.colors(),
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    LiquidInputNormalization.nonNegative(contentPadding, "LiquidCard contentPadding")
    val isInteractive = onClick != null
    val interactiveHighlight = rememberLiquidControlHighlight()
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val cardLayerBlock = liquidControlLayerBlock(isInteractive, interactiveHighlight)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .then(
                    if (isInteractive && cardLayerBlock != null) {
                        Modifier.graphicsLayer(cardLayerBlock)
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (isInteractive) {
                        Modifier.liquidControlPressFeedback(
                            enabled = true,
                            interactiveHighlight = interactiveHighlight,
                            shape = shape,
                            drawHighlightOverlay = true,
                        )
                    } else Modifier
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = null,
                            indication = null,
                            role = Role.Button,
                            onClick = onClick,
                        )
                    } else Modifier
                )
    ) {
        // Optical glass surface
        Box(
            modifier =
                Modifier.matchParentSize()
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        isCardSurface = true,
                        role = LiquidGlassRole.Surface,
                        containerColor = colors.containerColor,
                    )
        )

        Box(modifier = Modifier.align(Alignment.Center).fillMaxWidth().padding(contentPadding)) {
            CompositionLocalProvider(
                LocalContentColor provides colors.contentColor,
                LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
            ) {
                content()
            }
        }
    }
}

/** Semantic colors for a Liquid card surface. */
@Immutable
data class LiquidCardColors(
    val containerColor: Color?,
    val contentColor: Color,
)

/** Defaults shared by static and clickable Liquid cards. */
object LiquidCardDefaults {
    @Composable
    fun colors(
        // A normal card does not own a color. Null delegates its material to the shared glass
        // pipeline, whose ambient Monet reflection is then exported to every nested control.
        containerColor: Color? = null,
        contentColor: Color =
            LiquidGlassDefaults.contentColorFor(
                containerColor = containerColor,
                colorScheme = MaterialTheme.colorScheme,
            ),
    ): LiquidCardColors =
        remember(containerColor, contentColor) {
            LiquidCardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            )
        }
}
