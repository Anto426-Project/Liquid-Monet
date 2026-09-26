package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.SpringSpec
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import kotlin.test.*
import kotlinx.coroutines.test.runTest

class LiquidMotionTest {
    @Test
    fun invalidSpringParametersResolveToFinitePositiveValues() {
        val invalid = LiquidMotion.spring<Float>(Float.NaN, Float.POSITIVE_INFINITY)
        assertTrue(invalid.dampingRatio.isFinite() && invalid.dampingRatio > 0f)
        assertTrue(invalid.stiffness.isFinite() && invalid.stiffness > 0f)
        val negative = LiquidMotion.spring<Float>(-1f, -10f)
        assertTrue(negative.dampingRatio > 0f && negative.stiffness > 0f)
    }

    @Test
    fun reducedMotionSnapsInsteadOfStartingAnElasticReturn() {
        val reduced = LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f)
        assertIs<SnapSpec<Float>>(LiquidMotion.spring<Float>(reduced))
        assertIs<SnapSpec<Float>>(LiquidMotion.tween<Float>(reduced))
    }

    @Test
    fun durationNormalizationRejectsOverflowAndInvalidDeviceScales() {
        val invalid = LiquidGlassPerformanceState.Fallback.copy(motionScale = Float.NaN)
        assertEquals(10_000, LiquidMotion.durationMillis(invalid, Int.MAX_VALUE))
        assertEquals(0, LiquidMotion.durationMillis(invalid, -100))
        assertEquals(100, LiquidMotion.durationMillis(invalid, 100))
        assertEquals(0, LiquidMotion.durationMillis(invalid.copy(motionScale = -1f), 100))
    }

    @Test
    fun extremeDeviceScaleCannotCreateAnUnboundedSpring() {
        val state = LiquidGlassPerformanceState.Fallback.copy(motionScale = Float.POSITIVE_INFINITY)
        val spring = assertIs<SpringSpec<Float>>(LiquidMotion.spring<Float>(state))
        assertTrue(spring.stiffness.isFinite())
    }

    @Test
    fun rubberBandIsSymmetricAndBounded() {
        assertEquals(0f, liquidRubberBand(0f, 40f))
        assertEquals(-liquidRubberBand(100f, 40f), liquidRubberBand(-100f, 40f))
        assertTrue(liquidRubberBand(1_000_000f, 40f) <= 40f)
        assertTrue(liquidRubberBand(10f, 40f) < 10f)
    }

    @Test
    fun navbarAndPageSelectionsUseOneNavigationMotionContract() = runTest {
        val motion =
            createLiquidNavigationSelectionMotion(
                backgroundScope,
                { LiquidGlassPerformanceState.Fallback },
                99,
                5,
            )
        assertEquals(4f, motion.initialValue)
        assertEquals(0f..4f, motion.valueRange)
        assertEquals(LiquidDragMotion.NavigationPressedScale, motion.pressedScale)
        assertEquals(1f, motion.initialScale)
        assertEquals(1f, liquidNavigationSelectionScaleX(motion))
        assertEquals(1f, liquidNavigationSelectionScaleY(motion))
    }
}
