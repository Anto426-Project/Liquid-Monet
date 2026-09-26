package com.anto426.liquidmonet.glass

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    val drawBackgroundMotion = performance.animateBackground

    // 1. Risoluzione dei colori: priorità allo schema Monet attivo (MaterialTheme.colorScheme),
    // con supporto a override espliciti e fallback su monetSeed
    val colorScheme = MaterialTheme.colorScheme

    val rawPrimary =
        primaryOverride
            ?: colorScheme.primary.takeIf { it != Color.Unspecified }
            ?: monetSeed?.let { if (isDark) it.darkPrimary else it.lightPrimary }
            ?: Color(0xFF0061A4)

    val rawSecondary =
        secondaryOverride
            ?: colorScheme.secondary.takeIf { it != Color.Unspecified }
            ?: monetSeed?.let { if (isDark) it.darkSecondary else it.lightSecondary }
            ?: Color(0xFF535F70)

    val rawTertiary =
        tertiaryOverride
            ?: colorScheme.tertiary.takeIf { it != Color.Unspecified }
            ?: monetSeed?.let { if (isDark) it.darkTertiary else it.lightTertiary }
            ?: Color(0xFF6B5778)

    val rawAccent =
        accentOverride
            ?: colorScheme.primaryContainer.takeIf { it != Color.Unspecified }
            ?: monetSeed?.let { if (isDark) it.darkTertiary else it.lightTertiary }
            ?: Color(0xFFD6BAE4)

    // 2. Transizione animata fluida tra cambi di colore/tema
    val primary by
        LiquidBackgroundMotion.animateColor(
            targetValue = rawPrimary,
            performance = performance,
            label = "bgPrimary",
        )
    val secondary by
        LiquidBackgroundMotion.animateColor(
            targetValue = rawSecondary,
            performance = performance,
            label = "bgSecondary",
        )
    val tertiary by
        LiquidBackgroundMotion.animateColor(
            targetValue = rawTertiary,
            performance = performance,
            label = "bgTertiary",
        )
    val accent by
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

    // 4. Sfondo base armonizzato con la tavolozza Monet attiva:
    // Nero OLED profondo con velatura organica del tema in Dark Mode
    // Bianco perlato morbido armonizzato con i toni Monet in Light Mode
    val themeBg =
        colorScheme.background.takeIf { it != Color.Unspecified }
            ?: if (isDark) Color(0xFF090A0F) else Color(0xFFFAFAFA)

    val baseBackgroundBrush =
        remember(isDark, themeBg, primary, secondary) {
            Brush.verticalGradient(
                if (isDark) {
                    listOf(
                        Color(0xFF010206),
                        blend(themeBg, primary, 0.05f),
                        Color(0xFF020308),
                    )
                } else {
                    listOf(
                        Color(0xFFFFFFFF),
                        blend(themeBg, primary, 0.06f),
                        blend(Color(0xFFF2F4F8), secondary, 0.05f),
                    )
                }
            )
        }

    val alphaMultiplier = if (isDark) normalizedIntensity else normalizedIntensity * 0.85f

    Canvas(modifier = modifier.fillMaxSize().background(baseBackgroundBrush)) {
        // Reading oscillator state in the draw phase invalidates only this canvas. Reading it
        // during composition would recompose the whole background for every animation frame.
        val p1 = p1State.value
        val p2 = p2State.value
        val p3 = p3State.value
        val p4 = p4State.value
        val w = size.width
        val h = size.height

        when (effect) {
            LiquidBackgroundEffect.Aurora -> {
                // ==========================================
                // 1. SFUMATURE FLUIDE SHIFTING (AURORA)
                // Solo campi di colore e sfumature morbide che si compenetrano
                // ==========================================

                // Sfumatura diagonale diffusa
                drawRect(
                    brush =
                        Brush.linearGradient(
                            colors =
                                listOf(
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.28f * alphaMultiplier
                                            else 0.16f * alphaMultiplier
                                    ),
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.18f * alphaMultiplier
                                            else 0.10f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            start = Offset(w * (0.10f + 0.08f * sin(p1)), 0f),
                            end = Offset(w * 0.90f, h * 0.85f),
                        ),
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Campo radiale primario (superiore / centro-sinistra)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.42f * alphaMultiplier
                                            else 0.24f * alphaMultiplier
                                    ),
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.20f * alphaMultiplier
                                            else 0.10f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center =
                                Offset(
                                    w * (0.30f + 0.14f * sin(p1)),
                                    h * (0.22f + 0.10f * cos(p2)),
                                ),
                            radius = w * 0.95f,
                        ),
                    center = Offset(w * (0.30f + 0.14f * sin(p1)), h * (0.22f + 0.10f * cos(p2))),
                    radius = w * 0.95f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Campo radiale secondario (centro-destra / superiore)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.38f * alphaMultiplier
                                            else 0.20f * alphaMultiplier
                                    ),
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.16f * alphaMultiplier
                                            else 0.08f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center =
                                Offset(
                                    w * (0.75f + 0.12f * cos(p2)),
                                    h * (0.36f + 0.12f * sin(p1)),
                                ),
                            radius = w * 0.90f,
                        ),
                    center = Offset(w * (0.75f + 0.12f * cos(p2)), h * (0.36f + 0.12f * sin(p1))),
                    radius = w * 0.90f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Campo radiale d'accento (centro-inferiore)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    accent.copy(
                                        alpha =
                                            if (isDark) 0.32f * alphaMultiplier
                                            else 0.18f * alphaMultiplier
                                    ),
                                    tertiary.copy(
                                        alpha =
                                            if (isDark) 0.15f * alphaMultiplier
                                            else 0.08f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center =
                                Offset(
                                    w * (0.45f + 0.16f * sin(p3)),
                                    h * (0.58f + 0.12f * cos(p1)),
                                ),
                            radius = w * 0.85f,
                        ),
                    center = Offset(w * (0.45f + 0.16f * sin(p3)), h * (0.58f + 0.12f * cos(p1))),
                    radius = w * 0.85f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Terzo campo diffuso (inferiore-sinistra) in modalità dettagliata
                if (drawDetailedBackground) {
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        tertiary.copy(
                                            alpha =
                                                if (isDark) 0.26f * alphaMultiplier
                                                else 0.14f * alphaMultiplier
                                        ),
                                        primary.copy(
                                            alpha =
                                                if (isDark) 0.10f * alphaMultiplier
                                                else 0.05f * alphaMultiplier
                                        ),
                                        Color.Transparent,
                                    ),
                                center =
                                    Offset(
                                        w * (0.22f + 0.12f * cos(p3)),
                                        h * (0.76f + 0.08f * sin(p2)),
                                    ),
                                radius = w * 0.80f,
                            ),
                        center =
                            Offset(w * (0.22f + 0.12f * cos(p3)), h * (0.76f + 0.08f * sin(p2))),
                        radius = w * 0.80f,
                        blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                    )
                }
            }

            LiquidBackgroundEffect.MeshGlow -> {
                // ==========================================
                // 2. MESH CROMATICA DIFFUSA
                // 5 nodi generosi di colore che si fondono fluidamente
                // ==========================================
                val c1 = Offset(w * (0.18f + 0.12f * cos(p1)), h * (0.16f + 0.10f * sin(p2)))
                val c2 = Offset(w * (0.82f + 0.10f * sin(p2)), h * (0.22f + 0.12f * cos(p3)))
                val c3 = Offset(w * (0.20f + 0.12f * sin(p3)), h * (0.78f + 0.10f * cos(p1)))
                val c4 = Offset(w * (0.80f + 0.10f * cos(p2)), h * (0.80f + 0.10f * sin(p1)))
                val c5 =
                    Offset(w * (0.50f + 0.14f * sin(p1 + 1f)), h * (0.48f + 0.12f * cos(p2 + 1f)))

                // Nodo 1: Alto-Sinistra (Primary)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.40f * alphaMultiplier
                                            else 0.24f * alphaMultiplier
                                    ),
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.18f * alphaMultiplier
                                            else 0.09f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = c1,
                            radius = w * 0.85f,
                        ),
                    center = c1,
                    radius = w * 0.85f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Nodo 2: Alto-Destra (Secondary)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.38f * alphaMultiplier
                                            else 0.22f * alphaMultiplier
                                    ),
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.16f * alphaMultiplier
                                            else 0.08f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = c2,
                            radius = w * 0.85f,
                        ),
                    center = c2,
                    radius = w * 0.85f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Nodo 3: Basso-Sinistra (Tertiary)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    tertiary.copy(
                                        alpha =
                                            if (isDark) 0.36f * alphaMultiplier
                                            else 0.20f * alphaMultiplier
                                    ),
                                    tertiary.copy(
                                        alpha =
                                            if (isDark) 0.15f * alphaMultiplier
                                            else 0.07f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = c3,
                            radius = w * 0.90f,
                        ),
                    center = c3,
                    radius = w * 0.90f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Nodo 4: Basso-Destra (Accent)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    accent.copy(
                                        alpha =
                                            if (isDark) 0.38f * alphaMultiplier
                                            else 0.22f * alphaMultiplier
                                    ),
                                    accent.copy(
                                        alpha =
                                            if (isDark) 0.16f * alphaMultiplier
                                            else 0.08f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = c4,
                            radius = w * 0.85f,
                        ),
                    center = c4,
                    radius = w * 0.85f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Nodo 5: Centro Focale (Pulsazione di raccordo)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    (if (isDark) Color.White else primary).copy(
                                        alpha =
                                            if (isDark) 0.22f * alphaMultiplier
                                            else 0.14f * alphaMultiplier
                                    ),
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.24f * alphaMultiplier
                                            else 0.14f * alphaMultiplier
                                    ),
                                    accent.copy(
                                        alpha =
                                            if (isDark) 0.12f * alphaMultiplier
                                            else 0.06f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = c5,
                            radius = w * 0.75f,
                        ),
                    center = c5,
                    radius = w * 0.75f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )
            }

            LiquidBackgroundEffect.OrbitalPulse -> {
                // ==========================================
                // 3. PULSAZIONE RADIALE DI SFUMATURE
                // Gradienti concentrici che respirano dolcemente
                // ==========================================
                val coreCenter =
                    Offset(
                        w * (0.50f + 0.06f * sin(p1)),
                        h * (0.42f + 0.05f * cos(p2)),
                    )
                val pulseBreath = 1f + 0.08f * sin(p4 * 2f)

                // Sfumatura radiale primaria espansiva
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.44f * alphaMultiplier
                                            else 0.26f * alphaMultiplier
                                    ),
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.24f * alphaMultiplier
                                            else 0.14f * alphaMultiplier
                                    ),
                                    accent.copy(
                                        alpha =
                                            if (isDark) 0.10f * alphaMultiplier
                                            else 0.05f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = coreCenter,
                            radius = w * 0.90f * pulseBreath,
                        ),
                    center = coreCenter,
                    radius = w * 0.90f * pulseBreath,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Sfumatura focale interna più luminosa
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    (if (isDark) Color.White else primary).copy(
                                        alpha =
                                            if (isDark) 0.40f * alphaMultiplier
                                            else 0.22f * alphaMultiplier
                                    ),
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.32f * alphaMultiplier
                                            else 0.18f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = coreCenter,
                            radius = w * 0.42f * pulseBreath,
                        ),
                    center = coreCenter,
                    radius = w * 0.42f * pulseBreath,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Sfumatura complementare superiore-destra
                val haloCenter1 =
                    Offset(
                        w * (0.70f + 0.10f * cos(p2)),
                        h * (0.26f + 0.08f * sin(p3)),
                    )
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.32f * alphaMultiplier
                                            else 0.18f * alphaMultiplier
                                    ),
                                    tertiary.copy(
                                        alpha =
                                            if (isDark) 0.14f * alphaMultiplier
                                            else 0.07f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = haloCenter1,
                            radius = w * 0.70f,
                        ),
                    center = haloCenter1,
                    radius = w * 0.70f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Sfumatura complementare inferiore-sinistra
                val haloCenter2 =
                    Offset(
                        w * (0.30f + 0.10f * sin(p3)),
                        h * (0.72f + 0.08f * cos(p1)),
                    )
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    accent.copy(
                                        alpha =
                                            if (isDark) 0.30f * alphaMultiplier
                                            else 0.16f * alphaMultiplier
                                    ),
                                    tertiary.copy(
                                        alpha =
                                            if (isDark) 0.12f * alphaMultiplier
                                            else 0.06f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = haloCenter2,
                            radius = w * 0.75f,
                        ),
                    center = haloCenter2,
                    radius = w * 0.75f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )
            }

            LiquidBackgroundEffect.RadiantBeam -> {
                // ==========================================
                // 4. SFUMATURA ATMOSFERICA ZENITH
                // Gradiente zenitale morbido dall'alto verso il basso
                // ==========================================
                val topCenter = Offset(w * (0.50f + 0.05f * sin(p1)), -h * 0.05f)

                // Bagliore zenitale superiore
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    (if (isDark) Color.White else primary).copy(
                                        alpha =
                                            if (isDark) 0.50f * alphaMultiplier
                                            else 0.28f * alphaMultiplier
                                    ),
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.38f * alphaMultiplier
                                            else 0.22f * alphaMultiplier
                                    ),
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.18f * alphaMultiplier
                                            else 0.10f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = topCenter,
                            radius = w * 1.05f,
                        ),
                    center = topCenter,
                    radius = w * 1.05f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Gradiente lineare verticale morbido
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    primary.copy(
                                        alpha =
                                            if (isDark) 0.25f * alphaMultiplier
                                            else 0.14f * alphaMultiplier
                                    ),
                                    secondary.copy(
                                        alpha =
                                            if (isDark) 0.16f * alphaMultiplier
                                            else 0.08f * alphaMultiplier
                                    ),
                                    tertiary.copy(
                                        alpha =
                                            if (isDark) 0.08f * alphaMultiplier
                                            else 0.04f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            startY = 0f,
                            endY = h * 0.80f,
                        ),
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )

                // Sfumatura d'accento di base (fondo schermo)
                val bottomCenter = Offset(w * (0.50f + 0.06f * cos(p2)), h * 1.05f)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    accent.copy(
                                        alpha =
                                            if (isDark) 0.28f * alphaMultiplier
                                            else 0.15f * alphaMultiplier
                                    ),
                                    tertiary.copy(
                                        alpha =
                                            if (isDark) 0.14f * alphaMultiplier
                                            else 0.07f * alphaMultiplier
                                    ),
                                    Color.Transparent,
                                ),
                            center = bottomCenter,
                            radius = w * 0.85f,
                        ),
                    center = bottomCenter,
                    radius = w * 0.85f,
                    blendMode = if (isDark) BlendMode.Screen else BlendMode.SrcOver,
                )
            }
        }

        // 5. Vignettatura morbida perimetrale (incorniciatura senza linee nette)
        if (showVignette) {
            drawRect(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                Color.Transparent,
                                Color.Transparent,
                                if (isDark) {
                                    Color.Black.copy(alpha = 0.40f * normalizedIntensity)
                                } else {
                                    Color(0xFF1E2433).copy(alpha = 0.05f * normalizedIntensity)
                                },
                            ),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.95f,
                    )
            )
        }
    }
}
