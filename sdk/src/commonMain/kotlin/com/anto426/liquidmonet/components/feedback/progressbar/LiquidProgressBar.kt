package com.anto426.liquidmonet.components.feedback.progressbar

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.feedback.progressbar.motion.LiquidProgressBarMotion
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
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
import kotlin.math.PI
import kotlin.math.sin

/**
 * LiquidLinearProgressIndicator - Wavy Optical Liquid Glass Linear Progress Bar. Features
 * undulating sinusoidal liquid waves, radiant Monet gradient shimmer, and glowing leading droplet
 * without any surrounding bounding box container.
 */
@Composable
fun LiquidLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    height: Dp = 8.dp,
    trackColor: Color? = null,
    progressColor: Color? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    LiquidInputNormalization.positive(height, "LiquidLinearProgressIndicator height")
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val activePrimary = progressColor ?: colorScheme.primary
    val activeSecondary = colorScheme.tertiary
    val highlightColor = colorScheme.onSurface
    val defaultTrack = trackColor ?: LiquidGlassTheme.colors.inactiveTrack
    val normalizedProgress = LiquidInputNormalization.optionalUnit(progress)
    val animateIndicator = performance.animateFunctionalContent
    val semanticsModifier =
        if (normalizedProgress != null) {
            modifier.progressSemantics(normalizedProgress)
        } else {
            modifier.progressSemantics()
        }

    // The wave is the indicator itself. Applying liquidGlass to the full Canvas created a second
    // capsule around it, which read as an unrelated container instead of free-moving liquid.
    // Keep backdrop parameters for API compatibility; callers can still place the bare wave on a
    // glass parent when desired.
    val trackModifier = semanticsModifier

    val infiniteTransition =
        if (animateIndicator) {
            rememberLiquidInfiniteTransition(label = "LinearWavyProgressAnimation")
        } else {
            null
        }
    val wavePhase by
        if (infiniteTransition != null) {
            LiquidProgressBarMotion.rememberWavePhase(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = (2 * PI).toFloat(),
                label = "WavePhase",
            )
        } else {
            remember { mutableFloatStateOf(0f) }
        }

    val shimmerOffset by
        if (infiniteTransition != null) {
            LiquidProgressBarMotion.rememberShimmerOffset(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "ShimmerOffset",
            )
        } else {
            remember { mutableFloatStateOf(0.5f) }
        }

    val indeterminatePulse by
        if (infiniteTransition != null && normalizedProgress == null) {
            LiquidProgressBarMotion.rememberIndeterminatePulse(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "IndeterminatePulse",
            )
        } else {
            remember { mutableFloatStateOf(0.5f) }
        }

    if (normalizedProgress != null) {
        // Determinate Wavy Liquid Progress
        val animatedProgress by
            LiquidProgressBarMotion.animateFloat(
                targetValue = normalizedProgress,
                performance = performance,
                label = "determinateProgressAnim",
            )

        Canvas(modifier = trackModifier.fillMaxWidth().height(height)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val centerY = canvasHeight / 2f
            val strokePx = (canvasHeight * 0.45f).coerceIn(2.dp.toPx(), 4.dp.toPx())
            val amplitude = ((canvasHeight - strokePx) * 0.36f).coerceAtLeast(0f)
            val wavelength = (canvasHeight * 5f).coerceAtLeast(28.dp.toPx())

            // 1. Draw Full Wavy Background Track
            val trackPath = Path()
            val step = 3f
            var isFirst = true
            var x = 0f
            while (x <= canvasWidth) {
                val y = centerY + amplitude * sin(2 * PI * (x / wavelength) + wavePhase).toFloat()
                if (isFirst) {
                    trackPath.moveTo(x, y)
                    isFirst = false
                } else {
                    trackPath.lineTo(x, y)
                }
                x += step
            }
            drawPath(
                path = trackPath,
                color = defaultTrack,
                style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            // 2. Draw Active Wavy Progress Line
            val activeTargetX = canvasWidth * animatedProgress
            if (activeTargetX > 1f) {
                val activePath = Path()
                isFirst = true
                x = 0f
                var lastX = 0f
                var lastY = centerY
                while (x <= activeTargetX) {
                    val y =
                        centerY + amplitude * sin(2 * PI * (x / wavelength) + wavePhase).toFloat()
                    if (isFirst) {
                        activePath.moveTo(x, y)
                        isFirst = false
                    } else {
                        activePath.lineTo(x, y)
                    }
                    lastX = x
                    lastY = y
                    x += step
                }

                drawPath(
                    path = activePath,
                    brush =
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    activePrimary,
                                    activeSecondary,
                                    activePrimary,
                                ),
                            startX = -100f + 200f * shimmerOffset,
                            endX = canvasWidth + 200f * shimmerOffset,
                        ),
                    style =
                        Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Glowing Meniscus Droplet at the leading tip of the wave
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    highlightColor.copy(alpha = 0.90f),
                                    activePrimary.copy(alpha = 0.45f),
                                    Color.Transparent,
                                ),
                            center = Offset(lastX, lastY),
                            radius = strokePx * 1.8f,
                        ),
                    radius = strokePx * 1.8f,
                    center = Offset(lastX, lastY),
                )
            }
        }
    } else {
        // Indeterminate Wavy Traveling Liquid Wave
        Canvas(modifier = trackModifier.fillMaxWidth().height(height)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val centerY = canvasHeight / 2f
            val strokePx = (canvasHeight * 0.45f).coerceIn(2.dp.toPx(), 4.dp.toPx())
            val amplitude = ((canvasHeight - strokePx) * 0.36f).coerceAtLeast(0f)
            val wavelength = (canvasHeight * 5f).coerceAtLeast(28.dp.toPx())

            // Background Wavy Track
            val trackPath = Path()
            val step = 3f
            var isFirst = true
            var x = 0f
            while (x <= canvasWidth) {
                val y = centerY + amplitude * sin(2 * PI * (x / wavelength) + wavePhase).toFloat()
                if (isFirst) {
                    trackPath.moveTo(x, y)
                    isFirst = false
                } else {
                    trackPath.lineTo(x, y)
                }
                x += step
            }
            drawPath(
                path = trackPath,
                color = defaultTrack,
                style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
            )

            // Traveling Liquid Wave Segment
            val waveSegmentWidth = canvasWidth * (0.32f + 0.12f * indeterminatePulse)
            val startX = (canvasWidth + waveSegmentWidth) * shimmerOffset - waveSegmentWidth
            val endX = startX + waveSegmentWidth

            val activeWavePath = Path()
            isFirst = true
            x = startX.coerceAtLeast(0f)
            var lastX = x
            var lastY = centerY
            while (x <= endX.coerceAtMost(canvasWidth)) {
                val y = centerY + amplitude * sin(2 * PI * (x / wavelength) + wavePhase).toFloat()
                if (isFirst) {
                    activeWavePath.moveTo(x, y)
                    isFirst = false
                } else {
                    activeWavePath.lineTo(x, y)
                }
                lastX = x
                lastY = y
                x += step
            }

            if (!isFirst) {
                drawPath(
                    path = activeWavePath,
                    brush =
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    activePrimary.copy(alpha = 0.75f),
                                    activeSecondary,
                                    activePrimary,
                                    Color.Transparent,
                                ),
                            startX = startX,
                            endX = endX,
                        ),
                    style =
                        Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Glowing Head Droplet
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    highlightColor.copy(alpha = 0.82f),
                                    activePrimary.copy(alpha = 0f),
                                ),
                            center = Offset(lastX, lastY),
                            radius = strokePx * 1.8f,
                        ),
                    radius = strokePx * 1.8f,
                    center = Offset(lastX, lastY),
                )
            }
        }
    }
}

/**
 * LiquidCircularProgressIndicator - Sleek Optical Liquid Glass Circular Progress Spinner. Features
 * pure geometric circular arcs, radiant Monet sweep gradients, and smooth spring physics.
 */
@Composable
fun LiquidCircularProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    indicatorSize: Dp = 40.dp,
    strokeWidth: Dp = 4.dp,
    trackColor: Color? = null,
    progressColor: Color? = null,
    color: Color? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    LiquidInputNormalization.positive(
        indicatorSize,
        "LiquidCircularProgressIndicator indicatorSize",
    )
    LiquidInputNormalization.positive(strokeWidth, "LiquidCircularProgressIndicator strokeWidth")
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val activePrimary = color ?: progressColor ?: colorScheme.primary
    val activeSecondary = colorScheme.tertiary
    val highlightColor = colorScheme.onSurface
    val defaultTrack = trackColor ?: LiquidGlassTheme.colors.inactiveTrack
    val normalizedProgress = LiquidInputNormalization.optionalUnit(progress)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val semanticsModifier =
        if (normalizedProgress != null) {
            modifier.progressSemantics(normalizedProgress)
        } else {
            modifier.progressSemantics()
        }
    val trackModifier =
        semanticsModifier.liquidGlass(
            backdrop = effectiveBackdrop,
            shape = Capsule(),
            role = LiquidGlassRole.Control,
            containerColor = Color.Transparent,
        )

    val infiniteTransition =
        if (normalizedProgress == null && performance.animateFunctionalContent) {
            rememberLiquidInfiniteTransition(label = "CircularProgressAnimation")
        } else {
            null
        }
    val rotation by
        if (infiniteTransition != null) {
            LiquidProgressBarMotion.rememberRotation(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 360f,
                label = "Rotation",
            )
        } else {
            remember { mutableFloatStateOf(0f) }
        }

    val sweepAngle by
        if (infiniteTransition != null) {
            LiquidProgressBarMotion.rememberSweepAngle(
                transition = infiniteTransition,
                initialValue = 40f,
                targetValue = 280f,
                label = "SweepAngle",
            )
        } else {
            remember { mutableFloatStateOf(140f) }
        }

    if (normalizedProgress != null) {
        // Determinate Circular Progress Arc
        val animatedProgress by
            LiquidProgressBarMotion.animateFloat(
                targetValue = normalizedProgress,
                performance = performance,
                label = "determinateCircularAnim",
            )

        Canvas(
            modifier =
                trackModifier.defaultMinSize(minWidth = indicatorSize, minHeight = indicatorSize)
        ) {
            val strokePx = strokeWidth.toPx()
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val arcSize = Size(canvasWidth - strokePx, canvasHeight - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = defaultTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Active Progress Arc
            drawArc(
                brush =
                    Brush.sweepGradient(
                        colors =
                            listOf(
                                activePrimary,
                                activeSecondary,
                                activePrimary,
                            ),
                        center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                    ),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    } else {
        // Indeterminate Liquid Spinner
        Canvas(
            modifier =
                trackModifier.defaultMinSize(minWidth = indicatorSize, minHeight = indicatorSize)
        ) {
            val strokePx = strokeWidth.toPx()
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val arcSize = Size(canvasWidth - strokePx, canvasHeight - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background Track
            drawArc(
                color = defaultTrack,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Radiant Liquid Spinning Arc
            drawArc(
                brush =
                    Brush.sweepGradient(
                        colors =
                            listOf(
                                activePrimary.copy(alpha = 0.10f),
                                activePrimary,
                                activeSecondary,
                                highlightColor,
                            ),
                        center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                    ),
                startAngle = rotation,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}
