package com.anto426.liquidmonet.components.menu.internal

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.anto426.liquidmonet.components.internal.InteractiveHighlight

/** One selection owner per menu; pointer movement only invalidates the highlighted rows. */
internal class LiquidMenuDragSelection {
    private class Item(val bounds: () -> Rect?, val enabled: () -> Boolean, val onClick: () -> Unit)

    private val items = mutableMapOf<Any, Item>()
    var activeKey: Any? by mutableStateOf(null)
        private set

    fun register(key: Any, bounds: () -> Rect?, enabled: () -> Boolean, onClick: () -> Unit) {
        items[key] = Item(bounds, enabled, onClick)
    }

    fun unregister(key: Any) {
        items.remove(key)
        if (activeKey == key) cancel()
    }

    fun move(position: Offset) {
        activeKey =
            items.entries
                .firstOrNull { (_, item) ->
                    item.enabled() && item.bounds()?.contains(position) == true
                }
                ?.key
    }

    fun release(position: Offset) {
        // Resolve again: rows can move, disappear or become disabled during a gesture.
        move(position)
        val item = items[activeKey]
        cancel()
        item?.onClick?.invoke()
    }

    fun cancel() {
        activeKey = null
    }
}

internal val LocalLiquidMenuDragSelection =
    staticCompositionLocalOf<LiquidMenuDragSelection?> { null }

@Composable
internal fun Modifier.liquidMenuDragSelection(
    state: LiquidMenuDragSelection,
    highlight: InteractiveHighlight,
): Modifier {
    // Input is attached before the optical graphics transform. Resolve window coordinates at
    // each event, rather than caching rectangles from before the opening/press animation.
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    return onGloballyPositioned { coordinates = it }
        .pointerInput(state) {
            awaitEachGesture {
                val down =
                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                highlight.press(down.position)
                var dragging = false
                try {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (
                            event.changes.any { it.id != down.id && it.pressed } ||
                                change.isConsumed
                        ) {
                            if (dragging) event.changes.forEach { it.consume() }
                            break
                        }
                        val layout = coordinates?.takeIf { it.isAttached } ?: break
                        highlight.move(change.position)
                        if (!change.pressed) {
                            if (dragging) {
                                change.consume()
                                state.release(layout.localToWindow(change.position))
                            }
                            break
                        }
                        if (
                            !dragging &&
                                (change.position - down.position).getDistance() >
                                    viewConfiguration.touchSlop
                        ) {
                            dragging = true
                        }
                        if (dragging) {
                            // Claim movement before the original row's clickable sees it. Its
                            // normal tap, keyboard and accessibility behavior remains intact.
                            change.consume()
                            state.move(layout.localToWindow(change.position))
                        } else {
                            val finalEvent = awaitPointerEvent(PointerEventPass.Final)
                            if (finalEvent.changes.any { it.id == down.id && it.isConsumed }) break
                        }
                    }
                } finally {
                    state.cancel()
                    highlight.release()
                }
            }
        }
}

@Composable
internal fun Modifier.liquidMenuDragTarget(
    state: LiquidMenuDragSelection?,
    key: Any,
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier {
    if (state == null) return this
    var coordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val latestEnabled by rememberUpdatedState(enabled)
    val latestOnClick by rememberUpdatedState(onClick)
    DisposableEffect(state, key) {
        state.register(
            key = key,
            bounds = { coordinates?.takeIf { it.isAttached }?.boundsInWindow() },
            enabled = { latestEnabled },
            onClick = { latestOnClick() },
        )
        onDispose { state.unregister(key) }
    }
    return onGloballyPositioned { coordinates = it }
}
