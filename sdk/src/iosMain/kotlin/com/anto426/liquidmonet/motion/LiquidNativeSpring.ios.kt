package com.anto426.liquidmonet.motion

import com.anto426.liquidmonet.nativewave.liquid_spring_step
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned

@OptIn(ExperimentalForeignApi::class)
internal actual fun stepNativeSprings(channels: FloatArray, seconds: Float): Int {
    if (channels.isEmpty() || channels.size % LiquidSpringBatch.Stride != 0) return -1
    return channels.usePinned { pinned ->
        liquid_spring_step(pinned.addressOf(0), channels.size / LiquidSpringBatch.Stride, seconds)
    }
}
