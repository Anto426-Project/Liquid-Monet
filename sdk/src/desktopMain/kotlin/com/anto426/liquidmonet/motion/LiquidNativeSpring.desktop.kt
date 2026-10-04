package com.anto426.liquidmonet.motion
// The common Kotlin integrator is used when the mobile C bridge is unavailable.
internal actual fun stepNativeSprings(channels: FloatArray, seconds: Float): Int = -1
