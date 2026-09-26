package com.anto426.liquidmonet.components.pickers

import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerState
import com.anto426.liquidmonet.components.pickers.timepicker.state.LiquidTimePickerState
import kotlin.test.*
import kotlinx.datetime.LocalDate

class LiquidPickerStateTest {
    @Test
    fun invalidCalendarNavigationLeavesThePreviousMonthIntact() {
        val state = LiquidDatePickerState(LocalDate(2026, 9, 25))
        assertFailsWith<IllegalArgumentException> { state.showMonth(2026, 13) }
        assertEquals(2026, state.displayedYear)
        assertEquals(9, state.displayedMonth)
        assertEquals(LocalDate(2026, 9, 25), state.selectedDate)
    }

    @Test
    fun navigationAcrossTheYearDoesNotChangeSelection() {
        val date = LocalDate(2026, 1, 31)
        val state = LiquidDatePickerState(date)
        state.previousMonth()
        assertEquals(2025, state.displayedYear)
        assertEquals(12, state.displayedMonth)
        state.nextMonth()
        assertEquals(2026, state.displayedYear)
        assertEquals(1, state.displayedMonth)
        assertEquals(date, state.selectedDate)
    }

    @Test
    fun leapMonthSelectionRejectsDaysOutsideTheDisplayedCalendar() {
        val state = LiquidDatePickerState(LocalDate(2024, 2, 1))
        state.selectDay(29)
        assertEquals(LocalDate(2024, 2, 29), state.selectedDate)
        assertFailsWith<IllegalArgumentException> { state.selectDay(30) }
        state.showMonth(2025, 2)
        assertEquals(28, state.daysInMonth)
    }

    @Test
    fun clockValuesNormalizeBothInitialAndLaterAssignments() {
        val state = LiquidTimePickerState(-1, 61)
        assertEquals("23:01", state.formattedTime)
        state.hour = Int.MIN_VALUE
        state.minute = Int.MAX_VALUE
        assertTrue(state.hour in 0..23)
        assertTrue(state.minute in 0..59)
        state.hour = 23
        state.incrementHour()
        assertEquals(0, state.hour)
    }
}
