package com.anto426.liquidmonet.components.cards.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.internal.motion.LiquidCardMotion
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassLegibility
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LocalLiquidGlassLegibility
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.RoundedRectangle

private val LocalLiquidCardClipsContent = staticCompositionLocalOf { false }

/**
 * LiquidCard - Canonical Liquid Glass card surface with Snell optical refraction.
 *
 * Interaction is inferred from [onClick] / [onLongClick]. Clickable cards receive the shared
 * Liquid press response; container cards remain stable so nested controls and gestures do not
 * compete with their parent.
 */
@Composable
fun LiquidCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
    shape: Shape = RoundedRectangle(24.dp),
    colors: LiquidCardColors = LiquidCardDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    border: BorderStroke? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    fillWidth: Boolean = true,
    contentModifier: Modifier = Modifier,
    clipContent: Boolean = true,
    enabled: Boolean = true,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    LiquidInputNormalization.nonNegative(
        contentPadding.calculateStartPadding(layoutDirection),
        "LiquidCard contentPadding",
    )
    LiquidInputNormalization.nonNegative(
        contentPadding.calculateEndPadding(layoutDirection),
        "LiquidCard contentPadding",
    )
    LiquidInputNormalization.nonNegative(
        contentPadding.calculateTopPadding(),
        "LiquidCard contentPadding",
    )
    LiquidInputNormalization.nonNegative(
        contentPadding.calculateBottomPadding(),
        "LiquidCard contentPadding",
    )

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val ancestorClipsContent = LocalLiquidCardClipsContent.current
    val cardMotion =
        if (ancestorClipsContent) {
            LiquidCardMotion.Contained
        } else {
            LiquidCardMotion.Standalone
        }

    Box(
        modifier =
            modifier
                .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                .liquidClickable(
                    shape = shape,
                    clipContent = false,
                    enabled = enabled,
                    cardMotion = cardMotion,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        propagateMinConstraints = true,
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
                        backdropPolicy = backdropPolicy,
                        legibility = legibility,
                    )
                    .then(if (border != null) Modifier.border(border, shape) else Modifier)
        )

        Box(
            modifier = if (clipContent) Modifier.clip(shape) else Modifier,
            contentAlignment = contentAlignment,
            propagateMinConstraints = propagateMinConstraints,
        ) {
            Box(
                modifier =
                    Modifier.then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                        .padding(contentPadding)
                        .then(contentModifier),
                contentAlignment = contentAlignment,
                propagateMinConstraints = propagateMinConstraints,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides colors.contentColor,
                    LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                    LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
                    LocalLiquidCardClipsContent provides (clipContent || ancestorClipsContent),
                ) {
                    content()
                }
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
