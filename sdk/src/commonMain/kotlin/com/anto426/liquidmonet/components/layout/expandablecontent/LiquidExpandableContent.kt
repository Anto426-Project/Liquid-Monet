package com.anto426.liquidmonet.components.layout.expandablecontent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.layout.expandablecontent.motion.LiquidExpandableContentMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance

/** Expands/collapses related controls, removing hidden content after the exit completes. */
@Composable
fun LiquidExpandableContent(
    visible: Boolean,
    modifier: Modifier = Modifier,
    label: String = "LiquidExpandableContent",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val (enter, exit) = LiquidExpandableContentMotion.transitions(LocalLiquidGlassPerformance.current)
    LiquidExpandableContent(visible, enter, exit, modifier, label, content)
}

@Composable
internal fun LiquidExpandableContent(
    visible: Boolean,
    enter: EnterTransition,
    exit: ExitTransition,
    modifier: Modifier = Modifier,
    label: String = "LiquidExpandableContent",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        label = label,
        content = content,
    )
}
