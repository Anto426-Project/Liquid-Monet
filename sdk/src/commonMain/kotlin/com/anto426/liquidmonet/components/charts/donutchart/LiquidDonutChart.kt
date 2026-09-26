package com.anto426.liquidmonet.components.charts.donutchart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.charts.donutchart.motion.LiquidDonutChartMotion
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import kotlin.math.PI
import kotlin.math.atan2

/**
 * LiquidDonutChart - Optical Liquid Glass Radial Donut Chart.
 *
 * Features segmented optical Monet arcs, center refractive glass hub, smooth spring entrance, slice
 * selection, and legend pills.
 */
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

    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val trackColor = LiquidGlassTheme.colors.inactiveTrack
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val performance = LocalLiquidGlassPerformance.current

    // Default Monet palette if entry color is null
    val defaultColors =
        listOf(
            colorScheme.primary,
            colorScheme.tertiary,
            colorScheme.secondary,
            glassColors.warning,
            glassColors.success,
            colorScheme.error,
        )

    val totalValue = entries.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.001f)

    // Fluid spring entrance animation
    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by
        LiquidDonutChartMotion.animateFloat(
            targetValue = if (animationPlayed) 1f else 0f,
            performance = performance,
            label = "DonutChartProgress",
        )
    androidx.compose.runtime.LaunchedEffect(entries) {
        animationPlayed = true
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val selectedEntry = selectedIndex?.let { entries.getOrNull(it) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Donut Ring + Center Hub
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(
                modifier =
                    Modifier.matchParentSize().pointerInput(entries) {
                        detectTapGestures { offset ->
                            val center =
                                Offset(
                                    this@pointerInput.size.width / 2f,
                                    this@pointerInput.size.height / 2f,
                                )
                            val touchOffset = offset - center
                            var angle =
                                (atan2(touchOffset.y.toDouble(), touchOffset.x.toDouble()) * 180.0 /
                                        PI)
                                    .toFloat()
                            if (angle < 0) angle += 360f

                            // Angle 0 is at 3 o'clock; our chart starts at -90 (12 o'clock)
                            var adjustedAngle = (angle + 90f) % 360f
                            var accumulated = 0f
                            var clickedIndex: Int? = null

                            for (i in entries.indices) {
                                val sliceAngle = (entries[i].value / totalValue) * 360f
                                if (adjustedAngle in accumulated..(accumulated + sliceAngle)) {
                                    clickedIndex = i
                                    break
                                }
                                accumulated += sliceAngle
                            }

                            if (selectedIndex == clickedIndex) {
                                selectedIndex = null
                                onEntrySelected?.invoke(null)
                            } else {
                                selectedIndex = clickedIndex
                                clickedIndex?.let { onEntrySelected?.invoke(entries[it]) }
                            }
                        }
                    }
            ) {
                val strokePx = strokeWidth.toPx()
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height
                val arcSize = Size(canvasWidth - strokePx * 1.5f, canvasHeight - strokePx * 1.5f)
                val topLeft = Offset(strokePx * 0.75f, strokePx * 0.75f)

                // 1. Background Track Ring
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )

                // 2. Slices with Spring Progress
                var startAngle = -90f
                entries.forEachIndexed { index, entry ->
                    val sliceColor = entry.color ?: defaultColors[index % defaultColors.size]
                    val isSelected = index == selectedIndex
                    val sliceSweep = ((entry.value / totalValue) * 360f) * progressAnimation
                    val actualStroke = if (isSelected) strokePx * 1.25f else strokePx

                    if (sliceSweep > 0.5f) {
                        drawArc(
                            color = sliceColor,
                            startAngle = startAngle,
                            sweepAngle = (sliceSweep - 2f).coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = actualStroke, cap = StrokeCap.Round),
                        )
                    }

                    startAngle += (entry.value / totalValue) * 360f
                }
            }

            // Center Refractive Glass Hub
            Box(
                modifier =
                    Modifier.size(size * 0.52f)
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = Capsule(),
                            role = LiquidGlassRole.Control,
                            containerColor = colorScheme.surface.copy(alpha = 0.20f),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                ) {
                    val displayValue =
                        if (selectedEntry != null) {
                            "${selectedEntry.value}"
                        } else {
                            centerValue ?: "${totalValue.toInt()}"
                        }

                    val displayLabel =
                        if (selectedEntry != null) {
                            selectedEntry.label
                        } else {
                            centerLabel ?: "Totale"
                        }

                    Text(
                        text = displayValue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colorScheme.onSurface,
                    )
                    Text(
                        text = displayLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Legend Pills Row
        if (showLegend) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                entries.forEachIndexed { index, entry ->
                    val sliceColor = entry.color ?: defaultColors[index % defaultColors.size]
                    val isSelected = index == selectedIndex

                    Box(
                        modifier =
                            Modifier.padding(horizontal = 4.dp)
                                .liquidGlass(
                                    backdrop = effectiveBackdrop,
                                    shape = Capsule(),
                                    role = LiquidGlassRole.Control,
                                    containerColor =
                                        if (isSelected) sliceColor.copy(alpha = 0.22f)
                                        else Color.Transparent,
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier.size(8.dp).clip(Capsule()).background(sliceColor)
                            )
                            Text(
                                text = entry.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color =
                                    if (isSelected) sliceColor else colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
