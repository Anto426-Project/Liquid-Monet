package com.anto426.liquidmonet.components.pickers.datepicker

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
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
    backdropState: Backdrop = emptyBackdrop(),
) {
    if (!isOpen) return

    val state = rememberLiquidDatePickerState(initialDate)

    LiquidDialog(
        onDismissRequest = onDismissRequest,
        title = "Seleziona Data",
        modifier = modifier,
        backdropState = backdropState,
        confirmButton = {
            LiquidButton(
                text = "Conferma",
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
                text = "Annulla",
                variant = LiquidButtonVariant.Secondary,
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismissRequest,
            )
        },
    ) {
        LiquidDatePicker(
            state = state,
            backdropState = backdropState,
        )
    }
}
