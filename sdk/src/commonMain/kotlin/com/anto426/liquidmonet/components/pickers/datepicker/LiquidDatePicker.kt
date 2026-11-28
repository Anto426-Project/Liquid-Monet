package com.anto426.liquidmonet.components.pickers.datepicker

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.pickers.datepicker.motion.LiquidDatePickerMotion
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerFilter
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerLabels
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerState
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDateSelectionMode
import com.anto426.liquidmonet.components.pickers.datepicker.state.currentLocalDate
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Composable
fun rememberLiquidDatePickerState(
    initialDate: LocalDate = currentLocalDate(),
    selectionMode: LiquidDateSelectionMode = LiquidDateSelectionMode.Single,
    initialSelectedDates: Set<LocalDate> =
        if (selectionMode == LiquidDateSelectionMode.Single) setOf(initialDate) else emptySet(),
    filter: LiquidDatePickerFilter = LiquidDatePickerFilter.All,
): LiquidDatePickerState {
    val state =
        remember(initialDate, selectionMode) {
            LiquidDatePickerState(
                initialDate = initialDate,
                selectionMode = selectionMode,
                initialSelectedDates = initialSelectedDates,
                initialFilter = filter,
            )
        }
    SideEffect {
        if (state.filter != filter) {
            state.updateFilter(filter)
        }
    }
    return state
}

/**
 * Inline calendar built from Liquid Monet primitives ([LiquidCard], [LiquidBadge], [LiquidButton],
 * [LiquidIconButton], [LiquidHorizontalDivider], [liquidClickable], and [liquidGlass]): one
 * coherent glass surface, directional month transitions, date filtering, single or multi-date
 * selection, localized labels, and optional quick year jump.
 */
@Composable
fun LiquidDatePicker(
    state: LiquidDatePickerState,
    modifier: Modifier = Modifier,
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    labels: LiquidDatePickerLabels = LiquidDatePickerLabels.Default,
    allowYearSelection: Boolean = true,
    showTodayAction: Boolean = state.selectionMode == LiquidDateSelectionMode.Single,
    showClearAction: Boolean = state.selectionMode == LiquidDateSelectionMode.Multiple,
    clearActionLabel: String = labels.clearAction,
    todayActionLabel: String = labels.todayAction,
    emptySelectionLabel: String = labels.emptySelection,
) {
    val isInsideSharedContainer =
        LocalLiquidGlassContainerMode.current == LiquidGlassContainerMode.Shared

    if (isInsideSharedContainer) {
        Column(
            modifier = modifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiquidDatePickerContent(
                state = state,
                backdrop = backdrop,
                labels = labels,
                allowYearSelection = allowYearSelection,
                showTodayAction = showTodayAction,
                showClearAction = showClearAction,
                clearActionLabel = clearActionLabel,
                todayActionLabel = todayActionLabel,
                emptySelectionLabel = emptySelectionLabel,
            )
        }
    } else {
        LiquidCard(
            modifier = modifier.fillMaxWidth(),
            backdrop = backdrop,
            shape = RoundedRectangle(24.dp),
            contentPadding = PaddingValues(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LiquidDatePickerContent(
                    state = state,
                    backdrop = backdrop,
                    labels = labels,
                    allowYearSelection = allowYearSelection,
                    showTodayAction = showTodayAction,
                    showClearAction = showClearAction,
                    clearActionLabel = clearActionLabel,
                    todayActionLabel = todayActionLabel,
                    emptySelectionLabel = emptySelectionLabel,
                )
            }
        }
    }
}

@Composable
private fun LiquidDatePickerContent(
    state: LiquidDatePickerState,
    backdrop: Backdrop,
    labels: LiquidDatePickerLabels,
    allowYearSelection: Boolean,
    showTodayAction: Boolean,
    showClearAction: Boolean,
    clearActionLabel: String,
    todayActionLabel: String,
    emptySelectionLabel: String,
) {
    val scheme = MaterialTheme.colorScheme
    val colors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)
    val performance = LocalLiquidGlassPerformance.current
    val haptic = LocalHapticFeedback.current
    val today = remember { currentLocalDate() }

    var isSelectingYear by remember(allowYearSelection) { mutableStateOf(false) }
    var yearPageStart by remember(state.displayedYear) {
        mutableIntStateOf(state.displayedYear - 4)
    }

    val chevronRotation by
        LiquidDatePickerMotion.animateFloat(
            targetValue = if (isSelectingYear) 180f else 0f,
            performance = performance,
            label = "yearToggleChevron",
        )

    val canJumpToToday =
        state.filter.isSelectableMonth(today.year, today.month.number) &&
            (state.selectionMode == LiquidDateSelectionMode.Multiple || state.isDateSelectable(today))
    val isAlreadyOnToday =
        state.selectedDate == today &&
            state.displayedYear == today.year &&
            state.displayedMonth == today.month.number

    val selectedDates = state.selectedDates
    val selectedSummary =
        remember(selectedDates, emptySelectionLabel, labels) {
            when (selectedDates.size) {
                0 -> emptySelectionLabel
                1 -> {
                    val date = selectedDates.first()
                    val weekday = labels.shortWeekdayNames[date.dayOfWeek.ordinal]
                    val month = labels.shortMonthNames[date.month.number - 1]
                    "$weekday ${date.day} $month ${date.year}"
                }
                2 ->
                    selectedDates.sorted().joinToString(", ") { date ->
                        "${date.day} ${labels.shortMonthNames[date.month.number - 1]}"
                    }
                else -> labels.selectedCountFormat(selectedDates.size)
            }
        }

    // Selected date summary badge + quick action LiquidButton
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        LiquidBadge(
            shape = Capsule(),
            modifier = Modifier.weight(1f, fill = false),
            containerColor = colors.neutralContainer,
            contentColor = colors.content,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            border = BorderStroke(1.dp, colors.outline),
            backdrop = effectiveBackdrop,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Calendar,
                    contentDescription = null,
                    tint = scheme.primary,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = selectedSummary,
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                        ),
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (showClearAction && selectedDates.isNotEmpty()) {
            LiquidButton(
                onClick = {
                    haptic.performLiquidHaptic(LiquidHapticCue.Selection)
                    state.clearSelection()
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                text = "$clearActionLabel (${selectedDates.size})",
                variant = LiquidButtonVariant.Tonal,
                size = LiquidButtonSize.Small,
                minimumHeight = 32.dp,
                textStyle =
                    MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum",
                    ),
                backdrop = effectiveBackdrop,
                modifier = Modifier.padding(start = 8.dp),
            )
        } else if (showTodayAction) {
            val todayActionEnabled = !isAlreadyOnToday && canJumpToToday
            LiquidButton(
                onClick = {
                    haptic.performLiquidHaptic(LiquidHapticCue.Selection)
                    isSelectingYear = false
                    state.goToToday(today)
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                text = todayActionLabel,
                enabled = todayActionEnabled,
                variant = LiquidButtonVariant.Tonal,
                size = LiquidButtonSize.Small,
                minimumHeight = 32.dp,
                textStyle =
                    MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                backdrop = effectiveBackdrop,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }

    // Month / Year header with LiquidClickable title and LiquidIconButton arrows
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier =
                Modifier.weight(1f)
                    .liquidClickable(
                        shape = Capsule(),
                        enabled = allowYearSelection,
                        role = Role.Button,
                        clipContent = true,
                        onClick =
                            if (allowYearSelection) {
                                {
                                    haptic.performLiquidHaptic(LiquidHapticCue.Selection)
                                    if (!isSelectingYear) {
                                        yearPageStart = state.displayedYear - 4
                                    }
                                    isSelectingYear = !isSelectingYear
                                }
                            } else {
                                null
                            },
                    )
                    .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AnimatedContent(
                targetState =
                    if (isSelectingYear) "$yearPageStart – ${yearPageStart + 11}"
                    else state.formatMonthName(labels),
                transitionSpec = LiquidDatePickerMotion.contentTransition(performance),
                label = "calendarMonthHeader",
            ) { title ->
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                        ),
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (allowYearSelection) {
                Icon(
                    imageVector = LiquidIcons.KeyboardArrowDown,
                    contentDescription =
                        if (isSelectingYear) labels.closeYearSelectionDescription
                        else labels.selectYearDescription,
                    tint = colors.secondaryContent,
                    modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = chevronRotation },
                )
            }
        }

        LiquidIconButton(
            icon = LiquidIcons.ChevronLeft,
            enabled = isSelectingYear || state.canNavigatePreviousMonth,
            onClick = {
                haptic.performLiquidHaptic(LiquidHapticCue.Selection)
                if (isSelectingYear) {
                    yearPageStart = (yearPageStart - 12).coerceAtLeast(1900)
                } else {
                    state.previousMonth()
                }
            },
            contentDescription =
                if (isSelectingYear) labels.previousYearsDescription
                else labels.previousMonthDescription,
            variant = LiquidIconButtonVariant.Ghost,
            size = 38.dp,
            backdrop = effectiveBackdrop,
        )
        LiquidIconButton(
            icon = LiquidIcons.ChevronRight,
            enabled = isSelectingYear || state.canNavigateNextMonth,
            onClick = {
                haptic.performLiquidHaptic(LiquidHapticCue.Selection)
                if (isSelectingYear) {
                    yearPageStart = (yearPageStart + 12).coerceAtMost(2188)
                } else {
                    state.nextMonth()
                }
            },
            contentDescription =
                if (isSelectingYear) labels.nextYearsDescription
                else labels.nextMonthDescription,
            variant = LiquidIconButtonVariant.Ghost,
            size = 38.dp,
            backdrop = effectiveBackdrop,
        )
    }

    LiquidHorizontalDivider()

    // Body: either 4x3 year picker or 6x7 day grid (kept at matching height to prevent dialog jumps)
    AnimatedContent(
        targetState = isSelectingYear,
        transitionSpec = LiquidDatePickerMotion.contentTransition(performance),
        label = "calendarMode",
    ) { selectingYear ->
        if (selectingYear) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                repeat(4) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(3) { col ->
                            val year = yearPageStart + row * 3 + col
                            val isSelectedYear = year == state.displayedYear
                            val isCurrentYear = year == today.year
                            val selectableMonthInYear =
                                (1..12).firstOrNull { month ->
                                    state.filter.isSelectableMonth(year, month)
                                }
                            val isYearSelectable = selectableMonthInYear != null
                            val yearShape = RoundedRectangle(14.dp)
                            val bgColor by
                                LiquidDatePickerMotion.animateColor(
                                    targetValue =
                                        when {
                                            isSelectedYear -> scheme.primary
                                            isCurrentYear -> colors.accentContainer
                                            else -> colors.neutralContainer
                                        },
                                    performance = performance,
                                    label = "yearCellBg",
                                )
                            Box(
                                modifier =
                                    Modifier.weight(1f)
                                        .height(54.dp)
                                        .graphicsLayer {
                                            alpha = if (isYearSelectable) 1f else 0.32f
                                        }
                                        .semantics { this.selected = isSelectedYear }
                                        .liquidGlass(
                                            backdrop = backdrop,
                                            shape = yearShape,
                                            role = LiquidGlassRole.Control,
                                            containerColor = bgColor,
                                        )
                                        .border(
                                            width =
                                                if (isCurrentYear && !isSelectedYear) 1.5.dp
                                                else 1.dp,
                                            color =
                                                when {
                                                    isSelectedYear -> Color.Transparent
                                                    isCurrentYear -> scheme.primary
                                                    else -> colors.outline.copy(alpha = 0.6f)
                                                },
                                            shape = yearShape,
                                        )
                                        .liquidClickable(
                                            shape = yearShape,
                                            enabled = isYearSelectable,
                                            role = Role.RadioButton,
                                            clipContent = true,
                                            onClick = {
                                                haptic.performLiquidHaptic(
                                                    LiquidHapticCue.Selection
                                                )
                                                val targetMonth =
                                                    if (
                                                        state.filter.isSelectableMonth(
                                                            year,
                                                            state.displayedMonth,
                                                        )
                                                    ) {
                                                        state.displayedMonth
                                                    } else {
                                                        selectableMonthInYear
                                                            ?: state.displayedMonth
                                                    }
                                                state.showMonth(year, targetMonth)
                                                isSelectingYear = false
                                            },
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = year.toString(),
                                    style =
                                        MaterialTheme.typography.titleSmall.copy(
                                            fontWeight =
                                                if (isSelectedYear || isCurrentYear)
                                                    FontWeight.SemiBold
                                                else FontWeight.Medium,
                                            fontFeatureSettings = "tnum",
                                        ),
                                    color =
                                        when {
                                            isSelectedYear -> scheme.onPrimary
                                            isCurrentYear -> scheme.primary
                                            else -> colors.content
                                        },
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    labels.weekdayInitials.forEachIndexed { index, day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                            color =
                                if (index >= 5) scheme.primary.copy(alpha = 0.78f)
                                else colors.secondaryContent,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                val monthKey = state.displayedYear * 12 + (state.displayedMonth - 1)
                val dayRole =
                    if (state.selectionMode == LiquidDateSelectionMode.Multiple) Role.Checkbox
                    else Role.RadioButton
                AnimatedContent(
                    targetState = monthKey,
                    transitionSpec = LiquidDatePickerMotion.monthGridTransition(performance),
                    label = "calendarMonthGrid",
                ) { key ->
                    val gridYear = key / 12
                    val gridMonth = (key % 12) + 1
                    val gridFirstDayOffset =
                        remember(gridYear, gridMonth) {
                            LocalDate(gridYear, gridMonth, 1).dayOfWeek.ordinal
                        }
                    val gridDaysInMonth =
                        remember(gridYear, gridMonth) {
                            daysInCalendarMonth(gridYear, gridMonth)
                        }

                    // Six stable rows keep navigation from resizing the surrounding dialog.
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(6) { row ->
                            Row(Modifier.fillMaxWidth()) {
                                repeat(7) { column ->
                                    val day = row * 7 + column - gridFirstDayOffset + 1
                                    Box(
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (day in 1..gridDaysInMonth) {
                                            val cellDate = LocalDate(gridYear, gridMonth, day)
                                            val isSelected = state.isDateSelected(cellDate)
                                            val isSelectable = state.isDateSelectable(cellDate)
                                            val isMarked = state.isDateMarked(cellDate)
                                            val cellStateDescription =
                                                state.filter.dateStateDescription(cellDate)
                                            val isToday = cellDate == today
                                            val cellShape = Capsule()
                                            val hasGlassSurface = isSelected || isToday || isMarked
                                            val background by
                                                LiquidDatePickerMotion.animateColor(
                                                    targetValue =
                                                        when {
                                                            isSelected -> scheme.primary
                                                            isToday -> colors.accentContainer
                                                            isMarked -> colors.neutralContainer
                                                            else -> Color.Transparent
                                                        },
                                                    performance = performance,
                                                    label = "calendarDayColor",
                                                )
                                            Box(
                                                modifier =
                                                    Modifier.size(38.dp)
                                                        .graphicsLayer {
                                                            alpha =
                                                                when {
                                                                    isSelectable || isSelected -> 1f
                                                                    isMarked -> 0.72f
                                                                    else -> 0.30f
                                                                }
                                                        }
                                                        .semantics {
                                                            this.selected = isSelected
                                                            if (cellStateDescription != null) {
                                                                stateDescription =
                                                                    cellStateDescription
                                                            }
                                                        }
                                                        .then(
                                                            if (hasGlassSurface) {
                                                                Modifier.liquidGlass(
                                                                    backdrop = backdrop,
                                                                    shape = cellShape,
                                                                    role = LiquidGlassRole.Control,
                                                                    containerColor = background,
                                                                )
                                                            } else {
                                                                Modifier.background(
                                                                    background,
                                                                    cellShape,
                                                                )
                                                            }
                                                        )
                                                        .border(
                                                            width =
                                                                if (isToday && !isSelected) 1.5.dp
                                                                else 0.dp,
                                                            color =
                                                                if (isToday && !isSelected)
                                                                    scheme.primary
                                                                else Color.Transparent,
                                                            shape = cellShape,
                                                        )
                                                        .liquidClickable(
                                                            shape = cellShape,
                                                            enabled = isSelectable,
                                                            role = dayRole,
                                                            clipContent = true,
                                                            onClick = {
                                                                haptic.performLiquidHaptic(
                                                                    LiquidHapticCue.Selection
                                                                )
                                                                if (
                                                                    state.displayedYear ==
                                                                        gridYear &&
                                                                        state.displayedMonth ==
                                                                            gridMonth
                                                                ) {
                                                                    state.selectDay(day)
                                                                } else {
                                                                    state.showMonth(
                                                                        gridYear,
                                                                        gridMonth,
                                                                    )
                                                                    state.selectDay(day)
                                                                }
                                                            },
                                                        ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = day.toString(),
                                                    style =
                                                        MaterialTheme.typography.bodyMedium.copy(
                                                            fontWeight =
                                                                if (isSelected || isToday)
                                                                    FontWeight.SemiBold
                                                                else FontWeight.Normal,
                                                            fontFeatureSettings = "tnum",
                                                        ),
                                                    color =
                                                        when {
                                                            isSelected -> scheme.onPrimary
                                                            isToday -> scheme.primary
                                                            else -> colors.content
                                                        },
                                                )
                                                if (isMarked) {
                                                    Box(
                                                        modifier =
                                                            Modifier.align(Alignment.BottomCenter)
                                                                .padding(bottom = 4.dp)
                                                                .size(4.dp)
                                                                .background(
                                                                    if (isSelected)
                                                                        scheme.onPrimary
                                                                    else scheme.primary,
                                                                    CircleShape,
                                                                )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun daysInCalendarMonth(year: Int, month: Int): Int =
    when (month) {
        2 -> if (year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)) 29 else 28
        4,
        6,
        9,
        11 -> 30
        else -> 31
    }

/** LiquidDatePickerField - Liquid Glass Date Input Trigger Field. */
@Composable
fun LiquidDatePickerField(
    selectedDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    labels: LiquidDatePickerLabels = LiquidDatePickerLabels.Default,
    label: String = labels.fieldLabel,
    placeholder: String = labels.fieldPlaceholder,
    enabled: Boolean = true,
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
) {
    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)
    val shape = RoundedRectangle(20.dp)
    val borderColor =
        if (enabled) glassColors.outline
        else glassColors.outline.copy(alpha = glassColors.outline.alpha * 0.55f)

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val useCompactMonth = maxWidth < 215.dp
        val formattedDate =
            remember(selectedDate, useCompactMonth, labels) {
                selectedDate?.let { date ->
                    val month =
                        if (useCompactMonth) labels.shortMonthNames[date.month.number - 1]
                        else labels.monthNames[date.month.number - 1]
                    "${date.day} $month ${date.year}"
                }
            }

        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .liquidControlInteractive(
                        enabled = enabled,
                        interactiveHighlight = interactiveHighlight,
                        shape = shape,
                        backdrop = effectiveBackdrop,
                        onClick = onClick,
                    )
                    .border(width = 1.dp, color = borderColor, shape = shape)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = LiquidIcons.Calendar,
                contentDescription = label,
                tint = if (enabled) colorScheme.primary else glassColors.disabledContent,
                modifier = Modifier.size(20.dp),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                if (formattedDate != null) {
                    Text(
                        text = label,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                            ),
                        color =
                            if (enabled) glassColors.secondaryContent
                            else glassColors.disabledContent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = formattedDate,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFeatureSettings = "tnum",
                            ),
                        color = if (enabled) colorScheme.onSurface else glassColors.disabledContent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Text(
                        text = placeholder,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                            ),
                        color = glassColors.secondaryContent.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = if (enabled) glassColors.secondaryContent else glassColors.disabledContent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
