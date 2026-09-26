package com.anto426.liquidmonet.motion

import kotlin.test.*

class LiquidNativeSpringIosTest {
    @Test
    fun nativeBatchMatchesThePortableFallbackIncludingRetargets() {
        val native =
            floatArrayOf(
                0f,
                0f,
                1f,
                -3f,
                4f,
                0.001f,
                0f,
                0f,
                0f,
                1f,
                -5f,
                0f,
                0.001f,
                1f,
                0f,
                0f,
                1f,
                -2f,
                -8f,
                0.001f,
                2f,
            )
        val fallback = native.copyOf()
        repeat(200) { frame ->
            if (frame == 15) {
                native[2] = -2f
                fallback[2] = -2f
            }
            assertEquals(
                stepLiquidSprings(fallback, 1f / 120f),
                stepNativeSprings(native, 1f / 120f),
            )
            native.indices.forEach { assertEquals(fallback[it], native[it], 0.00001f) }
        }
    }
}
