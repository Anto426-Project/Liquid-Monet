package com.anto426.liquidmonet.glass.internal

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertTrue

class LiquidWaveMathTest {
    @Test
    fun lookupMatchesTrigonometryAcrossTwoPeriods() {
        for (index in -1000..1000) {
            val phase = index * (PI / 317.0).toFloat()
            assertTrue(abs(LiquidWaveMath.sin(phase) - sin(phase)) < 0.00002f)
            assertTrue(abs(LiquidWaveMath.cos(phase) - cos(phase)) < 0.00002f)
        }
    }

    @Test
    fun invalidPhaseIsFinite() {
        assertTrue(LiquidWaveMath.sin(Float.NaN) == 0f)
        assertTrue(LiquidWaveMath.cos(Float.POSITIVE_INFINITY) == 0f)
    }
}
