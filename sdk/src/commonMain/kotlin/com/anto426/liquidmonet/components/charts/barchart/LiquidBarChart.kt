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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.charts.barchart.motion.LiquidBarChartMotion
import com.anto426.liquidmonet.components.charts.internal.LiquidChartAxis
import com.anto426.liquidmonet.components.charts.internal.LiquidChartSelection
import com.anto426.liquidmonet.components.charts.internal.liquidChartScrub
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

/** A zero-based category comparison with a scrubber and a selected value disclosure. */
@Composable
fun LiquidBarChart(
    entries: List<LiquidChartEntry>,
    modifier: Modifier = Modifier,
    height: Dp = 190.dp,
    barWidth: Dp = 20.dp,
    barColor: Color? = null,
    maxValue: Float? = null,
    valueSuffix: String = "",
    valueFormatter: (Float) -> String = ::formatLiquidChartValue,
    detailDescription: String = "",
    showDetails: Boolean = false,
    showXAxisLabels: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
    onEntrySelected: ((LiquidChartEntry?) -> Unit)? = null,
) {
    LiquidInputNormalization.positive(height, "LiquidBarChart height")
    LiquidInputNormalization.positive(barWidth, "LiquidBarChart barWidth")
    require(entries.all { it.value.isFinite() && it.value >= 0f }) {
        "LiquidBarChart entry values must be finite and non-negative."
    }
    maxValue?.let { LiquidInputNormalization.positive(it, "LiquidBarChart maxValue") }
    if (entries.isEmpty()) return

    val scheme = MaterialTheme.colorScheme
    val primaryColor = barColor ?: scheme.primary
    val gridColor = scheme.outline.copy(alpha = 0.30f)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val performance = LocalLiquidGlassPerformance.current
    val dataMax = maxValue ?: (entries.maxOf { it.value } * 1.15f).coerceAtLeast(1f)
    val verticalInset = minOf(16.dp, height / 4)
    val suffix = if (valueSuffix.isBlank()) "" else " $valueSuffix"

    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by LiquidBarChartMotion.animateFloat(
        targetValue = if (animationPlayed) 1f else 0f,
        performance = performance,
        label = "BarChartProgress",
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    var selectedIndex by remember(entries) { mutableIntStateOf(entries.lastIndex) }
    val selectedEntry = entries[selectedIndex]
    val selectedValue = valueFormatter(selectedEntry.value) + suffix

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LiquidChartAxis(
                ticks = listOf(dataMax, dataMax * 0.5f, 0f).map(::formatLiquidChartValue),
                height = height,
                verticalInset = verticalInset,
            )
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    Modifier.fillMaxWidth().height(height).liquidChartScrub(
                        entryCount = entries.size,
                        selectedIndex = selectedIndex,
                        selectedDescription = "${selectedEntry.label}, $selectedValue",
                        indexAt = { x, width ->
                            (x / width.coerceAtLeast(1f) * entries.size).toInt().coerceIn(0, entries.lastIndex)
                        },
                        onSelect = { index ->
                            selectedIndex = index
                            onEntrySelected?.invoke(entries[index])
                        },
                    ),
                ) {
                    Canvas(Modifier.matchParentSize()) {
                        val inset = verticalInset.toPx()
                        val baseline = size.height - inset
                        val plotHeight = (baseline - inset).coerceAtLeast(0f)
                        val step = size.width / entries.size
                        val columnWidth = barWidth.toPx().coerceAtMost(step * 0.68f)
                        if (columnWidth <= 0f || plotHeight <= 0f) return@Canvas

                        for (tick in 0..2) {
                            val y = inset + plotHeight * tick / 2f
                            drawLine(gridColor, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
                        }

                        entries.forEachIndexed { index, entry ->
                            val center = step * (index + 0.5f)
                            val left = center - columnWidth / 2f
                            val color = entry.color ?: primaryColor
                            val selected = index == selectedIndex
                            if (selected) {
                                drawRoundRect(
                                    color = color.copy(alpha = 0.11f),
                                    topLeft = Offset(left, inset),
                                    size = Size(columnWidth, plotHeight),
                                    cornerRadius = CornerRadius(columnWidth / 2f),
                                )
                            }

                            val fraction = ((entry.value / dataMax) * progressAnimation).coerceIn(0f, 1f)
                            val columnHeight = fraction * plotHeight
                            if (columnHeight > 0f) {
                                val top = baseline - columnHeight
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            color.copy(alpha = if (selected) 1f else 0.82f),
                                            color.copy(alpha = if (selected) 0.72f else 0.42f),
                                        ),
                                        startY = top,
                                        endY = baseline,
                                    ),
                                    topLeft = Offset(left, top),
                                    size = Size(columnWidth, columnHeight),
                                    cornerRadius = CornerRadius(minOf(columnWidth, columnHeight) / 2f),
                                )
                            }
                            if (selected) {
                                drawCircle(color, radius = 2.5.dp.toPx(), center = Offset(center, baseline + inset / 2f))
                            }
                        }
                    }
                }

                if (showXAxisLabels || showDetails) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        entries.forEachIndexed { index, entry ->
                            val selected = index == selectedIndex
                            val showLabel = when {
                                showXAxisLabels && entries.size <= 8 -> true
                                showXAxisLabels -> selected || index == 0 || index == entries.lastIndex
                                else -> index == 0 || index == entries.lastIndex
                            }
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

        AnimatedContent(
            targetState = selectedEntry,
            transitionSpec = LiquidBarChartMotion.contentTransition(performance),
            label = "BarSelectionTransition",
        ) { currentSelection ->
            LiquidChartSelection(
                label = currentSelection.label,
                value = valueFormatter(currentSelection.value) + suffix,
                color = currentSelection.color ?: primaryColor,
                backdrop = effectiveBackdrop,
                description = detailDescription,
            )
        }
    }
}
