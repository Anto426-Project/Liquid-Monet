package com.anto426.liquidmonet.components.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import com.anto426.liquidmonet.motion.DampedDragAnimation
import com.anto426.liquidmonet.motion.LiquidDragMotion
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.essentialCardBaseColor
import com.anto426.liquidmonet.glass.liquidInteractiveZIndex
import com.anto426.liquidmonet.glass.internal.liquidGlassDynamic
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassDynamicPresets
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.asBalancedFrostedIfEssential
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.liquidmonet.theme.monet.blend
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule

/** Reference-aligned liquid switch renderer with tap, drag and elastic thumb deformation. */
@Composable
internal fun LiquidGlassToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val accentColor = colorScheme.primary
    val glassColors = LiquidGlassTheme.colors
    val trackColor = glassColors.inactiveTrack
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)
    val performance = LocalLiquidGlassPerformance.current
    val currentPerformance by rememberUpdatedState(performance)
    val currentChecked by rememberUpdatedState(checked)
    val currentEnabled by rememberUpdatedState(enabled)
    val currentOnCheckedChange by rememberUpdatedState(onCheckedChange)

    val density = LocalDensity.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val currentIsLtr by rememberUpdatedState(isLtr)
    val dragWidth = with(density) { 20.dp.toPx() }
    val currentDragWidth by rememberUpdatedState(dragWidth)
    val animationScope = rememberCoroutineScope()
    var didDrag by remember { mutableStateOf(false) }
    var fraction by remember { mutableFloatStateOf(if (checked) 1f else 0f) }
    val dampedDragAnimation = remember(animationScope) {
        DampedDragAnimation(
            animationScope = animationScope,
            performance = { currentPerformance },
            initialValue = fraction,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 1.5f,
            onDragStarted = { didDrag = false },
            onDragStopped = {
                if (!currentEnabled) return@DampedDragAnimation
                val target = if (didDrag) {
                    if (targetValue >= 0.5f) 1f else 0f
                } else {
                    if (currentChecked) 0f else 1f
                }
                fraction = target
                animateToValue(target)
                currentOnCheckedChange(target == 1f)
                didDrag = false
            },
            onDragCancelled = {
                val target = if (currentChecked) 1f else 0f
                fraction = target
                animateToValue(target)
                didDrag = false
            },
            onDrag = { _, dragAmount ->
                if (!currentEnabled) return@DampedDragAnimation
                if (dragAmount.x != 0f) didDrag = true
                val delta = dragAmount.x / currentDragWidth
                fraction = if (currentIsLtr) {
                    (fraction + delta).fastCoerceIn(0f, 1f)
                } else {
                    (fraction - delta).fastCoerceIn(0f, 1f)
                }
                updateValue(fraction)
            },
            animateToValueSpec = LiquidDragMotion::toggleGlide,
            trackVelocityOnAnimateToValue = true,
        )
    }

    LaunchedEffect(checked, dampedDragAnimation) {
        val target = if (checked) 1f else 0f
        if (target != fraction || dampedDragAnimation.targetValue != target) {
            fraction = target
            dampedDragAnimation.animateToValue(target)
        }
    }
    LaunchedEffect(enabled, dampedDragAnimation) {
        if (!enabled) {
            fraction = if (currentChecked) 1f else 0f
            didDrag = false
            dampedDragAnimation.updateValue(fraction)
            dampedDragAnimation.release()
        }
    }

    val trackBackdrop = rememberLayerBackdrop()
    val togglePerformance = remember(performance) { performance.asBalancedFrostedIfEssential() }
    val useEssentialFallback =
        (togglePerformance.opticalQualityTier ?: togglePerformance.qualityTier) ==
            LiquidGlassQualityTier.MINIMAL
    val isFrostedWithoutLens = togglePerformance.refractionScale <= 0f
    val isLightSurface = remember(colorScheme.surface) { colorScheme.surface.luminance() > 0.5f }
    val resolvedTrackColor = lerp(
        trackColor,
        glassColors.focusIndicator,
        dampedDragAnimation.value
    )
    val cardBase =
        remember(isLightSurface, colorScheme.surface, accentColor) {
            essentialCardBaseColor(isLightSurface, colorScheme.surface, accentColor)
        }
    val essentialOffTrack = trackColor.compositeOver(cardBase).copy(alpha = 0.74f)
    val essentialOnTrack =
        blend(cardBase, accentColor, if (isLightSurface) 0.78f else 0.65f).copy(alpha = 0.84f)
    val essentialTrackColor = lerp(
        essentialOffTrack,
        essentialOnTrack,
        dampedDragAnimation.value,
    )
    val essentialTrackBorder = lerp(
        Color.White.copy(alpha = if (isLightSurface) 0.48f else 0.24f),
        accentColor.copy(alpha = if (isLightSurface) 0.35f else 0.50f),
        dampedDragAnimation.value,
    )
    val essentialThumbColor = Color.White
    // Fade the resting thumb out on press only when the lens refracts the track; keep it visible
    // in frosted-no-lens mode so dragging never makes the thumb vanish.
    val thumbColor =
        if (isFrostedWithoutLens) {
            Color.White.copy(alpha = 0.94f)
        } else {
            glassColors.content.copy(alpha = 0.90f)
        }

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                role = Role.Switch
                toggleableState = ToggleableState(checked)
                if (!enabled) disabled()
                onClick {
                    if (enabled) currentOnCheckedChange(!currentChecked)
                    enabled
                }
            }
            .then(if (enabled) dampedDragAnimation.dragModifier(consumeTap = true) else Modifier),
        contentAlignment = Alignment.CenterStart
    ) {
        if (useEssentialFallback) {
            Box(
                modifier = Modifier
                    .size(52.dp, 32.dp)
                    .background(essentialTrackColor, Capsule())
                    .border(1.dp, essentialTrackBorder, Capsule())
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val padding = 4.dp.toPx()
                        alpha = if (enabled) 1f else 0.5f
                        translationX = if (isLtr) {
                            lerp(padding, padding + dragWidth, dampedDragAnimation.value)
                        } else {
                            lerp(-padding, -(padding + dragWidth), dampedDragAnimation.value)
                        }
                        val pressScale = lerp(1f, 1.12f, dampedDragAnimation.pressProgress.coerceIn(0f, 1f))
                        val velocity = dampedDragAnimation.velocity / 50f
                        scaleX = pressScale / (1f - (velocity * 0.45f).fastCoerceIn(-0.15f, 0.15f))
                        scaleY = pressScale * (1f - (velocity * 0.18f).fastCoerceIn(-0.12f, 0.12f))
                    }
                    .background(essentialThumbColor, Capsule())
                    .border(
                        width = 0.75.dp,
                        color = Color.Black.copy(alpha = if (isLightSurface) 0.14f else 0.22f),
                        shape = Capsule(),
                    )
                    .liquidInteractiveZIndex(enabled)
                    .size(24.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .layerBackdrop(trackBackdrop)
                    .size(52.dp, 32.dp)
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = Capsule(),
                        role = LiquidGlassRole.Control,
                        containerColor = resolvedTrackColor,
                        performance = togglePerformance,
                    )
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val padding = 4.dp.toPx()
                        alpha = if (enabled) 1f else 0.5f
                        translationX = if (isLtr) {
                            lerp(padding, padding + dragWidth, dampedDragAnimation.value)
                        } else {
                            lerp(-padding, -(padding + dragWidth), dampedDragAnimation.value)
                        }
                    }
                    .liquidGlassDynamic(
                        backdrop = rememberCombinedBackdrop(
                            effectiveBackdrop,
                            rememberBackdrop(trackBackdrop) { drawBackdrop ->
                                val progress = dampedDragAnimation.pressProgress
                                scale(
                                    scaleX = lerp(2f / 3f, 0.75f, progress),
                                    scaleY = lerp(0f, 0.75f, progress)
                                ) {
                                    drawBackdrop()
                                }
                            }
                        ),
                        shape = Capsule(),
                        preset = LiquidGlassDynamicPresets.ToggleThumb,
                        performance = togglePerformance,
                        effectProgress = { dampedDragAnimation.pressProgress },
                        layerBlock = {
                            scaleX = dampedDragAnimation.scaleX
                            scaleY = dampedDragAnimation.scaleY
                            val velocity = dampedDragAnimation.velocity / 50f
                            scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                            scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                        },
                        onDrawSurface = {
                            val pressFade =
                                if (isFrostedWithoutLens) {
                                    1f - 0.16f * dampedDragAnimation.pressProgress.coerceIn(0f, 1f)
                                } else {
                                    1f - dampedDragAnimation.pressProgress.coerceIn(0f, 1f)
                                }
                            drawRect(thumbColor.copy(alpha = thumbColor.alpha * pressFade))
                        }
                    )
                    .liquidInteractiveZIndex(enabled)
                    .size(24.dp)
            )
        }
    }
}
