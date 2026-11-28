package com.anto426.liquidmonet.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.glass.overlay.LiquidGlassModalOverlayHost
import com.anto426.liquidmonet.glass.overlay.LiquidGlassOverlayHost
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassModalOverlayState
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassOverlayState
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassModalOverlayState
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayState
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

internal val LocalLiquidGlassTopBarScrollBehavior =
    compositionLocalOf<TopAppBarScrollBehavior?> { null }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidGlassScene(
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
    background: @Composable BoxScope.() -> Unit = {},
    topBar: (@Composable BoxScope.(backdrop: Backdrop) -> Unit)? = null,
    bottomBar: (@Composable BoxScope.(backdrop: Backdrop) -> Unit)? = null,
    overlay: (@Composable BoxScope.(backdrop: Backdrop) -> Unit)? = null,
    content: @Composable BoxScope.(backdrop: LayerBackdrop) -> Unit
) {
    val backgroundBackdrop = rememberLayerBackdrop()
    val contentBackdrop = rememberLayerBackdrop()
    // Dropdown / menu glass must refract every surface visibly beneath it, including the topBar,
    // bottomBar, content cards, and background.
    val overlayBackdrop = rememberLayerBackdrop()
    // Modal glass must refract the complete scene that is visibly behind it, including bars,
    // floating actions and non-modal overlays. Recording only the scrollable content makes a
    // bottom sheet sample an empty/dark area where those elements actually are.
    val modalBackdrop = rememberLayerBackdrop()
    val overlayState = rememberLiquidGlassOverlayState()
    val modalOverlayState = rememberLiquidGlassModalOverlayState()
    CompositionLocalProvider(
        LocalLiquidGlassOverlayState provides overlayState,
        LocalLiquidGlassModalOverlayState provides modalOverlayState,
        LocalLiquidGlassContentBackdrop provides backgroundBackdrop,
        LocalLiquidGlassTopBarScrollBehavior provides scrollBehavior,
        LocalLiquidGlassLegibility provides legibility
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    overlayState.hostBoundsInWindow = coordinates.boundsInWindow()
                }
                .then(
                    if (scrollBehavior != null) {
                        Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    } else {
                        Modifier
                    }
                )
        ) {
            val needsChromeBackdrop = topBar != null || bottomBar != null || overlay != null
            val sceneBaseContent: @Composable BoxScope.() -> Unit = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (needsChromeBackdrop) {
                                Modifier.layerBackdrop(contentBackdrop)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    Box(modifier = Modifier.fillMaxSize().layerBackdrop(backgroundBackdrop)) {
                        background()
                    }
                    content(backgroundBackdrop)
                }
                val chromeBackdrop = if (needsChromeBackdrop) contentBackdrop else backgroundBackdrop
                CompositionLocalProvider(LocalLiquidGlassContentBackdrop provides chromeBackdrop) {
                    topBar?.invoke(this, chromeBackdrop)
                    bottomBar?.invoke(this, chromeBackdrop)
                    overlay?.invoke(this, chromeBackdrop)
                }
            }

            // Dropdown menu overlay must refract every surface that is beneath it (topBar, bottomBar,
            // content cards, and background). Recording is active only while an overlay entry exists.
            val isOverlayActive = overlayState.entry != null
            val sceneWithOverlay: @Composable BoxScope.() -> Unit = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isOverlayActive) {
                                Modifier.layerBackdrop(overlayBackdrop)
                            } else {
                                Modifier
                            }
                        )
                ) {
                    sceneBaseContent()
                }

                CompositionLocalProvider(LocalLiquidGlassContentBackdrop provides overlayBackdrop) {
                    LiquidGlassOverlayHost(overlayState)
                }
            }

            // A third full-screen recording is needed only while a dialog/sheet exists. Avoiding
            // it on the normal path matters because the animated background invalidates layers
            // continuously, even when no modal is visible.
            // Keep sceneContent at one stable composition call site. Moving it between two
            // branches when a modal opens disposes the whole screen, resets the caller's
            // `isDialogOpen`/`isSheetOpen` state and immediately removes the portal entry.
            // Only the recording modifier is conditional; the UI tree keeps its identity.
            val isModalActive = modalOverlayState.activeEntry != null
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isModalActive) {
                            Modifier.layerBackdrop(modalBackdrop)
                        } else {
                            Modifier
                        }
                    )
            ) {
                sceneWithOverlay()
            }

            // Keep the modal host outside the recorded layer to prevent recursive sampling.
            CompositionLocalProvider(LocalLiquidGlassContentBackdrop provides modalBackdrop) {
                LiquidGlassModalOverlayHost(modalOverlayState)
            }
        }
    }
}
