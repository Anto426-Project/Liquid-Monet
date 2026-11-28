package com.anto426.liquidmonet.components.menu.dropdownmenu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.anto426.liquidmonet.components.menu.dropdownmenu.internal.LiquidGlassMenuSurface
import com.anto426.liquidmonet.components.menu.dropdownmenu.motion.rememberLiquidDropdownEnterTransition
import com.anto426.liquidmonet.components.menu.dropdownmenu.motion.rememberLiquidDropdownExitTransition
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.glass.LiquidGlassLegibility
import com.anto426.liquidmonet.glass.LocalLiquidGlassLegibility
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.overlay.LiquidGlassDropdownPlacement
import com.anto426.liquidmonet.glass.overlay.LiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassOverlayState
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.kyant.backdrop.Backdrop

/**
 * Liquid dropdown with one canonical API. When [anchorState] is attached to the trigger through
 * [liquidGlassOverlayAnchor], the menu is scene-hosted and samples the composed content beneath it;
 * otherwise it falls back to a platform popup.
 */
@Composable
fun LiquidDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    anchorState: LiquidGlassOverlayAnchorState? = null,
    placement: LiquidGlassDropdownPlacement = LiquidGlassDropdownPlacement.BelowEnd,
    offset: DpOffset = DpOffset(0.dp, 8.dp),
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
    content: @Composable ColumnScope.() -> Unit,
) {
    val overlayState = LocalLiquidGlassOverlayState.current
    val overlayKey = remember { Any() }
    val anchorBounds = anchorState?.boundsInWindow
    val canUseHostedOverlay = overlayState != null && anchorBounds != null
    val latestOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val latestBackdrop by rememberUpdatedState(backdrop)
    val latestPolicy by rememberUpdatedState(backdropPolicy)
    val latestLegibility by rememberUpdatedState(legibility)
    val latestContent by rememberUpdatedState(content)
    val hostedContent: @Composable (dismiss: () -> Unit) -> Unit = {
        CompositionLocalProvider(LocalLiquidGlassLegibility provides latestLegibility) {
            LiquidGlassMenuSurface(
                modifier = modifier,
                backdrop = latestBackdrop,
                backdropPolicy = latestPolicy,
            ) {
                latestContent()
            }
        }
    }

    LaunchedEffect(
        expanded,
        overlayState,
        anchorBounds,
        offset,
        placement,
        canUseHostedOverlay,
    ) {
        if (overlayState != null && anchorBounds != null) {
            if (expanded) {
                overlayState.show(
                    key = overlayKey,
                    anchorBoundsInWindow = anchorBounds,
                    placement = placement,
                    offset = offset,
                    onDismissRequest = { latestOnDismissRequest() },
                    content = hostedContent,
                )
            } else {
                overlayState.dismiss(overlayKey, notifyOnDismiss = false)
            }
        }
    }
    DisposableEffect(overlayState, overlayKey) {
        onDispose { overlayState?.removeImmediately(overlayKey) }
    }

    val fallbackBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val fallbackVisibility = remember { MutableTransitionState(false) }
    fallbackVisibility.targetState = expanded && !canUseHostedOverlay
    if (fallbackVisibility.currentState || fallbackVisibility.targetState) {
        val density = LocalDensity.current
        val popupOffset =
            with(density) {
                IntOffset(offset.x.roundToPx(), offset.y.roundToPx())
            }
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = true, clippingEnabled = false),
            offset = popupOffset,
        ) {
            Box(modifier = Modifier.padding(12.dp).graphicsLayer(clip = false)) {
                AnimatedVisibility(
                    visibleState = fallbackVisibility,
                    modifier = Modifier.graphicsLayer(clip = false),
                    enter = rememberLiquidDropdownEnterTransition(placement),
                    exit = rememberLiquidDropdownExitTransition(placement),
                ) {
                    LiquidGlassMenuSurface(
                        modifier = modifier,
                        backdrop = fallbackBackdrop,
                        backdropPolicy = backdropPolicy,
                    ) {
                        content()
                    }
                }
            }
        }
    }
}
