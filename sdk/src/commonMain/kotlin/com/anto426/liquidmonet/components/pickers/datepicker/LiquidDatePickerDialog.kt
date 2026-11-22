package com.anto426.liquidmonet.components.pickers.datepicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerFilter
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerLabels
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** LiquidDatePickerDialog - Dedicated Optical Liquid Glass Modal Calendar Dialog Component. */
@Composable
fun LiquidDatePickerDialog(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    initialDate: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    filter: LiquidDatePickerFilter = LiquidDatePickerFilter.All,
    labels: LiquidDatePickerLabels = rememberLiquidDatePickerLabels(),
    backdropState: Backdrop = emptyBackdrop(),
) {
    if (!isOpen) return

    val state = rememberLiquidDatePickerState(initialDate = initialDate, filter = filter)

    LiquidDialog(
        onDismissRequest = onDismissRequest,
        title = labels.dialogTitle,
        modifier = modifier,
        backdropState = backdropState,
        confirmButton = {
            LiquidButton(
                text = labels.confirmAction,
                variant = LiquidButtonVariant.Primary,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    state.selectedDate?.let { onDateSelected(it) }
                    onDismissRequest()
                },
            )
        },
        dismissButton = {
            LiquidButton(
                text = labels.cancelAction,
                variant = LiquidButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismissRequest,
            )
        },
    ) {
        LiquidDatePicker(
            state = state,
            labels = labels,
            backdropState = backdropState,
        )
    }
}
