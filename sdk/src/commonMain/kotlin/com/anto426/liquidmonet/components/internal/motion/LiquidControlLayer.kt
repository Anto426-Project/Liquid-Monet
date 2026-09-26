package com.anto426.liquidmonet.components.internal.motion

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import com.anto426.liquidmonet.components.internal.InteractiveHighlight
import com.anto426.liquidmonet.components.internal.LiquidControlDefaults
import kotlin.math.sqrt
import kotlin.math.tanh

/** Applies the elastic layer transform shared by every interactive Liquid control. */
internal fun liquidControlLayerBlock(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
    stretchFactor: Float = LiquidControlDefaults.deformationFactor,
    translationFactor: Float = LiquidControlDefaults.deformationFactor,
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
            val distance = sqrt(offset.x * offset.x + offset.y * offset.y)
            // cos(atan2(y, x)) * x == x² / hypot(x, y), and likewise for y.
            // This keeps the original stretch while avoiding three transcendental calls per frame.
            val horizontalStretch =
                if (distance > 0f) {
                    maxDragScale * offset.x * offset.x / (distance * maxDim) * aspectX
                } else 0f
            val verticalStretch =
                if (distance > 0f) {
                    maxDragScale * offset.y * offset.y / (distance * maxDim) * aspectY
                } else 0f

            scaleX = baseScale + horizontalStretch
            scaleY = baseScale + verticalStretch
            clip = false
        }
    } else {
        null
    }

/** Creates an elastic graphics layer only when a control can react to input. */
internal fun Modifier.liquidControlLayer(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
): Modifier =
    liquidControlLayerBlock(enabled, interactiveHighlight)?.let { this.graphicsLayer(it) } ?: this
