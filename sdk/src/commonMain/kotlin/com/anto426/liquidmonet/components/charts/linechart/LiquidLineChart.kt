package com.anto426.liquidmonet.components.charts.linechart

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.charts.internal.LiquidChartAxis
import com.anto426.liquidmonet.components.charts.internal.LiquidChartLegend
import com.anto426.liquidmonet.components.charts.internal.LiquidChartLegendChip
import com.anto426.liquidmonet.components.charts.internal.LiquidChartSelectionReadout
import com.anto426.liquidmonet.components.charts.internal.LiquidChartSeriesValue
import com.anto426.liquidmonet.components.charts.internal.liquidChartScrub
import com.anto426.liquidmonet.components.charts.internal.toMark
import com.anto426.liquidmonet.components.charts.linechart.motion.LiquidLineChartMotion
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.LiquidLineChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidLineSeries
import com.anto426.liquidmonet.components.charts.model.LiquidLineStyle
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateBackground
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Single-series frameless convenience overload of [LiquidLineChart].
 *
 * Renders purely on a transparent surface with zero built-in card or badge containers.
 */
@Composable
fun LiquidLineChart(
    entries: List<LiquidChartEntry>,
    modifier: Modifier = Modifier,
    label: String = "",
    color: Color? = null,
    lineStyle: LiquidLineStyle = LiquidLineStyle.Solid,
    showArea: Boolean = true,
    height: Dp = 200.dp,
    style: LiquidLineChartStyle = LiquidLineChartStyle(),
    minValue: Float? = null,
    maxValue: Float? = null,
    showSelection: Boolean = false,
    interactive: Boolean = true,
    selectedIndex: Int? = null,
    valueSuffix: String = "",
    valueFormatter: (Float) -> String = ::formatLiquidChartValue,
    axisValueFormatter: (Float) -> String = valueFormatter,
    contentSpacing: Dp = 12.dp,
    selectionContent: (@Composable (index: Int, entry: LiquidChartEntry) -> Unit)? = null,
    onEntrySelected: ((index: Int, entry: LiquidChartEntry) -> Unit)? = null,
) {
    if (entries.isEmpty()) return
    val singleSeries = remember(label, entries, color, lineStyle, showArea) {
        listOf(
            LiquidLineSeries(
                label = label,
                entries = entries,
                color = color,
                lineStyle = lineStyle,
                showArea = showArea,
                showPoints = true,
            ),
        )
    }
    LiquidLineChart(
        series = singleSeries,
        modifier = modifier,
        height = height,
        style = style,
        minValue = minValue,
        maxValue = maxValue,
        showLegend = false,
        showSelection = showSelection,
        interactive = interactive,
        selectedIndex = selectedIndex,
        valueSuffix = valueSuffix,
        valueFormatter = valueFormatter,
        axisValueFormatter = axisValueFormatter,
        contentSpacing = contentSpacing,
        selectionContent = selectionContent?.let { slot ->
            { idx, values -> values.firstOrNull()?.second?.let { slot(idx, it) } }
        },
        onEntrySelected = onEntrySelected,
    )
}

/**
 * LiquidLineChart - Containerless, multi-series fluid spline chart.
 *
 * Supports arbitrary [series] with per-series stroke style, area fill, and colors, optical
 * refractive droplets, smooth scrubbing, and optional frameless legend/selection slots.
 */
@Composable
fun LiquidLineChart(
    series: List<LiquidLineSeries>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    style: LiquidLineChartStyle = LiquidLineChartStyle(),
    minValue: Float? = null,
    maxValue: Float? = null,
    showLegend: Boolean = series.size > 1,
    showSelection: Boolean = false,
    interactive: Boolean = true,
    selectedIndex: Int? = null,
    valueSuffix: String = "",
    valueFormatter: (Float) -> String = ::formatLiquidChartValue,
    axisValueFormatter: (Float) -> String = valueFormatter,
    contentSpacing: Dp = 12.dp,
    legendContent: (@Composable (
        series: List<LiquidLineSeries>,
        visibleMask: List<Boolean>,
        onToggle: (Int) -> Unit,
    ) -> Unit)? = null,
    selectionContent: (@Composable (
        index: Int,
        values: List<Pair<LiquidLineSeries, LiquidChartEntry>>,
    ) -> Unit)? = null,
    onEntrySelected: ((index: Int, entry: LiquidChartEntry) -> Unit)? = null,
) {
    LiquidInputNormalization.positive(height, "LiquidLineChart height")
    require(contentSpacing >= 0.dp) { "LiquidLineChart contentSpacing must be non-negative." }
    val nonEmptySeries = series.filter { it.entries.isNotEmpty() }
    if (nonEmptySeries.isEmpty()) return
    require(nonEmptySeries.all { s -> s.entries.all { it.value.isFinite() } }) {
        "LiquidLineChart entry values must be finite."
    }
    minValue?.let { LiquidInputNormalization.finite(it, "LiquidLineChart minValue") }
    maxValue?.let { LiquidInputNormalization.finite(it, "LiquidLineChart maxValue") }
    require(minValue == null || maxValue == null || maxValue > minValue) {
        "LiquidLineChart maxValue must be greater than minValue."
    }

    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val defaultPalette = listOf(
        colorScheme.primary,
        colorScheme.tertiary,
        colorScheme.secondary,
        glassColors.success,
        glassColors.warning,
        colorScheme.error,
    )
    val resolvedColors = nonEmptySeries.mapIndexed { idx, s ->
        s.color ?: defaultPalette[idx % defaultPalette.size]
    }
    val primarySeries = nonEmptySeries.first()
    val primaryEntries = primarySeries.entries
    val primaryColor = resolvedColors.first()
    val secondaryAccent = resolvedColors.getOrElse(1) { colorScheme.tertiary }
    val gridColor = style.gridColor ?: colorScheme.onSurface.copy(alpha = 0.10f)
    val performance = LocalLiquidGlassPerformance.current
    val animateDecorations = performance.animateBackground

    val visibleMask = remember(nonEmptySeries) {
        mutableStateListOf<Boolean>().apply {
            nonEmptySeries.forEach { add(it.initiallyVisible) }
        }
    }

    val allValues = nonEmptySeries.flatMap { s -> s.entries.map { it.value } }
    val dataMin = minValue ?: (allValues.minOrNull() ?: 0f)
    val dataMax = maxValue ?: ((allValues.maxOrNull() ?: 100f) + 1.4f)
    val valueRange = (dataMax - dataMin).coerceAtLeast(0.001f)

    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by LiquidLineChartMotion.animateFloatPrimary(
        targetValue = if (animationPlayed) 1f else 0f,
        performance = performance,
        label = "LineChartProgress",
    )
    LaunchedEffect(nonEmptySeries) { animationPlayed = true }

    val infiniteTransition =
        if (animateDecorations) rememberLiquidInfiniteTransition(label = "LiquidChartHarmonics") else null
    val wavePhase by
        if (infiniteTransition != null && style.showWaveOverlay) {
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

    var internalSelectedIndex by remember(primaryEntries) { mutableIntStateOf(primaryEntries.lastIndex) }
    val effectiveSelectedIndex = (selectedIndex ?: internalSelectedIndex).coerceIn(0, primaryEntries.lastIndex)
    val animatedSelectedIndex by LiquidLineChartMotion.animateFloatSecondary(
        targetValue = effectiveSelectedIndex.toFloat(),
        performance = performance,
        label = "AnimatedScrubberPosition",
    )

    val selectedEntry = primaryEntries[effectiveSelectedIndex]
    val plotInset = style.plotHorizontalInset
    val verticalInset = style.plotVerticalInset ?: minOf(16.dp, height / 4)
    val horizontalInsetPx = with(LocalDensity.current) { plotInset.toPx() }
    val suffix = if (valueSuffix.isBlank()) "" else " $valueSuffix"
    val selectedValue = valueFormatter(selectedEntry.value) + suffix
    val gridSteps = (style.gridLineCount - 1).coerceAtLeast(1)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        if (showLegend) {
            if (legendContent != null) {
                legendContent(nonEmptySeries, visibleMask.toList()) { idx ->
                    if (idx in visibleMask.indices) visibleMask[idx] = !visibleMask[idx]
                }
            } else {
                LiquidChartLegend {
                    nonEmptySeries.forEachIndexed { idx, s ->
                        val isVisible = visibleMask.getOrElse(idx) { true }
                        LiquidChartLegendChip(
                            label = s.label,
                            color = resolvedColors[idx],
                            selected = isVisible,
                            mark = s.lineStyle.toMark(),
                            onClick = { if (idx in visibleMask.indices) visibleMask[idx] = !visibleMask[idx] },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (style.showYAxis) 6.dp else 0.dp),
        ) {
            if (style.showYAxis) {
                val ticks = List(style.gridLineCount) { index ->
                    val fraction = 1f - index.toFloat() / gridSteps.toFloat()
                    axisValueFormatter(dataMin + (dataMax - dataMin) * fraction)
                }
                LiquidChartAxis(
                    ticks = ticks,
                    height = height,
                    verticalInset = verticalInset,
                )
            }
            val plotModifier = Modifier
                .weight(1f)
                .height(height)
                .let { base ->
                    if (interactive) {
                        base.liquidChartScrub(
                            entryCount = primaryEntries.size,
                            selectedIndex = effectiveSelectedIndex,
                            selectedDescription = "${selectedEntry.label}, $selectedValue",
                            indexAt = { x, width ->
                                val inset = horizontalInsetPx.coerceAtMost(width / 4f)
                                val fraction = (x - inset) / (width - inset * 2f).coerceAtLeast(1f)
                                (fraction * primaryEntries.lastIndex).roundToInt().coerceIn(0, primaryEntries.lastIndex)
                            },
                            onSelect = { index ->
                                internalSelectedIndex = index
                                onEntrySelected?.invoke(index, primaryEntries[index])
                            },
                        )
                    } else {
                        base
                    }
                }

            Box(modifier = plotModifier) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val canvasHeight = size.height
                    val paddingHorizontal = minOf(plotInset.toPx(), width / 4f)
                    val paddingTop = verticalInset.toPx()
                    val paddingBottom = paddingTop
                    val availableWidth = (width - paddingHorizontal * 2f).coerceAtLeast(0f)
                    val availableHeight = (canvasHeight - paddingTop - paddingBottom).coerceAtLeast(0f)
                    val progress = progressAnimation.coerceIn(0f, 1f)

                    fun buildPoints(data: List<LiquidChartEntry>): List<Offset> =
                        data.mapIndexed { index, entry ->
                            val x = if (data.size <= 1) width / 2f
                            else paddingHorizontal + index * (availableWidth / (data.size - 1))
                            val normalized = ((entry.value - dataMin) / valueRange).coerceIn(0f, 1f)
                            val y = canvasHeight - paddingBottom - (normalized * availableHeight * progress)
                            Offset(x, y)
                        }

                    fun buildSmoothPath(pts: List<Offset>): Path = Path().apply {
                        if (pts.isEmpty()) return@apply
                        moveTo(pts[0].x, pts[0].y)
                        if (!style.smoothCurves) {
                            pts.drop(1).forEach { point -> lineTo(point.x, point.y) }
                            return@apply
                        }
                        val tension = style.curveTension
                        for (i in 0 until pts.size - 1) {
                            val p0 = if (i > 0) pts[i - 1] else pts[i]
                            val p1 = pts[i]
                            val p2 = pts[i + 1]
                            val p3 = if (i + 2 < pts.size) pts[i + 2] else p2

                            val cp1x = p1.x + (p2.x - p0.x) * (tension / 2f)
                            val cp1y = p1.y + (p2.y - p0.y) * (tension / 2f)
                            val cp2x = p2.x - (p3.x - p1.x) * (tension / 2f)
                            val cp2y = p2.y - (p3.y - p1.y) * (tension / 2f)

                            val segment = minOf(p1.y, p2.y)..maxOf(p1.y, p2.y)
                            cubicTo(cp1x, cp1y.coerceIn(segment), cp2x, cp2y.coerceIn(segment), p2.x, p2.y)
                        }
                    }

                    fun drawRefractiveDroplet(center: Offset, color: Color, baseRadius: Float) {
                        val radius = baseRadius * 1.28f
                        if (style.refractiveGlow) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color.copy(alpha = 0.50f), color.copy(alpha = 0.16f), Color.Transparent),
                                    center = center,
                                    radius = radius * 2.4f,
                                ),
                                radius = radius * 2.4f,
                                center = center,
                            )
                        }
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.12f),
                            radius = radius + 0.8.dp.toPx(),
                            center = Offset(center.x, center.y + 0.8.dp.toPx()),
                        )
                        drawCircle(color = Color.White, radius = radius, center = center)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.92f), color.copy(alpha = 0.88f), color),
                                center = Offset(center.x - radius * 0.28f, center.y - radius * 0.28f),
                                radius = radius * 0.85f,
                            ),
                            radius = radius * 0.75f,
                            center = center,
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.94f),
                            radius = radius * 0.22f,
                            center = Offset(center.x - radius * 0.26f, center.y - radius * 0.26f),
                        )
                    }

                    // 1. Horizontal guide tracks
                    if (style.showGridLines) {
                        val dashEffect = if (style.dashedGridLines) {
                            PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()))
                        } else {
                            null
                        }
                        for (i in 0..gridSteps) {
                            val gridY = paddingTop + (availableHeight / gridSteps) * i
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, gridY),
                                end = Offset(width - paddingHorizontal, gridY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dashEffect,
                            )
                        }
                    }

                    val pointsBySeries = nonEmptySeries.map { buildPoints(it.entries) }

                    // 2. Render series back-to-front so index 0 (primary) draws on top
                    for (seriesIdx in nonEmptySeries.lastIndex downTo 0) {
                        if (!visibleMask.getOrElse(seriesIdx) { true }) continue
                        val s = nonEmptySeries[seriesIdx]
                        val pts = pointsBySeries[seriesIdx]
                        if (pts.isEmpty()) continue
                        val seriesColor = resolvedColors[seriesIdx]
                        val isPrimary = seriesIdx == 0
                        val curvePath = buildSmoothPath(pts)

                        // Area fill
                        if (s.showArea && pts.size >= 2 && availableWidth > 0f && style.areaTopAlpha > 0f) {
                            val areaStart = s.areaStartColor ?: seriesColor
                            val areaPath = Path().apply {
                                addPath(curvePath)
                                lineTo(pts.last().x, canvasHeight - paddingBottom)
                                lineTo(pts.first().x, canvasHeight - paddingBottom)
                                close()
                            }
                            drawPath(
                                path = areaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        areaStart.copy(alpha = style.areaTopAlpha * progress),
                                        secondaryAccent.copy(alpha = (style.areaTopAlpha * 0.32f) * progress),
                                        Color.Transparent,
                                    ),
                                    startY = paddingTop,
                                    endY = canvasHeight,
                                ),
                            )

                            if (isPrimary && style.showWaveOverlay) {
                                val waveSteps = 32
                                val wavePath = Path().apply {
                                    moveTo(pts.first().x, canvasHeight)
                                    for (i in 0..waveSteps) {
                                        val wx = pts.first().x + (i.toFloat() / waveSteps) * availableWidth
                                        val waveFactor = sin(wx / 48f + wavePhase) * 5.dp.toPx()
                                        val progressX = (wx - pts.first().x) / availableWidth
                                        val approxY = pts.first().y + (pts.last().y - pts.first().y) * progressX +
                                            16.dp.toPx() + waveFactor
                                        lineTo(wx, approxY)
                                    }
                                    lineTo(pts.last().x, canvasHeight)
                                    close()
                                }
                                drawPath(
                                    path = wavePath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            secondaryAccent.copy(alpha = 0.05f * progress),
                                            seriesColor.copy(alpha = 0.02f * progress),
                                            Color.Transparent,
                                        ),
                                        startY = paddingTop,
                                        endY = canvasHeight,
                                    ),
                                )
                            }
                        }

                        // Stroke
                        val strokePx = (s.strokeWidth ?: if (isPrimary) style.strokeWidth else style.strokeWidth * 0.78f).toPx()
                        val pathEffect = s.lineStyle.toMark().pathEffect(density)
                        if (isPrimary && pathEffect == null) {
                            drawPath(
                                path = curvePath,
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        seriesColor,
                                        seriesColor,
                                        lerp(seriesColor, secondaryAccent, 0.25f),
                                        seriesColor,
                                    ),
                                    startX = -200f + (width + 400f) * shimmerOffset,
                                    endX = (width + 400f) * shimmerOffset,
                                ),
                                style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round),
                            )
                        } else {
                            drawPath(
                                path = curvePath,
                                color = seriesColor.copy(alpha = if (isPrimary) 1f else 0.90f),
                                style = Stroke(
                                    width = strokePx,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                    pathEffect = pathEffect,
                                ),
                            )
                        }

                        // Minor points
                        if (s.showPoints && style.showDataDroplets && style.showAllDataPoints && pts.size <= 16) {
                            pts.forEachIndexed { ptIdx, point ->
                                if (ptIdx != effectiveSelectedIndex) {
                                    drawCircle(seriesColor, radius = 2.4.dp.toPx(), center = point)
                                }
                            }
                        }

                        // Selected droplet
                        if ((style.showDataDroplets || pts.size == 1) && effectiveSelectedIndex in pts.indices) {
                            val scale = if (isPrimary) 1f else 0.78f
                            drawRefractiveDroplet(
                                center = pts[effectiveSelectedIndex],
                                color = seriesColor,
                                baseRadius = style.dropletRadius.toPx() * scale,
                            )
                        }
                    }

                    // 3. Smooth scrubber hairline
                    if (style.showScrubber && primaryEntries.isNotEmpty()) {
                        val step = availableWidth / (primaryEntries.size - 1).coerceAtLeast(1)
                        val smoothX = if (primaryEntries.size == 1) width * 0.5f else {
                            paddingHorizontal + animatedSelectedIndex.coerceIn(0f, primaryEntries.lastIndex.toFloat()) * step
                        }
                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.70f),
                                    secondaryAccent.copy(alpha = 0.24f),
                                    Color.Transparent,
                                ),
                            ),
                            start = Offset(smoothX, 0f),
                            end = Offset(smoothX, canvasHeight),
                            strokeWidth = 1.4.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 2.6.dp.toPx(),
                            center = Offset(smoothX, 4.dp.toPx()),
                        )
                    }
                }
            }
        }

        if (style.showXAxisLabels) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = primaryEntries.first().label,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onSurfaceVariant,
                )
                if (primaryEntries.size > 1) {
                    Text(
                        text = primaryEntries.last().label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (showSelection) {
            AnimatedContent(
                targetState = effectiveSelectedIndex,
                transitionSpec = LiquidLineChartMotion.contentTransition(performance),
                label = "LineSelectionDetailTransition",
            ) { index ->
                val clamped = index.coerceIn(0, primaryEntries.lastIndex)
                val selectedPairs = nonEmptySeries.mapNotNull { s ->
                    s.entries.getOrNull(clamped)?.let { s to it }
                }

                if (selectionContent != null) {
                    selectionContent(clamped, selectedPairs)
                } else {
                    val primaryPoint = primaryEntries[clamped]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LiquidChartSelectionReadout(
                            label = primaryPoint.label,
                            value = valueFormatter(primaryPoint.value) + suffix,
                            color = primaryColor,
                        )
                        if (selectedPairs.size > 1) {
                            LiquidChartLegend {
                                selectedPairs.drop(1).forEachIndexed { offset, (s, e) ->
                                    val seriesIdx = offset + 1
                                    LiquidChartSeriesValue(
                                        label = s.label,
                                        value = valueFormatter(e.value) + suffix,
                                        color = resolvedColors.getOrElse(seriesIdx) { secondaryAccent },
                                        mark = s.lineStyle.toMark(),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
