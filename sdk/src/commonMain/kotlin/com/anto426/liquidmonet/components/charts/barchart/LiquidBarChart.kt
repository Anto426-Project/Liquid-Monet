package com.anto426.liquidmonet.components.charts.barchart

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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.charts.barchart.motion.LiquidBarChartMotion
import com.anto426.liquidmonet.components.charts.internal.LiquidChartAxis
import com.anto426.liquidmonet.components.charts.internal.LiquidChartSelectionReadout
import com.anto426.liquidmonet.components.charts.internal.liquidChartScrub
import com.anto426.liquidmonet.components.charts.model.LiquidBarChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance

/**
 * Containerless zero-based category comparison bar chart with continuous scrubbing and
 * customizable style & selection disclosure slot.
 */
@Composable
fun LiquidBarChart(
    entries: List<LiquidChartEntry>,
    modifier: Modifier = Modifier,
    height: Dp = 190.dp,
    barColor: Color? = null,
    style: LiquidBarChartStyle = LiquidBarChartStyle(),
    maxValue: Float? = null,
    valueSuffix: String = "",
    valueFormatter: (Float) -> String = ::formatLiquidChartValue,
    axisValueFormatter: (Float) -> String = valueFormatter,
    detailDescription: String? = null,
    showSelection: Boolean = false,
    interactive: Boolean = true,
    selectedIndex: Int? = null,
    contentSpacing: Dp = 10.dp,
    selectionContent: (@Composable (index: Int, entry: LiquidChartEntry) -> Unit)? = null,
    onEntrySelected: ((index: Int, entry: LiquidChartEntry) -> Unit)? = null,
) {
    LiquidInputNormalization.positive(height, "LiquidBarChart height")
    require(contentSpacing >= 0.dp) { "LiquidBarChart contentSpacing must be non-negative." }
    require(entries.all { it.value.isFinite() && it.value >= 0f }) {
        "LiquidBarChart entry values must be finite and non-negative."
    }
    maxValue?.let { LiquidInputNormalization.positive(it, "LiquidBarChart maxValue") }
    if (entries.isEmpty()) return

    val scheme = MaterialTheme.colorScheme
    val primaryColor = barColor ?: scheme.primary
    val resolvedGridColor = style.gridColor ?: scheme.onSurface.copy(alpha = 0.10f)
    val performance = LocalLiquidGlassPerformance.current
    val dataMax = maxValue ?: (entries.maxOf { it.value } * 1.15f).coerceAtLeast(1f)
    val verticalInset = style.plotVerticalInset ?: minOf(16.dp, height / 4)
    val suffix = if (valueSuffix.isBlank()) "" else " $valueSuffix"
    val gridSteps = (style.gridLineCount - 1).coerceAtLeast(1)

    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by LiquidBarChartMotion.animateFloat(
        targetValue = if (animationPlayed) 1f else 0f,
        performance = performance,
        label = "BarChartProgress",
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    var internalSelectedIndex by remember(entries) { mutableIntStateOf(entries.lastIndex) }
    val effectiveSelectedIndex = (selectedIndex ?: internalSelectedIndex).coerceIn(0, entries.lastIndex)
    val selectedEntry = entries[effectiveSelectedIndex]
    val selectedValue = valueFormatter(selectedEntry.value) + suffix

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(if (style.showYAxis) 6.dp else 0.dp),
        ) {
            if (style.showYAxis) {
                val ticks = List(style.gridLineCount) { index ->
                    val fraction = 1f - index.toFloat() / gridSteps.toFloat()
                    axisValueFormatter(dataMax * fraction)
                }
                LiquidChartAxis(
                    ticks = ticks,
                    height = height,
                    verticalInset = verticalInset,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                val plotModifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .let { base ->
                        if (interactive) {
                            base.liquidChartScrub(
                                entryCount = entries.size,
                                selectedIndex = effectiveSelectedIndex,
                                selectedDescription = "${selectedEntry.label}, $selectedValue",
                                indexAt = { x, width ->
                                    (x / width.coerceAtLeast(1f) * entries.size).toInt().coerceIn(0, entries.lastIndex)
                                },
                                onSelect = { index ->
                                    internalSelectedIndex = index
                                    onEntrySelected?.invoke(index, entries[index])
                                },
                            )
                        } else {
                            base
                        }
                    }

                Box(modifier = plotModifier) {
                    Canvas(Modifier.matchParentSize()) {
                        val inset = verticalInset.toPx()
                        val baseline = size.height - inset
                        val plotHeight = (baseline - inset).coerceAtLeast(0f)
                        val step = size.width / entries.size
                        val columnWidth = style.barWidth.toPx().coerceAtMost(step * 0.68f)
                        if (columnWidth <= 0f || plotHeight <= 0f) return@Canvas

                        if (style.showGridLines) {
                            val dashEffect = if (style.dashedGridLines) {
                                PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()))
                            } else {
                                null
                            }
                            for (tick in 0..gridSteps) {
                                val y = inset + plotHeight * tick / gridSteps.toFloat()
                                drawLine(
                                    color = resolvedGridColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = dashEffect,
                                )
                            }
                        }

                        entries.forEachIndexed { index, entry ->
                            val center = step * (index + 0.5f)
                            val left = center - columnWidth / 2f
                            val color = entry.color ?: primaryColor
                            val selected = style.showSelectionHighlight && index == effectiveSelectedIndex
                            val requestedRadius = style.barCornerRadius?.toPx()
                            val trackRadius = requestedRadius?.coerceAtMost(columnWidth / 2f) ?: (columnWidth / 2f)

                            if (selected) {
                                drawRoundRect(
                                    color = color.copy(alpha = 0.10f),
                                    topLeft = Offset(left, inset),
                                    size = Size(columnWidth, plotHeight),
                                    cornerRadius = CornerRadius(trackRadius),
                                )
                            }

                            val fraction = ((entry.value / dataMax) * progressAnimation).coerceIn(0f, 1f)
                            val columnHeight = fraction * plotHeight
                            if (columnHeight > 0f) {
                                val top = baseline - columnHeight
                                val barRadius = requestedRadius?.coerceAtMost(minOf(columnWidth, columnHeight) / 2f)
                                    ?: (minOf(columnWidth, columnHeight) / 2f)
                                val emphasize = !style.showSelectionHighlight || selected
                                if (style.useGradientFill) {
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(
                                                color.copy(alpha = if (emphasize) 1f else 0.80f),
                                                color.copy(alpha = if (emphasize) 0.70f else 0.40f),
                                            ),
                                            startY = top,
                                            endY = baseline,
                                        ),
                                        topLeft = Offset(left, top),
                                        size = Size(columnWidth, columnHeight),
                                        cornerRadius = CornerRadius(barRadius),
                                    )
                                } else {
                                    drawRoundRect(
                                        color = color.copy(alpha = if (emphasize) 1f else 0.62f),
                                        topLeft = Offset(left, top),
                                        size = Size(columnWidth, columnHeight),
                                        cornerRadius = CornerRadius(barRadius),
                                    )
                                }
                            }
                            if (selected) {
                                drawCircle(color, radius = 2.4.dp.toPx(), center = Offset(center, baseline + inset / 2f))
                            }
                        }
                    }
                }

                if (style.showXAxisLabels) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        entries.forEachIndexed { index, entry ->
                            val selected = style.showSelectionHighlight && index == effectiveSelectedIndex
                            val showLabel = entries.size <= 8 || selected || index == 0 || index == entries.lastIndex
                            Text(
                                text = if (showLabel) entry.label else "",
                                modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }

        if (showSelection) {
            AnimatedContent(
                targetState = effectiveSelectedIndex,
                transitionSpec = LiquidBarChartMotion.contentTransition(performance),
                label = "BarSelectionTransition",
            ) { index ->
                val clamped = index.coerceIn(0, entries.lastIndex)
                val currentSelection = entries[clamped]
                if (selectionContent != null) {
                    selectionContent(clamped, currentSelection)
                } else {
                    LiquidChartSelectionReadout(
                        label = currentSelection.label,
                        value = valueFormatter(currentSelection.value) + suffix,
                        color = currentSelection.color ?: primaryColor,
                        description = detailDescription,
                    )
                }
            }
        }
    }
}
