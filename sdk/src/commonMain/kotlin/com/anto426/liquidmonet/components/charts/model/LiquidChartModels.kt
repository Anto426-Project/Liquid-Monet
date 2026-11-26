package com.anto426.liquidmonet.components.charts.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

/** Single data point for [com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart] and [com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart]. */
@Immutable
data class LiquidChartEntry(
    val label: String,
    val value: Float,
    val color: Color? = null,
    val payload: Any? = null,
)

/** Single slice entry for [com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart]. */
@Immutable
data class LiquidPieEntry(
    val label: String,
    val value: Float,
    val color: Color? = null,
    val payload: Any? = null,
)

/** Stroke style for a [LiquidLineSeries]. */
enum class LiquidLineStyle {
    Solid,
    Dashed,
    Dotted,
}

/** Named data series rendered by [com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart]. */
@Immutable
data class LiquidLineSeries(
    val label: String,
    val entries: List<LiquidChartEntry>,
    val color: Color? = null,
    val lineStyle: LiquidLineStyle = LiquidLineStyle.Solid,
    val strokeWidth: Dp? = null,
    val showArea: Boolean = false,
    val areaStartColor: Color? = null,
    val showPoints: Boolean = false,
    val initiallyVisible: Boolean = true,
) {
    init {
        strokeWidth?.let {
            require(it > 0.dp) { "LiquidLineSeries strokeWidth must be positive." }
        }
    }
}

/** Style configuration for [com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart]. */
@Immutable
data class LiquidLineChartStyle(
    val showGridLines: Boolean = true,
    val showYAxis: Boolean = showGridLines,
    val showXAxisLabels: Boolean = false,
    val gridLineCount: Int = 3,
    val gridColor: Color? = null,
    val dashedGridLines: Boolean = false,
    val smoothCurves: Boolean = true,
    val curveTension: Float = 0.32f,
    val strokeWidth: Dp = 3.dp,
    val showDataDroplets: Boolean = true,
    val showAllDataPoints: Boolean = true,
    val dropletRadius: Dp = 5.dp,
    val refractiveGlow: Boolean = true,
    val areaTopAlpha: Float = 0.22f,
    val showWaveOverlay: Boolean = false,
    val showScrubber: Boolean = true,
    val plotHorizontalInset: Dp = 10.dp,
    val plotVerticalInset: Dp? = null,
) {
    init {
        require(strokeWidth > 0.dp) { "LiquidLineChartStyle strokeWidth must be positive." }
        require(dropletRadius > 0.dp) { "LiquidLineChartStyle dropletRadius must be positive." }
        require(gridLineCount >= 2) { "LiquidLineChartStyle gridLineCount must be at least 2." }
        require(areaTopAlpha in 0f..1f) { "LiquidLineChartStyle areaTopAlpha must be in 0..1." }
        require(plotHorizontalInset >= 0.dp) { "LiquidLineChartStyle plotHorizontalInset must be non-negative." }
        plotVerticalInset?.let {
            require(it >= 0.dp) { "LiquidLineChartStyle plotVerticalInset must be non-negative." }
        }
        require(curveTension in 0f..1f) { "LiquidLineChartStyle curveTension must be in 0..1." }
    }
}

/** Style configuration for [com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart]. */
@Immutable
data class LiquidBarChartStyle(
    val barWidth: Dp = 22.dp,
    val barCornerRadius: Dp? = null,
    val useGradientFill: Boolean = true,
    val showGridLines: Boolean = true,
    val showYAxis: Boolean = true,
    val showXAxisLabels: Boolean = true,
    val gridLineCount: Int = 3,
    val gridColor: Color? = null,
    val dashedGridLines: Boolean = false,
    val showSelectionHighlight: Boolean = true,
    val plotVerticalInset: Dp? = null,
) {
    init {
        require(barWidth > 0.dp) { "LiquidBarChartStyle barWidth must be positive." }
        barCornerRadius?.let {
            require(it >= 0.dp) { "LiquidBarChartStyle barCornerRadius must be non-negative." }
        }
        require(gridLineCount >= 2) { "LiquidBarChartStyle gridLineCount must be at least 2." }
        plotVerticalInset?.let {
            require(it >= 0.dp) { "LiquidBarChartStyle plotVerticalInset must be non-negative." }
        }
    }
}

/** Style configuration for [com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart]. */
@Immutable
data class LiquidDonutChartStyle(
    val strokeWidth: Dp = 22.dp,
    val strokeCap: StrokeCap = StrokeCap.Butt,
    val sliceGapDegrees: Float = 3f,
    val startAngle: Float = -90f,
    val trackColor: Color? = null,
    val activeStrokeScale: Float = 1.18f,
    val inactiveAlpha: Float = 0.48f,
    val showCenterContent: Boolean = true,
) {
    init {
        require(strokeWidth > 0.dp) { "LiquidDonutChartStyle strokeWidth must be positive." }
        require(sliceGapDegrees.isFinite() && sliceGapDegrees >= 0f) {
            "LiquidDonutChartStyle sliceGapDegrees must be finite and non-negative."
        }
        require(startAngle.isFinite()) { "LiquidDonutChartStyle startAngle must be finite." }
        require(activeStrokeScale >= 1f) { "LiquidDonutChartStyle activeStrokeScale must be >= 1f." }
        require(inactiveAlpha in 0f..1f) { "LiquidDonutChartStyle inactiveAlpha must be in 0..1." }
    }
}

/** Multiplatform-safe compact value formatter used by chart disclosures and scales. */
fun formatLiquidChartValue(value: Float): String {
    val scaled = (value * 100f).roundToInt()
    val sign = if (scaled < 0) "-" else ""
    val absScaled = abs(scaled)
    val integerPart = absScaled / 100
    val remainder = absScaled % 100
    if (remainder == 0) return "$sign$integerPart"
    if (remainder % 10 == 0) return "$sign$integerPart.${remainder / 10}"
    val fraction = remainder.toString().padStart(2, '0')
    return "$sign$integerPart.$fraction"
}
