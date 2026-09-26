package com.anto426.liquidmonet.motion

import androidx.compose.runtime.BroadcastFrameClock
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import kotlin.test.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class LiquidNavigationFrameTest {
    @Test
    fun reducedMotionPanelReleaseResetsImmediatelyWithoutRequestingFrames() = runTest {
        val clock = BroadcastFrameClock()
        val drag = LiquidElasticDrag(CoroutineScope(backgroundScope.coroutineContext + clock), 500f)
        drag.dragBy(120f)
        drag.release(LiquidDragMotion.panelReturn(LiquidGlassPerformanceState.Fallback))
        assertTrue(clock.hasAwaiters)
        drag.release(
            LiquidDragMotion.panelReturn(
                LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f)
            )
        )
        runCurrent()
        assertEquals(0f, drag.rawOffset)
        assertFalse(clock.hasAwaiters)
        drag.dragBy(25f)
        assertEquals(25f, drag.rawOffset, "The next gesture must start from the reset position")
        drag.release(
            LiquidDragMotion.panelReturn(
                LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f)
            )
        )
        assertEquals(0f, drag.rawOffset)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun densePointerInputUsesOneFrameJobAndKeepsTheLatestTarget() = runTest {
        val clock = BroadcastFrameClock()
        val motion =
            createLiquidNavigationSelectionMotion(
                CoroutineScope(backgroundScope.coroutineContext + clock),
                { LiquidGlassPerformanceState.Fallback },
                0,
                5,
            )
        repeat(10_000) { motion.updateValue((it % 5).toFloat()) }
        assertEquals(4f, motion.targetValue)
        assertEquals(1, motion.frameJobStarts)
        repeat(400) { frame ->
            clock.sendFrame(frame * 8_333_333L)
            runCurrent()
        }
        assertEquals(4f, motion.value)
        assertFalse(clock.hasAwaiters, "A settled control must stop requesting frames")
    }

    @Test
    fun aNewPressCancelsPendingReleaseAndCanThenSettle() = runTest {
        val clock = BroadcastFrameClock()
        val motion =
            createLiquidNavigationSelectionMotion(
                CoroutineScope(backgroundScope.coroutineContext + clock),
                { LiquidGlassPerformanceState.Fallback },
                0,
                3,
            )
        motion.animateToValue(2f)
        repeat(6) {
            clock.sendFrame(it * 16_000_000L)
            runCurrent()
        }
        val position = motion.value
        motion.press()
        assertEquals(position, motion.value)
        repeat(60) {
            clock.sendFrame((it + 6) * 16_000_000L)
            runCurrent()
        }
        assertEquals(LiquidDragMotion.NavigationPressedScale, motion.scaleX, 0.002f)
        motion.release()
        repeat(150) {
            clock.sendFrame((it + 66) * 16_000_000L)
            runCurrent()
        }
        assertEquals(1f, motion.scaleX)
        assertEquals(1f, motion.scaleY)
        assertEquals(0f, motion.pressProgress)
        assertFalse(clock.hasAwaiters)
    }

    @Test
    fun aSingleItemReleaseAndReducedMotionCannotLeaveAnAnimationRunning() = runTest {
        val clock = BroadcastFrameClock()
        var performance = LiquidGlassPerformanceState.Fallback
        val motion =
            createLiquidNavigationSelectionMotion(
                CoroutineScope(backgroundScope.coroutineContext + clock),
                { performance },
                0,
                1,
            )
        motion.press()
        repeat(10) {
            clock.sendFrame(it * 16_000_000L)
            runCurrent()
        }
        performance = performance.copy(motionScale = 0f)
        motion.release()
        repeat(5) {
            clock.sendFrame((it + 10) * 16_000_000L)
            runCurrent()
        }
        assertEquals(1f, motion.scaleX)
        assertEquals(0f, motion.pressProgress)
        assertFalse(clock.hasAwaiters)
    }
}
