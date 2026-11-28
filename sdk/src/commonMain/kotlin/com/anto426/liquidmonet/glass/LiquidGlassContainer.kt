package com.anto426.liquidmonet.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPreset
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.RoundedRectangle

/**
 * Groups several liquid controls behind one optical surface.
 *
 * The container samples the scene once. Nested controls reuse that source and, in [Shared] mode,
 * keep only their shape, tint and interaction instead of recording another glass layer.
 */
enum class LiquidGlassContainerMode {
    /** One optical pass for the panel; descendants keep only shape, tint and interaction. */
    Shared,

    /** Descendants may independently sample the same scene backdrop. */
    Layered
}

@Composable
fun LiquidGlassContainer(
    modifier: Modifier = Modifier,
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    shape: Shape = RoundedRectangle(24.dp),
    role: LiquidGlassRole = LiquidGlassRole.Surface,
    containerColor: Color? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    preset: LiquidGlassPreset? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    mode: LiquidGlassContainerMode = LiquidGlassContainerMode.Shared,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
    content: @Composable BoxScope.() -> Unit
) {
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val effectiveContainerColor = containerColor ?: LiquidGlassContainerDefaults.containerColor()

    Box(
        // Keep the host itself unclipped. The optical surface below still clips to [shape], but
        // a pressed or dragged child is free to squash beyond the container and draw over its
        // siblings instead of being cut at the panel edge.
        modifier = modifier,
        contentAlignment = contentAlignment
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = role,
                    containerColor = effectiveContainerColor,
                    preset = preset,
                    backdropPolicy = backdropPolicy,
                    legibility = legibility
                )
        )

        CompositionLocalProvider(
            LocalContentColor provides contentColor,
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides mode,
            LocalLiquidGlassLegibility provides legibility
        ) {
            content()
        }
    }
}

/** Rendering contract inherited by glass descendants of a shared panel. */
internal val LocalLiquidGlassContainerMode =
    staticCompositionLocalOf { LiquidGlassContainerMode.Layered }

/** Visual defaults for a shared glass panel, aligned with Kyant's catalog containers. */
object LiquidGlassContainerDefaults {
    /**
     * Translucent panel fill that carries the light theme hue and preserves the sampled backdrop.
     */
    @Composable
    fun containerColor(): Color {
        val surface = MaterialTheme.colorScheme.surface
        val body = if (surface.luminance() > 0.5f) surface else LiquidGlassStyleManager.neutralSurfaceColor(false)
        return body.copy(alpha = 0.40f)
    }
}
