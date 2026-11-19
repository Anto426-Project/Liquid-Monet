package com.anto426.liquidmonet.glass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPreset
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets

/** Unscaled optical values for one glass role. */
@Immutable
data class LiquidGlassSurfaceStyle(
    val preset: LiquidGlassPreset,
    val lightSurfaceAlpha: Float,
    val darkSurfaceAlpha: Float,
    val lightBrightness: Float,
    val saturation: Float,
    val highlightAlpha: Float,
    val shadowRadius: Dp,
    val shadowAlpha: Float,
    val innerShadowRadius: Dp,
    val innerShadowAlpha: Float
)

/** Single source of truth for the SDK's stable optical surface styles. */
object LiquidGlassStyleManager {
    private const val LightAmbientTintAlpha = 0.05f
    private const val DarkAmbientTintAlpha = 0.04f

    private val control = LiquidGlassSurfaceStyle(
        preset = LiquidGlassPresets.Subtle,
        lightSurfaceAlpha = 0.20f,
        darkSurfaceAlpha = 0.15f,
        lightBrightness = 0.025f,
        saturation = 1.22f,
        highlightAlpha = 0.48f,
        shadowRadius = 10.dp,
        shadowAlpha = 0.11f,
        innerShadowRadius = 1.dp,
        innerShadowAlpha = 0.10f
    )

    private val surface = LiquidGlassSurfaceStyle(
        preset = LiquidGlassPresets.Navigation,
        lightSurfaceAlpha = 0.20f,
        darkSurfaceAlpha = 0.16f,
        lightBrightness = 0.035f,
        saturation = 1.35f,
        highlightAlpha = 0.58f,
        shadowRadius = 16.dp,
        shadowAlpha = 0.15f,
        innerShadowRadius = 2.dp,
        innerShadowAlpha = 0.12f
    )

    private val dialog = LiquidGlassSurfaceStyle(
        preset = LiquidGlassPresets.Navigation,
        lightSurfaceAlpha = 0.12f,
        darkSurfaceAlpha = 0.22f,
        lightBrightness = 0.035f,
        saturation = 1.40f,
        highlightAlpha = 0.72f,
        shadowRadius = 28.dp,
        shadowAlpha = 0.26f,
        innerShadowRadius = 2.dp,
        innerShadowAlpha = 0.18f
    )

    // Dialog and sheet use the same modal optics; their placement is the only visual difference.
    private val sheet = dialog.copy()

    private val menu = LiquidGlassSurfaceStyle(
        preset = LiquidGlassPresets.Navigation,
        lightSurfaceAlpha = 0.20f,
        darkSurfaceAlpha = 0.16f,
        lightBrightness = 0.035f,
        saturation = 1.45f,
        highlightAlpha = 0.65f,
        shadowRadius = 20.dp,
        shadowAlpha = 0.18f,
        innerShadowRadius = 2.dp,
        innerShadowAlpha = 0.15f
    )

    private val navigation = LiquidGlassSurfaceStyle(
        preset = LiquidGlassPresets.Navigation,
        lightSurfaceAlpha = 0.20f,
        darkSurfaceAlpha = 0.16f,
        lightBrightness = 0.035f,
        saturation = 1.35f,
        highlightAlpha = 0.62f,
        shadowRadius = 18.dp,
        shadowAlpha = 0.18f,
        innerShadowRadius = 2.dp,
        innerShadowAlpha = 0.14f
    )

    private val topBar = LiquidGlassSurfaceStyle(
        preset = LiquidGlassPresets.Navigation,
        lightSurfaceAlpha = 0.20f,
        darkSurfaceAlpha = 0.16f,
        lightBrightness = 0.025f,
        saturation = 1.30f,
        highlightAlpha = 0f,
        shadowRadius = 0.dp,
        shadowAlpha = 0f,
        innerShadowRadius = 0.dp,
        innerShadowAlpha = 0f
    )

    fun resolve(role: LiquidGlassRole): LiquidGlassSurfaceStyle = when (role) {
        LiquidGlassRole.Control -> control
        LiquidGlassRole.Surface -> surface
        LiquidGlassRole.Dialog -> dialog
        LiquidGlassRole.Sheet -> sheet
        LiquidGlassRole.Menu -> menu
        LiquidGlassRole.TopBar -> topBar
        LiquidGlassRole.Navigation -> navigation
    }

    /** Neutral body of the material; theme color enters only through the ambient reflection. */
    fun neutralSurfaceColor(isLightSurface: Boolean): Color =
        if (isLightSurface) Color(0xFFFAFAFA) else Color(0xFF121212)

    /** Default material body for a semantic role. */
    fun surfaceColor(role: LiquidGlassRole, isLightSurface: Boolean): Color =
        surfaceColor(role, isLightSurface, neutralSurfaceColor(isLightSurface))

    internal fun surfaceColor(role: LiquidGlassRole, isLightSurface: Boolean, themedSurface: Color): Color {
        val style = resolve(role)
        val alpha = if (isLightSurface) style.lightSurfaceAlpha else style.darkSurfaceAlpha
        return if (
            isLightSurface &&
            (role == LiquidGlassRole.Dialog || role == LiquidGlassRole.Sheet)
        ) {
            Color.Black.copy(alpha = alpha)
        } else {
            val body = if (isLightSurface) themedSurface else neutralSurfaceColor(false)
            body.copy(alpha = alpha)
        }
    }

    /** Shared Monet reflection carried by every exported glass layer. */
    fun ambientTintAlpha(isLightSurface: Boolean): Float =
        if (isLightSurface) LightAmbientTintAlpha else DarkAmbientTintAlpha
}
