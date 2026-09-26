package com.anto426.liquidmonet.components.internal

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider as MaterialSlider
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
import com.anto426.liquidmonet.components.selection.slider.requireLiquidSliderRange
import com.anto426.liquidmonet.components.selection.slider.snapLiquidSliderValue
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.internal.liquidGlassDynamic
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.liquidInteractiveZIndex
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassDynamicPresets
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.DampedDragAnimation
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule

/**
 * LiquidGlassSlider - Liquid Glass Floating Thumb Slider. Features a sleek track with an optical
 * liquid glass floating thumb that elastically squashes and stretches on drag.
 */
@Composable
internal fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
    trackHeight: Dp = 8.dp,
    thumbSize: Dp = 24.dp,
) {
    requireLiquidSliderRange(valueRange)
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = if (tint.isSpecified) tint else colorScheme.primary
    val glassColors = LiquidGlassTheme.colors
    val trackColor = glassColors.inactiveTrack

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)
    val performance = LocalLiquidGlassPerformance.current
    val trackBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().height(48.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()

        val visibilityThreshold =
            ((valueRange.endInclusive - valueRange.start) / 1000f).coerceAtLeast(0.0001f)
        val currentOnValueChange by rememberUpdatedState(onValueChange)
        val inputInteractionSource = remember { MutableInteractionSource() }
        val isInputPressed by inputInteractionSource.collectIsPressedAsState()
        val isInputDragged by inputInteractionSource.collectIsDraggedAsState()
        val dampedDragAnimation =
            remember(animationScope, valueRange, visibilityThreshold, performance) {
                DampedDragAnimation(
                    animationScope = animationScope,
                    performance = performance,
                    initialValue = value.coerceIn(valueRange),
                    valueRange = valueRange,
                    visibilityThreshold = visibilityThreshold,
                    initialScale = 1f,
                    pressedScale = 1.5f,
                    onDragStarted = {},
                    onDragStopped = {},
                    onDrag = { _, _ -> },
                )
            }

        LaunchedEffect(value, dampedDragAnimation) {
            val targetValue = value.coerceIn(valueRange)
            if (dampedDragAnimation.targetValue != targetValue) {
                dampedDragAnimation.updateValue(targetValue)
            }
        }

        LaunchedEffect(isInputPressed, isInputDragged, enabled, dampedDragAnimation) {
            if (enabled && (isInputPressed || isInputDragged)) {
                dampedDragAnimation.press()
            } else {
                dampedDragAnimation.release()
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Track Layer
            Box(Modifier.fillMaxWidth().layerBackdrop(trackBackdrop)) {
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

                val visualProgress =
                    if (isLtr) {
                            dampedDragAnimation.progress
                        } else {
                            1f - dampedDragAnimation.progress
                        }
                        .fastCoerceIn(0f, 1f)
                if (visualProgress > 0.001f) {
                    Box(
                        Modifier.align(if (isLtr) Alignment.CenterStart else Alignment.CenterEnd)
                            .fillMaxWidth(visualProgress)
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

            // Floating Optical Lens Thumb with Snell Refraction & Specular Highlights
            Box(
                Modifier.graphicsLayer {
                        val visualProgress =
                            if (isLtr) {
                                    dampedDragAnimation.progress
                                } else {
                                    1f - dampedDragAnimation.progress
                                }
                                .fastCoerceIn(0f, 1f)
                        translationX =
                            (-size.width / 2f + trackWidth * visualProgress).fastCoerceIn(
                                -size.width / 4f,
                                trackWidth - size.width * 3f / 4f,
                            )
                        alpha = if (enabled) 1f else 0.5f
                    }
                    .liquidGlassDynamic(
                        backdrop =
                            rememberCombinedBackdrop(
                                effectiveBackdrop,
                                rememberBackdrop(trackBackdrop) { drawBackdrop ->
                                    val progress = dampedDragAnimation.pressProgress
                                    val scaleX = lerp(2f / 3f, 1f, progress)
                                    val scaleY = lerp(0f, 1f, progress)
                                    scale(scaleX, scaleY) {
                                        drawBackdrop()
                                    }
                                },
                            ),
                        shape = Capsule(),
                        preset = LiquidGlassDynamicPresets.SliderThumb,
                        performance = performance,
                        effectProgress = { dampedDragAnimation.pressProgress },
                        layerBlock = {
                            scaleX = dampedDragAnimation.scaleX
                            scaleY = dampedDragAnimation.scaleY
                            val velocity = dampedDragAnimation.velocity / 10f
                            scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        },
                        onDrawSurface = {
                            val progress = dampedDragAnimation.pressProgress
                            drawRect(
                                accentColor.copy(alpha = 0.70f * (1f - progress.coerceIn(0f, 1f)))
                            )
                        },
                    )
                    .liquidInteractiveZIndex(enabled)
                    .size(thumbSize)
            )

            // Material's input/semantics layer provides a reliable 48dp tap and drag target.
            // The custom liquid renderer above remains purely visual and cannot steal gestures.
            MaterialSlider(
                value = value.coerceIn(valueRange),
                onValueChange = { nextValue ->
                    currentOnValueChange(snapLiquidSliderValue(nextValue, valueRange, steps))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                enabled = enabled,
                valueRange = valueRange,
                steps = steps,
                interactionSource = inputInteractionSource,
                thumb = { Box(Modifier.size(48.dp)) },
                track = { Box(Modifier.fillMaxWidth().height(1.dp)) },
            )
        }
    }
}
