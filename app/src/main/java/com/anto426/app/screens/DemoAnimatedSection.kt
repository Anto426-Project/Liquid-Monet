package com.anto426.app.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.layout.sectionentrance.LiquidSectionEntrance
import com.anto426.liquidmonet.motion.LiquidMotion

internal data class DemoSectionMotion(
    val animation: LiquidScreenEntranceAnimation = LiquidScreenEntranceAnimation.FadeUp,
    val onAnimationSelected: (LiquidScreenEntranceAnimation) -> Unit = {},
    val replay: Int = 0,
    val onReplay: () -> Unit = {},
)

internal val LocalDemoSectionMotion = staticCompositionLocalOf { DemoSectionMotion() }
internal val LocalDemoSectionViewport = staticCompositionLocalOf<Rect?> { null }

/**
 * A section keeps its measured space and child state while waiting to enter the visible viewport.
 * Progress is read in the drawing layer only; no extra backdrop or per-frame child composition.
 * Once revealed it stays visible, including when scrolled out and back in. Replay changes motion,
 * without recreating text fields, player state, accordion state or any other child controls.
 */
@Composable
internal fun DemoAnimatedSection(
    modifier: Modifier = Modifier,
    order: Int = 0,
    content: @Composable ColumnScope.() -> Unit,
) {
    val motion = LocalDemoSectionMotion.current
    LiquidSectionEntrance(
        modifier = modifier.fillMaxWidth(),
        animation = motion.animation,
        delayMillis = LiquidMotion.sectionDelayMillis(order),
        replayKey = motion.replay,
        viewportBoundsInWindow = LocalDemoSectionViewport.current,
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}
