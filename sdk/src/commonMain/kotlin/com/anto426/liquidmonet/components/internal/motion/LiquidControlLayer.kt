package com.anto426.liquidmonet.components.internal.motion

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import com.anto426.liquidmonet.components.internal.InteractiveHighlight
import com.anto426.liquidmonet.components.internal.LiquidControlDefaults
import kotlin.math.abs
import kotlin.math.tanh

internal enum class LiquidCardMotion {
    Standalone,
    Contained,
}

/** Applies the elastic layer transform shared by every interactive Liquid control. */
internal fun liquidControlLayerBlock(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
    stretchFactor: Float = LiquidControlDefaults.deformationFactor,
    translationFactor: Float = LiquidControlDefaults.deformationFactor,
    cardMotion: LiquidCardMotion? = null,
): (GraphicsLayerScope.() -> Unit)? =
    if (enabled) {
        layer@{
            scaleX = 1f
            scaleY = 1f
            translationX = 0f
            translationY = 0f
            clip = false
            if (!interactiveHighlight.motionEnabled) return@layer
            val width = size.width.coerceAtLeast(1f)
            val height = size.height.coerceAtLeast(1f)
            val minDim = size.minDimension.coerceAtLeast(1f)
            val maxDim = size.maxDimension.coerceAtLeast(1f)

            val progress = interactiveHighlight.pressProgress
            val baseScale = 1f + (LiquidControlDefaults.pressedScale - 1f) * progress

            val initialDerivative = 0.20f * translationFactor
            val offset = interactiveHighlight.offset
            translationX = minDim * translationFactor * tanh(initialDerivative * offset.x / minDim)
            translationY = minDim * translationFactor * tanh(initialDerivative * offset.y / minDim)

            // Fluid non-linear elastic stretching in all directions.
            val maxDragScale = 0.15f * progress * stretchFactor
            val aspectX = (width / height).coerceIn(0.5f, 2f)
            val aspectY = (height / width).coerceIn(0.5f, 2f)
            val distance = offset.getDistance()
            if (distance > 0f) {
                val fraction = distance / maxDim
                val stretch = maxDragScale * if (cardMotion != null) tanh(fraction) else fraction
                val direction = offset / distance
                scaleX = baseScale + stretch * direction.x * direction.x * aspectX
                scaleY = baseScale + stretch * direction.y * direction.y * aspectY
            } else {
                scaleX = baseScale
                scaleY = baseScale
            }
            if (cardMotion == LiquidCardMotion.Contained) {
                // Use the space freed by the press. Ease toward the edge and then limit
                // stretch so the moving card stays inside its own resting space.
                translationX = containTranslation(translationX, width, baseScale)
                translationY = containTranslation(translationY, height, baseScale)
                scaleX = minOf(scaleX, 1f - 2f * abs(translationX) / width)
                scaleY = minOf(scaleY, 1f - 2f * abs(translationY) / height)
            }
        }
    } else {
        null
    }

/** Creates an elastic graphics layer only when a control can react to input. */
internal fun Modifier.liquidControlLayer(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
    cardMotion: LiquidCardMotion? = null,
): Modifier =
    liquidControlLayerBlock(
        enabled,
        interactiveHighlight,
        translationFactor = if (cardMotion != null) 0.7f else LiquidControlDefaults.deformationFactor,
        cardMotion = cardMotion,
    )?.let { graphicsLayer(it) } ?: this

private fun containTranslation(offset: Float, dimension: Float, scale: Float): Float {
    val availableSpace = dimension * (1f - scale) / 2f
    return if (availableSpace > 0f) availableSpace * tanh(offset / availableSpace) else 0f
}
