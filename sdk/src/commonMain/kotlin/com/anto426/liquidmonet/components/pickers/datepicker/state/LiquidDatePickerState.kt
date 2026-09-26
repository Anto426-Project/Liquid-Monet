package com.anto426.liquidmonet.components.pickers.datepicker.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.todayIn

/** State holder for LiquidDatePicker. */
@Stable
class LiquidDatePickerState(initialDate: LocalDate = currentLocalDate()) {
    var selectedDate: LocalDate? by mutableStateOf(initialDate)
    var displayedYear: Int by mutableIntStateOf(initialDate.year)
        private set

    var displayedMonth: Int by mutableIntStateOf(initialDate.month.number)
        private set

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

    /** Change the displayed month only after its calendar coordinates have been validated. */
    fun showMonth(year: Int, month: Int) {
        val validMonth = LocalDate(year, month, 1)
        displayedYear = validMonth.year
        displayedMonth = validMonth.month.number
    }

    fun previousMonth() {
        if (displayedMonth == 1) showMonth(displayedYear - 1, 12)
        else showMonth(displayedYear, displayedMonth - 1)
    }

    fun nextMonth() {
        if (displayedMonth == 12) showMonth(displayedYear + 1, 1)
        else showMonth(displayedYear, displayedMonth + 1)
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

internal val monthNames =
    listOf(
        "Gennaio",
        "Febbraio",
        "Marzo",
        "Aprile",
        "Maggio",
        "Giugno",
        "Luglio",
        "Agosto",
        "Settembre",
        "Ottobre",
        "Novembre",
        "Dicembre",
    )

internal fun currentLocalDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())

private fun daysInMonth(year: Int, month: Int): Int =
    when (month) {
        2 -> if (year % 400 == 0 || year % 4 == 0 && year % 100 != 0) 29 else 28
        4,
        6,
        9,
        11 -> 30
        else -> 31
    }
