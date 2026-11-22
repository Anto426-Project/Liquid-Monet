package com.anto426.liquidmonet.components.feedback.loading.motion

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal object LiquidLoadingMotion {
    @Immutable
    internal data class DropletSample(
        val lift: Float,
        val scaleX: Float,
        val scaleY: Float,
    )

    @Immutable
    internal data class RippleSample(
        val radiusFraction: Float,
        val alpha: Float,
        val strokeFraction: Float,
    )

    @Composable
    fun rememberDropletsWavePhase(
        transition: InfiniteTransition,
        initialValue: Float = 0f,
        targetValue: Float = (2.0 * PI).toFloat(),
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1320, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )

    @Composable
    fun rememberPulseProgress(
        transition: InfiniteTransition,
        initialValue: Float = 0f,
        targetValue: Float = 1f,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1850, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )

    /**
     * Harmonic travelling droplet wave with subtle fluid squash-and-stretch.
     * Each droplet is phase-shifted by 2π/3 along a continuous C-infinity cycle.
     */
    internal fun computeDropletSample(phaseRadians: Float, index: Int): DropletSample {
        val angle = phaseRadians - index * (2.0 * PI / 3.0)
        val sinVal = sin(angle).toFloat()
        val cosVal = cos(angle).toFloat()
        val lift = (sinVal + 1f) * 0.5f
        val baseScale = 0.86f + 0.24f * lift
        val stretch = abs(cosVal)
        return DropletSample(
            lift = lift,
            scaleX = (baseScale - 0.06f * stretch).coerceIn(0.75f, 1.20f),
            scaleY = (baseScale + 0.08f * stretch).coerceIn(0.75f, 1.24f),
        )
    }

    /**
     * Computes a single expanding optical liquid ripple for normalized progress `t in [0, 1]`.
     * Uses quadratic ease-out expansion and a bell-curve opacity fade so dual-staggered ripples
     * emerge and dissolve with zero pop.
     */
    internal fun computePulseRipple(progress: Float): RippleSample {
        val t = ((progress % 1f) + 1f) % 1f
        val easedRadius = 1f - (1f - t) * (1f - t)
        val bell = sin(t * PI).toFloat()
        val alpha = (bell * (1f - 0.42f * t)).coerceIn(0f, 1f)
        val strokeFraction = (1f - 0.65f * easedRadius).coerceIn(0.30f, 1f)
        return RippleSample(
            radiusFraction = easedRadius,
            alpha = alpha,
            strokeFraction = strokeFraction,
        )
    }
}
