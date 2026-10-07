package com.anto426.liquidmonet.components.layout.sectionentrance

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.layout.sectionentrance.motion.LiquidSectionEntranceMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance

/**
 * Reveals a section when it first reaches the visible viewport, keeping its measured space and
 * child state. Scrolling out and back in does not replay the entrance. Change [replayKey] to
 * replay without recreating controls; use a stable outer `key(sectionId)` for a different section.
 *
 * [viewportBoundsInWindow] optionally excludes bars or other covered areas, in window coordinates.
 * By default the window is used; ancestor clipping is also respected. At least half the section's
 * width and either 32 dp or a quarter of its height must be visible before the entrance starts.
 * [delayMillis] starts at that moment, and must be non-negative. Reduced motion reveals immediately.
 *
 * Progress is read only in the drawing layer. This component adds no scene, backdrop or blur pass.
 * For content that should leave composition after hiding, use `LiquidScreenEntrance` instead.
 */
@Composable
fun LiquidSectionEntrance(
    modifier: Modifier = Modifier,
    animation: LiquidScreenEntranceAnimation = LiquidScreenEntranceAnimation.FadeUp,
    delayMillis: Int = 0,
    replayKey: Any? = Unit,
    viewportBoundsInWindow: Rect? = null,
    content: @Composable () -> Unit,
) {
    require(delayMillis >= 0) { "LiquidSectionEntrance delayMillis must be non-negative." }
    val performance = LocalLiquidGlassPerformance.current
    val enabled = LiquidSectionEntranceMotion.motionEnabled(performance, animation)
    val windowSize = LocalWindowInfo.current.containerSize
    val viewport = viewportBoundsInWindow ?: Rect(0f, 0f, windowSize.width.toFloat(), windowSize.height.toFloat())
    val distance = with(LocalDensity.current) { 24.dp.toPx() }
    val threshold = with(LocalDensity.current) { 32.dp.toPx() }
    var revealed by remember { mutableStateOf(false) }
    var pendingBounds by remember { mutableStateOf<Pair<Rect, IntSize>?>(null) }
    val progress = LiquidSectionEntranceMotion.progress(performance, animation, revealed, delayMillis, replayKey)

    fun revealIfVisible(bounds: Rect, size: IntSize) {
        if (!revealed && size.width > 0 && size.height > 0) {
            val visible = bounds.intersect(viewport)
            if (visible.width >= size.width * .5f && visible.height >= minOf(threshold, size.height * .25f)) {
                revealed = true
                pendingBounds = null
            }
        }
    }

    LaunchedEffect(viewport, threshold) {
        pendingBounds?.let { (bounds, size) -> revealIfVisible(bounds, size) }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            if (!revealed) {
                val bounds = coordinates.boundsInWindow()
                pendingBounds = bounds to coordinates.size
                revealIfVisible(bounds, coordinates.size)
            }
        }.graphicsLayer {
            val fraction = if (!enabled) 1f else if (!revealed) 0f else progress.value.coerceIn(0f, 1f)
            alpha = fraction
            translationY = when (animation) {
                LiquidScreenEntranceAnimation.FadeUp -> distance * (1f - fraction)
                LiquidScreenEntranceAnimation.FadeDown -> -distance * (1f - fraction)
                else -> 0f
            }
            val scale = if (animation == LiquidScreenEntranceAnimation.Scale) .94f + .06f * fraction else 1f
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin.Center
            clip = false
        },
        propagateMinConstraints = true,
    ) { content() }
}
