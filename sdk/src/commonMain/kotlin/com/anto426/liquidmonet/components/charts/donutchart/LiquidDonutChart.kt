package com.anto426.liquidmonet.components.charts.donutchart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.charts.donutchart.motion.LiquidDonutChartMotion
import com.anto426.liquidmonet.components.charts.internal.LiquidChartLegendRow
import com.anto426.liquidmonet.components.charts.model.LiquidDonutChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

/**
 * Containerless part-to-whole donut chart with selectable ring, transparent center summary, and
 * optional frameless legend or custom slots.
 */
@Composable
fun LiquidDonutChart(
    entries: List<LiquidPieEntry>,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    style: LiquidDonutChartStyle = LiquidDonutChartStyle(),
    sliceColors: List<Color>? = null,
    centerLabel: String? = null,
    centerValue: String? = null,
    showLegend: Boolean = false,
    interactive: Boolean = true,
    selectedIndex: Int? = null,
    valueFormatter: (Float) -> String = ::formatLiquidChartValue,
    contentSpacing: Dp = 14.dp,
    centerContent: (@Composable (selectedEntry: LiquidPieEntry?, total: Float) -> Unit)? = null,
    legendContent: (@Composable (
        entries: List<LiquidPieEntry>,
        selectedIndex: Int?,
        onSelect: (Int) -> Unit,
    ) -> Unit)? = null,
    onEntrySelected: ((LiquidPieEntry?) -> Unit)? = null,
) {
    LiquidInputNormalization.positive(size, "LiquidDonutChart size")
    require(style.strokeWidth * 2 < size) {
        "LiquidDonutChart strokeWidth must be less than half of size."
    }
    require(contentSpacing >= 0.dp) { "LiquidDonutChart contentSpacing must be non-negative." }
    require(entries.all { it.value.isFinite() && it.value >= 0f }) {
        "LiquidDonutChart entry values must be finite and non-negative."
    }
    if (entries.isEmpty()) return

    val scheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val performance = LocalLiquidGlassPerformance.current
    val resolvedPalette = sliceColors?.takeIf { it.isNotEmpty() } ?: listOf(
        scheme.primary, scheme.tertiary, scheme.secondary,
        glassColors.warning, glassColors.success, scheme.error,
    )
    val resolvedTrackColor = style.trackColor ?: glassColors.inactiveTrack
    val total = entries.sumOf { it.value.toDouble() }
    val fractions = remember(entries, total) {
        entries.map { entry -> if (total == 0.0) 0f else (entry.value / total).toFloat() }
    }
    val singleSlice = fractions.count { it > 0f } == 1

    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by LiquidDonutChartMotion.animateFloat(
        targetValue = if (animationPlayed) 1f else 0f,
        performance = performance,
        label = "DonutChartProgress",
    )
    LaunchedEffect(Unit) { animationPlayed = true }

    var internalSelectedIndex by remember(entries) { mutableStateOf<Int?>(null) }
    val effectiveSelectedIndex = (selectedIndex ?: internalSelectedIndex)?.takeIf { it in entries.indices }
    val selectedEntry = effectiveSelectedIndex?.let(entries::get)
    val currentOnEntrySelected by rememberUpdatedState(onEntrySelected)
    val currentProgress by rememberUpdatedState(progressAnimation)
    val currentSelectedIndex by rememberUpdatedState(effectiveSelectedIndex)
    fun toggleSelection(index: Int?) {
        val next = index.takeUnless { it == currentSelectedIndex }
        internalSelectedIndex = next
        currentOnEntrySelected?.invoke(next?.let(entries::get))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(contentSpacing),
    ) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            val canvasModifier = Modifier.matchParentSize().let { base ->
                if (interactive) {
                    base.pointerInput(entries, style) {
                        detectTapGestures { position ->
                            val diameter = minOf(this.size.width, this.size.height).toFloat()
                            val touch = position - Offset(this.size.width / 2f, this.size.height / 2f)
                            val stroke = minOf(style.strokeWidth.toPx(), diameter / 3.5f)
                            val radius = (diameter - stroke * 1.5f) / 2f
                            val halfTarget = maxOf(24.dp.toPx(), stroke * 0.65f).coerceAtMost(radius * 0.65f)
                            if (abs(touch.getDistance() - radius) > halfTarget) return@detectTapGestures

                            val degrees = (atan2(touch.y.toDouble(), touch.x.toDouble()) * 180.0 / PI).toFloat()
                            val normalizedOffset = ((degrees - style.startAngle) % 360f + 360f) % 360f
                            val progress = currentProgress.coerceIn(0f, 1f)
                            var start = 0f
                            for (index in fractions.indices) {
                                val sweep = fractions[index] * 360f
                                val gap = resolveRingGap(sweep, singleSlice, style.sliceGapDegrees)
                                val first = start + gap / 2f
                                val last = start + sweep * progress - gap / 2f
                                if (sweep > gap && normalizedOffset >= first && normalizedOffset < last) {
                                    toggleSelection(index)
                                    return@detectTapGestures
                                }
                                start += sweep
                            }
                        }
                    }
                } else {
                    base
                }
            }

            Canvas(canvasModifier) {
                val diameter = this.size.minDimension
                if (diameter <= 0f) return@Canvas
                val stroke = minOf(style.strokeWidth.toPx(), diameter / 3.5f)
                val arcDiameter = diameter - stroke * 1.5f
                val arcSize = Size(arcDiameter, arcDiameter)
                val topLeft = center - Offset(arcDiameter / 2f, arcDiameter / 2f)
                if (resolvedTrackColor.alpha > 0f) {
                    drawArc(
                        color = resolvedTrackColor,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(stroke),
                    )
                }

                var start = style.startAngle
                val progress = progressAnimation.coerceIn(0f, 1f)
                entries.forEachIndexed { index, entry ->
                    val sweep = fractions[index] * 360f
                    val gap = resolveRingGap(sweep, singleSlice, style.sliceGapDegrees)
                    val animatedSweep = sweep * progress - gap
                    if (animatedSweep > 0f) {
                        val color = entry.color ?: resolvedPalette[index % resolvedPalette.size]
                        val active = effectiveSelectedIndex == index
                        drawArc(
                            color = if (effectiveSelectedIndex == null || active) color else color.copy(alpha = color.alpha * style.inactiveAlpha),
                            startAngle = start + gap / 2f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = if (active) stroke * style.activeStrokeScale else stroke,
                                cap = style.strokeCap,
                            ),
                        )
                    }
                    start += sweep
                }
            }

            if (style.showCenterContent) {
                Box(
                    modifier = Modifier.size(size - style.strokeWidth * 2.4f).padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (centerContent != null) {
                        centerContent(selectedEntry, total.toFloat())
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            val amount = selectedEntry?.value?.let(valueFormatter)
                                ?: centerValue ?: valueFormatter(total.toFloat())
                            Text(
                                text = amount,
                                style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold,
                                color = scheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val labelText = selectedEntry?.label ?: centerLabel
                            if (!labelText.isNullOrBlank()) {
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = scheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showLegend) {
            if (legendContent != null) {
                legendContent(entries, effectiveSelectedIndex) { index -> toggleSelection(index) }
            } else {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    entries.forEachIndexed { index, entry ->
                        key(entry, index) {
                            val percentage = fractions[index] * 100f
                            val percentageLabel = if (percentage > 0f && percentage < 0.5f) "<1%" else "${percentage.roundToInt()}%"
                            LiquidChartLegendRow(
                                label = entry.label,
                                value = "${valueFormatter(entry.value)} · $percentageLabel",
                                color = entry.color ?: resolvedPalette[index % resolvedPalette.size],
                                selected = index == effectiveSelectedIndex,
                                onClick = { toggleSelection(index) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun resolveRingGap(sweep: Float, singleSlice: Boolean, gapDegrees: Float): Float =
    if (singleSlice) 0f else minOf(gapDegrees, sweep * 0.30f)
