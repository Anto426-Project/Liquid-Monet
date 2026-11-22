package com.anto426.liquidmonet.components.feedback.progressbar.motion

import androidx.compose.animation.core.CubicBezierEasing
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
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.motion.animateLiquidFloatAsState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Motion owned by LiquidProgressBar; no rendering or application state. */
internal object LiquidProgressBarMotion {
    private val FluidWaveTravelEasing = CubicBezierEasing(0.36f, 0.0f, 0.24f, 1.0f)

    @Immutable
    internal data class CircularArcState(
        val startAngle: Float,
        val sweepAngle: Float,
    )

    @Composable
    fun animateFloat(
        targetValue: Float,
        performance: LiquidGlassPerformanceState,
        label: String,
    ): State<Float> =
        animateLiquidFloatAsState(
            targetValue,
            remember(performance) { LiquidMotion.interactiveSpring(performance) },
            label,
        )

    @Composable
    fun rememberWavePhase(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1750, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )

    @Composable
    fun rememberShimmerOffset(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 2100, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )

    @Composable
    fun rememberIndeterminateTravel(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1680, easing = FluidWaveTravelEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )

    @Composable
    fun rememberRotation(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1150, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )

    @Composable
    fun rememberSweepCycle(
        transition: InfiniteTransition,
        initialValue: Float,
        targetValue: Float,
        label: String,
    ): State<Float> =
        transition.animateFloat(
            initialValue = initialValue,
            targetValue = targetValue,
            animationSpec =
                remember {
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1450, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart,
                    )
                },
            label = label,
        )

    /**
     * Computes a C-infinity seamless organic circular arc where both leading head and trailing tail
     * move strictly clockwise at all instants, stretching and contracting like liquid surface
     * tension without mechanical direction reversals.
     */
    internal fun computeIndeterminateCircularArc(
        baseRotationDegrees: Float,
        sweepCycleFraction: Float,
    ): CircularArcState {
        val theta = sweepCycleFraction * (2.0 * PI)
        val tailOffset = 56.0 * sin(theta - 0.85 * PI)
        val headOffset = 155.0 + 56.0 * sin(theta)
        val rawStart = (baseRotationDegrees + tailOffset).toFloat()
        val normalizedStart = ((rawStart % 360f) + 360f) % 360f
        val sweep = (headOffset - tailOffset).toFloat().coerceIn(42f, 272f)
        return CircularArcState(startAngle = normalizedStart, sweepAngle = sweep)
    }

    /**
     * Smoothstep Hermite envelope that gently pins wave amplitude to zero at both endpoints so
     * fluid ribbons settle cleanly into the track without wobbling ends.
     */
    internal fun waveEdgeEnvelope(
        position: Float,
        start: Float,
        end: Float,
        edgeFade: Float,
    ): Float {
        if (end <= start) return 0f
        val fade = edgeFade.coerceAtLeast(1f).coerceAtMost((end - start) * 0.5f)
        val fadeIn = smoothstep(start, start + fade, position)
        val fadeOut = smoothstep(end, end - fade, position)
        return fadeIn * fadeOut
    }

    internal fun determinateWaveActivity(progress: Float): Float {
        val clamped = progress.coerceIn(0f, 1f)
        return smoothstep(0.0f, 0.08f, clamped) * smoothstep(1.0f, 0.94f, clamped)
    }

    internal fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
        if (edge0 == edge1) return if (x >= edge1) 1f else 0f
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    /** Auxiliary velocity helper used by tests to verify monotonic forward motion. */
    internal fun circularTailAngularVelocityDegPerMs(sweepCycleFraction: Float): Float {
        val theta = sweepCycleFraction * (2.0 * PI)
        val baseRate = 360.0 / 1150.0
        val harmonicRate = 56.0 * (2.0 * PI / 1450.0) * cos(theta - 0.85 * PI)
        return (baseRate + harmonicRate).toFloat()
    }
}
