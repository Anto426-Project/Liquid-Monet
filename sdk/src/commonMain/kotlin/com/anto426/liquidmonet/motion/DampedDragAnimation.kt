package com.anto426.liquidmonet.motion

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
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

    private val springs = LiquidSpringBatch(5)
    private var configuredPerformance: LiquidGlassPerformanceState? = null
    private var frameJob: Job? = null
    private var velocityTracking = false
    private var pendingRelease = false
    private var releaseRequestedFrame = -1L
    private var frameIndex = 0L

    internal var frameJobStarts = 0
        private set

    var value by mutableFloatStateOf(initialValue)
        private set

    var pressProgress by mutableFloatStateOf(0f)
        private set

    var scaleX by mutableFloatStateOf(initialScale)
        private set

    var scaleY by mutableFloatStateOf(initialScale)
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    init {
        springs.initialize(Value, initialValue)
        springs.initialize(Velocity, 0f)
        springs.initialize(Press, 0f)
        springs.initialize(ScaleX, initialScale)
        springs.initialize(ScaleY, initialScale)
        configureSprings()
    }

    val motionEnabled
        get() = performance().motionScale > 0f

    val targetValue
        get() = springs.target(Value)

    val progress: Float
        get() {
            val span = valueRange.endInclusive - valueRange.start
            return if (span.isFinite() && span > 0f) {
                ((value - valueRange.start) / span).coerceIn(0f, 1f)
            } else 0f
        }

    private fun configureSprings() {
        val state = performance()
        if (configuredPerformance == state) return
        configuredPerformance = state
        springs.configure(
            Value,
            LiquidDragMotion.tracking(state, visibilityThreshold),
            visibilityThreshold,
        )
        springs.configure(
            Velocity,
            LiquidDragMotion.velocity(state, visibilityThreshold * 10f),
            visibilityThreshold * 10f,
        )
        springs.configure(Press, LiquidDragMotion.tracking(state, 0.001f), 0.001f)
        springs.configure(ScaleX, LiquidDragMotion.scaleX(state), 0.001f)
        springs.configure(ScaleY, LiquidDragMotion.scaleY(state), 0.001f)
    }

    private fun publish() {
        value = springs.value(Value)
        velocity = springs.value(Velocity)
        pressProgress = springs.value(Press)
        scaleX = springs.value(ScaleX)
        scaleY = springs.value(ScaleY)
    }

    /** All pointer updates change targets synchronously; one frame loop owns the five springs. */
    private fun ensureFrames() {
        if (frameJob?.isActive == true) return
        frameJobStarts++
        frameJob =
            animationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                try {
                    var previousFrame = withFrameNanos { it }
                    while (isActive) {
                        val frame = withFrameNanos { it }
                        frameIndex++
                        configureSprings()
                        if (velocityTracking) {
                            val span = valueRange.endInclusive - valueRange.start
                            springs.setTarget(
                                Velocity,
                                if (motionEnabled && span.isFinite() && span > 0f)
                                    springs.velocity(Value) / span
                                else 0f,
                            )
                        }
                        val moving =
                            springs.step(
                                ((frame - previousFrame).coerceAtLeast(0L) / 1_000_000_000.0)
                                    .toFloat()
                            )
                        previousFrame = frame
                        if (pendingRelease && frameIndex > releaseRequestedFrame) {
                            val threshold =
                                maxOf(
                                    visibilityThreshold,
                                    (valueRange.endInclusive - valueRange.start) * 0.025f,
                                )
                            if (abs(springs.value(Value) - targetValue) <= threshold) {
                                pendingRelease = false
                                springs.setTarget(Press, 0f)
                                springs.setTarget(ScaleX, initialScale)
                                springs.setTarget(ScaleY, initialScale)
                            }
                        }
                        publish()
                        // A release may have created new targets after this frame's integration.
                        if (
                            !moving &&
                                !pendingRelease &&
                                springs.value(Press) == springs.target(Press) &&
                                springs.value(ScaleX) == springs.target(ScaleX) &&
                                springs.value(ScaleY) == springs.target(ScaleY)
                        )
                            break
                    }
                } finally {
                    frameJob = null
                }
            }
    }

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
        configureSprings()
        pendingRelease = false
        springs.setTarget(Press, 1f)
        val scale = if (motionEnabled) pressedScale else initialScale
        springs.setTarget(ScaleX, scale)
        springs.setTarget(ScaleY, scale)
        publish()
        ensureFrames()
    }

    fun release() {
        pendingRelease = true
        releaseRequestedFrame = frameIndex
        ensureFrames()
    }

    fun updateValue(value: Float) {
        if (!value.isFinite()) return
        configureSprings()
        velocityTracking = true
        springs.setTarget(Value, value.coerceIn(valueRange))
        publish()
        ensureFrames()
    }

    fun animateToValue(value: Float) {
        if (!value.isFinite()) return
        press()
        velocityTracking = false
        springs.setTarget(Value, value.coerceIn(valueRange))
        springs.setTarget(Velocity, 0f)
        release()
        publish()
    }

    private companion object {
        const val Value = 0
        const val Velocity = 1
        const val Press = 2
        const val ScaleX = 3
        const val ScaleY = 4
    }
}
