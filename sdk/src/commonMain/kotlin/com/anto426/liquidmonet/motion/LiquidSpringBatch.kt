package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.SpringSpec
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/** One reusable buffer and one native call for all channels of an interactive control. */
internal class LiquidSpringBatch(private val count: Int) {
    init {
        require(count in 1..64)
    }

    private val channels = FloatArray(count * Stride)

    fun initialize(index: Int, value: Float) {
        require(index in 0 until count && value.isFinite())
        val offset = index * Stride
        channels[offset] = value
        channels[offset + 2] = value
    }

    fun configure(index: Int, spec: FiniteAnimationSpec<Float>, threshold: Float) {
        require(index in 0 until count && threshold.isFinite() && threshold > 0f)
        val offset = index * Stride
        channels[offset + 5] = threshold
        if (spec is SnapSpec) {
            channels[offset + 3] = -1f
            channels[offset + 4] = 0f
            channels[offset + 6] = 3f
            setTarget(index, target(index))
            return
        }
        require(spec is SpringSpec) {
            "Interactive channels require a spring or snap specification"
        }
        val damping = spec.dampingRatio.toDouble()
        val frequency = sqrt(spec.stiffness.toDouble())
        when {
            damping < 1.0 -> {
                channels[offset + 3] = (-damping * frequency).toFloat()
                channels[offset + 4] = (frequency * sqrt(1.0 - damping * damping)).toFloat()
                channels[offset + 6] = 0f
            }
            damping == 1.0 -> {
                channels[offset + 3] = -frequency.toFloat()
                channels[offset + 4] = 0f
                channels[offset + 6] = 1f
            }
            else -> {
                val root = sqrt(damping * damping - 1.0)
                channels[offset + 3] = (-frequency / (damping + root)).toFloat()
                channels[offset + 4] = (-frequency * (damping + root)).toFloat()
                channels[offset + 6] = 2f
            }
        }
    }

    fun setTarget(index: Int, value: Float) {
        require(index in 0 until count && value.isFinite())
        val offset = index * Stride
        channels[offset + 2] = value
        if (channels[offset + 6] == 3f) {
            channels[offset] = value
            channels[offset + 1] = 0f
        }
    }

    fun value(index: Int): Float = channels[index * Stride]

    fun velocity(index: Int): Float = channels[index * Stride + 1]

    fun target(index: Int): Float = channels[index * Stride + 2]

    fun step(seconds: Float): Boolean {
        require(seconds.isFinite() && seconds >= 0f)
        val moving = stepNativeSprings(channels, seconds)
        return (if (moving >= 0) moving else stepLiquidSprings(channels, seconds)) > 0
    }

    internal companion object {
        const val Stride = 7
    }
}

/** Equivalent analytic solution when native code is unavailable; never an Euler approximation. */
internal fun stepLiquidSprings(channels: FloatArray, seconds: Float): Int {
    require(channels.isNotEmpty() && channels.size % LiquidSpringBatch.Stride == 0)
    require(seconds.isFinite() && seconds >= 0f)
    var moving = 0
    val t = seconds.toDouble()
    for (offset in channels.indices step LiquidSpringBatch.Stride) {
        val y = channels[offset].toDouble() - channels[offset + 2]
        val v = channels[offset + 1].toDouble()
        val a = channels[offset + 3].toDouble()
        val b = channels[offset + 4].toDouble()
        val threshold = channels[offset + 5]
        val mode = channels[offset + 6]
        var nextY = 0.0
        var nextV = 0.0
        if (mode != 3f && (abs(y) > threshold || abs(v) > threshold * 62.5)) {
            when (mode) {
                0f -> {
                    val e = exp(a * t)
                    val s = sin(b * t)
                    val cosine = cos(b * t)
                    val q = (v - a * y) / b
                    nextY = e * (y * cosine + q * s)
                    nextV = a * nextY + e * (-y * b * s + q * b * cosine)
                }
                1f -> {
                    val e = exp(a * t)
                    val q = v - a * y
                    nextY = (y + q * t) * e
                    nextV = q * e + a * nextY
                }
                2f -> {
                    val q = (v - b * y) / (a - b)
                    val first = q * exp(a * t)
                    val second = (y - q) * exp(b * t)
                    nextY = first + second
                    nextV = a * first + b * second
                }
                else -> error("Invalid spring mode")
            }
        }
        if (abs(nextY) <= threshold && abs(nextV) <= threshold * 62.5) {
            channels[offset] = channels[offset + 2]
            channels[offset + 1] = 0f
        } else {
            channels[offset] = (channels[offset + 2] + nextY).toFloat()
            channels[offset + 1] = nextV.toFloat()
            moving++
        }
    }
    return moving
}

/** Returns -1 when unavailable; callers retain the same Kotlin physics. */
internal expect fun stepNativeSprings(channels: FloatArray, seconds: Float): Int
