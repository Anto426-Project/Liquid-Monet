package com.anto426.liquidmonet.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.anto426.liquidmonet.theme.monet.blend

/**
 * Controls how Liquid Glass adapts its optical transmission when floating over high-contrast or
 * inverse-luminance content (such as white PDF pages in dark theme or dark media in light theme).
 */
enum class LiquidGlassLegibility {
    /** Default crystal optical pipeline with neutral transmission. */
    Standard,

    /**
     * Real-time GPU luminance-adaptive glass.
     *
     * Leaves same-polarity backdrop regions crystal clear while smoothly attenuating opposing
     * luminance pixels (bright content under dark glass, dark content under light glass) so
     * foreground text and controls remain crisp without losing refraction.
     */
    Adaptive,

    /**
     * Stronger uniform optical frosting for ultra-dense overlays where readability takes priority
     * over crisp background visibility while preserving live glass lensing.
     */
    Frosted,
}

/** Ambient optical legibility mode inherited by Liquid Glass surfaces and hosted modals. */
val LocalLiquidGlassLegibility = staticCompositionLocalOf { LiquidGlassLegibility.Standard }

/**
 * Scopes [legibility] to [content] and any Liquid Glass surfaces, sheets, dialogs, or menus
 * composed within it.
 */
@Composable
fun LiquidGlassLegibilityScope(
    legibility: LiquidGlassLegibility,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalLiquidGlassLegibility provides legibility, content = content)
}

@Immutable
internal data class LiquidGlassLegibilitySpec(
    val useShader: Boolean,
    val darkSurface: Boolean,
    val threshold: Float,
    val targetLuma: Float,
    val strength: Float,
    val tintColor: Color,
    val blurMultiplier: Float,
    val fallbackBrightnessDelta: Float,
    val fallbackContrast: Float,
    val fallbackMinSurfaceAlpha: Float,
    val shaderMinSurfaceAlpha: Float,
)

/** Pure optical curve resolver shared by the GPU shader path and the non-RuntimeShader fallback. */
internal object LiquidGlassLegibilityPolicy {
    private val StandardSpec =
        LiquidGlassLegibilitySpec(
            useShader = false,
            darkSurface = false,
            threshold = 0.5f,
            targetLuma = 0.5f,
            strength = 0f,
            tintColor = Color.Transparent,
            blurMultiplier = 1f,
            fallbackBrightnessDelta = 0f,
            fallbackContrast = 1f,
            fallbackMinSurfaceAlpha = 0f,
            shaderMinSurfaceAlpha = 0f,
        )

    fun resolve(
        legibility: LiquidGlassLegibility,
        role: LiquidGlassRole,
        isLightSurface: Boolean,
        surfaceColor: Color,
        primaryColor: Color,
        liquidStrength: Float,
    ): LiquidGlassLegibilitySpec {
        if (legibility == LiquidGlassLegibility.Standard) return StandardSpec

        val clampedIntensity = liquidStrength.coerceIn(0f, 1f)
        val intensityScale = 0.45f + 0.55f * clampedIntensity
        val isModal =
            role == LiquidGlassRole.Sheet ||
                role == LiquidGlassRole.Dialog ||
                role == LiquidGlassRole.Menu
        val darkSurface = !isLightSurface
        val tintColor =
            if (darkSurface) {
                blend(Color(0xFF121418), primaryColor, 0.12f)
            } else {
                blend(surfaceColor, Color.White, 0.45f)
            }

        return when (legibility) {
            LiquidGlassLegibility.Standard -> StandardSpec
            LiquidGlassLegibility.Adaptive ->
                if (darkSurface) {
                    LiquidGlassLegibilitySpec(
                        useShader = true,
                        darkSurface = true,
                        threshold = if (isModal) 0.20f else 0.24f,
                        targetLuma = if (isModal) 0.22f else 0.25f,
                        strength = (if (isModal) 0.86f else 0.82f) * intensityScale,
                        tintColor = tintColor,
                        blurMultiplier = if (isModal) 1.15f else 1.10f,
                        fallbackBrightnessDelta = if (isModal) -0.15f else -0.12f,
                        fallbackContrast = if (isModal) 0.84f else 0.88f,
                        fallbackMinSurfaceAlpha = if (isModal) 0.38f else 0.30f,
                        shaderMinSurfaceAlpha = 0f,
                    )
                } else {
                    LiquidGlassLegibilitySpec(
                        useShader = true,
                        darkSurface = false,
                        threshold = if (isModal) 0.62f else 0.56f,
                        targetLuma = if (isModal) 0.72f else 0.68f,
                        strength = (if (isModal) 0.80f else 0.76f) * intensityScale,
                        tintColor = tintColor,
                        blurMultiplier = if (isModal) 1.15f else 1.10f,
                        fallbackBrightnessDelta = if (isModal) 0.13f else 0.10f,
                        fallbackContrast = if (isModal) 0.86f else 0.90f,
                        fallbackMinSurfaceAlpha = if (isModal) 0.34f else 0.28f,
                        shaderMinSurfaceAlpha = 0f,
                    )
                }
            LiquidGlassLegibility.Frosted ->
                if (darkSurface) {
                    LiquidGlassLegibilitySpec(
                        useShader = true,
                        darkSurface = true,
                        threshold = 0.12f,
                        targetLuma = 0.18f,
                        strength = 0.92f * intensityScale,
                        tintColor = tintColor,
                        blurMultiplier = 1.28f,
                        fallbackBrightnessDelta = -0.20f,
                        fallbackContrast = 0.80f,
                        fallbackMinSurfaceAlpha = 0.48f,
                        shaderMinSurfaceAlpha = 0.30f,
                    )
                } else {
                    LiquidGlassLegibilitySpec(
                        useShader = true,
                        darkSurface = false,
                        threshold = 0.72f,
                        targetLuma = 0.78f,
                        strength = 0.88f * intensityScale,
                        tintColor = tintColor,
                        blurMultiplier = 1.28f,
                        fallbackBrightnessDelta = 0.18f,
                        fallbackContrast = 0.82f,
                        fallbackMinSurfaceAlpha = 0.46f,
                        shaderMinSurfaceAlpha = 0.30f,
                    )
                }
        }
    }
}
