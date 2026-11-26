package com.anto426.liquidmonet.components.charts.internal

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.charts.model.LiquidLineStyle
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

internal enum class LiquidChartMark {
    Dot, Solid, Dashed, Dotted;

    fun pathEffect(pixelsPerDp: Float): PathEffect? = when (this) {
        Dashed -> PathEffect.dashPathEffect(floatArrayOf(7f * pixelsPerDp, 5f * pixelsPerDp))
        Dotted -> PathEffect.dashPathEffect(floatArrayOf(pixelsPerDp, 4f * pixelsPerDp))
        Dot, Solid -> null
    }
}

internal fun LiquidLineStyle.toMark(): LiquidChartMark = when (this) {
    LiquidLineStyle.Solid -> LiquidChartMark.Solid
    LiquidLineStyle.Dashed -> LiquidChartMark.Dashed
    LiquidLineStyle.Dotted -> LiquidChartMark.Dotted
}

@Composable
internal fun LiquidChartSwatch(color: Color, mark: LiquidChartMark) {
    Canvas(Modifier.size(16.dp, 12.dp)) {
        if (mark == LiquidChartMark.Dot) {
            drawCircle(color = color, radius = 4.dp.toPx())
        } else {
            drawLine(
                color = color,
                start = Offset(1.dp.toPx(), center.y),
                end = Offset(size.width - 1.dp.toPx(), center.y),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = mark.pathEffect(density),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun LiquidChartLegend(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
    }
}

/** Frameless interactive legend toggle chip (no glass container). */
@Composable
internal fun LiquidChartLegendChip(
    label: String,
    color: Color,
    selected: Boolean,
    mark: LiquidChartMark = LiquidChartMark.Dot,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = remember { Capsule() }
    val fill = if (selected) color.copy(alpha = 0.14f) else scheme.onSurface.copy(alpha = 0.05f)
    val borderColor = if (selected) color.copy(alpha = 0.36f) else Color.Transparent

    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .clip(shape)
            .background(fill)
            .border(1.dp, borderColor, shape)
            .semantics { this.selected = selected }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Checkbox,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiquidChartSwatch(if (selected) color else color.copy(alpha = 0.40f), mark)
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) scheme.onSurface else scheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Frameless legend row for part-to-whole charts (no glass container). */
@Composable
internal fun LiquidChartLegendRow(
    label: String,
    value: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = remember { RoundedRectangle(12.dp) }
    val background = if (selected) color.copy(alpha = 0.12f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 36.dp)
            .clip(shape)
            .background(background)
            .semantics { this.selected = selected }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiquidChartSwatch(color, LiquidChartMark.Dot)
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = scheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) color else scheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1,
        )
    }
}

@Composable
internal fun LiquidChartSeriesValue(label: String, value: String, color: Color, mark: LiquidChartMark) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        LiquidChartSwatch(color, mark)
        Text(
            text = if (label.isBlank()) value else "$label  $value",
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Frameless selection readout (zero glass badge container). */
@Composable
internal fun LiquidChartSelectionReadout(
    label: String,
    value: String,
    color: Color,
    description: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = scheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
            )
        }
        if (!description.isNullOrBlank()) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun LiquidChartAxis(ticks: List<String>, height: Dp, verticalInset: Dp) {
    Layout(
        modifier = Modifier.widthIn(max = 64.dp).height(height),
        content = {
            ticks.forEach { tick ->
                Text(
                    text = tick,
                    style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        },
    ) { measurables, constraints ->
        val labels = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
        val width = constraints.constrainWidth(labels.maxOf { it.width })
        val plotHeight = constraints.maxHeight
        val inset = verticalInset.roundToPx()
        val gap = (plotHeight - 2 * inset).coerceAtLeast(0) / (labels.size - 1).coerceAtLeast(1)
        layout(width, plotHeight) {
            labels.forEachIndexed { index, label ->
                val y = (inset + index * gap - label.height / 2).coerceIn(0, plotHeight - label.height)
                label.placeRelative(width - label.width, y)
            }
        }
    }
}

@Composable
internal fun Modifier.liquidChartScrub(
    entryCount: Int,
    selectedIndex: Int,
    selectedDescription: String,
    indexAt: (x: Float, width: Float) -> Int,
    onSelect: (Int) -> Unit,
): Modifier {
    val currentIndexAt = rememberUpdatedState(indexAt)
    val currentOnSelect = rememberUpdatedState(onSelect)
    return semantics {
        stateDescription = selectedDescription
        if (entryCount > 1) {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = selectedIndex.toFloat(),
                range = 0f..(entryCount - 1).toFloat(),
                steps = entryCount - 2,
            )
            setProgress { progress ->
                val index = progress.roundToInt().coerceIn(0, entryCount - 1)
                currentOnSelect.value(index)
                true
            }
        }
    }.pointerInput(entryCount) {
        detectTapGestures { offset ->
            currentOnSelect.value(currentIndexAt.value(offset.x, size.width.toFloat()))
        }
    }.pointerInput(entryCount) {
        var lastDragIndex = -1
        fun selectAt(x: Float) {
            val index = currentIndexAt.value(x, size.width.toFloat())
            if (index != lastDragIndex) {
                lastDragIndex = index
                currentOnSelect.value(index)
            }
        }
        detectHorizontalDragGestures(
            onDragStart = {
                lastDragIndex = -1
                selectAt(it.x)
            },
            onHorizontalDrag = { change, _ -> selectAt(change.position.x) },
        )
    }
}
