package com.anto426.liquidmonet.components.selection.rangeslider

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider as MaterialRangeSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.selection.slider.requireLiquidSliderRange
import com.anto426.liquidmonet.components.selection.slider.snapLiquidSliderValue
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.internal.liquidGlassDynamic
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.liquidInteractiveZIndex
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassDynamicPresets
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.DampedDragAnimation
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule

/**
 * LiquidRangeSlider - Optical Liquid Glass Dual-Thumb Range Slider. Features a sleek track with two
 * floating optical liquid glass lens thumbs that elastically squash and stretch on drag.
 */
@Composable
fun LiquidRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    backdropState: Backdrop = emptyBackdrop(),
    trackHeight: Dp = 8.dp,
    thumbSize: Dp = 24.dp,
) {
    LiquidRangeSliderImpl(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        steps = steps,
        enabled = enabled,
        tint = tint,
        backdropState = backdropState,
        trackHeight = trackHeight,
        thumbSize = thumbSize,
    )
}

@Composable
private fun LiquidRangeSliderImpl(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    backdropState: Backdrop = emptyBackdrop(),
    trackHeight: Dp = 8.dp,
    thumbSize: Dp = 24.dp,
) {
    requireLiquidSliderRange(valueRange)
    require(steps >= 0) { "LiquidRangeSlider steps must be zero or positive." }
    require(value.start.isFinite() && value.endInclusive.isFinite()) {
        "LiquidRangeSlider values must be finite."
    }
    require(value.start <= value.endInclusive) {
        "LiquidRangeSlider value must be ordered from start to end."
    }
    LiquidInputNormalization.positive(trackHeight, "LiquidRangeSlider trackHeight")
    LiquidInputNormalization.positive(thumbSize, "LiquidRangeSlider thumbSize")
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = if (tint.isSpecified) tint else colorScheme.primary
    val glassColors = LiquidGlassTheme.colors
    val trackColor = glassColors.inactiveTrack

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val performance = LocalLiquidGlassPerformance.current
    val trackBackdrop = rememberLayerBackdrop()

    val currentRangeState by rememberUpdatedState(value)
    val onValueChangeState by rememberUpdatedState(onValueChange)

    val visibilityThreshold =
        ((valueRange.endInclusive - valueRange.start) / 1000f).coerceAtLeast(0.0001f)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(48.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        val startInteractionSource = remember { MutableInteractionSource() }
        val endInteractionSource = remember { MutableInteractionSource() }
        val isStartPressed by startInteractionSource.collectIsPressedAsState()
        val isStartDragged by startInteractionSource.collectIsDraggedAsState()
        val isEndPressed by endInteractionSource.collectIsPressedAsState()
        val isEndDragged by endInteractionSource.collectIsDraggedAsState()

        val startDragAnimation =
            remember(animationScope, valueRange, visibilityThreshold, performance) {
                DampedDragAnimation(
                    animationScope = animationScope,
                    performance = performance,
                    initialValue = currentRangeState.start,
                    valueRange = valueRange,
                    visibilityThreshold = visibilityThreshold,
                    initialScale = 1f,
                    pressedScale = 1.5f,
                    onDragStarted = {},
                    onDragStopped = {},
                    onDrag = { _, _ -> },
                )
            }

        val endDragAnimation =
            remember(animationScope, valueRange, visibilityThreshold, performance) {
                DampedDragAnimation(
                    animationScope = animationScope,
                    performance = performance,
                    initialValue = currentRangeState.endInclusive,
                    valueRange = valueRange,
                    visibilityThreshold = visibilityThreshold,
                    initialScale = 1f,
                    pressedScale = 1.5f,
                    onDragStarted = {},
                    onDragStopped = {},
                    onDrag = { _, _ -> },
                )
            }

        LaunchedEffect(value.start, startDragAnimation) {
            val targetValue = value.start.coerceIn(valueRange)
            if (startDragAnimation.targetValue != targetValue) {
                startDragAnimation.updateValue(targetValue)
            }
        }

        LaunchedEffect(value.endInclusive, endDragAnimation) {
            val targetValue = value.endInclusive.coerceIn(valueRange)
            if (endDragAnimation.targetValue != targetValue) {
                endDragAnimation.updateValue(targetValue)
            }
        }

        LaunchedEffect(isStartPressed, isStartDragged, enabled, startDragAnimation) {
            if (enabled && (isStartPressed || isStartDragged)) {
                startDragAnimation.press()
            } else {
                startDragAnimation.release()
            }
        }
        LaunchedEffect(isEndPressed, isEndDragged, enabled, endDragAnimation) {
            if (enabled && (isEndPressed || isEndDragged)) {
                endDragAnimation.press()
            } else {
                endDragAnimation.release()
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Track Layer
            Box(Modifier.fillMaxWidth().layerBackdrop(trackBackdrop)) {
                // Inactive Track Background
                Box(
                    Modifier.height(trackHeight)
                        .fillMaxWidth()
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = Capsule(),
                            role = LiquidGlassRole.Control,
                            containerColor = trackColor,
                            preset = LiquidGlassPresets.Subtle,
                        )
                )

                // Active Range Track Fill
                val startProgress =
                    if (isLtr) {
                            startDragAnimation.progress
                        } else {
                            1f - startDragAnimation.progress
                        }
                        .fastCoerceIn(0f, 1f)
                val endProgress =
                    if (isLtr) {
                            endDragAnimation.progress
                        } else {
                            1f - endDragAnimation.progress
                        }
                        .fastCoerceIn(0f, 1f)
                val rangeStart = minOf(startProgress, endProgress)
                val rangeFraction =
                    (maxOf(startProgress, endProgress) - rangeStart).fastCoerceIn(0f, 1f)
                if (rangeFraction > 0.001f) {
                    Box(
                        Modifier.graphicsLayer { translationX = trackWidth * rangeStart }
                            .fillMaxWidth(rangeFraction)
                            .height(trackHeight)
                            .liquidGlass(
                                backdrop = effectiveBackdrop,
                                shape = Capsule(),
                                role = LiquidGlassRole.Control,
                                containerColor =
                                    accentColor.copy(alpha = glassColors.activeTrack.alpha),
                                preset = LiquidGlassPresets.Subtle,
                            )
                    )
                }
            }

            LiquidRangeSliderThumb(
                animation = startDragAnimation,
                backdrop = effectiveBackdrop,
                trackBackdrop = trackBackdrop,
                trackWidth = trackWidth,
                isLtr = isLtr,
                enabled = enabled,
                thumbSize = thumbSize,
                performance = performance,
                accentColor = accentColor,
            )
            LiquidRangeSliderThumb(
                animation = endDragAnimation,
                backdrop = effectiveBackdrop,
                trackBackdrop = trackBackdrop,
                trackWidth = trackWidth,
                isLtr = isLtr,
                enabled = enabled,
                thumbSize = thumbSize,
                performance = performance,
                accentColor = accentColor,
            )

            // Native Material input and semantics stay on top of the custom optical renderer.
            MaterialRangeSlider(
                value = value,
                onValueChange = { nextRange ->
                    val start = snapLiquidSliderValue(nextRange.start, valueRange, steps)
                    val end = snapLiquidSliderValue(nextRange.endInclusive, valueRange, steps)
                    onValueChangeState(minOf(start, end)..maxOf(start, end))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                startInteractionSource = startInteractionSource,
                endInteractionSource = endInteractionSource,
                startThumb = { Box(Modifier.size(48.dp)) },
                endThumb = { Box(Modifier.size(48.dp)) },
                track = { Box(Modifier.fillMaxWidth().height(1.dp)) },
            )
        }
    }
}

@Composable
private fun LiquidRangeSliderThumb(
    animation: DampedDragAnimation,
    backdrop: Backdrop,
    trackBackdrop: Backdrop,
    trackWidth: Float,
    isLtr: Boolean,
    enabled: Boolean,
    thumbSize: Dp,
    performance: LiquidGlassPerformanceState,
    accentColor: Color,
) {
    Box(
        Modifier.graphicsLayer {
                val visualProgress = if (isLtr) animation.progress else 1f - animation.progress
                translationX =
                    (-size.width / 2f + trackWidth * visualProgress.fastCoerceIn(0f, 1f))
                        .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f)
                alpha = if (enabled) 1f else 0.5f
            }
            .liquidGlassDynamic(
                backdrop =
                    rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = animation.pressProgress
                            scale(
                                scaleX = lerp(2f / 3f, 1f, progress),
                                scaleY = lerp(0f, 1f, progress),
                            ) {
                                drawBackdrop()
                            }
                        },
                    ),
                shape = Capsule(),
                preset = LiquidGlassDynamicPresets.SliderThumb,
                performance = performance,
                effectProgress = { animation.pressProgress },
                layerBlock = {
                    scaleX = animation.scaleX
                    scaleY = animation.scaleY
                    val velocity = animation.velocity / 10f
                    scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                    scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                },
                onDrawSurface = {
                    val progress = animation.pressProgress
                    drawRect(accentColor.copy(alpha = lerp(0.70f, 0.18f, progress)))
                },
            )
            .liquidInteractiveZIndex(enabled)
            .size(thumbSize)
    )
}
