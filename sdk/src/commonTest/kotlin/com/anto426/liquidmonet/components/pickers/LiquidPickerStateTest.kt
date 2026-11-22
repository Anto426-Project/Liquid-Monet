package com.anto426.liquidmonet.components.pickers

import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerFilter
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerLabels
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerState
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDateSelectionMode
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
        state.adjustHour(-2)
        assertEquals(22, state.hour)
        state.minute = 5
        state.adjustMinute(-10)
        assertEquals(55, state.minute)
    }

    @Test
    fun goToTodayUpdatesDisplayedMonthAndSelection() {
        val state = LiquidDatePickerState(LocalDate(2024, 1, 10))
        val target = LocalDate(2026, 11, 21)
        state.goToToday(target)
        assertEquals(2026, state.displayedYear)
        assertEquals(11, state.displayedMonth)
        assertEquals(target, state.selectedDate)
    }

    @Test
    fun dateFilterRestrictsSelectionAndMonthNavigation() {
        val blocked = LocalDate(2026, 11, 25)
        val filter =
            LiquidDatePickerFilter.range(
                minDate = LocalDate(2026, 11, 22),
                maxDate = LocalDate(2026, 12, 5),
                isSelectable = { it != blocked },
                isMarked = { it == blocked },
            )
        val state =
            LiquidDatePickerState(
                initialDate = LocalDate(2026, 11, 22),
                selectionMode = LiquidDateSelectionMode.Multiple,
                initialFilter = filter,
            )

        assertFalse(state.canNavigatePreviousMonth)
        assertTrue(state.canNavigateNextMonth)
        assertFalse(state.selectDate(LocalDate(2026, 11, 21)))
        assertFalse(state.selectDate(blocked))
        assertTrue(state.isDateMarked(blocked))

        assertTrue(state.selectDate(LocalDate(2026, 11, 23)))
        assertTrue(state.selectDate(LocalDate(2026, 11, 24)))
        assertEquals(setOf(LocalDate(2026, 11, 23), LocalDate(2026, 11, 24)), state.selectedDates)

        // Toggle off an already-selected date in multi-select mode.
        assertTrue(state.selectDate(LocalDate(2026, 11, 23)))
        assertEquals(setOf(LocalDate(2026, 11, 24)), state.selectedDates)

        // Updating the filter prunes newly disallowed dates automatically.
        state.updateFilter(
            LiquidDatePickerFilter.range(
                minDate = LocalDate(2026, 11, 25),
                maxDate = LocalDate(2026, 12, 5),
            )
        )
        assertTrue(state.selectedDates.isEmpty())

        state.nextMonth()
        assertEquals(2026, state.displayedYear)
        assertEquals(12, state.displayedMonth)
        assertFalse(state.canNavigateNextMonth)
    }

    @Test
    fun datePickerLabelsUseDefaultAndAcceptCustomOverrides() {
        val defaultLabels = LiquidDatePickerLabels.Default
        assertEquals("November", defaultLabels.monthNames[10])
        assertEquals("3 dates", defaultLabels.selectedCountFormat(3))
        assertEquals("No date selected", defaultLabels.emptySelection)

        val customLabels =
            defaultLabels.copy(
                emptySelection = "Nessuna data",
                selectedCountFormat = { count -> "$count date" },
            )
        val state = LiquidDatePickerState(LocalDate(2026, 11, 22))
        assertEquals("November 2026", state.formatMonthName(defaultLabels))
        assertEquals("Nessuna data", customLabels.emptySelection)
        assertEquals("3 date", customLabels.selectedCountFormat(3))
    }
}
