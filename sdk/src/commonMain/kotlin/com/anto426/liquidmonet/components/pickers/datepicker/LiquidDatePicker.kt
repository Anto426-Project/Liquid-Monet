package com.anto426.liquidmonet.components.pickers.datepicker

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.pickers.datepicker.motion.LiquidDatePickerMotion
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerState
import com.anto426.liquidmonet.components.pickers.datepicker.state.currentLocalDate
import com.anto426.liquidmonet.components.pickers.datepicker.state.monthNames
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

@Composable
fun rememberLiquidDatePickerState(
    initialDate: LocalDate = currentLocalDate()
): LiquidDatePickerState {
    return remember { LiquidDatePickerState(initialDate) }
}

/** Inline calendar: one glass surface, aligned columns and one coherent selected-day style. */
@Composable
fun LiquidDatePicker(
    state: LiquidDatePickerState,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val scheme = MaterialTheme.colorScheme
    val colors = LiquidGlassTheme.colors
    val backdrop = resolveLiquidGlassBackdrop(backdropState)
    val performance = LocalLiquidGlassPerformance.current
    val today = remember { currentLocalDate() }
    val shape = RoundedRectangle(24.dp)
    Column(
        modifier
            .fillMaxWidth()
            .liquidGlass(
                backdrop,
                shape,
                role = LiquidGlassRole.Surface,
                containerColor = colors.neutralContainer,
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = state.monthName,
                transitionSpec = LiquidDatePickerMotion.contentTransition(performance),
                modifier = Modifier.weight(1f),
                label = "calendarMonth",
            ) { title ->
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LiquidIconButton(
                LiquidIcons.ChevronLeft,
                state::previousMonth,
                contentDescription = "Mese precedente",
                variant = LiquidIconButtonVariant.Ghost,
                size = 44.dp,
            )
            LiquidIconButton(
                LiquidIcons.ChevronRight,
                state::nextMonth,
                contentDescription = "Mese successivo",
                variant = LiquidIconButtonVariant.Ghost,
                size = 44.dp,
            )
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("L", "M", "M", "G", "V", "S", "D").forEach { day ->
                Text(
                    day,
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryContent,
                    textAlign = TextAlign.Center,
                )
            }
        }
        // Six stable rows keep navigation from resizing the surrounding dialog.
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(6) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { column ->
                        val day = row * 7 + column - state.firstDayOffset + 1
                        Box(
                            Modifier.weight(1f).height(44.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (day in 1..state.daysInMonth) {
                                val selected = state.isSelected(day)
                                val isToday =
                                    today.year == state.displayedYear &&
                                        today.month.number == state.displayedMonth &&
                                        today.day == day
                                val background by
                                    LiquidDatePickerMotion.animateColor(
                                        if (selected) scheme.primary else Color.Transparent,
                                        performance,
                                        "calendarDayColor",
                                    )
                                val cellShape = Capsule()
                                Box(
                                    Modifier.sizeIn(maxWidth = 40.dp, maxHeight = 40.dp)
                                        .aspectRatio(1f)
                                        .semantics { this.selected = selected }
                                        .liquidControlInteractive(
                                            true,
                                            rememberLiquidControlHighlight(),
                                            cellShape,
                                            onClick = { state.selectDay(day) },
                                        )
                                        .background(background, cellShape)
                                        .border(
                                            1.dp,
                                            if (isToday && !selected) colors.focusIndicator
                                            else Color.Transparent,
                                            cellShape,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        day.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight =
                                            if (selected || isToday) FontWeight.SemiBold
                                            else FontWeight.Normal,
                                        color = if (selected) scheme.onPrimary else colors.content,
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

/** LiquidDatePickerField - Liquid Glass Date Input Trigger Field. */
@Composable
fun LiquidDatePickerField(
    selectedDate: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Seleziona Data",
    placeholder: String = "GG/MM/AAAA",
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val formattedDate =
        remember(selectedDate) {
            if (selectedDate != null) {
                val month = monthNames[selectedDate.month.number - 1]
                "${selectedDate.day} $month ${selectedDate.year}"
            } else null
        }

    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .liquidControlInteractive(
                    enabled,
                    interactiveHighlight,
                    RoundedRectangle(18.dp),
                    backdrop = effectiveBackdrop,
                    onClick = onClick,
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = LiquidIcons.Calendar,
                    contentDescription = label,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp),
                )

                Column {
                    if (formattedDate != null) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = LiquidGlassTheme.colors.secondaryContent,
                        )
                        Text(
                            text = formattedDate,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                            color = colorScheme.onSurface,
                        )
                    } else {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = LiquidGlassTheme.colors.secondaryContent.copy(alpha = 0.72f),
                        )
                    }
                }
            }

            Icon(
                imageVector = LiquidIcons.ChevronRight,
                contentDescription = null,
                tint = LiquidGlassTheme.colors.secondaryContent,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
