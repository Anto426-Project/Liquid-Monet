package com.anto426.liquidmonet.components.internal.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Owns press, pointer tracking and cancellation; the highlight renderer reads its values. */
@Stable
internal class LiquidPressMotion(
    private val animationScope: CoroutineScope,
    private val performance: () -> LiquidGlassPerformanceState,
) {
    private val pressProgressAnimation = Animatable(0f, 0.001f)
    private val positionAnimation =
        Animatable(Offset.Zero, Offset.VectorConverter, Offset.VisibilityThreshold)

    private var startPosition = Offset.Zero
    private var pointerPosition by mutableStateOf(Offset.Zero)
    private var pressed by mutableStateOf(false)
    private var pressJob: Job? = null
    private var releaseJob: Job? = null
    val currentPosition: Offset
        get() = if (pressed) pointerPosition else positionAnimation.value

    val motionEnabled: Boolean
        get() = performance().motionScale > 0f

    val pressProgress: Float
        get() = pressProgressAnimation.value

    val offset: Offset
        get() = currentPosition - startPosition

    internal fun press(position: Offset) {
        releaseJob?.cancel()
        pressJob?.cancel()
        startPosition = position
        pointerPosition = position
        pressed = true
        pressJob = animationScope.launch {
            pressProgressAnimation.animateTo(
                targetValue = 1f,
                animationSpec = LiquidControlMotion.pressProgress(true, performance()),
            )
        }
    }

    internal fun move(position: Offset) {
        if (pressed) pointerPosition = position
    }

    internal fun release() {
        if (!pressed) return
        pressJob?.cancel()
        releaseJob?.cancel()
        val releasePosition = pointerPosition
        releaseJob =
            animationScope.launch(start = CoroutineStart.UNDISPATCHED) {
                positionAnimation.snapTo(releasePosition)
                pressed = false
                launch {
                    pressProgressAnimation.animateTo(
                        targetValue = 0f,
                        animationSpec = LiquidControlMotion.pressProgress(false, performance()),
                    )
                }
                launch {
                    positionAnimation.animateTo(
                        targetValue = startPosition,
                        animationSpec = LiquidControlMotion.pointerPosition(performance()),
                    )
                }
            }
    }
}
