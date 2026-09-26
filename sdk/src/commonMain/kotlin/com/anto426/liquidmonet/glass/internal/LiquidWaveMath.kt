package com.anto426.liquidmonet.glass.internal

import kotlin.math.PI

/** One process-wide lookup shared by every animated background. */
internal object LiquidWaveMath {
    private const val SampleCount = 4096
    private const val TwoPi = (2.0 * PI).toFloat()
    private const val HalfPi = (0.5 * PI).toFloat()

    private val samples: FloatArray by lazy {
        FloatArray(SampleCount + 1).also { table ->
            if (!fillNativeSineLookup(table)) {
                val step = TwoPi / SampleCount
                for (index in 0 until SampleCount) {
                    table[index] = kotlin.math.sin(step * index)
                }
                table[SampleCount] = table[0]
            }
        }
    }

    fun sin(phase: Float): Float {
        if (!phase.isFinite()) return 0f
        var wrapped = phase
        if (wrapped < 0f || wrapped >= TwoPi) wrapped %= TwoPi
        if (wrapped < 0f) wrapped += TwoPi
        val position = wrapped * SampleCount / TwoPi
        val index = position.toInt().coerceIn(0, SampleCount - 1)
        val fraction = (position - index).coerceIn(0f, 1f)
        val table = samples
        return table[index] + (table[index + 1] - table[index]) * fraction
    }

    fun cos(phase: Float): Float = sin(phase + HalfPi)
}

/** Native C11 initializes the table once; Kotlin fallback preserves platform availability. */
internal expect fun fillNativeSineLookup(samples: FloatArray): Boolean
