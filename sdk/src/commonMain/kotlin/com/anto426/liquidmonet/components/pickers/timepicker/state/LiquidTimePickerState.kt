package com.anto426.liquidmonet.components.pickers.timepicker.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf

/** State holder for LiquidTimePicker. */
@Stable
class LiquidTimePickerState(
    initialHour: Int = 12,
    initialMinute: Int = 0,
) {
    private val hourState = mutableIntStateOf(normalizeClockValue(initialHour, 24))
    var hour: Int
        get() = hourState.intValue
        set(value) {
            hourState.intValue = normalizeClockValue(value, 24)
        }

    private val minuteState = mutableIntStateOf(normalizeClockValue(initialMinute, 60))
    var minute: Int
        get() = minuteState.intValue
        set(value) {
            minuteState.intValue = normalizeClockValue(value, 60)
        }

    val formattedTime: String
        get() = "${hour.twoDigits()}:${minute.twoDigits()}"

    fun incrementHour() {
        adjustHour(1)
    }

    fun decrementHour() {
        adjustHour(-1)
    }

    fun adjustHour(delta: Int) {
        hour = normalizeClockValue(hour + delta, 24)
    }

    fun incrementMinute() {
        adjustMinute(1)
    }

    fun decrementMinute() {
        adjustMinute(-1)
    }

    fun adjustMinute(delta: Int) {
        minute = normalizeClockValue(minute + delta, 60)
    }
}

private fun normalizeClockValue(value: Int, modulus: Int): Int =
    ((value % modulus) + modulus) % modulus

internal fun Int.twoDigits(): String = toString().padStart(2, '0')
