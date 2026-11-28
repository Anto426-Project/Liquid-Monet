package com.kyant.backdrop.effects

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.internal.AdaptiveLegibilityShaderString

/**
 * Applies per-pixel luminance-adaptive optical attenuation on the sampled backdrop.
 *
 * When [darkSurface] is true, dark pixels pass through untouched while bright pixels above
 * [threshold] are smoothly compressed toward [targetLuma] and [tintColor]. When [darkSurface] is
 * false, bright pixels pass through untouched while dark pixels below [threshold] are softly lifted.
 */
fun BackdropEffectScope.adaptiveLegibility(
    darkSurface: Boolean,
    threshold: Float,
    targetLuma: Float,
    strength: Float,
    tintColor: Color,
) {
    val clampedStrength = strength.coerceIn(0f, 1f)
    if (clampedStrength <= 0.0001f) return

    val srgb = tintColor.convert(ColorSpaces.Srgb)
    runtimeShaderEffect(
        key = "AdaptiveLegibility",
        shaderString = AdaptiveLegibilityShaderString,
        uniformShaderName = "content",
    ) {
        setFloatUniform("darkSurface", if (darkSurface) 1f else 0f)
        setFloatUniform("threshold", threshold.coerceIn(0.05f, 0.95f))
        setFloatUniform("targetLuma", targetLuma.coerceIn(0.05f, 0.95f))
        setFloatUniform("strength", clampedStrength)
        setFloatUniform("tintRgb", srgb.red, srgb.green, srgb.blue)
    }
}
