package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import kotlin.math.abs
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    private val performance: () -> LiquidGlassPerformanceState,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDragCancelled: DampedDragAnimation.() -> Unit = {},
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
) {

    constructor(
        animationScope: CoroutineScope,
        performance: LiquidGlassPerformanceState,
        initialValue: Float,
        valueRange: ClosedRange<Float>,
        visibilityThreshold: Float,
        initialScale: Float,
        pressedScale: Float,
        onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
        onDragStopped: DampedDragAnimation.() -> Unit,
        onDragCancelled: DampedDragAnimation.() -> Unit = {},
        onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
    ) : this(
        animationScope = animationScope,
        performance = { performance },
        initialValue = initialValue,
        valueRange = valueRange,
        visibilityThreshold = visibilityThreshold,
        initialScale = initialScale,
        pressedScale = pressedScale,
        onDragStarted = onDragStarted,
        onDragStopped = onDragStopped,
        onDragCancelled = onDragCancelled,
        onDrag = onDrag,
    )

    private val valueAnimationSpec
        get() = LiquidDragMotion.tracking(performance(), visibilityThreshold)

    private val velocityAnimationSpec
        get() = LiquidDragMotion.velocity(performance(), visibilityThreshold * 10f)

    private val pressProgressAnimationSpec
        get() = LiquidDragMotion.tracking(performance(), 0.001f)

    private val scaleXAnimationSpec
        get() = LiquidDragMotion.scaleX(performance())

    private val scaleYAnimationSpec
        get() = LiquidDragMotion.scaleY(performance())

    val motionEnabled
        get() = performance().motionScale > 0f

    private val valueAnimation = Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation = Animatable(0f, 5f)
    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val scaleXAnimation = Animatable(initialScale, 0.001f)
    private val scaleYAnimation = Animatable(initialScale, 0.001f)

    private val mutatorMutex = MutatorMutex()
    private val velocityTracker = VelocityTracker()
    private var pressJob: Job? = null
    private var releaseJob: Job? = null

    val value: Float
        get() = valueAnimation.value

    val progress: Float
        get() {
            val span = valueRange.endInclusive - valueRange.start
            return if (span.isFinite() && span > 0f) {
                ((value - valueRange.start) / span).coerceIn(0f, 1f)
            } else {
                0f
            }
        }

    val targetValue: Float
        get() = valueAnimation.targetValue

    val pressProgress: Float
        get() = pressProgressAnimation.value

    val scaleX: Float
        get() = scaleXAnimation.value

    val scaleY: Float
        get() = scaleYAnimation.value

    val velocity: Float
        get() = velocityAnimation.value

    val modifier: Modifier =
        Modifier.pointerInput(Unit) {
            inspectDragGestures(
                onDragStart = { down ->
                    startDrag(down.position)
                },
                onDragEnd = {
                    stopDrag(cancelled = false)
                },
                onDragCancel = {
                    stopDrag(cancelled = true)
                },
            ) { change, dragAmount ->
                dragBy(size, dragAmount)
            }
        }

    fun startDrag(position: Offset) {
        onDragStarted(position)
        press()
    }

    fun dragBy(size: IntSize, dragAmount: Offset) = onDrag(size, dragAmount)

    fun stopDrag(cancelled: Boolean) {
        if (cancelled) onDragCancelled() else onDragStopped()
        release()
    }

    fun press() {
        releaseJob?.cancel()
        pressJob?.cancel()
        velocityTracker.resetTracking()
        pressJob = animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            val scale = if (motionEnabled) pressedScale else initialScale
            launch { scaleXAnimation.animateTo(scale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(scale, scaleYAnimationSpec) }
        }
    }

    fun release() {
        releaseJob?.cancel()
        releaseJob = animationScope.launch {
            withFrameNanos {}
            if (value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - valueAnimation.targetValue) < threshold }
                    .first()
            }
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
        }
    }

    fun updateValue(value: Float) {
        val targetValue = value.coerceIn(valueRange)
        animationScope.launch {
            launch {
                valueAnimation.animateTo(targetValue, valueAnimationSpec) { updateVelocity() }
            }
        }
    }

    fun animateToValue(value: Float) {
        animationScope.launch {
            mutatorMutex.mutate {
                press()
                val targetValue = value.coerceIn(valueRange)
                launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) }
                if (velocity != 0f) {
                    launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
                }
                release()
            }
        }
    }

    private fun updateVelocity() {
        velocityTracker.addPosition(
            Clock.System.now().toEpochMilliseconds(),
            Offset(value, 0f),
        )
        val span = valueRange.endInclusive - valueRange.start
        if (!span.isFinite() || span <= 0f) return
        val targetVelocity = if (motionEnabled) velocityTracker.calculateVelocity().x / span else 0f
        animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
    }
}
