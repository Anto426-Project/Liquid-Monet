package com.anto426.liquidmonet.components.cards.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.internal.motion.rememberLiquidOwnedPressInteractionSource
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
    LiquidCardContainer(
        contentPadding = PaddingValues(contentPadding),
        contentAlignment = Alignment.Center,
        contentBoxAlignment = Alignment.TopStart,
        modifier = modifier,
        backdropState = backdropState,
        shape = shape,
        colors = colors,
        onClick = onClick,
        content = content,
    )
}

/**
 * Configurable card container. [contentPadding] accepts independent, RTL-aware edges.
 *
 * [modifier] controls the complete card; [contentModifier] controls only its content box.
 * Set [fillWidth] to false for a card that wraps its content, or provide a width through [modifier].
 * [clipContent] clips children to [shape] when requested; by default child motion may extend outside.
 * [enabled] disables the card's own click response without disabling controls inside it.
 * The Dp-padding overload retains the original API and defaults for existing consumers.
 */
@Composable
fun LiquidCard(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
    shape: Shape = RoundedRectangle(24.dp),
    colors: LiquidCardColors = LiquidCardDefaults.colors(),
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    fillWidth: Boolean = true,
    contentModifier: Modifier = Modifier,
    clipContent: Boolean = false,
    enabled: Boolean = true,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    LiquidCardContainer(
        contentPadding = contentPadding, modifier = modifier, backdropState = backdropState,
        shape = shape, colors = colors, onClick = onClick, border = border,
        contentAlignment = contentAlignment, contentBoxAlignment = contentAlignment,
        fillWidth = fillWidth, contentModifier = contentModifier, clipContent = clipContent,
        enabled = enabled, propagateMinConstraints = propagateMinConstraints, content = content,
    )
}

@Composable
private fun LiquidCardContainer(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
    shape: Shape = RoundedRectangle(24.dp),
    colors: LiquidCardColors = LiquidCardDefaults.colors(),
    onClick: (() -> Unit)? = null,
    border: BorderStroke? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    contentBoxAlignment: Alignment = contentAlignment,
    fillWidth: Boolean = true,
    contentModifier: Modifier = Modifier,
    clipContent: Boolean = false,
    enabled: Boolean = true,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    listOf(
        contentPadding.calculateStartPadding(layoutDirection),
        contentPadding.calculateEndPadding(layoutDirection),
        contentPadding.calculateTopPadding(),
        contentPadding.calculateBottomPadding(),
    ).forEach { LiquidInputNormalization.nonNegative(it, "LiquidCard contentPadding") }
    val isInteractive = onClick != null && enabled
    val interactiveHighlight = rememberLiquidControlHighlight()
    val interactionSource = rememberLiquidOwnedPressInteractionSource(isInteractive, interactiveHighlight)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val cardLayerBlock = liquidControlLayerBlock(isInteractive, interactiveHighlight)

    Box(
        modifier =
            modifier
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .then(if (clipContent) Modifier.clip(shape) else Modifier)
                .then(
                    if (isInteractive && cardLayerBlock != null) {
                        Modifier.graphicsLayer(cardLayerBlock)
                    } else {
                        Modifier
                    }
                )
                .then(
                    if (isInteractive) {
                        interactiveHighlight.modifier(clipShape = shape)
                            .then(interactiveHighlight.pointerTrackingModifier)
                    } else Modifier
                )
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            enabled = enabled,
                            interactionSource = interactionSource,
                            indication = null,
                            role = Role.Button,
                            onClick = onClick,
                        )
                    } else Modifier
                ),
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
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
                    .then(if (border != null) Modifier.border(border, shape) else Modifier)
        )

        Box(
            modifier = Modifier.then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .padding(contentPadding).then(contentModifier),
            contentAlignment = contentBoxAlignment,
            propagateMinConstraints = propagateMinConstraints,
        ) {
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
