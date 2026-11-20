package com.anto426.liquidmonet.components.charts.internal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
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
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeaderSize
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.kyant.backdrop.Backdrop
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

@Composable
private fun LiquidChartSwatch(color: Color, mark: LiquidChartMark) {
    Canvas(Modifier.size(18.dp, 12.dp)) {
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
    content: @Composable () -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
internal fun LiquidChartLegendItem(
    label: String,
    color: Color,
    selected: Boolean,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    mark: LiquidChartMark = LiquidChartMark.Dot,
    value: String? = null,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = if (value == null) Capsule() else RoundedRectangle(14.dp)
    LiquidBadge(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { this.selected = selected }
            .liquidClickable(shape = shape, onClick = onClick),
        shape = shape,
        containerColor = if (selected) color.copy(alpha = 0.15f) else scheme.surfaceVariant.copy(alpha = 0.40f),
        contentColor = scheme.onSurface,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        border = if (selected) BorderStroke(1.dp, scheme.outline) else null,
        backdropState = backdrop,
    ) {
        Row(
            modifier = if (value == null) Modifier else Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidChartSwatch(color, mark)
            Text(
                text = label,
                modifier = if (value == null) Modifier else Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = if (value == null) 2 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
internal fun LiquidChartSeriesValue(label: String, value: String, color: Color, mark: LiquidChartMark) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        LiquidChartSwatch(color, mark)
        Text(
            text = "$label  $value",
            style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun LiquidChartSelection(
    label: String,
    value: String,
    color: Color,
    backdrop: Backdrop,
    description: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    val valueBadge: @Composable () -> Unit = {
        LiquidBadge(
            shape = Capsule(),
            text = value,
            containerColor = color.copy(alpha = 0.16f),
            contentColor = scheme.onSurface,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            textStyle = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
            ),
            backdropState = backdrop,
        )
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val stack = maxWidth < 200.dp || LocalDensity.current.fontScale > 1.4f
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LiquidSectionHeader(
                title = label,
                subtitle = description,
                subtitleColor = scheme.onSurfaceVariant,
                size = LiquidSectionHeaderSize.Small,
                trailingContent = if (stack) null else valueBadge,
            )
            if (stack) valueBadge()
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
        val gap = (plotHeight - 2 * inset).coerceAtLeast(0) / (labels.size - 1)
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
