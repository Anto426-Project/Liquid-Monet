package com.anto426.liquidmonet.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.anto426.liquidmonet.theme.monet.blend
import com.anto426.liquidmonet.theme.monet.contentColorFor as monetContentColorFor

/**
 * Semantic colors shared by Liquid Monet components.
 *
 * Container colors are intentionally translucent: they are overlays applied to refracted scene
 * content, not opaque Material surfaces. Content colors remain derived from [ColorScheme] so they
 * retain contrast in both light and dark themes.
 */
@Immutable
data class LiquidGlassColors(
    val content: Color,
    val secondaryContent: Color,
    val disabledContent: Color,
    val neutralContainer: Color,
    val accentContainer: Color,
    val selectedContainer: Color,
    val inactiveTrack: Color,
    val activeTrack: Color,
    val focusIndicator: Color,
    val outline: Color,
    val scrim: Color,
    val success: Color,
    val warning: Color,
    val error: Color
)

object LiquidGlassDefaults {
    /** Builds adaptive glass colors from any Material 3 [colorScheme]. */
    fun colors(colorScheme: ColorScheme): LiquidGlassColors {
        val isLight = colorScheme.surface.luminance() > 0.5f
        return LiquidGlassColors(
            content = colorScheme.onSurface,
            secondaryContent = colorScheme.onSurfaceVariant,
            disabledContent = colorScheme.onSurface.copy(alpha = 0.38f),
            neutralContainer = colorScheme.onSurface.copy(alpha = if (isLight) 0.055f else 0.08f),
            accentContainer = colorScheme.primary.copy(alpha = if (isLight) 0.18f else 0.22f),
            selectedContainer = colorScheme.primary.copy(alpha = if (isLight) 0.24f else 0.28f),
            inactiveTrack = colorScheme.onSurface.copy(alpha = if (isLight) 0.14f else 0.18f),
            activeTrack = colorScheme.primary.copy(alpha = if (isLight) 0.56f else 0.52f),
            focusIndicator = colorScheme.primary.copy(alpha = if (isLight) 0.78f else 0.72f),
            outline =
                blend(colorScheme.onSurface, colorScheme.primary, 0.25f)
                    .copy(alpha = if (isLight) 0.24f else 0.26f),
            scrim = Color.Black.copy(alpha = if (isLight) 0.22f else 0.32f),
            success = if (isLight) Color(0xFF1E824C) else Color(0xFF4EE49B),
            warning = if (isLight) Color(0xFFD97706) else Color(0xFFFFB039),
            error = colorScheme.error
        )
    }

    /**
     * Uses the active Monet foreground for translucent glass and the theme engine's contrast
     * resolver only when a caller replaces the material with an effectively opaque color.
     */
    fun contentColorFor(
        containerColor: Color?,
        colorScheme: ColorScheme
    ): Color = if (containerColor != null && containerColor.alpha >= 0.50f) {
        monetContentColorFor(containerColor)
    } else {
        colorScheme.onSurface
    }
}

internal val LocalLiquidGlassColors = compositionLocalOf<LiquidGlassColors?> { null }

/** Access to the active semantic glass palette. */
object LiquidGlassTheme {
    val colors: LiquidGlassColors
        @Composable get() = LocalLiquidGlassColors.current
            ?: LiquidGlassDefaults.colors(MaterialTheme.colorScheme)
}
