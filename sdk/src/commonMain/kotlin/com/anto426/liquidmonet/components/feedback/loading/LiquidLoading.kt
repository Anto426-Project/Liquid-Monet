package com.anto426.liquidmonet.components.feedback.loading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.feedback.loading.motion.LiquidLoadingMotion
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.feedback.shimmer.LiquidShimmerBox
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateFunctionalContent
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/** Visual style variant for unified LiquidLoading. */
enum class LiquidLoadingStyle {
    /** Organic 360° refractive comet ring spinner with meniscus tip. */
    Circular,
    /** Fluid undulating liquid progress wave along a recessed glass channel. */
    Linear,
    /** 3 harmonic refractive liquid glass droplets with subtle surface-tension glide. */
    Dots,
    /** Breathing luminous refractive orb with dual staggered optical ripples. */
    Pulse,
    /** Optical liquid glass skeleton placeholder with diagonal caustic sweep. */
    Shimmer,
    /** Modal liquid glass card overlay with scaled circular indicator and message. */
    Overlay,
}

/** Dimension scale for unified LiquidLoading. */
enum class LiquidLoadingSize {
    Small,
    Medium,
    Large,
}

/**
 * LiquidLoading - Single Unified Organic Liquid Glass Loading & Progress Component.
 *
 * Configurable with a single consistent API:
 *
 * @param style Visual loading style ([LiquidLoadingStyle.Circular], [LiquidLoadingStyle.Linear],
 *   [LiquidLoadingStyle.Dots], [LiquidLoadingStyle.Pulse], [LiquidLoadingStyle.Shimmer],
 *   [LiquidLoadingStyle.Overlay]).
 * @param progress When non-null (0.0f..1.0f), renders determinate progress; when null, runs
 *   infinite indeterminate animation.
 * @param size Dimension scale ([LiquidLoadingSize.Small], [LiquidLoadingSize.Medium],
 *   [LiquidLoadingSize.Large]).
 * @param message Optional text message displayed alongside/beneath the indicator.
 * @param tint Custom color tint (harmonized with Monet theme by default).
 * @param backdropState Optical glass backdrop reference.
 */
@Composable
fun LiquidLoading(
    modifier: Modifier = Modifier,
    style: LiquidLoadingStyle = LiquidLoadingStyle.Circular,
    progress: Float? = null,
    size: LiquidLoadingSize = LiquidLoadingSize.Medium,
    message: String? = null,
    tint: Color = Color.Unspecified,
    shape: Shape = RoundedRectangle(16.dp),
    backdropState: Backdrop = emptyBackdrop(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val hasCustomTint = tint.isSpecified
    val primaryColor = if (hasCustomTint) tint else colorScheme.primary
    val secondaryColor =
        if (hasCustomTint) {
            lerp(primaryColor, colorScheme.tertiary, 0.30f)
        } else {
            colorScheme.tertiary
        }
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val explicitProgressColor = if (hasCustomTint) tint else null

    when (style) {
        LiquidLoadingStyle.Circular -> {
            val indicatorSize: Dp =
                when (size) {
                    LiquidLoadingSize.Small -> 24.dp
                    LiquidLoadingSize.Medium -> 40.dp
                    LiquidLoadingSize.Large -> 56.dp
                }
            val strokeWidth: Dp =
                when (size) {
                    LiquidLoadingSize.Small -> 2.6.dp
                    LiquidLoadingSize.Medium -> 3.8.dp
                    LiquidLoadingSize.Large -> 4.8.dp
                }

            if (message != null) {
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LiquidCircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(indicatorSize),
                        indicatorSize = indicatorSize,
                        strokeWidth = strokeWidth,
                        progressColor = explicitProgressColor,
                        backdropState = effectiveBackdrop,
                    )
                    Text(
                        text = message,
                        style =
                            if (size == LiquidLoadingSize.Small) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                        color = glassColors.content,
                    )
                }
            } else {
                LiquidCircularProgressIndicator(
                    progress = progress,
                    modifier = modifier.size(indicatorSize),
                    indicatorSize = indicatorSize,
                    strokeWidth = strokeWidth,
                    progressColor = explicitProgressColor,
                    backdropState = effectiveBackdrop,
                )
            }
        }

        LiquidLoadingStyle.Linear -> {
            val heightDp =
                when (size) {
                    LiquidLoadingSize.Small -> 6.dp
                    LiquidLoadingSize.Medium -> 8.dp
                    LiquidLoadingSize.Large -> 10.dp
                }
            Column(modifier = modifier.fillMaxWidth()) {
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.content,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                LiquidLinearProgressIndicator(
                    progress = progress,
                    height = heightDp,
                    progressColor = explicitProgressColor,
                    backdropState = effectiveBackdrop,
                )
            }
        }

        LiquidLoadingStyle.Dots -> {
            LiquidLoadingDots(
                modifier = modifier,
                loadingSize = size,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                hasCustomTint = hasCustomTint,
                message = message,
            )
        }

        LiquidLoadingStyle.Pulse -> {
            LiquidLoadingPulse(
                modifier = modifier,
                loadingSize = size,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                message = message,
                backdropState = effectiveBackdrop,
            )
        }

        LiquidLoadingStyle.Shimmer -> {
            LiquidShimmerBox(
                modifier = modifier,
                shape = shape,
                backdropState = effectiveBackdrop,
                tint = tint,
            )
        }

        LiquidLoadingStyle.Overlay -> {
            LiquidLoadingOverlay(
                modifier = modifier,
                progress = progress,
                loadingSize = size,
                message = message ?: "Caricamento in corso...",
                primaryColor = primaryColor,
                explicitProgressColor = explicitProgressColor,
                backdropState = effectiveBackdrop,
            )
        }
    }
}

/** 3 Harmonic refractive liquid glass droplets with subtle surface-tension bridge. */
@Composable
private fun LiquidLoadingDots(
    modifier: Modifier = Modifier,
    loadingSize: LiquidLoadingSize,
    primaryColor: Color,
    secondaryColor: Color,
    hasCustomTint: Boolean,
    message: String?,
) {
    val animate = LocalLiquidGlassPerformance.current.animateFunctionalContent
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val dotDiameter: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 7.dp
            LiquidLoadingSize.Medium -> 9.dp
            LiquidLoadingSize.Large -> 12.dp
        }
    val spacing: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 6.dp
            LiquidLoadingSize.Medium -> 8.dp
            LiquidLoadingSize.Large -> 10.dp
        }

    val infiniteTransition =
        if (animate) {
            rememberLiquidInfiniteTransition(label = "LiquidDropletsWaveAnim")
        } else {
            null
        }
    val wavePhase by
        if (infiniteTransition != null) {
            LiquidLoadingMotion.rememberDropletsWavePhase(
                transition = infiniteTransition,
                label = "dropletsWavePhase",
            )
        } else {
            remember { mutableFloatStateOf(0.95f) }
        }

    val dropletColors =
        remember(primaryColor, secondaryColor, hasCustomTint) {
            if (hasCustomTint) {
                listOf(
                    primaryColor,
                    lerp(primaryColor, Color.White, 0.14f),
                    lerp(primaryColor, secondaryColor, 0.35f),
                )
            } else {
                listOf(
                    primaryColor,
                    lerp(primaryColor, secondaryColor, 0.50f),
                    secondaryColor,
                )
            }
        }

    val canvasWidth = dotDiameter * 3 + spacing * 2 + dotDiameter * 1.4f
    val canvasHeight = dotDiameter * 2.8f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Canvas(modifier = Modifier.size(width = canvasWidth, height = canvasHeight)) {
            val baseDiameter = dotDiameter.toPx()
            val baseRadius = baseDiameter / 2f
            val gapPx = spacing.toPx()
            val stride = baseDiameter + gapPx
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val travelAmplitude = (size.height - baseDiameter * 1.45f).coerceAtLeast(baseRadius) * 0.48f

            val samples =
                List(3) { idx -> LiquidLoadingMotion.computeDropletSample(wavePhase, idx) }
            val centers =
                List(3) { idx ->
                    val cx = centerX + (idx - 1) * stride
                    val cy = centerY + (0.5f - samples[idx].lift) * 2f * travelAmplitude
                    Offset(cx, cy)
                }

            // Subtle liquid surface-tension bridge between adjacent droplets when close in height
            for (i in 0..1) {
                val proximity = (1f - abs(samples[i].lift - samples[i + 1].lift)).coerceIn(0f, 1f)
                val bridgeAlpha = (proximity * proximity) * (if (isLight) 0.16f else 0.22f)
                if (bridgeAlpha > 0.02f) {
                    drawLine(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        dropletColors[i].copy(alpha = bridgeAlpha),
                                        dropletColors[i + 1].copy(alpha = bridgeAlpha),
                                    ),
                                start = centers[i],
                                end = centers[i + 1],
                            ),
                        start = centers[i],
                        end = centers[i + 1],
                        strokeWidth = (baseRadius * 0.44f * proximity).coerceAtLeast(1f),
                        cap = StrokeCap.Round,
                    )
                }
            }

            // Refractive liquid glass droplets
            for (i in 0..2) {
                val sample = samples[i]
                val center = centers[i]
                val dotColor = dropletColors[i]
                val rx = baseRadius * sample.scaleX
                val ry = baseRadius * sample.scaleY
                val maxR = max(rx, ry)

                // Soft optical ambient glow
                val haloRadius = maxR * 1.75f
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    dotColor.copy(alpha = (0.16f + 0.16f * sample.lift)),
                                    Color.Transparent,
                                ),
                            center = center,
                            radius = haloRadius,
                        ),
                    radius = haloRadius,
                    center = center,
                )

                // Translucent jewel droplet body with internal light refraction
                val ovalTopLeft = Offset(center.x - rx, center.y - ry)
                val ovalSize = Size(rx * 2f, ry * 2f)
                val highlightCenter = Offset(center.x - rx * 0.28f, center.y - ry * 0.30f)
                drawOval(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    lerp(dotColor, Color.White, if (isLight) 0.46f else 0.56f),
                                    dotColor.copy(alpha = 0.94f),
                                    dotColor.copy(alpha = 0.78f),
                                ),
                            center = highlightCenter,
                            radius = maxR * 1.35f,
                        ),
                    topLeft = ovalTopLeft,
                    size = ovalSize,
                )

                // Crisp specular glass rim
                drawOval(
                    color = Color.White.copy(alpha = if (isLight) 0.52f else 0.34f),
                    topLeft = ovalTopLeft,
                    size = ovalSize,
                    style = Stroke(width = 0.9.dp.toPx()),
                )

                // Upper-left catchlight dot
                drawCircle(
                    color = Color.White.copy(alpha = 0.82f),
                    radius = (maxR * 0.24f).coerceAtLeast(0.8.dp.toPx()),
                    center = Offset(center.x - rx * 0.30f, center.y - ry * 0.32f),
                )
            }
        }
        if (message != null) {
            Text(
                text = message,
                style =
                    when (loadingSize) {
                        LiquidLoadingSize.Small -> MaterialTheme.typography.bodySmall
                        LiquidLoadingSize.Medium -> MaterialTheme.typography.bodyMedium
                        LiquidLoadingSize.Large -> MaterialTheme.typography.titleMedium
                    },
                color = LiquidGlassTheme.colors.content,
            )
        }
    }
}

/** Breathing luminous refractive orb with dual staggered concentric optical ripples. */
@Composable
private fun LiquidLoadingPulse(
    modifier: Modifier = Modifier,
    loadingSize: LiquidLoadingSize,
    primaryColor: Color,
    secondaryColor: Color,
    message: String?,
    backdropState: Backdrop,
) {
    val animate = LocalLiquidGlassPerformance.current.animateFunctionalContent
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val totalSize: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 32.dp
            LiquidLoadingSize.Medium -> 46.dp
            LiquidLoadingSize.Large -> 62.dp
        }

    val infiniteTransition =
        if (animate) {
            rememberLiquidInfiniteTransition(label = "LiquidPulseAnim")
        } else {
            null
        }
    val pulseProg by
        if (infiniteTransition != null) {
            LiquidLoadingMotion.rememberPulseProgress(
                transition = infiniteTransition,
                label = "LiquidPulseProg",
            )
        } else {
            remember { mutableFloatStateOf(0.28f) }
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier.size(totalSize),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxR = (size.minDimension / 2f - 1.5.dp.toPx()).coerceAtLeast(2f)
                val coreR = maxR * 0.36f
                val maxStrokePx = 2.2.dp.toPx()

                // Two staggered optical ripples for continuous fluid radiation
                val ripplePhases = floatArrayOf(pulseProg, (pulseProg + 0.5f) % 1f)
                for (phase in ripplePhases) {
                    val sample = LiquidLoadingMotion.computePulseRipple(phase)
                    val radius = coreR + (maxR - coreR) * sample.radiusFraction
                    val rippleColor = lerp(primaryColor, secondaryColor, sample.radiusFraction * 0.45f)

                    // Soft diffused ripple wash
                    drawCircle(
                        color = rippleColor.copy(alpha = sample.alpha * (if (isLight) 0.14f else 0.20f)),
                        radius = radius,
                        center = center,
                        style = Stroke(width = maxStrokePx * sample.strokeFraction * 2.4f),
                    )
                    // Crisp refractive wave crest
                    drawCircle(
                        color = rippleColor.copy(alpha = sample.alpha * (if (isLight) 0.58f else 0.68f)),
                        radius = radius,
                        center = center,
                        style = Stroke(width = maxStrokePx * sample.strokeFraction),
                    )
                }

                // Ambient halo directly beneath the breathing core orb
                val breathe = sin(pulseProg * 2.0 * PI).toFloat()
                val orbAuraR = coreR * (1.55f + 0.16f * breathe)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    primaryColor.copy(alpha = if (isLight) 0.30f else 0.38f),
                                    Color.Transparent,
                                ),
                            center = center,
                            radius = orbAuraR,
                        ),
                    radius = orbAuraR,
                    center = center,
                )
            }

            // Breathing refractive glass core orb
            val breatheTheta = pulseProg * 2.0 * PI
            val breatheScale = 0.93f + 0.10f * sin(breatheTheta).toFloat()
            val organicFlex = 0.025f * cos(breatheTheta * 2.0).toFloat()

            Box(
                modifier =
                    Modifier.size(totalSize * 0.38f)
                        .graphicsLayer {
                            scaleX = breatheScale * (1f + organicFlex)
                            scaleY = breatheScale * (1f - organicFlex)
                        }
                        .liquidGlass(
                            backdrop = backdropState,
                            shape = Capsule(),
                            role = LiquidGlassRole.Control,
                            containerColor = primaryColor.copy(alpha = if (isLight) 0.22f else 0.28f),
                        )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val highlightOffset = Offset(center.x - radius * 0.28f, center.y - radius * 0.28f)
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        lerp(primaryColor, Color.White, 0.52f),
                                        primaryColor.copy(alpha = 0.90f),
                                        lerp(primaryColor, secondaryColor, 0.40f)
                                            .copy(alpha = 0.80f),
                                    ),
                                center = highlightOffset,
                                radius = radius * 1.35f,
                            ),
                        radius = radius,
                        center = center,
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = if (isLight) 0.58f else 0.38f),
                        radius = (radius - 0.6.dp.toPx()).coerceAtLeast(1f),
                        center = center,
                        style = Stroke(width = 1.dp.toPx()),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.85f),
                        radius = (radius * 0.24f).coerceAtLeast(1.dp.toPx()),
                        center = Offset(center.x - radius * 0.30f, center.y - radius * 0.30f),
                    )
                }
            }
        }
        if (message != null) {
            Text(
                text = message,
                style =
                    when (loadingSize) {
                        LiquidLoadingSize.Small -> MaterialTheme.typography.bodySmall
                        LiquidLoadingSize.Medium -> MaterialTheme.typography.bodyMedium
                        LiquidLoadingSize.Large -> MaterialTheme.typography.titleMedium
                    },
                color = LiquidGlassTheme.colors.content,
            )
        }
    }
}

/** Modal liquid glass dialog overlay with scaled circular indicator and optical aura. */
@Composable
private fun LiquidLoadingOverlay(
    modifier: Modifier,
    progress: Float?,
    loadingSize: LiquidLoadingSize,
    message: String,
    primaryColor: Color,
    explicitProgressColor: Color?,
    backdropState: Backdrop,
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val spinnerSize: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 32.dp
            LiquidLoadingSize.Medium -> 44.dp
            LiquidLoadingSize.Large -> 56.dp
        }
    val strokeWidth: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 3.dp
            LiquidLoadingSize.Medium -> 4.dp
            LiquidLoadingSize.Large -> 4.8.dp
        }

    Box(
        modifier =
            modifier
                .widthIn(min = 148.dp, max = 280.dp)
                .liquidGlass(
                    backdrop = backdropState,
                    shape = RoundedRectangle(28.dp),
                    role = LiquidGlassRole.Dialog,
                    containerColor = LiquidGlassTheme.colors.neutralContainer,
                )
                .padding(horizontal = 28.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier.size(spinnerSize + 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        primaryColor.copy(alpha = if (isLight) 0.14f else 0.22f),
                                        Color.Transparent,
                                    ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.minDimension / 2f,
                            ),
                        radius = size.minDimension / 2f,
                        center = Offset(size.width / 2f, size.height / 2f),
                    )
                }
                LiquidCircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(spinnerSize),
                    indicatorSize = spinnerSize,
                    strokeWidth = strokeWidth,
                    progressColor = explicitProgressColor,
                    backdropState = backdropState,
                )
            }
            Text(
                text = message,
                style =
                    when (loadingSize) {
                        LiquidLoadingSize.Small -> MaterialTheme.typography.bodySmall
                        LiquidLoadingSize.Medium -> MaterialTheme.typography.bodyMedium
                        LiquidLoadingSize.Large -> MaterialTheme.typography.titleSmall
                    },
                color = LiquidGlassTheme.colors.content,
                textAlign = TextAlign.Center,
            )
        }
    }
}
