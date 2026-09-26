package com.anto426.liquidmonet.components.charts.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

/** Single data point for Liquid Line and Bar Charts. */
@Immutable
data class LiquidChartEntry(
    val label: String,
    val value: Float,
    val secondaryValue: Float? = null,
    val color: Color? = null,
    val payload: Any? = null,
)

/** Single slice entry for Liquid Donut and Pie Charts. */
@Immutable
data class LiquidPieEntry(
    val label: String,
    val value: Float,
    val color: Color? = null,
    val payload: Any? = null,
)

/** Configuration style for optical Liquid Monet chart rendering. */
@Immutable
data class LiquidChartStyle(
    val showGridLines: Boolean = true,
    val showRefractiveArea: Boolean = true,
    val showDataDroplets: Boolean = true,
    val smoothCurves: Boolean = true,
    val strokeWidth: Dp = 3.dp,
    val dropletRadius: Dp = 5.dp,
    val refractiveGlow: Boolean = true,
) {
    init {
        require(strokeWidth > 0.dp) { "LiquidChartStyle strokeWidth must be positive." }
        require(dropletRadius > 0.dp) { "LiquidChartStyle dropletRadius must be positive." }
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
