package com.anto426.liquidmonet.glass

import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.internal.BuiltinRuntimeShader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiquidGlassLegibilityTest {
    @Test
    fun standardModeDisablesAdaptiveShaderAndFallbackAdjustments() {
        val spec =
            LiquidGlassLegibilityPolicy.resolve(
                legibility = LiquidGlassLegibility.Standard,
                role = LiquidGlassRole.Sheet,
                isLightSurface = false,
                surfaceColor = Color(0xFF121212),
                primaryColor = Color(0xFF64B5F6),
                liquidStrength = 1f,
            )

        assertFalse(spec.useShader)
        assertEquals(0f, spec.strength)
        assertEquals(1f, spec.blurMultiplier)
        assertEquals(0f, spec.fallbackBrightnessDelta)
        assertEquals(1f, spec.fallbackContrast)
        assertEquals(0f, spec.fallbackMinSurfaceAlpha)
    }

    @Test
    fun adaptiveDarkModeCompressesBrightBackdropsAndLeavesModalStrongerThanChrome() {
        val sheetSpec =
            LiquidGlassLegibilityPolicy.resolve(
                legibility = LiquidGlassLegibility.Adaptive,
                role = LiquidGlassRole.Sheet,
                isLightSurface = false,
                surfaceColor = Color(0xFF121212),
                primaryColor = Color(0xFF64B5F6),
                liquidStrength = 1f,
            )
        val topBarSpec =
            LiquidGlassLegibilityPolicy.resolve(
                legibility = LiquidGlassLegibility.Adaptive,
                role = LiquidGlassRole.TopBar,
                isLightSurface = false,
                surfaceColor = Color(0xFF121212),
                primaryColor = Color(0xFF64B5F6),
                liquidStrength = 1f,
            )

        assertTrue(sheetSpec.useShader)
        assertTrue(sheetSpec.darkSurface)
        assertTrue(sheetSpec.strength > topBarSpec.strength)
        assertTrue(sheetSpec.threshold < topBarSpec.threshold)
        assertTrue(sheetSpec.fallbackBrightnessDelta < 0f)
        assertTrue(sheetSpec.fallbackContrast < 1f)
        assertEquals(0f, sheetSpec.shaderMinSurfaceAlpha)
    }

    @Test
    fun adaptiveLightModeLiftsDarkBackdropsWithPositiveFallbackBrightness() {
        val spec =
            LiquidGlassLegibilityPolicy.resolve(
                legibility = LiquidGlassLegibility.Adaptive,
                role = LiquidGlassRole.Sheet,
                isLightSurface = true,
                surfaceColor = Color(0xFFFAFAFA),
                primaryColor = Color(0xFF1565C0),
                liquidStrength = 1f,
            )

        assertTrue(spec.useShader)
        assertFalse(spec.darkSurface)
        assertTrue(spec.targetLuma > spec.threshold)
        assertTrue(spec.fallbackBrightnessDelta > 0f)
    }

    @Test
    fun frostedModeStrengthensAttenuationAndAddsBodyAlphaFloor() {
        val adaptive =
            LiquidGlassLegibilityPolicy.resolve(
                legibility = LiquidGlassLegibility.Adaptive,
                role = LiquidGlassRole.Sheet,
                isLightSurface = false,
                surfaceColor = Color(0xFF121212),
                primaryColor = Color(0xFF64B5F6),
                liquidStrength = 1f,
            )
        val frosted =
            LiquidGlassLegibilityPolicy.resolve(
                legibility = LiquidGlassLegibility.Frosted,
                role = LiquidGlassRole.Sheet,
                isLightSurface = false,
                surfaceColor = Color(0xFF121212),
                primaryColor = Color(0xFF64B5F6),
                liquidStrength = 1f,
            )

        assertTrue(frosted.strength > adaptive.strength)
        assertTrue(frosted.blurMultiplier > adaptive.blurMultiplier)
        assertTrue(frosted.shaderMinSurfaceAlpha > 0f)
        assertTrue(frosted.fallbackMinSurfaceAlpha > adaptive.fallbackMinSurfaceAlpha)
    }

    @Test
    fun adaptiveLegibilityShaderIsRegisteredForWarmup() {
        val entry = BuiltinRuntimeShader.AdaptiveLegibility
        assertTrue(entry.hasBackdropInput)
        assertTrue(entry.source.contains("uniform shader content;"))
        assertTrue(entry.source.contains("uniform float darkSurface;"))
        assertTrue(entry.source.contains("uniform float3 tintRgb;"))
    }
}
