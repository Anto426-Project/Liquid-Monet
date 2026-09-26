package com.anto426.liquidmonet.components.charts.linechart

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.charts.linechart.motion.LiquidLineChartMotion
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.LiquidChartStyle
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateBackground
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import kotlin.math.PI
import kotlin.math.sin

/**
 * LiquidLineChart - Advanced Fluid Multi-Series Chart with Consistent Refractive Lens Points.
 *
 * Implements identical optical refractive lens droplets across all data points, Catmull-Rom velvety
 * spline curves, and multi-series controls.
 */
@Composable
fun LiquidLineChart(
    entries: List<LiquidChartEntry>,
    modifier: Modifier = Modifier,
    weightedAverageEntries: List<LiquidChartEntry>? = null,
    arithmeticAverageEntries: List<LiquidChartEntry>? = null,
    height: Dp = 215.dp,
    lineColor: Color? = null,
    weightedColor: Color? = null,
    arithmeticColor: Color? = null,
    gradientStartColor: Color? = null,
    style: LiquidChartStyle = LiquidChartStyle(),
    minValue: Float? = null,
    maxValue: Float? = null,
    showLegend: Boolean = true,
    primarySeriesLabel: String = "Valore",
    secondarySeriesLabel: String = "Serie 2",
    tertiarySeriesLabel: String = "Serie 3",
    valueSuffix: String = "",
    valueFormatter: (Float) -> String = ::formatLiquidChartValue,
    showDetails: Boolean = false,
    backdropState: Backdrop = emptyBackdrop(),
    onEntrySelected: ((LiquidChartEntry) -> Unit)? = null,
) {
    LiquidInputNormalization.positive(height, "LiquidLineChart height")
    require(
        entries.all { it.value.isFinite() } &&
            weightedAverageEntries.orEmpty().all { it.value.isFinite() } &&
            arithmeticAverageEntries.orEmpty().all { it.value.isFinite() }
    ) {
        "LiquidLineChart entry values must be finite."
    }
    minValue?.let { LiquidInputNormalization.finite(it, "LiquidLineChart minValue") }
    maxValue?.let { LiquidInputNormalization.finite(it, "LiquidLineChart maxValue") }
    require(minValue == null || maxValue == null || maxValue > minValue) {
        "LiquidLineChart maxValue must be greater than minValue."
    }
    if (entries.isEmpty()) return

    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = lineColor ?: colorScheme.primary
    val secondaryColor = weightedColor ?: colorScheme.tertiary
    val tertiaryColor = arithmeticColor ?: colorScheme.secondary
    val areaColor = gradientStartColor ?: primaryColor
    val gridColor = colorScheme.outlineVariant.copy(alpha = 0.16f)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val performance = LocalLiquidGlassPerformance.current
    val animateDecorations = performance.animateBackground

    // Series visibility toggles
    var showGradesLine by remember { mutableStateOf(true) }
    var showWeightedLine by remember { mutableStateOf(true) }
    var showArithmeticLine by remember { mutableStateOf(true) }

    // Combined bounds calculation
    val allValues =
        entries.map { it.value } +
            (weightedAverageEntries?.map { it.value } ?: emptyList()) +
            (arithmeticAverageEntries?.map { it.value } ?: emptyList())

    val dataMin = minValue ?: (allValues.minOrNull() ?: 0f)
    val dataMax = maxValue ?: ((allValues.maxOrNull() ?: 100f) + 1.4f)
    val actualMin = allValues.minOrNull() ?: dataMin
    val actualMax = allValues.maxOrNull() ?: dataMax
    val valueRange = (dataMax - dataMin).coerceAtLeast(0.001f)

    // Fluid entrance animation with spring
    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by
        LiquidLineChartMotion.animateFloatPrimary(
            targetValue = if (animationPlayed) 1f else 0f,
            performance = performance,
            label = "LineChartProgress",
        )
    androidx.compose.runtime.LaunchedEffect(entries) {
        animationPlayed = true
    }

    // Dynamic wave phase
    val infiniteTransition =
        if (animateDecorations) {
            rememberLiquidInfiniteTransition(label = "LiquidChartHarmonics")
        } else {
            null
        }
    val wavePhase by
        if (infiniteTransition != null) {
            LiquidLineChartMotion.rememberWavePhase(
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
            LiquidLineChartMotion.rememberShimmerOffset(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "ShimmerOffset",
            )
        } else {
            remember { mutableFloatStateOf(0f) }
        }

    // Persistent touch scrubber state with spring glide
    var selectedIndex by remember { mutableIntStateOf(entries.lastIndex) }
    var isDragging by remember { mutableStateOf(false) }

    val animatedSelectedIndex by
        LiquidLineChartMotion.animateFloatSecondary(
            targetValue = selectedIndex.toFloat(),
            performance = performance,
            label = "AnimatedScrubberPosition",
        )

    val effectiveSelectedIndex = selectedIndex.coerceIn(0, entries.lastIndex)
    val selectedEntry = entries[effectiveSelectedIndex]
    val selectedWeighted = weightedAverageEntries?.getOrNull(effectiveSelectedIndex)
    val selectedArithmetic = arithmeticAverageEntries?.getOrNull(effectiveSelectedIndex)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 1. Series Pill Controls (Compact, non-wrapping chips)
        if (showLegend) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier.clip(Capsule())
                            .background(
                                if (showGradesLine) primaryColor.copy(alpha = 0.16f)
                                else colorScheme.surfaceVariant.copy(alpha = 0.40f)
                            )
                            .clickable { showGradesLine = !showGradesLine }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier.size(6.dp)
                                    .clip(Capsule())
                                    .background(
                                        if (showGradesLine) primaryColor
                                        else colorScheme.outlineVariant
                                    )
                        )
                        Text(
                            text = primarySeriesLabel,
                            fontSize = 11.sp,
                            fontWeight = if (showGradesLine) FontWeight.Bold else FontWeight.Medium,
                            color =
                                if (showGradesLine) primaryColor else colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false,
                        )
                    }
                }

                if (weightedAverageEntries != null) {
                    Box(
                        modifier =
                            Modifier.clip(Capsule())
                                .background(
                                    if (showWeightedLine) secondaryColor.copy(alpha = 0.16f)
                                    else colorScheme.surfaceVariant.copy(alpha = 0.40f)
                                )
                                .clickable { showWeightedLine = !showWeightedLine }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(6.dp)
                                        .clip(Capsule())
                                        .background(
                                            if (showWeightedLine) secondaryColor
                                            else colorScheme.outlineVariant
                                        )
                            )
                            Text(
                                text = secondarySeriesLabel,
                                fontSize = 11.sp,
                                fontWeight =
                                    if (showWeightedLine) FontWeight.Bold else FontWeight.Medium,
                                color =
                                    if (showWeightedLine) secondaryColor
                                    else colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }

                if (arithmeticAverageEntries != null) {
                    Box(
                        modifier =
                            Modifier.clip(Capsule())
                                .background(
                                    if (showArithmeticLine) tertiaryColor.copy(alpha = 0.16f)
                                    else colorScheme.surfaceVariant.copy(alpha = 0.40f)
                                )
                                .clickable { showArithmeticLine = !showArithmeticLine }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(6.dp)
                                        .clip(Capsule())
                                        .background(
                                            if (showArithmeticLine) tertiaryColor
                                            else colorScheme.outlineVariant
                                        )
                            )
                            Text(
                                text = tertiarySeriesLabel,
                                fontSize = 11.sp,
                                fontWeight =
                                    if (showArithmeticLine) FontWeight.Bold else FontWeight.Medium,
                                color =
                                    if (showArithmeticLine) tertiaryColor
                                    else colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Min ${valueFormatter(actualMin)}${valueSuffix.withLeadingSpace()}",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Max ${valueFormatter(actualMax)}${valueSuffix.withLeadingSpace()}",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant,
            )
        }

        // 2. Optical Canvas Viewport with Uniform Refractive Lens Droplets
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(height)
                    .pointerInput(entries) {
                        val paddingHorizontalPx = 22.dp.toPx()
                        detectTapGestures(
                            onPress = { offset ->
                                val availableWidth =
                                    (size.width - paddingHorizontalPx * 2).coerceAtLeast(1f)
                                val step = availableWidth / (entries.size - 1).coerceAtLeast(1)
                                val relX =
                                    (offset.x - paddingHorizontalPx).coerceIn(0f, availableWidth)
                                val index =
                                    ((relX + step / 2) / step)
                                        .toInt()
                                        .coerceIn(0, entries.lastIndex)
                                selectedIndex = index
                                onEntrySelected?.invoke(entries[index])
                            }
                        )
                    }
                    .pointerInput(entries) {
                        val paddingHorizontalPx = 22.dp.toPx()
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val availableWidth =
                                    (size.width - paddingHorizontalPx * 2).coerceAtLeast(1f)
                                val step = availableWidth / (entries.size - 1).coerceAtLeast(1)
                                val relX =
                                    (offset.x - paddingHorizontalPx).coerceIn(0f, availableWidth)
                                val index =
                                    ((relX + step / 2) / step)
                                        .toInt()
                                        .coerceIn(0, entries.lastIndex)
                                selectedIndex = index
                                onEntrySelected?.invoke(entries[index])
                            },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, _ ->
                                change.consume()
                                val availableWidth =
                                    (size.width - paddingHorizontalPx * 2).coerceAtLeast(1f)
                                val step = availableWidth / (entries.size - 1).coerceAtLeast(1)
                                val relX =
                                    (change.position.x - paddingHorizontalPx).coerceIn(
                                        0f,
                                        availableWidth,
                                    )
                                val index =
                                    ((relX + step / 2) / step)
                                        .toInt()
                                        .coerceIn(0, entries.lastIndex)
                                selectedIndex = index
                                onEntrySelected?.invoke(entries[index])
                            },
                        )
                    }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val canvasHeight = size.height
                val paddingHorizontal = 22.dp.toPx()
                val paddingTop = 20.dp.toPx()
                val paddingBottom = 16.dp.toPx()
                val availableWidth = width - paddingHorizontal * 2
                val availableHeight = canvasHeight - paddingTop - paddingBottom

                fun buildPoints(data: List<LiquidChartEntry>): List<Offset> {
                    return data.mapIndexed { index, entry ->
                        val x =
                            if (data.size <= 1) width / 2f
                            else paddingHorizontal + index * (availableWidth / (data.size - 1))
                        val normalized = (entry.value - dataMin) / valueRange
                        val y =
                            canvasHeight -
                                paddingBottom -
                                (normalized * availableHeight * progressAnimation)
                        Offset(x, y)
                    }
                }

                fun buildSmoothPath(pts: List<Offset>): Path {
                    return Path().apply {
                        if (pts.isEmpty()) return@apply
                        moveTo(pts[0].x, pts[0].y)
                        val tension = 0.32f
                        for (i in 0 until pts.size - 1) {
                            val p0 = if (i > 0) pts[i - 1] else pts[i]
                            val p1 = pts[i]
                            val p2 = pts[i + 1]
                            val p3 = if (i + 2 < pts.size) pts[i + 2] else p2

                            val cp1x = p1.x + (p2.x - p0.x) * (tension / 2f)
                            val cp1y = p1.y + (p2.y - p0.y) * (tension / 2f)
                            val cp2x = p2.x - (p3.x - p1.x) * (tension / 2f)
                            val cp2y = p2.y - (p3.y - p1.y) * (tension / 2f)

                            cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                        }
                    }
                }

                // Uniform Optical Refractive Droplet Renderer (Switch Fresnel Lens DNA)
                fun drawRefractiveDroplet(
                    center: Offset,
                    color: Color,
                    isSelected: Boolean,
                    baseRadius: Float,
                ) {
                    val radius = if (isSelected) baseRadius * 1.35f else baseRadius

                    if (isSelected) {
                        // A. Caustic Diffuse Halo
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            color.copy(alpha = 0.55f),
                                            color.copy(alpha = 0.18f),
                                            Color.Transparent,
                                        ),
                                    center = center,
                                    radius = radius * 2.5f,
                                ),
                            radius = radius * 2.5f,
                            center = center,
                        )
                    }

                    // B. Outer Depth Drop Shadow
                    drawCircle(
                        color = Color.Black.copy(alpha = if (isSelected) 0.14f else 0.08f),
                        radius = radius + 0.8.dp.toPx(),
                        center = Offset(center.x, center.y + 0.8.dp.toPx()),
                    )

                    // C. Outer Refractive Glass Shell (Pure Specular White Rim)
                    drawCircle(
                        color = Color.White.copy(alpha = if (isSelected) 1.0f else 0.92f),
                        radius = radius,
                        center = center,
                    )

                    // D. Internal Refractive Lens Core (Fresnel Gradient with Off-center Glint)
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = if (isSelected) 0.95f else 0.80f),
                                        color.copy(alpha = if (isSelected) 0.90f else 0.75f),
                                        color,
                                    ),
                                center =
                                    Offset(center.x - radius * 0.28f, center.y - radius * 0.28f),
                                radius = radius * 0.85f,
                            ),
                        radius = radius * 0.75f,
                        center = center,
                    )

                    // E. Specular Highlight Glint Sparkle
                    drawCircle(
                        color = Color.White.copy(alpha = if (isSelected) 0.95f else 0.85f),
                        radius = radius * 0.22f,
                        center = Offset(center.x - radius * 0.26f, center.y - radius * 0.26f),
                    )
                }

                val gradePoints = buildPoints(entries)
                val weightedPoints = weightedAverageEntries?.let { buildPoints(it) }
                val arithmeticPoints = arithmeticAverageEntries?.let { buildPoints(it) }

                // 1. Dashed Horizontal Guide Tracks
                if (style.showGridLines) {
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val gridY = paddingTop + (availableHeight / gridSteps) * i
                        drawLine(
                            color = gridColor,
                            start = Offset(paddingHorizontal * 0.5f, gridY),
                            end = Offset(width - paddingHorizontal * 0.5f, gridY),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f)),
                        )
                    }
                }

                // 2. Dual-Layered Organic Liquid Caustic Fill
                if (showGradesLine && style.showRefractiveArea && gradePoints.size >= 2) {
                    val curvePath = buildSmoothPath(gradePoints)
                    val baseAreaPath =
                        Path().apply {
                            addPath(curvePath)
                            lineTo(gradePoints.last().x, canvasHeight - paddingBottom)
                            lineTo(gradePoints.first().x, canvasHeight - paddingBottom)
                            close()
                        }

                    drawPath(
                        path = baseAreaPath,
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        areaColor.copy(alpha = 0.35f * progressAnimation),
                                        secondaryColor.copy(alpha = 0.12f * progressAnimation),
                                        Color.Transparent,
                                    ),
                                startY = paddingTop,
                                endY = canvasHeight,
                            ),
                    )

                    // Fluid Harmonic Wave Area
                    val waveSteps = 32
                    val wavePath =
                        Path().apply {
                            moveTo(gradePoints.first().x, canvasHeight)
                            for (i in 0..waveSteps) {
                                val wx =
                                    gradePoints.first().x +
                                        (i.toFloat() / waveSteps) * availableWidth
                                val waveFactor = sin(wx / 48f + wavePhase) * 5.5.dp.toPx()
                                val progressX = (wx - gradePoints.first().x) / availableWidth
                                val approxY =
                                    gradePoints.first().y +
                                        (gradePoints.last().y - gradePoints.first().y) * progressX +
                                        16.dp.toPx() +
                                        waveFactor
                                lineTo(wx, approxY)
                            }
                            lineTo(gradePoints.last().x, canvasHeight)
                            close()
                        }

                    drawPath(
                        path = wavePath,
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        secondaryColor.copy(alpha = 0.08f * progressAnimation),
                                        primaryColor.copy(alpha = 0.02f * progressAnimation),
                                        Color.Transparent,
                                    ),
                                startY = paddingTop,
                                endY = canvasHeight,
                            ),
                    )
                }

                // 3. Render Tertiary Curve: Media Aritmetica
                if (showArithmeticLine && arithmeticPoints != null && arithmeticPoints.size >= 2) {
                    val arithPath = buildSmoothPath(arithmeticPoints)
                    drawPath(
                        path = arithPath,
                        color = tertiaryColor.copy(alpha = 0.85f),
                        style =
                            Stroke(
                                width = 1.8.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 5f)),
                            ),
                    )

                    // Draw ONLY the selected point
                    if (effectiveSelectedIndex in arithmeticPoints.indices) {
                        drawRefractiveDroplet(
                            center = arithmeticPoints[effectiveSelectedIndex],
                            color = tertiaryColor,
                            isSelected = true,
                            baseRadius = 3.6.dp.toPx(),
                        )
                    }
                }

                // 4. Render Secondary Curve: Media Ponderata
                if (showWeightedLine && weightedPoints != null && weightedPoints.size >= 2) {
                    val weightedPath = buildSmoothPath(weightedPoints)
                    drawPath(
                        path = weightedPath,
                        color = secondaryColor.copy(alpha = 0.95f),
                        style =
                            Stroke(
                                width = 2.4.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                    )

                    // Draw ONLY the selected point
                    if (effectiveSelectedIndex in weightedPoints.indices) {
                        drawRefractiveDroplet(
                            center = weightedPoints[effectiveSelectedIndex],
                            color = secondaryColor,
                            isSelected = true,
                            baseRadius = 4.0.dp.toPx(),
                        )
                    }
                }

                // 5. Render Primary Curve: Voti Singoli
                if (showGradesLine && gradePoints.size >= 2) {
                    val curvePath = buildSmoothPath(gradePoints)
                    drawPath(
                        path = curvePath,
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        primaryColor,
                                        secondaryColor,
                                        primaryColor.copy(alpha = 0.9f),
                                        Color.White.copy(alpha = 0.85f),
                                        primaryColor,
                                    ),
                                startX = -200f + (width + 400f) * shimmerOffset,
                                endX = (width + 400f) * shimmerOffset,
                            ),
                        style =
                            Stroke(
                                width = style.strokeWidth.toPx() * 1.15f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                            ),
                    )

                    // Draw ONLY the selected point
                    if (effectiveSelectedIndex in gradePoints.indices) {
                        drawRefractiveDroplet(
                            center = gradePoints[effectiveSelectedIndex],
                            color = primaryColor,
                            isSelected = true,
                            baseRadius = 4.6.dp.toPx(),
                        )
                    }
                }

                // 6. Smooth Scrubber Hairline with Top Tracking Pip
                if (gradePoints.isNotEmpty()) {
                    val step = availableWidth / (entries.size - 1).coerceAtLeast(1)
                    val smoothX = paddingHorizontal + animatedSelectedIndex * step

                    // Scrubber Hairline
                    drawLine(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        primaryColor.copy(alpha = 0.75f),
                                        secondaryColor.copy(alpha = 0.30f),
                                        Color.Transparent,
                                    )
                            ),
                        start = Offset(smoothX, 0f),
                        end = Offset(smoothX, canvasHeight),
                        strokeWidth = 1.4.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
                    )

                    // Top Tracking Pip
                    drawCircle(
                        color = primaryColor,
                        radius = 2.8.dp.toPx(),
                        center = Offset(smoothX, 5.dp.toPx()),
                    )
                }
            }
        }

        if (showDetails) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = entries.first().label,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
                if (entries.size > 1) {
                    Text(
                        text = entries.last().label,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // 3. Selection Detail Display (Clean, flat, no double box)
        AnimatedContent(
            targetState = selectedEntry,
            transitionSpec = LiquidLineChartMotion.contentTransition(performance),
            label = "LineSelectionDetailTransition",
        ) { currentSelection ->
            val curWeighted = selectedWeighted?.value ?: currentSelection.secondaryValue
            val curArithmetic = selectedArithmetic?.value

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = currentSelection.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false).padding(end = 12.dp),
                    )

                    Text(
                        text =
                            "${valueFormatter(currentSelection.value)}${valueSuffix.withLeadingSpace()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor,
                    )
                }

                if (curWeighted != null || curArithmetic != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (curWeighted != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier.size(6.dp)
                                            .clip(Capsule())
                                            .background(secondaryColor)
                                )
                                Text(
                                    text = secondarySeriesLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text =
                                        "${valueFormatter(curWeighted)}${valueSuffix.withLeadingSpace()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = secondaryColor,
                                )
                            }
                        }

                        if (curArithmetic != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier =
                                        Modifier.size(6.dp)
                                            .clip(Capsule())
                                            .background(tertiaryColor)
                                )
                                Text(
                                    text = tertiarySeriesLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text =
                                        "${valueFormatter(curArithmetic)}${valueSuffix.withLeadingSpace()}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tertiaryColor,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun String.withLeadingSpace(): String = if (isBlank()) "" else " $this"
