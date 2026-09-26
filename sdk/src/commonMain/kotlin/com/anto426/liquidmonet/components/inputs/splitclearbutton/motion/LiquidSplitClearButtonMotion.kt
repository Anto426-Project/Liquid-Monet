package com.anto426.liquidmonet.components.inputs.splitclearbutton.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateFloat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.LiquidMotion
import kotlin.math.roundToInt

internal object LiquidSplitClearButtonMotion {
    /**
     * Registered with visibility so exit finishes before disposal and rapid edits reverse smoothly.
     */
    @Composable
    fun AnimatedVisibilityScope.rememberSeparation(
        performance: LiquidGlassPerformanceState
    ): State<Float> {
        val specification =
            remember(performance) { LiquidMotion.spring<Float>(performance, 1f, 480f) }
        return transition.animateFloat(
            transitionSpec = { specification },
            label = "clearDropletSeparation",
        ) {
            if (it == EnterExitState.Visible) 1f else 0f
        }
    }
}

/** One progress controls both the opening slot and the emerging droplet; no second spring. */
internal fun Modifier.liquidDropletReveal(progress: State<Float>, rtl: Boolean): Modifier =
    layout { measurable, constraints ->
        val child = measurable.measure(constraints.copy(minWidth = 0))
        val phase = progress.value.coerceIn(0f, 1f)
        val remaining = 1f - phase
        val widthFraction = 1f - remaining * remaining * remaining
        val width = (child.width * widthFraction).roundToInt()
        layout(width, child.height) {
            child.placeRelativeWithLayer(width - child.width, 0) {
                alpha = (phase / 0.35f).coerceIn(0f, 1f)
                scaleX = 0.86f + phase * 0.14f
                scaleY = 0.96f + phase * 0.04f
                transformOrigin = TransformOrigin(if (rtl) 0f else 1f, 0.5f)
                clip = false
            }
        }
    }
