package com.anto426.liquidmonet.components.pickers.datepicker.state

import androidx.compose.runtime.Immutable
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
 * Localized labels and calendar symbols used by [LiquidDatePicker], [LiquidDatePickerField], and
 * [LiquidDatePickerDialog].
 */
@Immutable
data class LiquidDatePickerLabels(
    val monthNames: List<String>,
    val shortMonthNames: List<String>,
    val shortWeekdayNames: List<String>,
    val weekdayInitials: List<String>,
    val emptySelection: String,
    val todayAction: String,
    val clearAction: String,
    val selectedCountFormat: (Int) -> String,
    val selectYearDescription: String,
    val closeYearSelectionDescription: String,
    val previousMonthDescription: String,
    val nextMonthDescription: String,
    val previousYearsDescription: String,
    val nextYearsDescription: String,
    val dialogTitle: String,
    val confirmAction: String,
    val cancelAction: String,
    val fieldLabel: String,
    val fieldPlaceholder: String,
) {
    init {
        require(monthNames.size == 12) { "monthNames must contain 12 entries." }
        require(shortMonthNames.size == 12) { "shortMonthNames must contain 12 entries." }
        require(shortWeekdayNames.size == 7) { "shortWeekdayNames must contain 7 entries." }
        require(weekdayInitials.size == 7) { "weekdayInitials must contain 7 entries." }
    }

    companion object {
        val English =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "January",
                        "February",
                        "March",
                        "April",
                        "May",
                        "June",
                        "July",
                        "August",
                        "September",
                        "October",
                        "November",
                        "December",
                    ),
                shortMonthNames =
                    listOf(
                        "Jan",
                        "Feb",
                        "Mar",
                        "Apr",
                        "May",
                        "Jun",
                        "Jul",
                        "Aug",
                        "Sep",
                        "Oct",
                        "Nov",
                        "Dec",
                    ),
                shortWeekdayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                weekdayInitials = listOf("M", "T", "W", "T", "F", "S", "S"),
                emptySelection = "No date selected",
                todayAction = "Today",
                clearAction = "Clear",
                selectedCountFormat = { count -> if (count == 1) "1 date" else "$count dates" },
                selectYearDescription = "Select year",
                closeYearSelectionDescription = "Close year selection",
                previousMonthDescription = "Previous month",
                nextMonthDescription = "Next month",
                previousYearsDescription = "Previous years",
                nextYearsDescription = "Next years",
                dialogTitle = "Select Date",
                confirmAction = "Confirm",
                cancelAction = "Cancel",
                fieldLabel = "Select Date",
                fieldPlaceholder = "DD/MM/YYYY",
            )

        val Italian =
            LiquidDatePickerLabels(
                monthNames =
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
                    ),
                shortMonthNames =
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
                    ),
                shortWeekdayNames = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom"),
                weekdayInitials = listOf("L", "M", "M", "G", "V", "S", "D"),
                emptySelection = "Nessuna data",
                todayAction = "Oggi",
                clearAction = "Pulisci",
                selectedCountFormat = { count -> if (count == 1) "1 data" else "$count date" },
                selectYearDescription = "Seleziona anno",
                closeYearSelectionDescription = "Chiudi selezione anno",
                previousMonthDescription = "Mese precedente",
                nextMonthDescription = "Mese successivo",
                previousYearsDescription = "Anni precedenti",
                nextYearsDescription = "Anni successivi",
                dialogTitle = "Seleziona Data",
                confirmAction = "Conferma",
                cancelAction = "Annulla",
                fieldLabel = "Seleziona Data",
                fieldPlaceholder = "GG/MM/AAAA",
            )

        val German =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "Januar",
                        "Februar",
                        "März",
                        "April",
                        "Mai",
                        "Juni",
                        "Juli",
                        "August",
                        "September",
                        "Oktober",
                        "November",
                        "Dezember",
                    ),
                shortMonthNames =
                    listOf(
                        "Jan",
                        "Feb",
                        "Mär",
                        "Apr",
                        "Mai",
                        "Jun",
                        "Jul",
                        "Aug",
                        "Sep",
                        "Okt",
                        "Nov",
                        "Dez",
                    ),
                shortWeekdayNames = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So"),
                weekdayInitials = listOf("M", "D", "M", "D", "F", "S", "S"),
                emptySelection = "Kein Datum ausgewählt",
                todayAction = "Heute",
                clearAction = "Zurücksetzen",
                selectedCountFormat = { count -> if (count == 1) "1 Datum" else "$count Daten" },
                selectYearDescription = "Jahr auswählen",
                closeYearSelectionDescription = "Jahresauswahl schließen",
                previousMonthDescription = "Vorheriger Monat",
                nextMonthDescription = "Nächster Monat",
                previousYearsDescription = "Vorherige Jahre",
                nextYearsDescription = "Nächste Jahre",
                dialogTitle = "Datum wählen",
                confirmAction = "Bestätigen",
                cancelAction = "Abbrechen",
                fieldLabel = "Datum wählen",
                fieldPlaceholder = "TT.MM.JJJJ",
            )

        val Spanish =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "Enero",
                        "Febrero",
                        "Marzo",
                        "Abril",
                        "Mayo",
                        "Junio",
                        "Julio",
                        "Agosto",
                        "Septiembre",
                        "Octubre",
                        "Noviembre",
                        "Diciembre",
                    ),
                shortMonthNames =
                    listOf(
                        "Ene",
                        "Feb",
                        "Mar",
                        "Abr",
                        "May",
                        "Jun",
                        "Jul",
                        "Ago",
                        "Sep",
                        "Oct",
                        "Nov",
                        "Dic",
                    ),
                shortWeekdayNames = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"),
                weekdayInitials = listOf("L", "M", "X", "J", "V", "S", "D"),
                emptySelection = "Ninguna fecha",
                todayAction = "Hoy",
                clearAction = "Limpiar",
                selectedCountFormat = { count -> if (count == 1) "1 fecha" else "$count fechas" },
                selectYearDescription = "Seleccionar año",
                closeYearSelectionDescription = "Cerrar selección de año",
                previousMonthDescription = "Mes anterior",
                nextMonthDescription = "Mes siguiente",
                previousYearsDescription = "Años anteriores",
                nextYearsDescription = "Años siguientes",
                dialogTitle = "Seleccionar fecha",
                confirmAction = "Confirmar",
                cancelAction = "Cancelar",
                fieldLabel = "Seleccionar fecha",
                fieldPlaceholder = "DD/MM/AAAA",
            )

        val French =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "Janvier",
                        "Février",
                        "Mars",
                        "Avril",
                        "Mai",
                        "Juin",
                        "Juillet",
                        "Août",
                        "Septembre",
                        "Octobre",
                        "Novembre",
                        "Décembre",
                    ),
                shortMonthNames =
                    listOf(
                        "Jan",
                        "Fév",
                        "Mar",
                        "Avr",
                        "Mai",
                        "Juin",
                        "Juil",
                        "Août",
                        "Sep",
                        "Oct",
                        "Nov",
                        "Déc",
                    ),
                shortWeekdayNames = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"),
                weekdayInitials = listOf("L", "M", "M", "J", "V", "S", "D"),
                emptySelection = "Aucune date",
                todayAction = "Aujourd'hui",
                clearAction = "Effacer",
                selectedCountFormat = { count -> if (count == 1) "1 date" else "$count dates" },
                selectYearDescription = "Choisir l'année",
                closeYearSelectionDescription = "Fermer le choix de l'année",
                previousMonthDescription = "Mois précédent",
                nextMonthDescription = "Mois suivant",
                previousYearsDescription = "Années précédentes",
                nextYearsDescription = "Années suivantes",
                dialogTitle = "Sélectionner une date",
                confirmAction = "Confirmer",
                cancelAction = "Annuler",
                fieldLabel = "Sélectionner une date",
                fieldPlaceholder = "JJ/MM/AAAA",
            )

        val Portuguese =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "Janeiro",
                        "Fevereiro",
                        "Março",
                        "Abril",
                        "Maio",
                        "Junho",
                        "Julho",
                        "Agosto",
                        "Setembro",
                        "Outubro",
                        "Novembro",
                        "Dezembro",
                    ),
                shortMonthNames =
                    listOf(
                        "Jan",
                        "Fev",
                        "Mar",
                        "Abr",
                        "Mai",
                        "Jun",
                        "Jul",
                        "Ago",
                        "Set",
                        "Out",
                        "Nov",
                        "Dez",
                    ),
                shortWeekdayNames = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sáb", "Dom"),
                weekdayInitials = listOf("S", "T", "Q", "Q", "S", "S", "D"),
                emptySelection = "Nenhuma data",
                todayAction = "Hoje",
                clearAction = "Limpar",
                selectedCountFormat = { count -> if (count == 1) "1 data" else "$count datas" },
                selectYearDescription = "Selecionar ano",
                closeYearSelectionDescription = "Fechar seleção de ano",
                previousMonthDescription = "Mês anterior",
                nextMonthDescription = "Próximo mês",
                previousYearsDescription = "Anos anteriores",
                nextYearsDescription = "Próximos anos",
                dialogTitle = "Selecionar data",
                confirmAction = "Confirmar",
                cancelAction = "Cancelar",
                fieldLabel = "Selecionar data",
                fieldPlaceholder = "DD/MM/AAAA",
            )

        val Dutch =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "Januari",
                        "Februari",
                        "Maart",
                        "April",
                        "Mei",
                        "Juni",
                        "Juli",
                        "Augustus",
                        "September",
                        "Oktober",
                        "November",
                        "December",
                    ),
                shortMonthNames =
                    listOf(
                        "Jan",
                        "Feb",
                        "Mrt",
                        "Apr",
                        "Mei",
                        "Jun",
                        "Jul",
                        "Aug",
                        "Sep",
                        "Okt",
                        "Nov",
                        "Dec",
                    ),
                shortWeekdayNames = listOf("Ma", "Di", "Wo", "Do", "Vr", "Za", "Zo"),
                weekdayInitials = listOf("M", "D", "W", "D", "V", "Z", "Z"),
                emptySelection = "Geen datum",
                todayAction = "Vandaag",
                clearAction = "Wissen",
                selectedCountFormat = { count -> if (count == 1) "1 datum" else "$count datums" },
                selectYearDescription = "Jaar selecteren",
                closeYearSelectionDescription = "Jaarselectie sluiten",
                previousMonthDescription = "Vorige maand",
                nextMonthDescription = "Volgende maand",
                previousYearsDescription = "Vorige jaren",
                nextYearsDescription = "Volgende jaren",
                dialogTitle = "Datum kiezen",
                confirmAction = "Bevestigen",
                cancelAction = "Annuleren",
                fieldLabel = "Datum kiezen",
                fieldPlaceholder = "DD-MM-JJJJ",
            )

        val Polish =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "Styczeń",
                        "Luty",
                        "Marzec",
                        "Kwiecień",
                        "Maj",
                        "Czerwiec",
                        "Lipiec",
                        "Sierpień",
                        "Wrzesień",
                        "Październik",
                        "Listopad",
                        "Grudzień",
                    ),
                shortMonthNames =
                    listOf(
                        "Sty",
                        "Lut",
                        "Mar",
                        "Kwi",
                        "Maj",
                        "Cze",
                        "Lip",
                        "Sie",
                        "Wrz",
                        "Paź",
                        "Lis",
                        "Gru",
                    ),
                shortWeekdayNames = listOf("Pon", "Wt", "Śr", "Czw", "Pt", "Sob", "Ndz"),
                weekdayInitials = listOf("P", "W", "Ś", "C", "P", "S", "N"),
                emptySelection = "Brak daty",
                todayAction = "Dzisiaj",
                clearAction = "Wyczyść",
                selectedCountFormat = { count -> "$count dni" },
                selectYearDescription = "Wybierz rok",
                closeYearSelectionDescription = "Zamknij wybór roku",
                previousMonthDescription = "Poprzedni miesiąc",
                nextMonthDescription = "Następny miesiąc",
                previousYearsDescription = "Poprzednie lata",
                nextYearsDescription = "Następne lata",
                dialogTitle = "Wybierz datę",
                confirmAction = "Zatwierdź",
                cancelAction = "Anuluj",
                fieldLabel = "Wybierz datę",
                fieldPlaceholder = "DD.MM.RRRR",
            )

        val Japanese =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "1月",
                        "2月",
                        "3月",
                        "4月",
                        "5月",
                        "6月",
                        "7月",
                        "8月",
                        "9月",
                        "10月",
                        "11月",
                        "12月",
                    ),
                shortMonthNames =
                    listOf(
                        "1月",
                        "2月",
                        "3月",
                        "4月",
                        "5月",
                        "6月",
                        "7月",
                        "8月",
                        "9月",
                        "10月",
                        "11月",
                        "12月",
                    ),
                shortWeekdayNames = listOf("月", "火", "水", "木", "金", "土", "日"),
                weekdayInitials = listOf("月", "火", "水", "木", "金", "土", "日"),
                emptySelection = "日付未選択",
                todayAction = "今日",
                clearAction = "クリア",
                selectedCountFormat = { count -> "${count}日選択" },
                selectYearDescription = "年を選択",
                closeYearSelectionDescription = "年選択を閉じる",
                previousMonthDescription = "前の月",
                nextMonthDescription = "次の月",
                previousYearsDescription = "前の年",
                nextYearsDescription = "次の年",
                dialogTitle = "日付を選択",
                confirmAction = "確定",
                cancelAction = "キャンセル",
                fieldLabel = "日付を選択",
                fieldPlaceholder = "YYYY/MM/DD",
            )

        val Chinese =
            LiquidDatePickerLabels(
                monthNames =
                    listOf(
                        "一月",
                        "二月",
                        "三月",
                        "四月",
                        "五月",
                        "六月",
                        "七月",
                        "八月",
                        "九月",
                        "十月",
                        "十一月",
                        "十二月",
                    ),
                shortMonthNames =
                    listOf(
                        "1月",
                        "2月",
                        "3月",
                        "4月",
                        "5月",
                        "6月",
                        "7月",
                        "8月",
                        "9月",
                        "10月",
                        "11月",
                        "12月",
                    ),
                shortWeekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日"),
                weekdayInitials = listOf("一", "二", "三", "四", "五", "六", "日"),
                emptySelection = "未选择日期",
                todayAction = "今天",
                clearAction = "清除",
                selectedCountFormat = { count -> "已选 ${count} 天" },
                selectYearDescription = "选择年份",
                closeYearSelectionDescription = "关闭年份选择",
                previousMonthDescription = "上个月",
                nextMonthDescription = "下个月",
                previousYearsDescription = "前几年",
                nextYearsDescription = "后几年",
                dialogTitle = "选择日期",
                confirmAction = "确认",
                cancelAction = "取消",
                fieldLabel = "选择日期",
                fieldPlaceholder = "YYYY/MM/DD",
            )

        fun forLanguage(languageCode: String): LiquidDatePickerLabels =
            when (languageCode.lowercase().substringBefore('-').substringBefore('_')) {
                "it" -> Italian
                "de" -> German
                "es" -> Spanish
                "fr" -> French
                "pt" -> Portuguese
                "nl" -> Dutch
                "pl" -> Polish
                "ja" -> Japanese
                "zh" -> Chinese
                else -> English
            }
    }
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
        get() = formatMonthName(LiquidDatePickerLabels.Italian)

    fun formatMonthName(labels: LiquidDatePickerLabels): String =
        "${labels.monthNames[displayedMonth - 1]} $displayedYear"

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
