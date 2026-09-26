package com.anto426.liquidmonet.components.pickers

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlin.time.Clock

/**
 * State holder for LiquidDatePicker.
 */
@Stable
class LiquidDatePickerState(
    initialDate: LocalDate = currentLocalDate()
) {
    var selectedDate: LocalDate? by mutableStateOf(initialDate)
    var displayedYear: Int by mutableIntStateOf(initialDate.year)
    var displayedMonth: Int by mutableIntStateOf(initialDate.month.number)

    val monthName: String
        get() {
            return "${monthNames[displayedMonth - 1]} $displayedYear"
        }

    val daysInMonth: Int
        get() {
            return daysInMonth(displayedYear, displayedMonth)
        }

    val firstDayOffset: Int
        get() {
            return LocalDate(displayedYear, displayedMonth, 1).dayOfWeek.ordinal
        }

    fun previousMonth() {
        if (displayedMonth == 1) {
            displayedMonth = 12
            displayedYear--
        } else {
            displayedMonth--
        }
    }

    fun nextMonth() {
        if (displayedMonth == 12) {
            displayedMonth = 1
            displayedYear++
        } else {
            displayedMonth++
        }
    }

    fun selectDay(day: Int) {
        require(day in 1..daysInMonth) {
            "LiquidDatePicker day must belong to the displayed month."
        }
        selectedDate = LocalDate(displayedYear, displayedMonth, day)
    }

    fun isSelected(day: Int): Boolean {
        val current = selectedDate ?: return false
        return current.year == displayedYear &&
                current.month.number == displayedMonth &&
                current.day == day
    }

    fun isToday(day: Int): Boolean {
        val today = currentLocalDate()
        return today.year == displayedYear &&
                today.month.number == displayedMonth &&
                today.day == day
    }
}

internal val monthNames = listOf(
    "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
    "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
)

internal fun currentLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    2 -> if (year % 400 == 0 || year % 4 == 0 && year % 100 != 0) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
}
