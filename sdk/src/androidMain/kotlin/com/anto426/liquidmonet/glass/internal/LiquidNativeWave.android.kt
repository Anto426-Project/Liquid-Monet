package com.anto426.liquidmonet.glass.internal

internal actual fun fillNativeSineLookup(samples: FloatArray): Boolean =
    LiquidNativeWave.fill(samples)

/** Kept by the SDK consumer rules because the entry point is resolved by JNI name. */
internal object LiquidNativeWave {
    private val loaded: Boolean by lazy {
        runCatching { System.loadLibrary("liquidwave") }.isSuccess
    }

    fun fill(samples: FloatArray): Boolean =
        loaded && runCatching { fillSineLookup(samples) }.getOrDefault(false)

    private external fun fillSineLookup(samples: FloatArray): Boolean
}
