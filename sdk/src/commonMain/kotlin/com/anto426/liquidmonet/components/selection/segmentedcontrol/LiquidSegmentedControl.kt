package com.anto426.liquidmonet.components.selection.segmentedcontrol

import androidx.compose.animation.core.EaseOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.anto426.liquidmonet.components.internal.InteractiveHighlight
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.selection.segmentedcontrol.motion.LiquidSegmentedControlMotion
import com.anto426.liquidmonet.components.selection.segmentedcontrol.motion.LiquidSegmentedControlMotion.applyDropletLayer
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassLegibility
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LiquidGlassStyleManager
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LocalLiquidGlassLegibility
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.internal.liquidGlassDynamic
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.liquidInteractiveZIndex
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveEssentialChromeSurfaceColor
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassDynamicPresets
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.asBalancedFrostedIfEssential
import com.anto426.liquidmonet.motion.LiquidElasticDrag
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign
import kotlinx.coroutines.flow.collectLatest

/** One selectable option inside a [LiquidSegmentedControl]. */
@Immutable
data class LiquidSegment(
    val label: String,
    val icon: ImageVector? = null,
    val badge: String? = null,
    val enabled: Boolean = true,
    val contentDescription: String? = null,
)

/** Vertical density scale for [LiquidSegmentedControl]. */
enum class LiquidSegmentedControlSize {
    /** Compact 40dp capsule suited to toolbars, sheets, and dense inspectors. */
    Compact,

    /** Standard 48dp capsule meeting full touch-target ergonomics. */
    Medium,
}

/** Pure layout and gesture math for [LiquidSegmentedControl]. */
internal object LiquidSegmentedControlMath {
    fun clampIndex(index: Int, count: Int): Int {
        if (count <= 0) return 0
        return index.coerceIn(0, count - 1)
    }

    fun indexFromPosition(
        xPx: Float,
        trackWidthPx: Float,
        count: Int,
        isRtl: Boolean,
    ): Int {
        if (count <= 1 || trackWidthPx <= 0f) return 0
        val fraction = (xPx / trackWidthPx).coerceIn(0f, 0.9999f)
        val visualIndex = floor(fraction * count).toInt().coerceIn(0, count - 1)
        return if (isRtl) (count - 1 - visualIndex) else visualIndex
    }

    fun indicatorTranslationX(
        animatedIndex: Float,
        segmentWidthPx: Float,
        count: Int,
        isRtl: Boolean,
    ): Float {
        if (count <= 0 || segmentWidthPx <= 0f) return 0f
        val clamped = animatedIndex.coerceIn(0f, (count - 1).toFloat())
        val visualSlot = if (isRtl) (count - 1).toFloat() - clamped else clamped
        return visualSlot * segmentWidthPx
    }
}

/**
 * Text-only convenience overload of [LiquidSegmentedControl].
 */
@kotlin.jvm.JvmName("LiquidSegmentedControlLabels")
@Composable
fun LiquidSegmentedControl(
    labels: List<String>,
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: LiquidSegmentedControlSize = LiquidSegmentedControlSize.Medium,
    enabled: Boolean = true,
    shape: Shape = Capsule(),
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
) {
    val segments = remember(labels) { labels.map { LiquidSegment(label = it) } }
    LiquidSegmentedControl(
        segments = segments,
        selectedIndex = selectedIndex,
        onSegmentSelected = onSegmentSelected,
        modifier = modifier,
        size = size,
        enabled = enabled,
        shape = shape,
        backdrop = backdrop,
        backdropPolicy = backdropPolicy,
        legibility = legibility,
    )
}

/**
 * LiquidSegmentedControl - Compact refractive glass segmented switch with a live liquid droplet
 * lens that stretches, refracts, and magnifies the tinted active segment on tap and drag.
 */
@Composable
fun LiquidSegmentedControl(
    segments: List<LiquidSegment>,
    selectedIndex: Int,
    onSegmentSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    size: LiquidSegmentedControlSize = LiquidSegmentedControlSize.Medium,
    enabled: Boolean = true,
    shape: Shape = Capsule(),
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
) {
    if (segments.isEmpty()) return

    val safeSelectedIndex = LiquidSegmentedControlMath.clampIndex(selectedIndex, segments.size)
    val safeCount = segments.size.coerceAtLeast(1)
    val maxIndex = safeCount - 1

    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val isLightSurface = colorScheme.surface.luminance() > 0.5f
    val accentColor = colorScheme.primary
    val containerColor =
        LiquidGlassStyleManager.surfaceColor(
            role = LiquidGlassRole.Navigation,
            isLightSurface = isLightSurface,
            themedSurface = colorScheme.surface,
        )
    val lightTint = rememberUpdatedState(if (isLightSurface) accentColor else Color.Unspecified)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val performance = LocalLiquidGlassPerformance.current
    val segPerformance = remember(performance) { performance.asBalancedFrostedIfEssential() }
    val useEssentialFallback =
        (segPerformance.opticalQualityTier ?: segPerformance.qualityTier) ==
            LiquidGlassQualityTier.MINIMAL
    val maskSurfaceColor =
        if (useEssentialFallback) {
            remember(isLightSurface, colorScheme.surface, colorScheme.onSurface, accentColor) {
                resolveEssentialChromeSurfaceColor(
                    containerColor = null,
                    role = LiquidGlassRole.Navigation,
                    isLightSurface = isLightSurface,
                    surface = colorScheme.surface,
                    onSurface = colorScheme.onSurface,
                    primary = accentColor,
                )
            }
        } else {
            containerColor
        }

    val motionEnabled = performance.motionScale > 0f
    val hapticFeedback = LocalHapticFeedback.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val isRtl = !isLtr

    val innerPadding = 4.dp
    val baseContentHeight =
        when (size) {
            LiquidSegmentedControlSize.Compact -> 32.dp
            LiquidSegmentedControlSize.Medium -> 40.dp
        }
    val fontSize = if (size == LiquidSegmentedControlSize.Compact) 12.5.sp else 13.5.sp
    val iconSize = if (size == LiquidSegmentedControlSize.Compact) 16.dp else 18.dp

    val currentSegments by rememberUpdatedState(segments)
    val currentSelectedIndex by rememberUpdatedState(safeSelectedIndex)
    val currentOnSelected by rememberUpdatedState(onSegmentSelected)
    val currentPerformance by rememberUpdatedState(performance)
    val currentEnabled by rememberUpdatedState(enabled)

    val segmentsBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val contentHeight =
            maxOf(baseContentHeight * density.fontScale.coerceAtLeast(1f), baseContentHeight)
        val totalHeight = contentHeight + innerPadding * 2
        val segmentWidthPx =
            with(density) {
                (maxWidth.toPx() - (innerPadding * 2).toPx()).coerceAtLeast(1f) / safeCount
            }

        val animationScope = rememberCoroutineScope()
        val panelDrag = remember(animationScope) { LiquidElasticDrag(animationScope, 1_000_000f) }
        val currentSegmentWidthPx by rememberUpdatedState(segmentWidthPx)
        val currentIsLtr by rememberUpdatedState(isLtr)
        val panelOffset by
            remember(panelDrag, density, constraints.maxWidth, motionEnabled) {
                derivedStateOf {
                    if (!motionEnabled) 0f
                    else {
                        val fraction =
                            (panelDrag.rawOffset / constraints.maxWidth.coerceAtLeast(1))
                                .fastCoerceIn(-1f, 1f)
                        with(density) {
                            4.dp.toPx() * fraction.sign * EaseOut.transform(abs(fraction))
                        }
                    }
                }
            }

        var currentIndex by remember { mutableIntStateOf(safeSelectedIndex) }
        var lastDragHapticIndex by remember { mutableIntStateOf(safeSelectedIndex) }

        val dampedDragAnimation =
            remember(animationScope, safeCount) {
                LiquidSegmentedControlMotion.createSelectionMotion(
                    scope = animationScope,
                    performance = { currentPerformance },
                    initialIndex = safeSelectedIndex,
                    itemCount = safeCount,
                    onDragStarted = {
                        lastDragHapticIndex = currentSelectedIndex
                    },
                    onDragStopped = {
                        val candidateIndex = targetValue.fastRoundToInt().coerceIn(0, maxIndex)
                        val canSelect =
                            currentEnabled &&
                                currentSegments.getOrNull(candidateIndex)?.enabled == true
                        val resolvedIndex = if (canSelect) candidateIndex else currentIndex
                        animateToValue(resolvedIndex.toFloat())
                        if (canSelect && currentSelectedIndex != resolvedIndex) {
                            hapticFeedback.performLiquidHaptic(LiquidHapticCue.Selection)
                            currentOnSelected(resolvedIndex)
                        }
                        panelDrag.release(
                            LiquidSegmentedControlMotion.panelReturnSpec(currentPerformance)
                        )
                    },
                    onDragCancelled = {
                        val targetIndex = currentIndex.coerceIn(0, maxIndex)
                        animateToValue(targetIndex.toFloat())
                        panelDrag.release(
                            LiquidSegmentedControlMotion.panelReturnSpec(currentPerformance)
                        )
                    },
                    onDrag = { _, dragAmount ->
                        if (!currentEnabled) return@createSelectionMotion
                        val nextValue =
                            (targetValue +
                                    dragAmount.x /
                                        currentSegmentWidthPx *
                                        if (currentIsLtr) 1f else -1f)
                                .fastCoerceIn(0f, maxIndex.toFloat())
                        updateValue(nextValue)
                        val crossedIndex = nextValue.fastRoundToInt().coerceIn(0, maxIndex)
                        if (
                            crossedIndex != lastDragHapticIndex &&
                                currentSegments.getOrNull(crossedIndex)?.enabled == true
                        ) {
                            lastDragHapticIndex = crossedIndex
                            hapticFeedback.performLiquidHaptic(LiquidHapticCue.Selection)
                        }
                        if (currentPerformance.motionScale > 0f) {
                            panelDrag.dragBy(dragAmount.x)
                        }
                    },
                )
            }

        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { currentSelectedIndex }
                .collectLatest { index ->
                    val clamped = LiquidSegmentedControlMath.clampIndex(index, safeCount)
                    currentIndex = clamped
                    if (dampedDragAnimation.targetValue != clamped.toFloat()) {
                        dampedDragAnimation.animateToValue(clamped.toFloat())
                    }
                }
        }

        val interactiveHighlight =
            remember(animationScope, dampedDragAnimation) {
                InteractiveHighlight(
                    animationScope = animationScope,
                    lightTint = { lightTint.value },
                    position = { size, _ ->
                        Offset(
                            x =
                                if (currentIsLtr) {
                                    (dampedDragAnimation.value + 0.5f) * currentSegmentWidthPx +
                                        panelOffset
                                } else {
                                    size.width -
                                        (dampedDragAnimation.value + 0.5f) * currentSegmentWidthPx +
                                        panelOffset
                                },
                            y = size.height / 2f,
                        )
                    },
                )
            }

        val dropletInteractionProgress by
            remember(dampedDragAnimation) {
                derivedStateOf {
                    LiquidSegmentedControlMotion.dropletInteractionProgress(dampedDragAnimation)
                }
            }

        val contentScale: () -> Float = {
            lerp(
                1f,
                if (motionEnabled) LiquidSegmentedControlMotion.ContentPressedScale else 1f,
                dampedDragAnimation.pressProgress,
            )
        }

        // 1. Base liquid glass capsule track with elastic press breathing and specular highlight.
        Box(
            modifier =
                Modifier.graphicsLayer { translationX = panelOffset }
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Navigation,
                        containerColor = if (useEssentialFallback) null else containerColor,
                        preset = LiquidGlassPresets.Navigation,
                        performance = segPerformance,
                        backdropPolicy = backdropPolicy,
                        legibility = legibility,
                        layerBlock = {
                            val growth =
                                if (motionEnabled) 12.dp.toPx() / this.size.width.coerceAtLeast(1f)
                                else 0f
                            val scale = lerp(1f, 1f + growth, dampedDragAnimation.pressProgress)
                            scaleX = scale
                            scaleY = scale
                        },
                    )
                    .then(interactiveHighlight.modifier(clipShape = shape))
                    .height(totalHeight)
                    .fillMaxWidth()
        )

        // 2. Interactive segment strip in shared mode (unselected resting appearance + tap targets).
        CompositionLocalProvider(
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
            LocalLiquidGlassLegibility provides legibility,
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .height(totalHeight)
                        .padding(innerPadding)
                        .graphicsLayer { translationX = panelOffset },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SegmentStripContent(
                    segments = segments,
                    selectedIndex = safeSelectedIndex,
                    enabled = enabled,
                    interactive = true,
                    shape = shape,
                    fontSize = fontSize,
                    iconSize = iconSize,
                    contentScale = contentScale,
                    contentColor = glassColors.content,
                    disabledColor = glassColors.disabledContent,
                    badgeBackground = glassColors.neutralContainer,
                    onSegmentClick = { index ->
                        if (index != safeSelectedIndex) {
                            hapticFeedback.performLiquidHaptic(LiquidHapticCue.Selection)
                            onSegmentSelected(index)
                        }
                    },
                )
            }
        }

        // 3. Off-screen accent-tinted mask pass sampled by the moving refractive droplet lens.
        Row(
            modifier =
                Modifier.clearAndSetSemantics {}
                    .alpha(0f)
                    .layerBackdrop(segmentsBackdrop)
                    .graphicsLayer { translationX = panelOffset }
                    .liquidGlassDynamic(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        preset = LiquidGlassDynamicPresets.NavigationMask,
                        performance = segPerformance,
                        effectProgress = { dampedDragAnimation.pressProgress },
                        onDrawSurface = { drawRect(maskSurfaceColor) },
                    )
                    .then(interactiveHighlight.modifier(clipShape = shape))
                    .height(contentHeight)
                    .fillMaxWidth()
                    .padding(horizontal = innerPadding)
                    .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SegmentStripContent(
                segments = segments,
                selectedIndex = safeSelectedIndex,
                enabled = enabled,
                interactive = false,
                shape = shape,
                fontSize = fontSize,
                iconSize = iconSize,
                contentScale = contentScale,
                contentColor = glassColors.content,
                disabledColor = glassColors.disabledContent,
                badgeBackground = glassColors.neutralContainer,
                onSegmentClick = {},
            )
        }

        // 4. Moving liquid refractive droplet lens sampling both outer backdrop & tinted mask.
        val dragModifiers =
            if (enabled && safeCount > 1) {
                Modifier.then(interactiveHighlight.gestureModifier)
                    .then(dampedDragAnimation.modifier)
            } else {
                Modifier
            }
        Box(
            modifier =
                Modifier.align(AbsoluteAlignment.CenterLeft)
                    .padding(horizontal = innerPadding)
                    .liquidInteractiveZIndex()
                    .graphicsLayer {
                        translationX =
                            LiquidSegmentedControlMath.indicatorTranslationX(
                                animatedIndex = dampedDragAnimation.value,
                                segmentWidthPx = segmentWidthPx,
                                count = safeCount,
                                isRtl = isRtl,
                            ) + panelOffset
                    }
                    .then(dragModifiers)
                    .liquidGlassDynamic(
                        backdrop = rememberCombinedBackdrop(effectiveBackdrop, segmentsBackdrop),
                        shape = shape,
                        preset = LiquidGlassDynamicPresets.NavigationDroplet,
                        performance = segPerformance,
                        effectProgress = { dropletInteractionProgress },
                        decorationProgress = { dampedDragAnimation.pressProgress },
                        layerBlock = {
                            applyDropletLayer(dampedDragAnimation)
                        },
                        onDrawSurface = {
                            val progress = dropletInteractionProgress
                            val selectionColor = if (isLightSurface) Color.Black else Color.White
                            drawRect(selectionColor.copy(alpha = 0.10f * (1f - progress)))
                            drawRect(Color.Black.copy(alpha = 0.03f * progress))
                        },
                    )
                    .height(contentHeight)
                    .width(with(density) { segmentWidthPx.toDp() })
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.SegmentStripContent(
    segments: List<LiquidSegment>,
    selectedIndex: Int,
    enabled: Boolean,
    interactive: Boolean,
    shape: Shape,
    fontSize: TextUnit,
    iconSize: Dp,
    contentScale: () -> Float,
    contentColor: Color,
    disabledColor: Color,
    badgeBackground: Color,
    onSegmentClick: (Int) -> Unit,
) {
    segments.forEachIndexed { index, segment ->
        val isSegmentEnabled = enabled && segment.enabled
        val isSelected = index == selectedIndex
        val slotColor = if (isSegmentEnabled) contentColor else disabledColor

        Row(
            modifier =
                Modifier.weight(1f)
                    .fillMaxHeight()
                    .graphicsLayer {
                        val s = contentScale()
                        scaleX = s
                        scaleY = s
                        clip = false
                    }
                    .clip(shape)
                    .then(
                        if (interactive) {
                            Modifier.semantics {
                                this.selected = isSelected
                                segment.contentDescription?.let { contentDescription = it }
                            }
                                .selectable(
                                    selected = isSelected,
                                    enabled = isSegmentEnabled,
                                    interactionSource = null,
                                    indication = null,
                                    role = Role.Tab,
                                    onClick = { onSegmentClick(index) },
                                )
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (segment.icon != null) {
                Icon(
                    imageVector = segment.icon,
                    contentDescription = null,
                    tint = slotColor,
                    modifier = Modifier.size(iconSize),
                )
            }
            if (segment.label.isNotEmpty()) {
                BasicText(
                    text = segment.label,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        TextStyle(
                            color = slotColor,
                            fontSize = fontSize,
                            fontWeight = FontWeight.SemiBold,
                        ),
                )
            }
            if (!segment.badge.isNullOrBlank()) {
                Box(
                    modifier =
                        Modifier.clip(Capsule())
                            .background(badgeBackground)
                            .padding(horizontal = 5.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        text = segment.badge,
                        maxLines = 1,
                        softWrap = false,
                        style =
                            TextStyle(
                                color = slotColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                    )
                }
            }
        }
    }
}
