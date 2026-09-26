package com.kyant.backdrop

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackdropSamplingBudgetTest {
    @Test
    fun compactGlassKeepsRequestedSampling() {
        assertEquals(0.85f, backdropSamplingScale(300f, 200f, 0.85f))
    }

    @Test
    fun largeGlassCapsOnlyTheOpticalTexture() {
        val scale = backdropSamplingScale(1440f, 3200f, 1f)
        assertTrue(scale in 0.6f..0.7f)
        assertTrue(1440 * scale * 3200 * scale <= 2_000_100f)
    }

    @Test
    fun invalidSizeDoesNotChangeScale() {
        assertEquals(0.67f, backdropSamplingScale(0f, 3200f, 0.67f))
    }
}
