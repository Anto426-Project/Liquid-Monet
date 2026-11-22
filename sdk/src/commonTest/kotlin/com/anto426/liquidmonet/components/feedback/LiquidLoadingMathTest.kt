package com.anto426.liquidmonet.components.feedback

import com.anto426.liquidmonet.components.feedback.loading.motion.LiquidLoadingMotion
import com.anto426.liquidmonet.components.feedback.progressbar.motion.LiquidProgressBarMotion
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.Test
import kotlin.test.assertTrue

class LiquidLoadingMathTest {

    @Test
    fun circularIndeterminateArcMovesStrictlyForwardAndWrapsSeamlessly() {
        var minSweep = Float.MAX_VALUE
        var maxSweep = Float.MIN_VALUE

        for (i in 0..100) {
            val cycle = i / 100f
            val tailVelocity = LiquidProgressBarMotion.circularTailAngularVelocityDegPerMs(cycle)
            assertTrue(
                tailVelocity > 0f,
                "Circular tail angular velocity must remain strictly positive, got $tailVelocity at $cycle",
            )
            val arc = LiquidProgressBarMotion.computeIndeterminateCircularArc(
                baseRotationDegrees = cycle * 360f,
                sweepCycleFraction = cycle,
            )
            assertTrue(arc.startAngle in 0f..360f, "startAngle out of bounds: ${arc.startAngle}")
            assertTrue(arc.sweepAngle in 42f..272f, "sweepAngle out of bounds: ${arc.sweepAngle}")
            if (arc.sweepAngle < minSweep) minSweep = arc.sweepAngle
            if (arc.sweepAngle > maxSweep) maxSweep = arc.sweepAngle
        }

        assertTrue(
            maxSweep - minSweep > 180f,
            "Circular arc should breathe organically across cycle: min=$minSweep max=$maxSweep",
        )

        val startAtZero = LiquidProgressBarMotion.computeIndeterminateCircularArc(90f, 0f)
        val startAtOne = LiquidProgressBarMotion.computeIndeterminateCircularArc(90f, 1f)
        assertTrue(abs(startAtZero.startAngle - startAtOne.startAngle) < 0.01f)
        assertTrue(abs(startAtZero.sweepAngle - startAtOne.sweepAngle) < 0.01f)
    }

    @Test
    fun linearWaveEnvelopePinsEndpointsAndCalmsAtFullOrEmpty() {
        assertTrue(LiquidProgressBarMotion.waveEdgeEnvelope(0f, 0f, 200f, 30f) == 0f)
        assertTrue(LiquidProgressBarMotion.waveEdgeEnvelope(200f, 0f, 200f, 30f) == 0f)
        assertTrue(LiquidProgressBarMotion.waveEdgeEnvelope(100f, 0f, 200f, 30f) == 1f)

        assertTrue(LiquidProgressBarMotion.determinateWaveActivity(0f) == 0f)
        assertTrue(LiquidProgressBarMotion.determinateWaveActivity(1f) == 0f)
        assertTrue(LiquidProgressBarMotion.determinateWaveActivity(0.5f) == 1f)
    }

    @Test
    fun dropletSamplesAreHarmonicBoundedAndContinuous() {
        val twoPi = (2.0 * PI).toFloat()
        for (step in 0..36) {
            val phase = twoPi * (step / 36f)
            val lifts = (0..2).map { idx ->
                val sample = LiquidLoadingMotion.computeDropletSample(phase, idx)
                assertTrue(sample.lift in 0f..1f)
                assertTrue(sample.scaleX in 0.75f..1.20f)
                assertTrue(sample.scaleY in 0.75f..1.24f)
                sample.lift
            }
            val spread = lifts.max() - lifts.min()
            assertTrue(spread > 0.35f, "Droplets should maintain visible wave separation at phase=$phase")
        }

        val atZero = LiquidLoadingMotion.computeDropletSample(0f, 1)
        val atTwoPi = LiquidLoadingMotion.computeDropletSample(twoPi, 1)
        assertTrue(abs(atZero.lift - atTwoPi.lift) < 1e-4f)
        assertTrue(abs(atZero.scaleX - atTwoPi.scaleX) < 1e-4f)
        assertTrue(abs(atZero.scaleY - atTwoPi.scaleY) < 1e-4f)
    }

    @Test
    fun dualPulseRipplesMaintainContinuousVisibilityWithoutPop() {
        val atZero = LiquidLoadingMotion.computePulseRipple(0f)
        assertTrue(atZero.radiusFraction == 0f)
        assertTrue(atZero.alpha < 1e-4f)

        var prevRadius = -1f
        for (step in 0..100) {
            val t = step / 100f
            val r1 = LiquidLoadingMotion.computePulseRipple(t)
            val r2 = LiquidLoadingMotion.computePulseRipple((t + 0.5f) % 1f)
            if (step < 100) {
                assertTrue(r1.radiusFraction >= prevRadius, "Ripple radius must expand monotonically")
                prevRadius = r1.radiusFraction
            }
            assertTrue(
                max(r1.alpha, r2.alpha) > 0.25f,
                "Staggered dual ripples should always stay visibly active at t=$t",
            )
        }
    }
}
