package com.anto426.liquidmonet.components.menu.dropdownmenu.motion

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.anto426.liquidmonet.glass.overlay.LiquidGlassDropdownPlacement
import com.anto426.liquidmonet.glass.overlay.resolveMenuTransformOrigin
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.LiquidPopupMotion

@Composable
internal fun rememberLiquidDropdownEnterTransition(
    placement: LiquidGlassDropdownPlacement = LiquidGlassDropdownPlacement.BelowEnd
): EnterTransition {
    val performance = LocalLiquidGlassPerformance.current
    val origin = remember(placement) { resolveMenuTransformOrigin(placement) }
    return remember(performance, origin) { LiquidPopupMotion.enter(performance, origin) }
}

@Composable
internal fun rememberLiquidDropdownExitTransition(
    placement: LiquidGlassDropdownPlacement = LiquidGlassDropdownPlacement.BelowEnd
): ExitTransition {
    val performance = LocalLiquidGlassPerformance.current
    val origin = remember(placement) { resolveMenuTransformOrigin(placement) }
    return remember(performance, origin) { LiquidPopupMotion.exit(performance, origin) }
}
