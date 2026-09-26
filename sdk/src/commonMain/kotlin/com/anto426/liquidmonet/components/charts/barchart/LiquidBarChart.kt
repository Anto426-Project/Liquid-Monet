package com.anto426.liquidmonet.components.charts.barchart

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.charts.barchart.motion.LiquidBarChartMotion
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateBackground
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

/**
 * LiquidBarChart - Optical Liquid Glass Capsule Bar Chart.
 *
 * Features rounded refractive glass pill bars, Monet vertical gradients, dynamic liquid surface
 * tension meniscus, spring bounce entrance, and an interactive dynamic disclosure pod beneath the
 * chart.
 */
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

    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val primaryColor = barColor ?: colorScheme.primary
    val secondaryColor = colorScheme.tertiary
    val trackColor = LiquidGlassTheme.colors.inactiveTrack
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val performance = LocalLiquidGlassPerformance.current
    val animateDecorations = performance.animateBackground

    val dataMax = maxValue ?: ((entries.maxOfOrNull { it.value } ?: 100f) * 1.15f).coerceAtLeast(1f)
    val actualMin = entries.minOfOrNull { it.value } ?: 0f
    val actualMax = entries.maxOfOrNull { it.value } ?: 0f

    // Fluid spring entrance animation
    var animationPlayed by remember { mutableStateOf(false) }
    val progressAnimation by
        LiquidBarChartMotion.animateFloat(
            targetValue = if (animationPlayed) 1f else 0f,
            performance = performance,
            label = "BarChartProgress",
        )
    androidx.compose.runtime.LaunchedEffect(entries) {
        animationPlayed = true
    }

    // Shimmer sweep animation
    val infiniteTransition =
        if (animateDecorations) {
            rememberLiquidInfiniteTransition(label = "BarShimmerTransition")
        } else {
            null
        }
    val shimmerPhase by
        if (infiniteTransition != null) {
            LiquidBarChartMotion.rememberBarShimmerPhase(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "BarShimmerPhase",
            )
        } else {
            remember { mutableFloatStateOf(0f) }
        }

    // Selection state
    var selectedIndex by remember(entries) { mutableIntStateOf(entries.lastIndex) }
    val selectedEntry = entries[selectedIndex.coerceIn(0, entries.lastIndex)]

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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

        // Canvas Viewport
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .height(height)
                    .pointerInput(entries) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val totalBars = entries.size
                            val step = width / totalBars
                            val index = (offset.x / step).toInt().coerceIn(0, entries.lastIndex)
                            selectedIndex = index
                            onEntrySelected?.invoke(entries[index])
                        }
                    }
                    .pointerInput(entries) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val width = size.width
                                val totalBars = entries.size
                                val step = width / totalBars
                                val index = (offset.x / step).toInt().coerceIn(0, entries.lastIndex)
                                selectedIndex = index
                                onEntrySelected?.invoke(entries[index])
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val width = size.width
                                val totalBars = entries.size
                                val step = width / totalBars
                                val index =
                                    (change.position.x / step)
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
                val paddingBottom = 8.dp.toPx()
                val availableHeight = canvasHeight - paddingBottom

                val totalBars = entries.size
                val step = width / totalBars
                val actualBarWidth = (barWidth.toPx()).coerceAtMost(step * 0.65f)
                val cornerRadius = CornerRadius(actualBarWidth / 2f, actualBarWidth / 2f)

                entries.forEachIndexed { index, entry ->
                    val isSelected = index == selectedIndex
                    val centerX = step * index + step / 2f
                    val left = centerX - actualBarWidth / 2f
                    val right = centerX + actualBarWidth / 2f

                    // 1. Background Inactive Glass Track
                    val trackRect =
                        RoundRect(
                            left = left,
                            top = 0f,
                            right = right,
                            bottom = availableHeight,
                            cornerRadius = cornerRadius,
                        )
                    val trackPath = Path().apply { addRoundRect(trackRect) }
                    drawPath(path = trackPath, color = trackColor)

                    // 2. Active Liquid Bar Capsule
                    val normalized = (entry.value / dataMax).coerceIn(0f, 1f)
                    val barHeight =
                        (normalized * availableHeight * progressAnimation).coerceAtLeast(
                            if (entry.value > 0) actualBarWidth else 0f
                        )
                    val barTop = availableHeight - barHeight

                    if (barHeight > 0) {
                        val barRect =
                            RoundRect(
                                left = left,
                                top = barTop,
                                right = right,
                                bottom = availableHeight,
                                cornerRadius = cornerRadius,
                            )
                        val barPath = Path().apply { addRoundRect(barRect) }

                        val activeBarColor =
                            entry.color ?: if (isSelected) secondaryColor else primaryColor

                        // Liquid gradient column
                        drawPath(
                            path = barPath,
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            activeBarColor,
                                            activeBarColor.copy(alpha = 0.75f),
                                            secondaryColor.copy(alpha = 0.45f),
                                        ),
                                    startY = barTop,
                                    endY = availableHeight,
                                ),
                        )

                        // 3. Glowing Meniscus Droplet Lens on the top cap
                        drawCircle(
                            brush =
                                Brush.radialGradient(
                                    colors =
                                        listOf(
                                            Color.White.copy(
                                                alpha = if (isSelected) 0.90f else 0.50f
                                            ),
                                            activeBarColor.copy(alpha = 0.20f),
                                            Color.Transparent,
                                        ),
                                    center = Offset(centerX, barTop + actualBarWidth / 2f),
                                    radius = actualBarWidth * 0.90f,
                                ),
                            radius = actualBarWidth * 0.90f,
                            center = Offset(centerX, barTop + actualBarWidth / 2f),
                        )

                        // 4. Selection Highlight Halo
                        if (isSelected) {
                            drawCircle(
                                brush =
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                secondaryColor.copy(alpha = 0.50f),
                                                Color.Transparent,
                                            ),
                                        center = Offset(centerX, barTop),
                                        radius = actualBarWidth * 2.2f,
                                    ),
                                radius = actualBarWidth * 2.2f,
                                center = Offset(centerX, barTop),
                            )
                        }
                    }
                }
            }
        }

        // X-Axis Labels directly under each bar
        if (showXAxisLabels && entries.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                entries.forEachIndexed { index, entry ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = entry.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) primaryColor else colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
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

        // Selection Detail Display (Clean, flat, no double box)
        AnimatedContent(
            targetState = selectedEntry,
            transitionSpec = LiquidBarChartMotion.contentTransition(performance),
            label = "BarSelectionTransition",
        ) { currentSelection ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = currentSelection.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (detailDescription.isNotBlank()) {
                        Text(
                            text = detailDescription,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Text(
                    text =
                        "${valueFormatter(currentSelection.value)}${valueSuffix.withLeadingSpace()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryColor,
                    softWrap = false,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun String.withLeadingSpace(): String = if (isBlank()) "" else " $this"
