package com.anto426.liquidmonet.glass.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.LiquidGlassRole

/** Highlight treatment for a dynamic glass layer. */
internal enum class LiquidGlassDynamicHighlight {
    None,
    Ambient,
    Directional,
}

/**
 * Optical recipe for a moving or deforming glass layer.
 *
 * Components select one semantic recipe and provide only live interaction progress. This keeps
 * blur, refraction, highlight and depth values out of slider, switch and navigation renderers.
 */
@Immutable
internal data class LiquidGlassDynamicPreset(
    val role: LiquidGlassRole,
    val optics: LiquidGlassPreset,
    val blurAtRest: Float,
    val blurWhenActive: Float,
    val refractionAtRest: Float,
    val refractionWhenActive: Float,
    val refractionHeightScale: Float = 1f,
    val refractionAmountScale: Float = 1f,
    val highlight: LiquidGlassDynamicHighlight = LiquidGlassDynamicHighlight.None,
    val highlightWidth: Dp = 0.dp,
    val highlightBlurRadius: Dp = 0.dp,
    val highlightAtRest: Float = 0f,
    val highlightWhenActive: Float = 0f,
    val shadowRadius: Dp = 0.dp,
    val shadowColorAlpha: Float = 0f,
    val shadowAtRest: Float = 0f,
    val shadowWhenActive: Float = 0f,
    val innerShadowRadius: Dp = 0.dp,
    val innerShadowAtRest: Float = 0f,
    val innerShadowWhenActive: Float = 0f,
    val vibrancy: Boolean = false,
    // Moving controls use Kyant's magnifying lens, not the stable panel's depth treatment.
    val depthEffect: Boolean = false,
)

/** Canonical recipes used by the SDK's custom moving glass layers. */
internal object LiquidGlassDynamicPresets {
    val SliderThumb =
        LiquidGlassDynamicPreset(
            role = LiquidGlassRole.Control,
            optics = LiquidGlassPresets.Interactive,
            blurAtRest = 1f,
            blurWhenActive = 0f,
            refractionAtRest = 0f,
            refractionWhenActive = 1f,
            highlight = LiquidGlassDynamicHighlight.Ambient,
            highlightWidth = 1.2.dp,
            highlightBlurRadius = 2.dp,
            highlightWhenActive = 1f,
            shadowRadius = 4.dp,
            shadowColorAlpha = 0.05f,
            shadowAtRest = 1f,
            shadowWhenActive = 1f,
            innerShadowRadius = 4.dp,
            innerShadowWhenActive = 1f,
        )

    val ToggleThumb =
        SliderThumb.copy(
            refractionHeightScale = 0.5f,
            refractionAmountScale = 10f / 14f,
            highlightWidth = 1.dp / 3f,
            highlightBlurRadius = 1.dp / 6f,
        )

    val NavigationMask =
        LiquidGlassDynamicPreset(
            role = LiquidGlassRole.Navigation,
            optics = LiquidGlassPresets.Navigation,
            blurAtRest = 1f,
            blurWhenActive = 1f,
            refractionAtRest = 0f,
            refractionWhenActive = 1f,
            highlight = LiquidGlassDynamicHighlight.Directional,
            highlightWidth = 0.5.dp,
            highlightBlurRadius = 0.25.dp,
            highlightWhenActive = 1f,
            vibrancy = true,
        )

    val NavigationDroplet =
        LiquidGlassDynamicPreset(
            role = LiquidGlassRole.Navigation,
            // Kyant's moving lens samples the already-blurred panel/tab mask. A second panel-size
            // blur and lens washes out the magnified icons; use the shared compact lens instead.
            optics = LiquidGlassPresets.Interactive,
            blurAtRest = 0f,
            blurWhenActive = 0f,
            refractionAtRest = 0f,
            refractionWhenActive = 1f,
            highlight = LiquidGlassDynamicHighlight.Directional,
            highlightWidth = 0.5.dp,
            highlightBlurRadius = 0.25.dp,
            highlightAtRest = 0f,
            highlightWhenActive = 1f,
            shadowRadius = 10.dp,
            shadowColorAlpha = 0.07f,
            shadowAtRest = 0f,
            shadowWhenActive = 1f,
            innerShadowRadius = 8.dp,
            innerShadowAtRest = 0f,
            innerShadowWhenActive = 1f,
        )
}
