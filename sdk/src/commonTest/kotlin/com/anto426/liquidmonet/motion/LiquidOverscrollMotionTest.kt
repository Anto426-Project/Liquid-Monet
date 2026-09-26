package com.anto426.liquidmonet.motion

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import kotlin.test.*
import kotlinx.coroutines.test.runTest

class LiquidOverscrollMotionTest {
    @Test
    fun consumedScrollingDoesNotDeformTheViewport() = runTest {
        val effect =
            LiquidOverscrollMotion(Orientation.Vertical, 40f, backgroundScope) {
                LiquidGlassPerformanceState.Fallback
            }
        var calls = 0
        val delta = Offset(3f, 100f)
        val consumed =
            effect.applyToScroll(delta, NestedScrollSource.UserInput) {
                calls++
                it
            }
        assertEquals(1, calls)
        assertEquals(delta, consumed)
        assertEquals(0f, effect.displacement)
    }

    @Test
    fun edgePullIsBoundedAndReversalRelaxesBeforeScrolling() = runTest {
        val effect =
            LiquidOverscrollMotion(Orientation.Horizontal, 40f, backgroundScope) {
                LiquidGlassPerformanceState.Fallback
            }
        effect.applyToScroll(Offset(100f, 0f), NestedScrollSource.UserInput) { Offset.Zero }
        assertTrue(effect.displacement in 0f..40f)
        var delivered = Offset.Zero
        effect.applyToScroll(Offset(-120f, 0f), NestedScrollSource.UserInput) {
            delivered = it
            it
        }
        assertEquals(Offset(-20f, 0f), delivered)
        assertEquals(0f, effect.displacement)
    }

    @Test
    fun reducedMotionAndProgrammaticScrollNeverAccumulateDeformation() = runTest {
        var state = LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f)
        val effect = LiquidOverscrollMotion(Orientation.Vertical, 40f, backgroundScope) { state }
        var calls = 0
        effect.applyToScroll(Offset(0f, 100f), NestedScrollSource.UserInput) {
            calls++
            Offset.Zero
        }
        assertEquals(1, calls)
        assertEquals(0f, effect.displacement)
        state = state.copy(motionScale = 1f)
        effect.applyToScroll(Offset(0f, 100f), NestedScrollSource.SideEffect) { Offset.Zero }
        assertEquals(0f, effect.displacement)
    }
}
