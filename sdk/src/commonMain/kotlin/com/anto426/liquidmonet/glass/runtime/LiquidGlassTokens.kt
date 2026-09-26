package com.anto426.liquidmonet.glass.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Unscaled design values for a family of liquid-glass surfaces. */
@Immutable
data class LiquidGlassPreset(
    val blurRadius: Dp,
    val refractionHeight: Dp,
    val refractionAmount: Dp,
    val chromaticAberration: Float,
) {
    init {
        require(blurRadius >= 0.dp) { "LiquidGlassPreset blurRadius must not be negative." }
        require(refractionHeight >= 0.dp) {
            "LiquidGlassPreset refractionHeight must not be negative."
        }
        require(refractionAmount >= 0.dp) {
            "LiquidGlassPreset refractionAmount must not be negative."
        }
        require(chromaticAberration.isFinite() && chromaticAberration >= 0f) {
            "LiquidGlassPreset chromaticAberration must be finite and non-negative."
        }
    }

    /** Resolves this visual preset against current device and user intensity. */
    fun resolve(performance: LiquidGlassPerformanceState): LiquidGlassTokens =
        LiquidGlassTokens(
            blurRadius = blurRadius * performance.blurScale,
            refractionHeight = refractionHeight * performance.refractionScale,
            refractionAmount = refractionAmount * performance.refractionScale,
            chromaticAberration = chromaticAberration * performance.chromaticAberrationScale,
            motionScale = performance.motionScale,
            qualityTier = performance.qualityTier,
        )
}

/** Final values which components can consume without repeating adaptation logic. */
@Immutable
data class LiquidGlassTokens(
    val blurRadius: Dp,
    val refractionHeight: Dp,
    val refractionAmount: Dp,
    val chromaticAberration: Float,
    val motionScale: Float,
    val qualityTier: LiquidGlassQualityTier,
)

/** Shared optical amplitudes; hardware budgets change sampling, not these material values. */
object LiquidGlassPresets {
    val Subtle =
        LiquidGlassPreset(
            blurRadius = 2.dp,
            refractionHeight = 10.dp,
            refractionAmount = 20.dp,
            chromaticAberration = 0.08f,
        )

    val Standard =
        LiquidGlassPreset(
            blurRadius = 14.dp,
            refractionHeight = 20.dp,
            refractionAmount = 44.dp,
            chromaticAberration = 0.18f,
        )

    /** Strong navigation lens with the existing 8dp blur and compact edge band. */
    val Navigation =
        LiquidGlassPreset(
            blurRadius = 8.dp,
            refractionHeight = 24.dp,
            refractionAmount = 32.dp,
            chromaticAberration = 0.14f,
        )

    /** Compact moving lens used by thumbs and the navigation selection droplet. */
    val Interactive =
        LiquidGlassPreset(
            blurRadius = 8.dp,
            refractionHeight = 10.dp,
            refractionAmount = 18.dp,
            chromaticAberration = 0.18f,
        )

    val Immersive =
        LiquidGlassPreset(
            blurRadius = 8.dp,
            refractionHeight = 26.dp,
            refractionAmount = 64.dp,
            chromaticAberration = 0.24f,
        )
}
