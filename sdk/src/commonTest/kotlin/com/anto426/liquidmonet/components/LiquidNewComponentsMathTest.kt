package com.anto426.liquidmonet.components

import com.anto426.liquidmonet.components.inputs.numberfield.LiquidNumberFieldMath
import com.anto426.liquidmonet.components.inputs.pinpad.LiquidPinPadMath
import com.anto426.liquidmonet.components.inputs.taginput.LiquidTagInputMath
import com.anto426.liquidmonet.components.navigation.floatingtoolbar.LiquidFloatingToolbarMath
import com.anto426.liquidmonet.components.selection.segmentedcontrol.LiquidSegmentedControlMath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LiquidNewComponentsMathTest {
    @Test
    fun segmentedControlMathHandlesLtrAndRtlMapping() {
        assertEquals(0, LiquidSegmentedControlMath.clampIndex(-2, 3))
        assertEquals(2, LiquidSegmentedControlMath.clampIndex(9, 3))
        assertEquals(0, LiquidSegmentedControlMath.indexFromPosition(10f, 300f, 3, isRtl = false))
        assertEquals(1, LiquidSegmentedControlMath.indexFromPosition(150f, 300f, 3, isRtl = false))
        assertEquals(2, LiquidSegmentedControlMath.indexFromPosition(295f, 300f, 3, isRtl = false))
        assertEquals(2, LiquidSegmentedControlMath.indexFromPosition(10f, 300f, 3, isRtl = true))
        assertEquals(0, LiquidSegmentedControlMath.indexFromPosition(295f, 300f, 3, isRtl = true))
        assertEquals(100f, LiquidSegmentedControlMath.indicatorTranslationX(1f, 100f, 3, isRtl = false))
        assertEquals(200f, LiquidSegmentedControlMath.indicatorTranslationX(0f, 100f, 3, isRtl = true))
    }

    @Test
    fun floatingToolbarMathFormatsBadgesSafely() {
        assertNull(LiquidFloatingToolbarMath.compactBadge(null))
        assertNull(LiquidFloatingToolbarMath.compactBadge("   "))
        assertEquals("12", LiquidFloatingToolbarMath.compactBadge(" 12 "))
        assertEquals("999+", LiquidFloatingToolbarMath.compactBadge("12345"))
        assertEquals("Nuo…", LiquidFloatingToolbarMath.compactBadge("Nuovo"))
    }

    @Test
    fun pinPadMathFiltersNonDigitsAndRespectsMaxLength() {
        assertEquals("1", LiquidPinPadMath.appendDigit("", '1', 4))
        assertEquals("12", LiquidPinPadMath.appendDigit("1a", '2', 4))
        assertEquals("1234", LiquidPinPadMath.appendDigit("1234", '5', 4))
        assertEquals("123", LiquidPinPadMath.appendDigit("123", 'x', 4))
        assertEquals("12", LiquidPinPadMath.deleteLast("123"))
        assertEquals("", LiquidPinPadMath.deleteLast(""))
    }

    @Test
    fun tagInputMathDeduplicatesSplitsAndRespectsMaxTags() {
        val afterAdd = LiquidTagInputMath.addTag(listOf("Kotlin"), "  compose ", maxTags = 3)
        assertEquals(listOf("Kotlin", "compose"), afterAdd)

        // Case-insensitive deduplication
        assertEquals(afterAdd, LiquidTagInputMath.addTag(afterAdd, "KOTLIN", maxTags = 3))

        // Allow duplicates when requested
        assertEquals(
            listOf("Kotlin", "compose", "KOTLIN"),
            LiquidTagInputMath.addTag(afterAdd, "KOTLIN", maxTags = 3, allowDuplicates = true),
        )

        // Max tags cap
        assertEquals(
            listOf("A", "B"),
            LiquidTagInputMath.addTag(listOf("A", "B"), "C", maxTags = 2),
        )

        // Comma and newline splitting
        val (completed, remainder) = LiquidTagInputMath.splitCompletedTokens("ui, glass,\nmonet")
        assertEquals(listOf("ui", "glass"), completed)
        assertEquals("monet", remainder)

        // Removal
        assertEquals(listOf("Kotlin"), LiquidTagInputMath.removeTag(afterAdd, "compose"))
    }

    @Test
    fun numberFieldMathClampsStepsFormatsAndParses() {
        val range = 0.0..100.0
        assertEquals(0.0, LiquidNumberFieldMath.clamp(-5.0, range))
        assertEquals(100.0, LiquidNumberFieldMath.clamp(250.0, range))
        assertEquals(1.3, LiquidNumberFieldMath.step(1.2, 0.1, range, decimals = 1))
        assertEquals("12.50", LiquidNumberFieldMath.format(12.5, decimals = 2))
        assertEquals("42", LiquidNumberFieldMath.format(42.0, decimals = 0))
        assertEquals(18.75, LiquidNumberFieldMath.parse("18,75", range))
        assertNull(LiquidNumberFieldMath.parse("abc", range))
    }
}
