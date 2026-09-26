package com.anto426.liquidmonet.components.charts

import com.anto426.liquidmonet.components.charts.model.formatLiquidChartValue
import kotlin.test.Test
import kotlin.test.assertEquals

class LiquidChartFormattingTest {

    @Test
    fun formatsWholeNumbersWithoutDecimals() {
        assertEquals("28", formatLiquidChartValue(28.0f))
        assertEquals("30", formatLiquidChartValue(30.0f))
        assertEquals("0", formatLiquidChartValue(0.0f))
    }

    @Test
    fun formatsSingleDecimalWhenSecondDecimalIsZero() {
        assertEquals("28.5", formatLiquidChartValue(28.5f))
        assertEquals("28.5", formatLiquidChartValue(28.50f))
        assertEquals("27.1", formatLiquidChartValue(27.10f))
    }

    @Test
    fun formatsTwoDecimalsCorrectly() {
        assertEquals("28.33", formatLiquidChartValue(28.33f))
        assertEquals("28.33", formatLiquidChartValue(28.33333f))
        assertEquals("27.86", formatLiquidChartValue(27.85714f))
        assertEquals("28.05", formatLiquidChartValue(28.05f))
    }

    @Test
    fun formatsNegativeValues() {
        assertEquals("-28", formatLiquidChartValue(-28.0f))
        assertEquals("-0.5", formatLiquidChartValue(-0.5f))
        assertEquals("-2.33", formatLiquidChartValue(-2.333f))
    }
}
