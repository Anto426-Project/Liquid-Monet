package com.anto426.liquidmonet.glass.runtime

import androidx.compose.runtime.Immutable
import com.anto426.liquidmonet.glass.LiquidGlassRole

/**
 * One global rendering decision shared by every Liquid Glass renderer.
 *
 * Components provide only their semantic [role]. They do not choose individual quality levels; the
 * active theme profile decides which effects are safe for that role.
 */
@Immutable
internal data class LiquidGlassEffectPolicy(
    val blur: Boolean,
    val refraction: Boolean,
    val chromaticAberration: Boolean,
    val highlight: Boolean,
    val shadow: Boolean,
    val innerShadow: Boolean,
)

/** Resolves the one effect budget used by both public surfaces and internal renderers. */
internal fun LiquidGlassPerformanceState.effectPolicy(
    role: LiquidGlassRole,
    interactive: Boolean = false,
    isCardSurface: Boolean = false,
): LiquidGlassEffectPolicy {
    val tier = opticalQualityTier ?: qualityTier
    val canRenderEffects = tier != LiquidGlassQualityTier.MINIMAL
    val isLargeSurface =
        role == LiquidGlassRole.Surface ||
            role == LiquidGlassRole.Dialog ||
            role == LiquidGlassRole.Sheet ||
            role == LiquidGlassRole.Menu
    val isInteractiveControl = role == LiquidGlassRole.Control && interactive
    val isNavigationInteraction = role == LiquidGlassRole.Navigation && interactive
    val isCompactFunctionalSurface =
        role == LiquidGlassRole.Control ||
            role == LiquidGlassRole.Navigation ||
            role == LiquidGlassRole.TopBar
    // Even the lightest material retains its compact touch lens. Large static surfaces
    // still require HIGH; sampling resolution remains an independent hardware budget.
    val canUseLens =
        refractionScale > 0f &&
            when (tier) {
                LiquidGlassQualityTier.MINIMAL -> isInteractiveControl || isNavigationInteraction
                LiquidGlassQualityTier.BALANCED -> isCompactFunctionalSurface
                LiquidGlassQualityTier.HIGH,
                LiquidGlassQualityTier.ULTRA ->
                    isLargeSurface || isCompactFunctionalSurface || interactive
            }

    return LiquidGlassEffectPolicy(
        blur =
            !isCardSurface && canRenderEffects && (role != LiquidGlassRole.Control || interactive),
        refraction = canUseLens,
        chromaticAberration = !isCardSurface && tier == LiquidGlassQualityTier.ULTRA && canUseLens,
        highlight = canRenderEffects,
        shadow =
            canRenderEffects && (isLargeSurface || isInteractiveControl || isNavigationInteraction),
        innerShadow =
            !isCardSurface &&
                tier == LiquidGlassQualityTier.ULTRA &&
                (isLargeSurface || isInteractiveControl || isNavigationInteraction),
    )
}

/** Decorative animation follows requested material fidelity and explicit accessibility settings. */
internal val LiquidGlassPerformanceState.animateBackground: Boolean
    get() =
        motionScale > 0f && (opticalQualityTier ?: qualityTier) != LiquidGlassQualityTier.MINIMAL

/**
 * Loading feedback stays animated at every sampling budget, unless motion is explicitly reduced.
 */
internal val LiquidGlassPerformanceState.animateFunctionalContent: Boolean
    get() = motionScale > 0f

internal val LiquidGlassPerformanceState.renderDetailedBackground: Boolean
    get() = (opticalQualityTier ?: qualityTier) != LiquidGlassQualityTier.MINIMAL
