package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.tanh
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Direct finger tracking and one cancellable return; dragging never launches per-event jobs. */
internal class LiquidElasticDrag(
    private val scope: CoroutineScope,
    private val maximumRawDistance: Float,
    private val transform: (Float) -> Float = { it },
) {
    private var rawDrag by mutableFloatStateOf(0f)
    private var tracking by mutableStateOf(false)
    private val returnMotion = LiquidFloatMotion(0f)
    private var returnJob: Job? = null
    val rawOffset: Float
        get() = if (tracking) rawDrag else returnMotion.value

    val offset: Float
        get() = transform(rawOffset)

    fun dragBy(delta: Float) {
        if (!delta.isFinite()) return
        if (!tracking) {
            rawDrag = returnMotion.value
            returnJob?.cancel()
            tracking = true
        }
        rawDrag = (rawDrag + delta).coerceIn(-maximumRawDistance, maximumRawDistance)
    }

    fun release(specification: FiniteAnimationSpec<Float>) {
        val start = rawOffset
        returnJob?.cancel()
        returnJob =
            scope.launch(start = CoroutineStart.UNDISPATCHED) {
                returnMotion.snapTo(start)
                tracking = false
                returnMotion.animateTo(0f, specification)
            }
    }

    fun reset() {
        returnJob?.cancel()
        rawDrag = 0f
        tracking = true
    }
}

internal fun liquidRubberBand(distance: Float, limit: Float): Float =
    limit * tanh(distance * 0.35f / limit)
