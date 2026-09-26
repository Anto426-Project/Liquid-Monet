package com.anto426.liquidmonet.components.internal

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.LiquidMotion
import kotlin.math.sqrt
import kotlin.math.tanh

/** Creates the shared press/highlight state at the control call site. */
@Composable
internal fun rememberLiquidControlHighlight(): InteractiveHighlight {
    val animationScope = rememberCoroutineScope()
    val performance = rememberUpdatedState(LocalLiquidGlassPerformance.current)
    return remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope, performance = { performance.value })
    }
}

/** Shared motion rules for press-driven Liquid controls. */
internal object LiquidControlMotion {
    fun pressProgress(isPressed: Boolean, performance: LiquidGlassPerformanceState): AnimationSpec<Float> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = if (isPressed) LiquidMotion.PressDampingRatio else LiquidMotion.ReleaseDampingRatio,
            stiffness = if (isPressed) LiquidMotion.PressStiffness else LiquidMotion.ReleaseStiffness,
            visibilityThreshold = 0.001f
        )

    fun pointerPosition(performance: LiquidGlassPerformanceState): AnimationSpec<Offset> =
        LiquidMotion.spring(
            performance = performance,
            dampingRatio = LiquidMotion.ReleaseDampingRatio,
            stiffness = LiquidMotion.ReleaseStiffness,
            visibilityThreshold = Offset.VisibilityThreshold
        )
}

/** Applies the elastic layer transform shared by every interactive Liquid control. */
internal fun liquidControlLayerBlock(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
    stretchFactor: Float = 1f,
    translationFactor: Float = 1f
): (GraphicsLayerScope.() -> Unit)? = if (enabled) {
    layer@{
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
        val horizontalStretch = if (distance > 0f) {
            maxDragScale * offset.x * offset.x / (distance * maxDim) * aspectX
        } else 0f
        val verticalStretch = if (distance > 0f) {
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
    interactiveHighlight: InteractiveHighlight
): Modifier = liquidControlLayerBlock(enabled, interactiveHighlight)
    ?.let { this.graphicsLayer(it) } ?: this

/** Adds the single press gesture and optical highlight used by Liquid controls. */
internal fun Modifier.liquidControlPressFeedback(
    enabled: Boolean,
    interactiveHighlight: InteractiveHighlight,
    shape: Shape? = null,
    drawHighlightOverlay: Boolean = true,
    highlightColor: Color = Color.Unspecified
): Modifier {
    if (!enabled) return this

    val interactiveModifier = this
    return if (drawHighlightOverlay) {
        interactiveModifier
            .then(interactiveHighlight.modifier(highlightColor, shape))
            .then(interactiveHighlight.gestureModifier)
    } else {
        interactiveModifier.then(interactiveHighlight.gestureModifier)
    }
}
