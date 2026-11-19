package com.anto426.liquidmonet.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.luminance
import com.anto426.liquidmonet.glass.background.LiquidLinearGradient
import com.anto426.liquidmonet.glass.background.LiquidRadialGradient
import com.anto426.liquidmonet.glass.background.motion.LiquidBackgroundMotion
import com.anto426.liquidmonet.glass.internal.LiquidWaveMath
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateBackground
import com.anto426.liquidmonet.glass.runtime.renderDetailedBackground
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.anto426.liquidmonet.theme.monet.LiquidMonetSeed
import com.anto426.liquidmonet.theme.monet.blend
import kotlin.math.PI

private fun sin(phase: Float): Float = LiquidWaveMath.sin(phase)

private fun cos(phase: Float): Float = LiquidWaveMath.cos(phase)

/**
 * Stati cromatici e sfumature supportati da LiquidBackground. Pura composizione di campi di colore
 * e gradienti armonizzati, senza disegni o tratti rigidi.
 */
enum class LiquidBackgroundEffect {
    /** Sfumature fluide cromatiche che si muovono e fondono organicamente */
    Aurora,
    /** Mesh cromatica diffusa a 5 poli con continue metamorfosi di colore */
    MeshGlow,
    /** Pulsazione radiale concentrica con alone e respirazione armonica */
    OrbitalPulse,
    /** Gradiente atmosferico zenitale verticale con morbido alone diffuso */
    RadiantBeam,
}

/**
 * LiquidBackground - Sintetizzatore di sfondi cromatici fluidi per Material 3.
 *
 * Realizzato esclusivamente con stati di colore e sfumature morbide (gradienti radiali e lineari
 * multi-stop), senza disegni, forme geometriche o linee di contorno.
 */
@Composable
fun LiquidBackground(
    modifier: Modifier = Modifier,
    effect: LiquidBackgroundEffect = LiquidBackgroundEffect.Aurora,
    isDark: Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f,
    monetSeed: LiquidMonetSeed? = null,
    primaryOverride: Color? = null,
    secondaryOverride: Color? = null,
    tertiaryOverride: Color? = null,
    accentOverride: Color? = null,
    speedFactor: Float = 1.0f,
    intensity: Float = 1.0f,
    showVignette: Boolean = true,
) {
    val performance = LocalLiquidGlassPerformance.current
    val normalizedSpeed =
        if (speedFactor.isFinite()) {
            speedFactor.coerceIn(0.05f, 10f)
        } else {
            1f
        }
    val normalizedIntensity = if (intensity.isFinite()) intensity.coerceIn(0f, 1f) else 1f
    val drawDetailedBackground = performance.renderDetailedBackground
    val drawBackgroundMotion = normalizedIntensity > 0f && performance.animateBackground

    val colorScheme = MaterialTheme.colorScheme

    val rawPrimary =
        primaryOverride
            ?: monetSeed?.let { if (isDark) it.darkPrimary else it.lightPrimary }
            ?: colorScheme.primary.takeIf { it.isSpecified }
            ?: Color(0xFF0061A4)

    val rawSecondary =
        secondaryOverride
            ?: monetSeed?.let { if (isDark) it.darkSecondary else it.lightSecondary }
            ?: colorScheme.secondary.takeIf { it.isSpecified }
            ?: Color(0xFF535F70)

    val rawTertiary =
        tertiaryOverride
            ?: monetSeed?.let { if (isDark) it.darkTertiary else it.lightTertiary }
            ?: colorScheme.tertiary.takeIf { it.isSpecified }
            ?: Color(0xFF6B5778)

    val rawAccent =
        accentOverride
            ?: monetSeed?.let { blend(rawPrimary, rawTertiary, 0.45f) }
            ?: colorScheme.primaryContainer.takeIf { it.isSpecified }
            ?: Color(0xFFD6BAE4)

    // 2. Transizione animata fluida tra cambi di colore/tema
    val primaryState =
        LiquidBackgroundMotion.animateColor(
            targetValue = rawPrimary,
            performance = performance,
            label = "bgPrimary",
        )
    val secondaryState =
        LiquidBackgroundMotion.animateColor(
            targetValue = rawSecondary,
            performance = performance,
            label = "bgSecondary",
        )
    val tertiaryState =
        LiquidBackgroundMotion.animateColor(
            targetValue = rawTertiary,
            performance = performance,
            label = "bgTertiary",
        )
    val accentState =
        LiquidBackgroundMotion.animateColor(
            targetValue = rawAccent,
            performance = performance,
            label = "bgAccent",
        )

    // 3. Oscillatori periodici per il moto continuo e vellutato delle sfumature
    val infiniteTransition = rememberLiquidInfiniteTransition(label = "LiquidBackgroundMotion")
    val twoPi = (2.0 * PI).toFloat()

    val p1State =
        if (drawBackgroundMotion) {
            LiquidBackgroundMotion.rememberP1(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = twoPi,
                label = "p1",
                normalizedSpeed = normalizedSpeed,
            )
        } else {
            remember { mutableFloatStateOf(0f) }
        }

    val p2State =
        if (drawBackgroundMotion) {
            LiquidBackgroundMotion.rememberP2(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = twoPi,
                label = "p2",
                normalizedSpeed = normalizedSpeed,
            )
        } else {
            remember { mutableFloatStateOf(1f) }
        }

    val p3State =
        if (drawBackgroundMotion) {
            LiquidBackgroundMotion.rememberP3(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = twoPi,
                label = "p3",
                normalizedSpeed = normalizedSpeed,
            )
        } else {
            remember { mutableFloatStateOf(2f) }
        }

    val p4State =
        if (drawBackgroundMotion) {
            LiquidBackgroundMotion.rememberP4(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = twoPi,
                label = "p4",
                normalizedSpeed = normalizedSpeed,
            )
        } else {
            remember { mutableFloatStateOf(0.5f) }
        }

    val neutralCanvas = if (isDark) Color(0xFF0B0E17) else Color(0xFFF3F3F6)
    val themeCanvas = colorScheme.background.takeIf { it.isSpecified } ?: neutralCanvas
    val canvas = blend(neutralCanvas, themeCanvas, 0.48f)
    val effectBlend = if (isDark) BlendMode.Screen else BlendMode.SrcOver

    Box(
        modifier.fillMaxSize().drawWithCache {
            val primary = primaryState.value
            val secondary = secondaryState.value
            val tertiary = tertiaryState.value
            val accent = accentState.value
            val width = size.width
            val height = size.height
            val span = maxOf(width, height * 0.56f)

            // The base itself carries the chosen color, including with the effects at zero.
            fun tint(color: Color, amount: Float) = blend(canvas, color, amount * if (isDark) 0.48f else 1f)

            val canvasBrush = when (effect) {
                LiquidBackgroundEffect.Aurora -> Brush.linearGradient(
                    listOf(tint(primary, 0.23f), tint(secondary, 0.13f), tint(tertiary, 0.21f)),
                    start = Offset(0f, height * 0.10f),
                    end = Offset(width, height * 0.90f),
                )
                LiquidBackgroundEffect.MeshGlow -> Brush.linearGradient(
                    listOf(tint(secondary, 0.21f), tint(accent, 0.13f), tint(primary, 0.23f)),
                    start = Offset(width, 0f),
                    end = Offset(0f, height),
                )
                LiquidBackgroundEffect.OrbitalPulse -> Brush.verticalGradient(
                    listOf(tint(tertiary, 0.21f), tint(primary, 0.13f), tint(secondary, 0.24f)),
                )
                LiquidBackgroundEffect.RadiantBeam -> Brush.verticalGradient(
                    listOf(tint(primary, 0.24f), tint(accent, 0.12f), tint(secondary, 0.25f)),
                )
            }

            fun field(color: Color, lightOpacity: Float, darkOpacity: Float) =
                LiquidRadialGradient(
                    listOf(
                        color.copy(alpha = normalizedIntensity * if (isDark) darkOpacity else lightOpacity),
                        color.copy(alpha = normalizedIntensity * if (isDark) darkOpacity * 0.38f else lightOpacity * 0.38f),
                        color.copy(alpha = 0f),
                    ),
                )

            val primaryField = field(primary, lightOpacity = 0.18f, darkOpacity = 0.30f)
            val secondaryField = field(secondary, lightOpacity = 0.14f, darkOpacity = 0.22f)
            val tertiaryField = field(tertiary, lightOpacity = 0.14f, darkOpacity = 0.25f)
            val accentField = field(accent, lightOpacity = 0.10f, darkOpacity = 0.16f)
            val orbitalHalo = LiquidRadialGradient(
                listOf(
                    primary.copy(alpha = 0f),
                    primary.copy(alpha = 0f),
                    primary.copy(alpha = normalizedIntensity * if (isDark) 0.23f else 0.14f),
                    accent.copy(alpha = 0f),
                ),
            )
            val ribbon = LiquidLinearGradient(
                listOf(
                    primary.copy(alpha = 0f),
                    primary.copy(alpha = normalizedIntensity * if (isDark) 0.15f else 0.10f),
                    accent.copy(alpha = normalizedIntensity * if (isDark) 0.11f else 0.07f),
                    tertiary.copy(alpha = 0f),
                ),
            )
            val beam = LiquidLinearGradient(
                listOf(
                    accent.copy(alpha = 0f),
                    accent.copy(alpha = normalizedIntensity * if (isDark) 0.20f else 0.12f),
                    primary.copy(alpha = normalizedIntensity * if (isDark) 0.13f else 0.09f),
                    primary.copy(alpha = 0f),
                ),
            )
            val vignette = Brush.radialGradient(
                0.0f to Color.Transparent,
                0.54f to Color.Transparent,
                1.0f to blend(Color.Black, tertiary, 0.42f)
                    .copy(alpha = if (isDark) 0.24f else 0.075f),
                center = Offset(width * 0.52f, height * 0.48f),
                radius = span * 1.45f,
            )

            onDrawBehind {
                drawRect(canvasBrush)

                if (normalizedIntensity > 0f) {
                    fun drawField(field: LiquidRadialGradient, x: Float, y: Float, radius: Float) {
                        field.draw(this, Offset(width * x, height * y), span * radius, effectBlend)
                    }

                    // The phases invalidate drawing without recomposing the whole scene.
                    val p1 = p1State.value
                    val p2 = p2State.value
                    val p3 = p3State.value
                    val p4 = p4State.value
                    when (effect) {
                        LiquidBackgroundEffect.Aurora -> {
                            // Two broad color bands drift past one another across the canvas.
                            ribbon.draw(
                                this,
                                start = Offset(width * (-0.22f + 0.10f * sin(p1)), height * 0.07f),
                                end = Offset(width * 1.14f, height * 0.72f),
                                blendMode = effectBlend,
                            )
                            drawField(tertiaryField, -0.04f + 0.12f * sin(p2), 0.27f, 0.85f)
                            drawField(primaryField, 0.87f, 0.38f + 0.10f * cos(p1), 0.88f)
                            drawField(secondaryField, 0.34f + 0.12f * cos(p3), 0.89f, 0.76f)
                            if (drawDetailedBackground) {
                                drawField(accentField, 0.56f, 0.64f + 0.09f * sin(p4), 0.62f)
                            }
                        }
                        LiquidBackgroundEffect.MeshGlow -> {
                            // Four offset pools form a mesh, with a smaller fifth pool between them.
                            drawField(primaryField, 0.19f + 0.11f * sin(p1), 0.15f, 0.64f)
                            drawField(tertiaryField, 0.84f, 0.28f + 0.08f * cos(p2), 0.60f)
                            drawField(secondaryField, 0.13f, 0.77f + 0.07f * cos(p1), 0.68f)
                            drawField(primaryField, 0.90f + 0.09f * cos(p3), 0.83f, 0.55f)
                            if (drawDetailedBackground) {
                                drawField(accentField, 0.49f + 0.11f * sin(p4), 0.49f, 0.46f)
                            }
                        }
                        LiquidBackgroundEffect.OrbitalPulse -> {
                            // An off-center halo breathes around a quieter, softly colored center.
                            val x = 0.68f + 0.06f * sin(p1)
                            val y = 0.38f + 0.06f * cos(p2)
                            val breath = 1f + 0.065f * sin(p4)
                            drawField(orbitalHalo, x, y, 0.89f * breath)
                            drawField(accentField, x, y, 0.44f * breath)
                            drawField(secondaryField, 0.13f + 0.06f * cos(p3), 0.80f, 0.70f)
                            if (drawDetailedBackground) {
                                drawField(tertiaryField, 1.02f, 0.70f, 0.54f)
                            }
                        }
                        LiquidBackgroundEffect.RadiantBeam -> {
                            // An angled shaft of light fades into cooler pools at the bottom.
                            val drift = 0.08f * sin(p1)
                            beam.draw(
                                this,
                                start = Offset(width * (-0.25f + drift), height * 0.37f),
                                end = Offset(width * (1.18f + drift), height * 0.61f),
                                blendMode = effectBlend,
                            )
                            drawField(primaryField, 0.72f + 0.09f * cos(p2), -0.08f, 0.92f)
                            drawField(tertiaryField, 0.12f, 0.92f + 0.07f * sin(p3), 0.69f)
                            if (drawDetailedBackground) {
                                drawField(accentField, 0.72f, 0.62f + 0.07f * sin(p4), 0.57f)
                            }
                        }
                    }
                }
                if (showVignette) drawRect(brush = vignette)
            }
        },
    )
}
