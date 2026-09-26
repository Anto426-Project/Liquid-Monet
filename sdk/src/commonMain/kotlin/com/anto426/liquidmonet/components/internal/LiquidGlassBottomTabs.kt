package com.anto426.liquidmonet.components.internal

import androidx.compose.animation.core.EaseOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LiquidGlassStyleManager
import com.anto426.liquidmonet.glass.internal.liquidGlassDynamic
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.liquidInteractiveZIndex
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassDynamicPresets
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.LiquidDragMotion
import com.anto426.liquidmonet.motion.LiquidElasticDrag
import com.anto426.liquidmonet.motion.createLiquidNavigationSelectionMotion
import com.anto426.liquidmonet.motion.liquidNavigationSelectionLayer
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule
import kotlin.math.abs
import kotlin.math.sign
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop

/** Shared segmented-control renderer used by navigation and category tabs. */
@Composable
internal fun LiquidGlassBottomTabs(
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop,
    tabsCount: Int,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLightSurface = colorScheme.surface.luminance() > 0.5f
    val accentColor = colorScheme.primary
    val containerColor =
        LiquidGlassStyleManager.surfaceColor(
            role = LiquidGlassRole.Navigation,
            isLightSurface = isLightSurface,
        )
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)
    val performance = LocalLiquidGlassPerformance.current
    val motionEnabled = performance.motionScale > 0f
    val currentSelectedTabIndex by rememberUpdatedState(selectedTabIndex)
    val currentOnTabSelected by rememberUpdatedState(onTabSelected)

    val tabsBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        val density = LocalDensity.current
        val safeTabsCount = tabsCount.coerceAtLeast(1)
        val maxIndex = safeTabsCount - 1
        val tabWidth =
            with(density) {
                (constraints.maxWidth.toFloat() - 8.dp.toPx()).coerceAtLeast(1f) / safeTabsCount
            }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        val panelDrag = remember(animationScope) { LiquidElasticDrag(animationScope, 1_000_000f) }
        val currentTabWidth by rememberUpdatedState(tabWidth)
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

        val currentPerformance by rememberUpdatedState(performance)
        var currentIndex by remember {
            mutableIntStateOf(selectedTabIndex().coerceIn(0, maxIndex))
        }
        val dampedDragAnimation =
            remember(animationScope, safeTabsCount) {
                createLiquidNavigationSelectionMotion(
                    scope = animationScope,
                    performance = { currentPerformance },
                    initialIndex = selectedTabIndex(),
                    itemCount = safeTabsCount,
                    onDragStarted = {},
                    onDragStopped = {
                        val targetIndex = targetValue.fastRoundToInt().coerceIn(0, maxIndex)
                        if (currentIndex == targetIndex) {
                            animateToValue(targetIndex.toFloat())
                        } else {
                            currentIndex = targetIndex
                        }
                        panelDrag.release(LiquidDragMotion.panelReturn(currentPerformance))
                    },
                    onDragCancelled = {
                        val targetIndex = currentIndex.coerceIn(0, maxIndex)
                        animateToValue(targetIndex.toFloat())
                        panelDrag.release(LiquidDragMotion.panelReturn(currentPerformance))
                    },
                    onDrag = { _, dragAmount ->
                        updateValue(
                            (targetValue +
                                    dragAmount.x / currentTabWidth * if (currentIsLtr) 1f else -1f)
                                .fastCoerceIn(0f, maxIndex.toFloat())
                        )
                        if (currentPerformance.motionScale > 0f) panelDrag.dragBy(dragAmount.x)
                    },
                )
            }

        LaunchedEffect(Unit) {
            snapshotFlow { currentSelectedTabIndex() }
                .collectLatest { index ->
                    currentIndex = index.coerceIn(0, maxIndex)
                }
        }
        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { currentIndex }
                .drop(1)
                .collectLatest { index ->
                    dampedDragAnimation.animateToValue(index.toFloat())
                    if (currentSelectedTabIndex() != index) {
                        currentOnTabSelected(index)
                    }
                }
        }

        val interactiveHighlight =
            remember(animationScope, dampedDragAnimation) {
                InteractiveHighlight(
                    animationScope = animationScope,
                    position = { size, _ ->
                        Offset(
                            x =
                                if (isLtr)
                                    (dampedDragAnimation.value + 0.5f) * tabWidth + panelOffset
                                else
                                    size.width - (dampedDragAnimation.value + 0.5f) * tabWidth +
                                        panelOffset,
                            y = size.height / 2f,
                        )
                    },
                )
            }
        val dropletInteractionProgress by
            remember(dampedDragAnimation) {
                derivedStateOf {
                    val displacementProgress =
                        abs(dampedDragAnimation.targetValue - dampedDragAnimation.value)
                            .fastCoerceIn(0f, 1f)
                    val velocityProgress =
                        (abs(dampedDragAnimation.velocity) / 4f).fastCoerceIn(0f, 1f)
                    maxOf(
                            dampedDragAnimation.pressProgress,
                            displacementProgress,
                            velocityProgress,
                        )
                        .fastCoerceIn(0f, 1f)
                }
            }

        Row(
            modifier =
                Modifier.graphicsLayer { translationX = panelOffset }
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = Capsule(),
                        role = LiquidGlassRole.Navigation,
                        containerColor = containerColor,
                        preset = LiquidGlassPresets.Navigation,
                        layerBlock = {
                            val growth =
                                if (motionEnabled) 16.dp.toPx() / size.width.coerceAtLeast(1f)
                                else 0f
                            val scale = lerp(1f, 1f + growth, dampedDragAnimation.pressProgress)
                            scaleX = scale
                            scaleY = scale
                        },
                    )
                    .then(interactiveHighlight.modifier(clipShape = Capsule()))
                    .height(64.dp)
                    .fillMaxWidth()
                    .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )

        // This off-screen render is the reference's color-mask pass for the moving droplet.
        // It is not interactive or exposed to accessibility.
        CompositionLocalProvider(
            LocalLiquidBottomTabScale provides
                {
                    lerp(
                        1f,
                        if (motionEnabled) LiquidDragMotion.NavigationContentScale else 1f,
                        dampedDragAnimation.pressProgress,
                    )
                },
            // The mask is rendered a second time only to feed the droplet backdrop. Keeping its
            // duplicated tab click handlers active places an invisible hit target above the real
            // row and cuts short tap/release feedback during navigation.
            LocalLiquidBottomTabInteractive provides false,
        ) {
            Row(
                modifier =
                    Modifier.clearAndSetSemantics {}
                        .alpha(0f)
                        .layerBackdrop(tabsBackdrop)
                        .graphicsLayer { translationX = panelOffset }
                        .liquidGlassDynamic(
                            backdrop = effectiveBackdrop,
                            shape = Capsule(),
                            preset = LiquidGlassDynamicPresets.NavigationMask,
                            performance = performance,
                            effectProgress = { dampedDragAnimation.pressProgress },
                            onDrawSurface = { drawRect(containerColor) },
                        )
                        .then(interactiveHighlight.modifier(clipShape = Capsule()))
                        .height(56.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .graphicsLayer(colorFilter = ColorFilter.tint(accentColor)),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }

        Box(
            modifier =
                Modifier.padding(horizontal = 4.dp)
                    // The moving droplet is the active material, so it must remain above the static
                    // panel and tab mask throughout press, drag and the release spring.
                    .liquidInteractiveZIndex()
                    .graphicsLayer {
                        translationX =
                            if (isLtr) {
                                dampedDragAnimation.value * tabWidth + panelOffset
                            } else {
                                (maxIndex - dampedDragAnimation.value) * tabWidth + panelOffset
                            }
                    }
                    .then(interactiveHighlight.gestureModifier)
                    .then(dampedDragAnimation.modifier)
                    .liquidGlassDynamic(
                        backdrop = rememberCombinedBackdrop(effectiveBackdrop, tabsBackdrop),
                        shape = Capsule(),
                        preset = LiquidGlassDynamicPresets.NavigationDroplet,
                        performance = performance,
                        effectProgress = { dropletInteractionProgress },
                        decorationProgress = { dampedDragAnimation.pressProgress },
                        layerBlock = {
                            liquidNavigationSelectionLayer(dampedDragAnimation)
                        },
                        onDrawSurface = {
                            // The accent comes from the recorded tab content. Tinting the whole
                            // active lens hides that content and makes the glass look like a pill.
                            val progress = dropletInteractionProgress
                            val selectionColor =
                                if (isLightSurface) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            drawRect(selectionColor.copy(alpha = 0.10f * (1f - progress)))
                            drawRect(Color.Black.copy(alpha = 0.03f * progress))
                        },
                    )
                    .height(56.dp)
                    .fillMaxWidth(1f / safeTabsCount)
        )
    }
}
