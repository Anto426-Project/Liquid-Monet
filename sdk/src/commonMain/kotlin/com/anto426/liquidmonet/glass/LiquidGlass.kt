package com.anto426.liquidmonet.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPreset
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidGlass - Core Translucent Liquid Glass Container Primitive.
 * Neutral crystal glass surface without Monet color contamination.
 */
@Composable
fun LiquidGlass(
    modifier: Modifier = Modifier,
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    shape: Shape = RoundedRectangle(24.dp),
    blurRadius: Dp = LiquidGlassPresets.Standard.blurRadius,
    refractionHeight: Dp = LiquidGlassPresets.Standard.refractionHeight,
    refractionAmount: Dp = LiquidGlassPresets.Standard.refractionAmount,
    containerColor: Color? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    role: LiquidGlassRole = LiquidGlassRole.Surface,
    contentAlignment: Alignment = Alignment.TopStart,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
    content: @Composable BoxScope.() -> Unit,
) {
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val preset =
        remember(blurRadius, refractionHeight, refractionAmount) {
            LiquidGlassPreset(
                blurRadius = blurRadius,
                refractionHeight = refractionHeight,
                refractionAmount = refractionAmount,
                chromaticAberration = LiquidGlassPresets.Standard.chromaticAberration,
            )
        }
    Box(
        modifier =
            modifier.liquidGlass(
                backdrop = effectiveBackdrop,
                shape = shape,
                role = role,
                containerColor = containerColor,
                preset = preset,
                backdropPolicy = backdropPolicy,
                legibility = legibility,
            ),
        contentAlignment = contentAlignment,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
            LocalLiquidGlassLegibility provides legibility,
        ) {
            content()
        }
    }
}
