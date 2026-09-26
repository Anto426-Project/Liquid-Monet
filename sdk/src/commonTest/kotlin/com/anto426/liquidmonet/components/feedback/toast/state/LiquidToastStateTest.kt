package com.anto426.liquidmonet.components.feedback.toast.state

import kotlin.test.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class LiquidToastStateTest {
    @Test
    fun duplicateCurrentAndPendingMessagesAreSuppressed() = runTest {
        val state = LiquidToastState(backgroundScope)
        state.show("First")
        state.show("First")
        state.show("Second")
        state.show("Second")
        runCurrent()
        assertEquals("First", state.currentToast?.message)
        state.dismiss()
        runCurrent()
        assertEquals("Second", state.currentToast?.message)
        state.dismiss()
        runCurrent()
        assertNull(state.currentToast)
    }

    @Test
    fun invalidDurationsAreBoundedAndBlankMessagesDoNotQueue() = runTest {
        val state = LiquidToastState(backgroundScope)
        state.show(" ")
        runCurrent()
        assertNull(state.currentToast)
        state.show("Short", durationMillis = -1)
        state.show("Long", durationMillis = Long.MAX_VALUE)
        runCurrent()
        assertEquals(900L, state.currentToast?.durationMillis)
        state.dismiss()
        runCurrent()
        assertEquals(30_000L, state.currentToast?.durationMillis)
    }

    @Test
    fun cancelledTimerCannotDismissTheFollowingToast() = runTest {
        val state = LiquidToastState(backgroundScope)
        state.show("First", durationMillis = 900)
        state.show("Second", durationMillis = 5000)
        runCurrent()
        advanceTimeBy(100)
        state.dismiss()
        runCurrent()
        advanceTimeBy(900)
        runCurrent()
        assertEquals("Second", state.currentToast?.message)
        advanceTimeBy(4100)
        runCurrent()
        assertNull(state.currentToast)
    }

    @Test
    fun producerFloodRetainsOnlyTheLatestBoundedPendingQueue() = runTest {
        val state = LiquidToastState(backgroundScope)
        repeat(100) { state.show("Toast $it") }
        runCurrent()
        assertEquals("Toast 0", state.currentToast?.message)
        state.dismiss()
        runCurrent()
        assertEquals("Toast 68", state.currentToast?.message)
        repeat(32) {
            state.dismiss()
            runCurrent()
        }
        assertNull(state.currentToast)
    }
}
