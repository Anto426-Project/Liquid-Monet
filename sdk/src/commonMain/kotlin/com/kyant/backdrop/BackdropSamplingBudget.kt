package com.kyant.backdrop

import kotlin.math.sqrt

/** Caps the sampled optical texture; foreground content still renders at full resolution. */
internal fun backdropSamplingScale(
    width: Float,
    height: Float,
    requestedScale: Float
): Float {
    if (width <= 0f || height <= 0f || !width.isFinite() || !height.isFinite()) {
        return requestedScale
    }
    val pixelArea = width.toDouble() * height.toDouble()
    val areaScale = sqrt(2_000_000.0 / pixelArea).toFloat()
    return minOf(requestedScale, areaScale).coerceAtLeast(0.25f)
}
