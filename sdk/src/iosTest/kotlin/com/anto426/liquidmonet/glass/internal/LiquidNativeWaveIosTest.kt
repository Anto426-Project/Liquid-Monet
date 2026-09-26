package com.anto426.liquidmonet.glass.internal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LiquidNativeWaveIosTest {
    @Test fun c11WaveKernelFillsTheIosLookupTable() {
        val samples = FloatArray(4097)
        assertTrue(fillNativeSineLookup(samples))
        assertEquals(0f, samples[0], 0.0001f)
        assertEquals(1f, samples[1024], 0.0001f)
        assertEquals(-1f, samples[3072], 0.0001f)
        assertEquals(samples[0], samples[4096])
    }
}
