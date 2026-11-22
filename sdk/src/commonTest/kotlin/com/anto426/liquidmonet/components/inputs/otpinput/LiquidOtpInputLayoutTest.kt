package com.anto426.liquidmonet.components.inputs.otpinput

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiquidOtpInputLayoutTest {
    @Test
    fun keepsPreferredDimensionsOnWideContainers() {
        val metrics = resolveLiquidOtpLayout(
            length = 6,
            availableWidth = 340.dp,
            preferredCellWidth = 42.dp,
            preferredCellHeight = 52.dp,
            preferredSpacing = 8.dp,
        )

        assertEquals(6, metrics.columns)
        assertEquals(42.dp, metrics.cellWidth)
        assertEquals(52.dp, metrics.cellHeight)
        assertEquals(8.dp, metrics.spacing)
        assertEquals(22f, metrics.digitFontSizeSp)
    }

    @Test
    fun keepsSixDigitPinOnSingleRowOnCompactPhoneWidth() {
        // 361dp phone inside 18dp screen padding + 24dp card padding -> 277dp available
        val metrics = resolveLiquidOtpLayout(
            length = 6,
            availableWidth = 277.dp,
            preferredCellWidth = 42.dp,
            preferredCellHeight = 52.dp,
            preferredSpacing = 8.dp,
        )

        assertEquals(6, metrics.columns)
        val totalWidth = metrics.cellWidth * 6 + metrics.spacing * 5
        assertTrue(totalWidth <= 273.1.dp, "Expected single-row strip to fit inside available width, got $totalWidth")
        assertTrue(metrics.cellWidth >= 38.dp, "Expected comfortable cell width, got ${metrics.cellWidth}")
        assertTrue(metrics.spacing >= 4.dp, "Expected readable cell spacing, got ${metrics.spacing}")
    }

    @Test
    fun keepsSixDigitPinOnSingleRowEvenOnNarrow320dpPhone() {
        val metrics = resolveLiquidOtpLayout(
            length = 6,
            availableWidth = 236.dp,
            preferredCellWidth = 42.dp,
            preferredCellHeight = 52.dp,
            preferredSpacing = 8.dp,
        )

        assertEquals(6, metrics.columns)
        val totalWidth = metrics.cellWidth * 6 + metrics.spacing * 5
        assertTrue(totalWidth <= 232.1.dp, "Expected single-row strip to fit inside 236dp, got $totalWidth")
        assertTrue(metrics.cellWidth >= 30.dp, "Expected cellWidth >= 30dp, got ${metrics.cellWidth}")
    }

    @Test
    fun clampsDigitFontSizeUnderLargeAccessibilityFontScaleWithoutWrapping() {
        val metrics = resolveLiquidOtpLayout(
            length = 6,
            availableWidth = 277.dp,
            preferredCellWidth = 42.dp,
            preferredCellHeight = 52.dp,
            preferredSpacing = 8.dp,
            fontScale = 1.4f,
        )

        assertEquals(6, metrics.columns)
        assertTrue(metrics.digitFontSizeSp * 1.4f <= metrics.cellWidth.value * 0.6f)
    }

    @Test
    fun wrapsGracefullyWhenSingleRowWouldBeTooCramped() {
        val metrics = resolveLiquidOtpLayout(
            length = 10,
            availableWidth = 220.dp,
            preferredCellWidth = 42.dp,
            preferredCellHeight = 52.dp,
            preferredSpacing = 8.dp,
        )

        assertEquals(5, metrics.columns)
        val rowWidth = metrics.cellWidth * metrics.columns + metrics.spacing * (metrics.columns - 1)
        assertTrue(rowWidth <= 216.1.dp, "Expected wrapped row to fit inside 220dp, got $rowWidth")
    }
}
