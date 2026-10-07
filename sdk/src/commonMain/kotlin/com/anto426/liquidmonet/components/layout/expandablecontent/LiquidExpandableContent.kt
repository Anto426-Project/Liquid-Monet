package com.anto426.liquidmonet.components.layout.expandablecontent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
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
    val transitions = LiquidExpandableContentMotion.transitions(LocalLiquidGlassPerformance.current)
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = transitions.first,
        exit = transitions.second,
        label = label,
        content = content,
    )
}
