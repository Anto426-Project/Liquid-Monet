package com.anto426.liquidmonet.glass.internal

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import com.anto426.liquidmonet.nativewave.liquid_wave_fill_sine

@OptIn(ExperimentalForeignApi::class)
internal actual fun fillNativeSineLookup(samples: FloatArray): Boolean {
    if (samples.size < 17) return false
    return samples.usePinned { pinned ->
        liquid_wave_fill_sine(pinned.addressOf(0), samples.size - 1) != 0
    }
}
