package com.anto426.liquidmonet.glass.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.anto426.liquidmonet.glass.LiquidGlassZIndex
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.LiquidPopupMotion
import com.anto426.liquidmonet.motion.rememberLiquidPredictiveBackState
import com.kyant.backdrop.Backdrop
import kotlin.math.roundToInt

/**
 * Coordinates a single glass overlay rendered inside [LiquidGlassScene].
 *
 * Rendering in the scene, instead of in a platform popup window, keeps the overlay in the same
 * graphics tree as the recorded Backdrop layer. Consumers normally interact with this state through
 * the menu components rather than calling it directly.
 */
@Stable
class LiquidGlassOverlayState internal constructor() {

    internal var entry: LiquidGlassOverlayEntry? by mutableStateOf(null)
        private set

    internal var hostBoundsInWindow: Rect by mutableStateOf(Rect.Zero)

    internal fun show(
        key: Any,
        anchorBoundsInWindow: Rect,
        placement: LiquidGlassOverlayPlacement,
        offset: DpOffset,
        onDismissRequest: () -> Unit,
        content: @Composable (dismiss: () -> Unit) -> Unit,
    ) {
        val current = entry
        if (current != null && current.key != key && current.visible) {
            current.onDismissRequest()
        }
        entry =
            LiquidGlassOverlayEntry(
                key = key,
                anchorBoundsInWindow = anchorBoundsInWindow,
                placement = placement,
                offset = offset,
                visible = true,
                onDismissRequest = onDismissRequest,
                content = content,
            )
    }

    internal fun updateAnchor(key: Any, anchorBoundsInWindow: Rect) {
        val current = entry ?: return
        if (current.key == key && current.anchorBoundsInWindow != anchorBoundsInWindow) {
            entry = current.copy(anchorBoundsInWindow = anchorBoundsInWindow)
        }
    }

    internal fun dismiss(key: Any? = null, notifyOnDismiss: Boolean = true) {
        val current = entry ?: return
        if (key != null && current.key != key) return
        if (!current.visible) return

        entry = current.copy(visible = false)
        if (notifyOnDismiss) current.onDismissRequest()
    }

    internal fun removeImmediately(key: Any) {
        if (entry?.key == key) entry = null
    }

    internal fun removeAfterExit(key: Any) {
        val current = entry ?: return
        if (current.key == key && !current.visible) entry = null
    }
}

/** Bounds holder used to anchor a hosted overlay to a real laid-out component. */
@Stable
class LiquidGlassOverlayAnchorState internal constructor() {

    var boundsInWindow: Rect? by mutableStateOf(null)
        internal set
}

@Composable
fun rememberLiquidGlassOverlayAnchorState(): LiquidGlassOverlayAnchorState = remember {
    LiquidGlassOverlayAnchorState()
}

/** Captures the final window-space bounds used by [LiquidGlassScene]'s overlay host. */
fun Modifier.liquidGlassOverlayAnchor(state: LiquidGlassOverlayAnchorState): Modifier =
    onGloballyPositioned { coordinates ->
        state.boundsInWindow = coordinates.boundsInWindow()
    }

/** Direction and alignment options for glass dropdown menus and overlays. */
enum class LiquidGlassDropdownPlacement {
    /** Appears below the trigger, aligned to its trailing edge (right in LTR). Default. */
    BelowEnd,

    /** Appears below the trigger, aligned to its leading edge (left in LTR). */
    BelowStart,

    /** Appears below the trigger, centered horizontally. */
    BelowCenter,

    /** Appears above the trigger, aligned to its trailing edge. */
    AboveEnd,

    /** Appears above the trigger, aligned to its leading edge. */
    AboveStart,

    /** Appears above the trigger, centered horizontally. */
    AboveCenter,

    /** Legacy anchor placement overlaying the anchor top edge. */
    AnchorTopEnd,

    /** Automatically determines whether to open above or below depending on available space. */
    Auto,
}

typealias LiquidGlassOverlayPlacement = LiquidGlassDropdownPlacement

internal val LocalLiquidGlassOverlayState =
    staticCompositionLocalOf<LiquidGlassOverlayState?> { null }

/** Backdrop containing the fully composed base scene, excluding hosted overlays themselves. */
internal val LocalLiquidGlassContentBackdrop = staticCompositionLocalOf<Backdrop?> { null }

@Composable
internal fun rememberLiquidGlassOverlayState(): LiquidGlassOverlayState = remember {
    LiquidGlassOverlayState()
}

@Composable
internal fun BoxScope.LiquidGlassOverlayHost(state: LiquidGlassOverlayState) {
    val entry = state.entry ?: return
    val performance = LocalLiquidGlassPerformance.current
    var hostBoundsInWindow by remember { mutableStateOf(state.hostBoundsInWindow) }
    val visibility =
        remember(entry.key) {
            MutableTransitionState(false).apply { targetState = entry.visible }
        }

    LaunchedEffect(entry.visible) {
        visibility.targetState = entry.visible
    }
    LaunchedEffect(visibility.isIdle, visibility.currentState) {
        if (visibility.isIdle && !visibility.currentState && !visibility.targetState) {
            state.removeAfterExit(entry.key)
        }
    }

    val predictiveBack =
        rememberLiquidPredictiveBackState(
            onPredictiveBack = { state.dismiss(entry.key) },
            enabled = entry.visible,
        )

    Box(
        modifier =
            Modifier.matchParentSize().zIndex(LiquidGlassZIndex.Menu).onGloballyPositioned {
                coordinates ->
                val bounds = coordinates.boundsInWindow()
                hostBoundsInWindow = bounds
                state.hostBoundsInWindow = bounds
            }
    ) {
        Box(
            modifier =
                Modifier.matchParentSize()
                    .clickable(
                        interactionSource = null,
                        indication = null,
                        enabled = entry.visible,
                        onClick = { state.dismiss(entry.key) },
                    )
                    .clearAndSetSemantics {}
        )

        LiquidGlassOverlayPositioner(
            anchorBoundsInWindow = entry.anchorBoundsInWindow,
            hostBoundsInWindow = hostBoundsInWindow,
            placement = entry.placement,
            offset = entry.offset,
            modifier = Modifier.matchParentSize(),
        ) { _, transformOrigin, isAbove ->
            AnimatedVisibility(
                visibleState = visibility,
                modifier = Modifier.graphicsLayer(clip = false),
                enter = LiquidPopupMotion.enter(performance, transformOrigin, isAbove),
                exit = LiquidPopupMotion.exit(performance, transformOrigin, isAbove),
            ) {
                Box(
                    modifier =
                        Modifier.graphicsLayer {
                            clip = false
                            if (predictiveBack.progress > 0f) {
                                val progress = predictiveBack.progress
                                translationX =
                                    size.width * 0.10f * progress * predictiveBack.edgeDirection
                                scaleX = 1f - progress * 0.06f
                                scaleY = 1f - progress * 0.06f
                                alpha = 1f - progress * 0.18f
                            }
                        }
                ) {
                    entry.content { state.dismiss(entry.key) }
                }
            }
        }
    }
}

internal fun resolveMenuTransformOrigin(
    placement: LiquidGlassDropdownPlacement,
    isAbove: Boolean? = null,
): TransformOrigin {
    val above =
        isAbove
            ?: when (placement) {
                LiquidGlassDropdownPlacement.AboveStart,
                LiquidGlassDropdownPlacement.AboveCenter,
                LiquidGlassDropdownPlacement.AboveEnd -> true
                LiquidGlassDropdownPlacement.BelowStart,
                LiquidGlassDropdownPlacement.BelowCenter,
                LiquidGlassDropdownPlacement.BelowEnd,
                LiquidGlassDropdownPlacement.AnchorTopEnd -> false
                LiquidGlassDropdownPlacement.Auto -> false
            }
    return when {
        above && (placement == LiquidGlassDropdownPlacement.AboveStart) ->
            TransformOrigin(0.08f, 0.96f)
        above && (placement == LiquidGlassDropdownPlacement.AboveCenter) ->
            TransformOrigin(0.50f, 0.96f)
        above -> TransformOrigin(0.92f, 0.96f)
        !above && (placement == LiquidGlassDropdownPlacement.BelowStart) ->
            TransformOrigin(0.08f, 0.04f)
        !above && (placement == LiquidGlassDropdownPlacement.BelowCenter) ->
            TransformOrigin(0.50f, 0.04f)
        else -> TransformOrigin(0.92f, 0.04f)
    }
}

@Composable
private fun LiquidGlassOverlayPositioner(
    anchorBoundsInWindow: Rect,
    hostBoundsInWindow: Rect,
    placement: LiquidGlassDropdownPlacement,
    offset: DpOffset,
    modifier: Modifier = Modifier,
    content:
        @Composable
        (alignment: Alignment, transformOrigin: TransformOrigin, isAbove: Boolean) -> Unit,
) {
    val initialAbove =
        when (placement) {
            LiquidGlassDropdownPlacement.AboveStart,
            LiquidGlassDropdownPlacement.AboveCenter,
            LiquidGlassDropdownPlacement.AboveEnd -> true
            else -> false
        }
    val initialOrigin = remember(placement) { resolveMenuTransformOrigin(placement, initialAbove) }
    var animationAlignment by remember { mutableStateOf(Alignment.TopEnd) }
    var animationOrigin by remember(placement) { mutableStateOf(initialOrigin) }
    var isAboveState by remember(placement) { mutableStateOf(initialAbove) }

    Layout(
        modifier = modifier.graphicsLayer(clip = false),
        content = {
            content(animationAlignment, animationOrigin, isAboveState)
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val margin = 12.dp.roundToPx()
        val maxOverlayWidth = (width - margin * 2).coerceAtLeast(0)
        val maxOverlayHeight = (height - margin * 2).coerceAtLeast(0)
        val placeable =
            measurables
                .singleOrNull()
                ?.measure(
                    constraints.copy(
                        minWidth = 0,
                        minHeight = 0,
                        maxWidth = maxOverlayWidth,
                        maxHeight = maxOverlayHeight,
                    )
                )

        layout(width, height) {
            if (placeable == null) return@layout

            val anchorLeft = anchorBoundsInWindow.left - hostBoundsInWindow.left
            val anchorTop = anchorBoundsInWindow.top - hostBoundsInWindow.top
            val anchorRight = anchorBoundsInWindow.right - hostBoundsInWindow.left
            val anchorBottom = anchorBoundsInWindow.bottom - hostBoundsInWindow.top
            val anchorCenterX = (anchorLeft + anchorRight) / 2f
            val offsetX = offset.x.roundToPx()
            val offsetY = offset.y.roundToPx()

            val minX = margin
            val maxX = (width - margin - placeable.width).coerceAtLeast(minX)

            val preferredX =
                when (placement) {
                    LiquidGlassDropdownPlacement.BelowStart,
                    LiquidGlassDropdownPlacement.AboveStart -> (anchorLeft + offsetX).roundToInt()
                    LiquidGlassDropdownPlacement.BelowCenter,
                    LiquidGlassDropdownPlacement.AboveCenter ->
                        (anchorCenterX - placeable.width / 2f + offsetX).roundToInt()
                    LiquidGlassDropdownPlacement.BelowEnd,
                    LiquidGlassDropdownPlacement.AboveEnd,
                    LiquidGlassDropdownPlacement.AnchorTopEnd,
                    LiquidGlassDropdownPlacement.Auto ->
                        (anchorRight - placeable.width + offsetX).roundToInt()
                }
            val x = preferredX.coerceIn(minX, maxX)

            val belowY = (anchorBottom + offsetY).roundToInt()
            val aboveY = (anchorTop - offsetY - placeable.height).roundToInt()
            val minY = margin
            val maxY = (height - margin - placeable.height).coerceAtLeast(minY)

            val isAbove =
                when (placement) {
                    LiquidGlassDropdownPlacement.AboveStart,
                    LiquidGlassDropdownPlacement.AboveCenter,
                    LiquidGlassDropdownPlacement.AboveEnd -> true
                    LiquidGlassDropdownPlacement.BelowStart,
                    LiquidGlassDropdownPlacement.BelowCenter,
                    LiquidGlassDropdownPlacement.BelowEnd,
                    LiquidGlassDropdownPlacement.AnchorTopEnd -> false
                    LiquidGlassDropdownPlacement.Auto -> (belowY > maxY && aboveY >= minY)
                }

            val preferredY =
                when (placement) {
                    LiquidGlassDropdownPlacement.AnchorTopEnd -> (anchorTop + offsetY).roundToInt()
                    else -> if (isAbove) aboveY else belowY
                }
            val y = preferredY.coerceIn(minY, maxY)

            val targetAlignment =
                when {
                    isAbove && (placement == LiquidGlassDropdownPlacement.AboveStart) ->
                        Alignment.BottomStart
                    isAbove && (placement == LiquidGlassDropdownPlacement.AboveCenter) ->
                        Alignment.BottomCenter
                    isAbove -> Alignment.BottomEnd
                    !isAbove && (placement == LiquidGlassDropdownPlacement.BelowStart) ->
                        Alignment.TopStart
                    !isAbove && (placement == LiquidGlassDropdownPlacement.BelowCenter) ->
                        Alignment.TopCenter
                    else -> Alignment.TopEnd
                }

            val targetOrigin = resolveMenuTransformOrigin(placement, isAbove)

            if (
                animationAlignment != targetAlignment ||
                    animationOrigin != targetOrigin ||
                    isAboveState != isAbove
            ) {
                animationAlignment = targetAlignment
                animationOrigin = targetOrigin
                isAboveState = isAbove
            }

            placeable.place(x, y)
        }
    }
}

internal data class LiquidGlassOverlayEntry(
    val key: Any,
    val anchorBoundsInWindow: Rect,
    val placement: LiquidGlassOverlayPlacement,
    val offset: DpOffset,
    val visible: Boolean,
    val onDismissRequest: () -> Unit,
    val content: @Composable (dismiss: () -> Unit) -> Unit,
)
