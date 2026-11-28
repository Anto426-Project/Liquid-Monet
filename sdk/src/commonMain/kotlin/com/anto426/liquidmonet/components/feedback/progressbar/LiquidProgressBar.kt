package com.anto426.liquidmonet.components.feedback.progressbar

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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.feedback.progressbar.motion.LiquidProgressBarMotion
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateFunctionalContent
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * LiquidLinearProgressIndicator - Organic Refractive Liquid Glass Linear Progress Indicator.
 * Features a calm recessed glass channel carrying an undulating fluid wave with tapered meniscus
 * endpoints, internal caustic sheen, and a luminous leading droplet.
 */
@Composable
fun LiquidLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    height: Dp = 8.dp,
    trackColor: Color? = null,
    progressColor: Color? = null,
    backdrop: Backdrop = emptyBackdrop(),
) {
    LiquidInputNormalization.positive(height, "LiquidLinearProgressIndicator height")
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.surface.luminance() > 0.5f
    val isCustomColor = progressColor != null
    val activePrimary = progressColor ?: colorScheme.primary
    val activeSecondary =
        if (isCustomColor) {
            lerp(activePrimary, Color.White, if (isLight) 0.24f else 0.34f)
        } else {
            lerp(activePrimary, colorScheme.tertiary, 0.55f)
        }
    val specularTip = lerp(activePrimary, Color.White, if (isLight) 0.66f else 0.80f)
    val defaultTrack = trackColor ?: LiquidGlassTheme.colors.inactiveTrack
    val normalizedProgress = LiquidInputNormalization.optionalUnit(progress)
    val animateIndicator = performance.animateFunctionalContent
    val semanticsModifier =
        if (normalizedProgress != null) {
            modifier.progressSemantics(normalizedProgress)
        } else {
            modifier.progressSemantics()
        }

    // Keep backdrop for API compatibility; the fluid ribbon renders directly without an
    // outer glass capsule so it remains lightweight and avoids nested-glass artifacts.
    @Suppress("UNUSED_EXPRESSION") backdrop

    val infiniteTransition =
        if (animateIndicator) {
            rememberLiquidInfiniteTransition(label = "LinearFluidProgressAnimation")
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

    val indeterminateTravel by
        if (infiniteTransition != null && normalizedProgress == null) {
            LiquidProgressBarMotion.rememberIndeterminateTravel(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "IndeterminateTravel",
            )
        } else {
            remember { mutableFloatStateOf(0.45f) }
        }

    val activePath = remember { Path() }
    val sheenPath = remember { Path() }

    if (normalizedProgress != null) {
        val animatedProgress by
            LiquidProgressBarMotion.animateFloat(
                targetValue = normalizedProgress,
                performance = performance,
                label = "determinateProgressAnim",
            )

        Canvas(modifier = semanticsModifier.fillMaxWidth().height(height)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

            val centerY = canvasHeight / 2f
            val strokePx = (canvasHeight * 0.48f).coerceIn(2.2.dp.toPx(), 4.5.dp.toPx())
            val capInset = strokePx / 2f
            val usableWidth = (canvasWidth - strokePx).coerceAtLeast(0f)

            drawGlassLinearTrack(
                startX = capInset,
                endX = canvasWidth - capInset,
                centerY = centerY,
                strokePx = strokePx,
                trackColor = defaultTrack,
                isLight = isLight,
            )

            val activeTargetX = capInset + usableWidth * animatedProgress
            if (animatedProgress > 0.005f && activeTargetX > capInset) {
                val maxAmplitude = ((canvasHeight - strokePx) * 0.38f).coerceAtLeast(0f)
                val activity = LiquidProgressBarMotion.determinateWaveActivity(animatedProgress)
                val amplitude = maxAmplitude * activity
                val wavelength = (canvasHeight * 4.8f).coerceAtLeast(28.dp.toPx())
                val edgeFade = (wavelength * 0.42f).coerceAtMost((activeTargetX - capInset) * 0.45f)
                val sheenLift = -strokePx * 0.16f

                val (tipX, tipY) =
                    buildLiquidWavePaths(
                        activePath = activePath,
                        sheenPath = sheenPath,
                        startX = capInset,
                        endX = activeTargetX,
                        centerY = centerY,
                        amplitude = amplitude,
                        wavelength = wavelength,
                        wavePhase = wavePhase,
                        edgeFade = edgeFade,
                        sheenLift = sheenLift,
                        step = 2.dp.toPx().coerceAtLeast(2f),
                    )

                val gradientShift = canvasWidth * (shimmerOffset - 0.5f) * 0.7f
                val fluidBrush =
                    Brush.horizontalGradient(
                        colors = listOf(activePrimary, activeSecondary, activePrimary),
                        startX = gradientShift,
                        endX = gradientShift + canvasWidth,
                    )

                // Soft optical refraction glow
                drawPath(
                    path = activePath,
                    brush = fluidBrush,
                    alpha = if (isLight) 0.18f else 0.24f,
                    style =
                        Stroke(
                            width = strokePx * 1.9f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )

                // Core liquid body
                drawPath(
                    path = activePath,
                    brush = fluidBrush,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )

                // Cylindrical top specular sheen
                drawPath(
                    path = sheenPath,
                    color = Color.White.copy(alpha = if (isLight) 0.38f else 0.28f),
                    style =
                        Stroke(
                            width = (strokePx * 0.26f).coerceAtLeast(0.8.dp.toPx()),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                        ),
                )

                // Luminous meniscus droplet at leading edge
                val tipVisibility =
                    LiquidProgressBarMotion.smoothstep(0.02f, 0.10f, animatedProgress) *
                        LiquidProgressBarMotion.smoothstep(1.0f, 0.96f, animatedProgress)
                if (tipVisibility > 0.01f) {
                    drawMeniscusDroplet(
                        center = Offset(tipX, tipY),
                        strokePx = strokePx,
                        primaryColor = activePrimary,
                        specularTip = specularTip,
                        alpha = tipVisibility,
                    )
                }
            }
        }
    } else {
        Canvas(modifier = semanticsModifier.fillMaxWidth().height(height)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            if (canvasWidth <= 0f || canvasHeight <= 0f) return@Canvas

            val centerY = canvasHeight / 2f
            val strokePx = (canvasHeight * 0.48f).coerceIn(2.2.dp.toPx(), 4.5.dp.toPx())
            val capInset = strokePx / 2f
            val maxAmplitude = ((canvasHeight - strokePx) * 0.38f).coerceAtLeast(0f)
            val wavelength = (canvasHeight * 4.8f).coerceAtLeast(28.dp.toPx())
            val step = 2.dp.toPx().coerceAtLeast(2f)
            val sheenLift = -strokePx * 0.16f

            drawGlassLinearTrack(
                startX = capInset,
                endX = canvasWidth - capInset,
                centerY = centerY,
                strokePx = strokePx,
                trackColor = defaultTrack,
                isLight = isLight,
            )

            // Primary flowing liquid crest + gentle trailing echo
            drawTravelingLiquidSegment(
                activePath = activePath,
                sheenPath = sheenPath,
                travel = indeterminateTravel,
                canvasWidth = canvasWidth,
                capInset = capInset,
                centerY = centerY,
                strokePx = strokePx,
                amplitude = maxAmplitude,
                wavelength = wavelength,
                wavePhase = wavePhase,
                sheenLift = sheenLift,
                step = step,
                widthRatio = 0.30f + 0.14f * sin(indeterminateTravel * PI).toFloat(),
                alphaScale = 1f,
                activePrimary = activePrimary,
                activeSecondary = activeSecondary,
                specularTip = specularTip,
                isLight = isLight,
                drawHeadDroplet = true,
            )

            val secondaryTravel = (indeterminateTravel + 0.52f) % 1f
            drawTravelingLiquidSegment(
                activePath = activePath,
                sheenPath = sheenPath,
                travel = secondaryTravel,
                canvasWidth = canvasWidth,
                capInset = capInset,
                centerY = centerY,
                strokePx = strokePx * 0.85f,
                amplitude = maxAmplitude * 0.72f,
                wavelength = wavelength,
                wavePhase = wavePhase + 1.1f,
                sheenLift = sheenLift,
                step = step,
                widthRatio = 0.18f + 0.06f * sin(secondaryTravel * PI).toFloat(),
                alphaScale = 0.48f,
                activePrimary = activePrimary,
                activeSecondary = activeSecondary,
                specularTip = specularTip,
                isLight = isLight,
                drawHeadDroplet = false,
            )
        }
    }
}

private fun DrawScope.drawGlassLinearTrack(
    startX: Float,
    endX: Float,
    centerY: Float,
    strokePx: Float,
    trackColor: Color,
    isLight: Boolean,
) {
    if (endX <= startX) return
    val trackStroke = strokePx * 0.76f
    drawLine(
        color = trackColor,
        start = Offset(startX, centerY),
        end = Offset(endX, centerY),
        strokeWidth = trackStroke,
        cap = StrokeCap.Round,
    )
    drawLine(
        color = Color.White.copy(alpha = if (isLight) 0.32f else 0.09f),
        start = Offset(startX, centerY - trackStroke * 0.24f),
        end = Offset(endX, centerY - trackStroke * 0.24f),
        strokeWidth = (trackStroke * 0.20f).coerceAtLeast(0.6.dp.toPx()),
        cap = StrokeCap.Round,
    )
}

private fun buildLiquidWavePaths(
    activePath: Path,
    sheenPath: Path,
    startX: Float,
    endX: Float,
    centerY: Float,
    amplitude: Float,
    wavelength: Float,
    wavePhase: Float,
    edgeFade: Float,
    sheenLift: Float,
    step: Float,
): Pair<Float, Float> {
    activePath.reset()
    sheenPath.reset()

    var x = startX
    var isFirst = true
    var lastX = startX
    var lastY = centerY
    val twoPiOverWave = (2.0 * PI) / wavelength.coerceAtLeast(1f)

    while (x <= endX) {
        val env = LiquidProgressBarMotion.waveEdgeEnvelope(x, startX, endX, edgeFade)
        val y = centerY + amplitude * env * sin(x * twoPiOverWave + wavePhase).toFloat()
        if (isFirst) {
            activePath.moveTo(x, y)
            sheenPath.moveTo(x, y + sheenLift)
            isFirst = false
        } else {
            activePath.lineTo(x, y)
            sheenPath.lineTo(x, y + sheenLift)
        }
        lastX = x
        lastY = y
        if (x == endX) break
        x = (x + step).coerceAtMost(endX)
    }
    return lastX to lastY
}

private fun DrawScope.drawTravelingLiquidSegment(
    activePath: Path,
    sheenPath: Path,
    travel: Float,
    canvasWidth: Float,
    capInset: Float,
    centerY: Float,
    strokePx: Float,
    amplitude: Float,
    wavelength: Float,
    wavePhase: Float,
    sheenLift: Float,
    step: Float,
    widthRatio: Float,
    alphaScale: Float,
    activePrimary: Color,
    activeSecondary: Color,
    specularTip: Color,
    isLight: Boolean,
    drawHeadDroplet: Boolean,
) {
    val segmentWidth = (canvasWidth * widthRatio).coerceAtLeast(strokePx * 3f)
    val rawStart = -segmentWidth + (canvasWidth + segmentWidth) * travel
    val rawEnd = rawStart + segmentWidth
    val clippedStart = rawStart.coerceAtLeast(capInset)
    val clippedEnd = rawEnd.coerceAtMost(canvasWidth - capInset)
    if (clippedEnd - clippedStart <= 1f) return

    val edgeFade = (segmentWidth * 0.34f).coerceAtLeast(12.dp.toPx())
    val (headX, headY) =
        buildLiquidWavePaths(
            activePath = activePath,
            sheenPath = sheenPath,
            startX = clippedStart,
            endX = clippedEnd,
            centerY = centerY,
            amplitude = amplitude,
            wavelength = wavelength,
            wavePhase = wavePhase,
            edgeFade = edgeFade,
            sheenLift = sheenLift,
            step = step,
        )

    val ribbonBrush =
        Brush.horizontalGradient(
            0.0f to Color.Transparent,
            0.22f to activePrimary.copy(alpha = 0.65f * alphaScale),
            0.64f to activeSecondary.copy(alpha = 0.94f * alphaScale),
            0.90f to activePrimary.copy(alpha = alphaScale),
            1.0f to specularTip.copy(alpha = 0.35f * alphaScale),
            startX = rawStart,
            endX = rawEnd,
        )

    drawPath(
        path = activePath,
        brush = ribbonBrush,
        alpha = if (isLight) 0.20f else 0.26f,
        style = Stroke(width = strokePx * 1.85f, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    drawPath(
        path = activePath,
        brush = ribbonBrush,
        style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )

    drawPath(
        path = sheenPath,
        color = Color.White.copy(alpha = (if (isLight) 0.34f else 0.24f) * alphaScale),
        style =
            Stroke(
                width = (strokePx * 0.24f).coerceAtLeast(0.7.dp.toPx()),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
    )

    if (drawHeadDroplet && rawEnd > capInset && rawEnd < canvasWidth - capInset) {
        val edgeAlpha =
            LiquidProgressBarMotion.smoothstep(capInset, capInset + 16.dp.toPx(), rawEnd) *
                LiquidProgressBarMotion.smoothstep(
                    canvasWidth - capInset,
                    canvasWidth - capInset - 16.dp.toPx(),
                    rawEnd,
                )
        if (edgeAlpha > 0.02f) {
            drawMeniscusDroplet(
                center = Offset(headX, headY),
                strokePx = strokePx,
                primaryColor = activePrimary,
                specularTip = specularTip,
                alpha = edgeAlpha * alphaScale,
            )
        }
    }
}

private fun DrawScope.drawMeniscusDroplet(
    center: Offset,
    strokePx: Float,
    primaryColor: Color,
    specularTip: Color,
    alpha: Float,
) {
    val haloRadius = (strokePx * 1.45f).coerceAtLeast(2f)
    drawCircle(
        brush =
            Brush.radialGradient(
                colors =
                    listOf(
                        specularTip.copy(alpha = 0.52f * alpha),
                        primaryColor.copy(alpha = 0.22f * alpha),
                        Color.Transparent,
                    ),
                center = center,
                radius = haloRadius,
            ),
        radius = haloRadius,
        center = center,
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.88f * alpha),
        radius = (strokePx * 0.28f).coerceAtLeast(1f),
        center = center,
    )
}

/**
 * LiquidCircularProgressIndicator - Organic Refractive Liquid Glass Circular Progress Indicator.
 * Features a clean recessed glass ring without outer container artifacts, a rotating comet-tail
 * Monet sweep gradient, subtle optical aura, and a luminous leading meniscus droplet.
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
    backdrop: Backdrop = emptyBackdrop(),
) {
    LiquidInputNormalization.positive(
        indicatorSize,
        "LiquidCircularProgressIndicator indicatorSize",
    )
    LiquidInputNormalization.positive(strokeWidth, "LiquidCircularProgressIndicator strokeWidth")
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.surface.luminance() > 0.5f
    val isCustomColor = color != null || progressColor != null
    val activePrimary = color ?: progressColor ?: colorScheme.primary
    val activeSecondary =
        if (isCustomColor) {
            lerp(activePrimary, Color.White, if (isLight) 0.26f else 0.36f)
        } else {
            lerp(activePrimary, colorScheme.tertiary, 0.55f)
        }
    val specularTip = lerp(activePrimary, Color.White, if (isLight) 0.62f else 0.78f)
    val defaultTrack = trackColor ?: LiquidGlassTheme.colors.inactiveTrack
    val normalizedProgress = LiquidInputNormalization.optionalUnit(progress)
    val semanticsModifier =
        if (normalizedProgress != null) {
            modifier.progressSemantics(normalizedProgress)
        } else {
            modifier.progressSemantics()
        }

    // Keep backdrop for API compatibility; rendering bare fluid geometry prevents nested
    // glass pills inside buttons, cards, and modal overlays.
    @Suppress("UNUSED_EXPRESSION") backdrop

    val infiniteTransition =
        if (normalizedProgress == null && performance.animateFunctionalContent) {
            rememberLiquidInfiniteTransition(label = "CircularFluidProgressAnimation")
        } else {
            null
        }
    val baseRotation by
        if (infiniteTransition != null) {
            LiquidProgressBarMotion.rememberRotation(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 360f,
                label = "CircularBaseRotation",
            )
        } else {
            remember { mutableFloatStateOf(-90f) }
        }

    val sweepCycle by
        if (infiniteTransition != null) {
            LiquidProgressBarMotion.rememberSweepCycle(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "CircularSweepCycle",
            )
        } else {
            remember { mutableFloatStateOf(0.25f) }
        }

    if (normalizedProgress != null) {
        val animatedProgress by
            LiquidProgressBarMotion.animateFloat(
                targetValue = normalizedProgress,
                performance = performance,
                label = "determinateCircularAnim",
            )

        Canvas(
            modifier =
                semanticsModifier.defaultMinSize(
                    minWidth = indicatorSize,
                    minHeight = indicatorSize,
                )
        ) {
            val strokePx = strokeWidth.toPx().coerceAtMost(size.minDimension * 0.45f)
            val arcDiameter = (size.minDimension - strokePx).coerceAtLeast(1f)
            val topLeft =
                Offset(
                    (size.width - arcDiameter) / 2f,
                    (size.height - arcDiameter) / 2f,
                )
            val arcSize = Size(arcDiameter, arcDiameter)
            val arcCenter = Offset(size.width / 2f, size.height / 2f)
            val radius = arcDiameter / 2f

            drawCircularGlassTrack(
                center = arcCenter,
                radius = radius,
                topLeft = topLeft,
                arcSize = arcSize,
                strokePx = strokePx,
                trackColor = defaultTrack,
                isLight = isLight,
                showInnerRim = size.minDimension >= 24.dp.toPx(),
            )

            val sweepAngle = (360f * animatedProgress).coerceIn(0f, 360f)
            if (sweepAngle > 0.5f) {
                val closeBlend = LiquidProgressBarMotion.smoothstep(0.92f, 1.0f, animatedProgress)
                val tailColor =
                    lerp(
                        activePrimary.copy(alpha = 0.62f),
                        activePrimary,
                        closeBlend,
                    )
                val tipColor = lerp(specularTip, activePrimary, closeBlend)
                val tipVisibility =
                    LiquidProgressBarMotion.smoothstep(0.03f, 0.12f, animatedProgress) *
                        LiquidProgressBarMotion.smoothstep(1.0f, 0.96f, animatedProgress)

                drawRotatedLiquidArc(
                    startRotationDegrees = -90f,
                    sweepAngle = sweepAngle,
                    center = arcCenter,
                    radius = radius,
                    topLeft = topLeft,
                    arcSize = arcSize,
                    strokePx = strokePx,
                    tailColor = tailColor,
                    midPrimary = activePrimary,
                    secondaryHarmony = activeSecondary,
                    tipColor = tipColor,
                    primaryColor = activePrimary,
                    isLight = isLight,
                    showAura = size.minDimension >= 22.dp.toPx(),
                    dropletAlpha = tipVisibility,
                )
            }
        }
    } else {
        Canvas(
            modifier =
                semanticsModifier.defaultMinSize(
                    minWidth = indicatorSize,
                    minHeight = indicatorSize,
                )
        ) {
            val strokePx = strokeWidth.toPx().coerceAtMost(size.minDimension * 0.45f)
            val arcDiameter = (size.minDimension - strokePx).coerceAtLeast(1f)
            val topLeft =
                Offset(
                    (size.width - arcDiameter) / 2f,
                    (size.height - arcDiameter) / 2f,
                )
            val arcSize = Size(arcDiameter, arcDiameter)
            val arcCenter = Offset(size.width / 2f, size.height / 2f)
            val radius = arcDiameter / 2f

            drawCircularGlassTrack(
                center = arcCenter,
                radius = radius,
                topLeft = topLeft,
                arcSize = arcSize,
                strokePx = strokePx,
                trackColor = defaultTrack,
                isLight = isLight,
                showInnerRim = size.minDimension >= 24.dp.toPx(),
            )

            val arcState =
                LiquidProgressBarMotion.computeIndeterminateCircularArc(
                    baseRotationDegrees = baseRotation,
                    sweepCycleFraction = sweepCycle,
                )

            drawRotatedLiquidArc(
                startRotationDegrees = arcState.startAngle,
                sweepAngle = arcState.sweepAngle,
                center = arcCenter,
                radius = radius,
                topLeft = topLeft,
                arcSize = arcSize,
                strokePx = strokePx,
                tailColor = activePrimary.copy(alpha = 0.04f),
                midPrimary = activePrimary.copy(alpha = 0.82f),
                secondaryHarmony = activeSecondary,
                tipColor = specularTip,
                primaryColor = activePrimary,
                isLight = isLight,
                showAura = size.minDimension >= 22.dp.toPx(),
                dropletAlpha = if (size.minDimension >= 22.dp.toPx()) 1f else 0.45f,
            )
        }
    }
}

private fun DrawScope.drawCircularGlassTrack(
    center: Offset,
    radius: Float,
    topLeft: Offset,
    arcSize: Size,
    strokePx: Float,
    trackColor: Color,
    isLight: Boolean,
    showInnerRim: Boolean,
) {
    drawArc(
        color = trackColor,
        startAngle = 0f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokePx, cap = StrokeCap.Round),
    )
    if (showInnerRim) {
        val innerRadius = (radius - strokePx * 0.42f).coerceAtLeast(1f)
        drawCircle(
            color = Color.White.copy(alpha = if (isLight) 0.30f else 0.10f),
            radius = innerRadius,
            center = center,
            style = Stroke(width = 0.75.dp.toPx()),
        )
    }
}

private fun DrawScope.drawRotatedLiquidArc(
    startRotationDegrees: Float,
    sweepAngle: Float,
    center: Offset,
    radius: Float,
    topLeft: Offset,
    arcSize: Size,
    strokePx: Float,
    tailColor: Color,
    midPrimary: Color,
    secondaryHarmony: Color,
    tipColor: Color,
    primaryColor: Color,
    isLight: Boolean,
    showAura: Boolean,
    dropletAlpha: Float,
) {
    rotate(degrees = startRotationDegrees, pivot = center) {
        val sweepFrac = (sweepAngle / 360f).coerceIn(0.06f, 0.94f)
        val stop1 = sweepFrac * 0.35f
        val stop2 = sweepFrac * 0.78f
        val tipCapEnd = (sweepFrac + 0.025f).coerceAtMost(0.965f)
        val tailCapStart = (tipCapEnd + 0.01f).coerceAtMost(0.985f)

        val sweepBrush =
            Brush.sweepGradient(
                0.0f to tailColor,
                stop1 to midPrimary,
                stop2 to secondaryHarmony,
                sweepFrac to tipColor,
                tipCapEnd to tipColor,
                tailCapStart to tailColor,
                1.0f to tailColor,
                center = center,
            )

        // Soft ambient refraction halo
        if (showAura) {
            drawArc(
                brush = sweepBrush,
                startAngle = 0f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                alpha = if (isLight) 0.18f else 0.24f,
                style = Stroke(width = strokePx * 1.9f, cap = StrokeCap.Round),
            )
        }

        // Crisp comet liquid arc
        drawArc(
            brush = sweepBrush,
            startAngle = 0f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )

        // Specular leading meniscus droplet
        if (dropletAlpha > 0.02f) {
            val tipRad = sweepAngle * (PI / 180.0)
            val tipCenter =
                Offset(
                    x = center.x + radius * cos(tipRad).toFloat(),
                    y = center.y + radius * sin(tipRad).toFloat(),
                )
            drawMeniscusDroplet(
                center = tipCenter,
                strokePx = strokePx,
                primaryColor = primaryColor,
                specularTip = tipColor,
                alpha = dropletAlpha,
            )
        }
    }
}
