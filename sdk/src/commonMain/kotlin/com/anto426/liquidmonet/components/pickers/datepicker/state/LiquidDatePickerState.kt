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

/** Selection cardinality supported by [LiquidDatePicker]. */
enum class LiquidDateSelectionMode {
    Single,
    Multiple,
}

/**
 * Controls which dates and months can be navigated to, selected, or marked with a subtle status
 * indicator in [LiquidDatePicker].
 */
@Stable
interface LiquidDatePickerFilter {
    /** Returns true when [date] can be selected by the user. */
    fun isSelectableDate(date: LocalDate): Boolean = true

    /** Returns true when the calendar is allowed to display [year] and [month] (1..12). */
    fun isSelectableMonth(year: Int, month: Int): Boolean = true

    /** Returns true when [date] should render a subtle indicator dot (for example, booked days). */
    fun isMarkedDate(date: LocalDate): Boolean = false

    /** Optional accessibility state description for restricted or marked dates. */
    fun dateStateDescription(date: LocalDate): String? = null

    companion object {
        val All: LiquidDatePickerFilter = object : LiquidDatePickerFilter {}

        fun range(
            minDate: LocalDate? = null,
            maxDate: LocalDate? = null,
            isSelectable: (LocalDate) -> Boolean = { true },
            isMarked: (LocalDate) -> Boolean = { false },
            stateDescription: (LocalDate) -> String? = { null },
        ): LiquidDatePickerFilter =
            object : LiquidDatePickerFilter {
                override fun isSelectableDate(date: LocalDate): Boolean {
                    if (minDate != null && date < minDate) return false
                    if (maxDate != null && date > maxDate) return false
                    return isSelectable(date)
                }

                override fun isSelectableMonth(year: Int, month: Int): Boolean {
                    val monthStart = LocalDate(year, month, 1)
                    val monthEnd = LocalDate(year, month, daysInMonth(year, month))
                    if (minDate != null && monthEnd < minDate) return false
                    if (maxDate != null && monthStart > maxDate) return false
                    return true
                }

                override fun isMarkedDate(date: LocalDate): Boolean = isMarked(date)

                override fun dateStateDescription(date: LocalDate): String? = stateDescription(date)
            }
    }
}

/** State holder for LiquidDatePicker. */
@Stable
class LiquidDatePickerState(
    initialDate: LocalDate = currentLocalDate(),
    val selectionMode: LiquidDateSelectionMode = LiquidDateSelectionMode.Single,
    initialSelectedDates: Set<LocalDate> =
        if (selectionMode == LiquidDateSelectionMode.Single) setOf(initialDate) else emptySet(),
    initialFilter: LiquidDatePickerFilter = LiquidDatePickerFilter.All,
) {
    var filter: LiquidDatePickerFilter by mutableStateOf(initialFilter)
        private set

    var selectedDates: Set<LocalDate> by mutableStateOf(
        initialSelectedDates.filterTo(LinkedHashSet()) { initialFilter.isSelectableDate(it) }
    )

    var selectedDate: LocalDate?
        get() = selectedDates.firstOrNull()
        set(value) {
            selectedDates =
                if (value != null && filter.isSelectableDate(value)) setOf(value) else emptySet()
        }

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

    val canNavigatePreviousMonth: Boolean
        get() {
            val (prevYear, prevMonth) = previousMonthCoordinates(displayedYear, displayedMonth)
            return filter.isSelectableMonth(prevYear, prevMonth)
        }

    val canNavigateNextMonth: Boolean
        get() {
            val (nextYear, nextMonth) = nextMonthCoordinates(displayedYear, displayedMonth)
            return filter.isSelectableMonth(nextYear, nextMonth)
        }

    /** Change the displayed month only after its calendar coordinates have been validated. */
    fun showMonth(year: Int, month: Int) {
        val validMonth = LocalDate(year, month, 1)
        if (!filter.isSelectableMonth(validMonth.year, validMonth.month.number)) return
        displayedYear = validMonth.year
        displayedMonth = validMonth.month.number
    }

    fun previousMonth() {
        val (prevYear, prevMonth) = previousMonthCoordinates(displayedYear, displayedMonth)
        showMonth(prevYear, prevMonth)
    }

    fun nextMonth() {
        val (nextYear, nextMonth) = nextMonthCoordinates(displayedYear, displayedMonth)
        showMonth(nextYear, nextMonth)
    }

    fun isDateSelectable(date: LocalDate): Boolean = filter.isSelectableDate(date)

    fun isDateMarked(date: LocalDate): Boolean = filter.isMarkedDate(date)

    fun isDateSelected(date: LocalDate): Boolean = date in selectedDates

    fun selectDate(date: LocalDate): Boolean {
        if (!filter.isSelectableDate(date)) return false
        selectedDates =
            when (selectionMode) {
                LiquidDateSelectionMode.Single -> setOf(date)
                LiquidDateSelectionMode.Multiple ->
                    if (date in selectedDates) selectedDates - date else selectedDates + date
            }
        return true
    }

    fun selectDay(day: Int) {
        require(day in 1..daysInMonth) {
            "LiquidDatePicker day must belong to the displayed month."
        }
        selectDate(LocalDate(displayedYear, displayedMonth, day))
    }

    fun clearSelection() {
        if (selectedDates.isNotEmpty()) {
            selectedDates = emptySet()
        }
    }

    fun updateFilter(newFilter: LiquidDatePickerFilter, pruneSelection: Boolean = true) {
        filter = newFilter
        if (pruneSelection) {
            val pruned = selectedDates.filterTo(LinkedHashSet()) { newFilter.isSelectableDate(it) }
            if (pruned != selectedDates) {
                selectedDates = pruned
            }
        }
    }

    fun goToToday(today: LocalDate = currentLocalDate()) {
        if (filter.isSelectableMonth(today.year, today.month.number)) {
            showMonth(today.year, today.month.number)
        }
        if (selectionMode == LiquidDateSelectionMode.Single && filter.isSelectableDate(today)) {
            selectedDates = setOf(today)
        }
    }

    fun isSelected(day: Int): Boolean {
        if (day !in 1..daysInMonth) return false
        return LocalDate(displayedYear, displayedMonth, day) in selectedDates
    }

    fun isToday(day: Int): Boolean {
        val today = currentLocalDate()
        return today.year == displayedYear &&
            today.month.number == displayedMonth &&
            today.day == day
    }
}

private fun previousMonthCoordinates(year: Int, month: Int): Pair<Int, Int> =
    if (month == 1) (year - 1) to 12 else year to (month - 1)

private fun nextMonthCoordinates(year: Int, month: Int): Pair<Int, Int> =
    if (month == 12) (year + 1) to 1 else year to (month + 1)

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

internal val shortMonthNames =
    listOf(
        "Gen",
        "Feb",
        "Mar",
        "Apr",
        "Mag",
        "Giu",
        "Lug",
        "Ago",
        "Set",
        "Ott",
        "Nov",
        "Dic",
    )

internal val shortWeekdayNames =
    listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")

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
