package com.anto426.liquidmonet.components.charts.donutchart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.charts.donutchart.motion.LiquidDonutChartMotion
import com.anto426.liquidmonet.components.charts.internal.LiquidChartLegendItem
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt

/** A part-to-whole chart with a selectable ring and a numerical legend. */
@Composable
fun LiquidDonutChart(
    entries: List<LiquidPieEntry>,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 22.dp,
    centerLabel: String? = null,
    centerValue: String? = null,
    showLegend: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
    onEntrySelected: ((LiquidPieEntry?) -> Unit)? = null,
) {
    LiquidInputNormalization.positive(size, "LiquidDonutChart size")
    LiquidInputNormalization.positive(strokeWidth, "LiquidDonutChart strokeWidth")
    require(strokeWidth * 2 < size) {
        "LiquidDonutChart strokeWidth must be less than half of size."
    }
    require(entries.all { it.value.isFinite() && it.value >= 0f }) {
        "LiquidDonutChart entry values must be finite and non-negative."
    }
    if (entries.isEmpty()) return

    val scheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val performance = LocalLiquidGlassPerformance.current
    val defaultColors = listOf(
        scheme.primary, scheme.tertiary, scheme.secondary,
        glassColors.warning, glassColors.success, scheme.error,
    )
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

    var selectedIndex by remember(entries) { mutableStateOf<Int?>(null) }
    val selectedEntry = selectedIndex?.let(entries::get)
    val currentOnEntrySelected by rememberUpdatedState(onEntrySelected)
    val currentProgress by rememberUpdatedState(progressAnimation)
    fun toggleSelection(index: Int?) {
        val next = index.takeUnless { it == selectedIndex }
        selectedIndex = next
        currentOnEntrySelected?.invoke(next?.let(entries::get))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(size), contentAlignment = Alignment.Center) {
            Canvas(
                Modifier.matchParentSize().pointerInput(entries, strokeWidth) {
                    detectTapGestures { position ->
                        val diameter = minOf(this.size.width, this.size.height).toFloat()
                        val touch = position - Offset(this.size.width / 2f, this.size.height / 2f)
                        val stroke = minOf(strokeWidth.toPx(), diameter / 3.5f)
                        val radius = (diameter - stroke * 1.5f) / 2f
                        val halfTarget = maxOf(24.dp.toPx(), stroke * 0.65f).coerceAtMost(radius * 0.65f)
                        if (abs(touch.getDistance() - radius) > halfTarget) return@detectTapGestures

                        val degrees = (atan2(touch.y.toDouble(), touch.x.toDouble()) * 180.0 / PI).toFloat()
                        val angle = (degrees + 450f) % 360f
                        val progress = currentProgress.coerceIn(0f, 1f)
                        var start = 0f
                        for (index in fractions.indices) {
                            val sweep = fractions[index] * 360f
                            val gap = ringGap(sweep, singleSlice)
                            val first = start + gap / 2f
                            val last = start + sweep * progress - gap / 2f
                            if (sweep > gap && angle >= first && angle < last) {
                                toggleSelection(index)
                                return@detectTapGestures
                            }
                            start += sweep
                        }
                    }
                },
            ) {
                val diameter = this.size.minDimension
                if (diameter <= 0f) return@Canvas
                val stroke = minOf(strokeWidth.toPx(), diameter / 3.5f)
                val arcDiameter = diameter - stroke * 1.5f
                val arcSize = Size(arcDiameter, arcDiameter)
                val topLeft = center - Offset(arcDiameter / 2f, arcDiameter / 2f)
                drawArc(
                    color = glassColors.inactiveTrack,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke),
                )

                var start = -90f
                val progress = progressAnimation.coerceIn(0f, 1f)
                entries.forEachIndexed { index, entry ->
                    val sweep = fractions[index] * 360f
                    val gap = ringGap(sweep, singleSlice)
                    val animatedSweep = sweep * progress - gap
                    if (animatedSweep > 0f) {
                        val color = entry.color ?: defaultColors[index % defaultColors.size]
                        val active = selectedIndex == index
                        drawArc(
                            color = if (selectedIndex == null || active) color else color.copy(alpha = color.alpha * 0.58f),
                            startAngle = start + gap / 2f,
                            sweepAngle = animatedSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = if (active) stroke * 1.22f else stroke, cap = StrokeCap.Butt),
                        )
                    }
                    start += sweep
                }
            }

            LiquidBadge(
                shape = Capsule(),
                modifier = Modifier.size(size * 0.52f),
                contentPadding = PaddingValues(horizontal = 4.dp),
                containerColor = scheme.surface.copy(alpha = 0.26f),
                backdropState = effectiveBackdrop,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val amount = selectedEntry?.value?.let(::formatLiquidChartValue)
                        ?: centerValue ?: formatLiquidChartValue(total.toFloat())
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                        fontWeight = FontWeight.Bold,
                        color = scheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = selectedEntry?.label ?: centerLabel ?: "Totale",
                        modifier = Modifier.padding(horizontal = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if (showLegend) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entries.forEachIndexed { index, entry ->
                    key(entry, index) {
                        val percentage = fractions[index] * 100f
                        val percentageLabel = if (percentage > 0f && percentage < 0.5f) "<1%" else "${percentage.roundToInt()}%"
                        LiquidChartLegendItem(
                            label = entry.label,
                            color = entry.color ?: defaultColors[index % defaultColors.size],
                            selected = index == selectedIndex,
                            backdrop = effectiveBackdrop,
                            modifier = Modifier.fillMaxWidth(),
                            value = "${formatLiquidChartValue(entry.value)} · $percentageLabel",
                            onClick = { toggleSelection(index) },
                        )
                    }
                }
            }
        }
    }
}

private fun ringGap(sweep: Float, singleSlice: Boolean): Float =
    if (singleSlice) 0f else minOf(3f, sweep * 0.30f)
