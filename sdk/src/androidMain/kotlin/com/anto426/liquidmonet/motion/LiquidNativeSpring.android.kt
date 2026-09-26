package com.anto426.liquidmonet.motion

import com.anto426.liquidmonet.glass.internal.LiquidNativeWave

internal actual fun stepNativeSprings(channels: FloatArray, seconds: Float): Int =
    if (LiquidNativeWave.loaded) LiquidNativeSpring.step(channels, seconds) else -1

/** Protected JNI entry point. A single call advances every channel in the control. */
internal object LiquidNativeSpring {
    private var available = true

    fun step(channels: FloatArray, seconds: Float): Int {
        if (!available) return -1
        return try {
            stepBatch(channels, seconds)
        } catch (_: LinkageError) {
            available = false
            -1
        }
    }

    private external fun stepBatch(channels: FloatArray, seconds: Float): Int
}
