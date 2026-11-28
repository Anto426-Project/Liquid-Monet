package com.anto426.liquidmonet.glass

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/** Controls whether scene or explicitly supplied content is sampled first. */
enum class LiquidGlassBackdropPolicy {
    /** Prefer the backdrop installed by [LiquidGlassScene]. */
    SceneFirst,

    /** Prefer a local [LayerBackdrop] recorded by a custom component. */
    ExplicitFirst,
}

/** Creates and remembers a local [LayerBackdrop] for custom refractive glass hosts. */
@Composable
fun rememberLiquidGlassBackdrop(): LayerBackdrop = rememberLayerBackdrop()

/** Returns the canonical empty [Backdrop] sentinel used when no backdrop is supplied. */
fun emptyLiquidGlassBackdrop(): Backdrop = emptyBackdrop()

/** Records this composable's rendered output into [backdrop] so floating glass can refract it. */
fun Modifier.liquidGlassBackdropSource(backdrop: LayerBackdrop): Modifier =
    layerBackdrop(backdrop)

/** Pure precedence resolver shared by every SDK component and unit-tested directly. */
internal fun selectLiquidGlassBackdrop(
    explicitBackdrop: Backdrop,
    sceneBackdrop: Backdrop?,
    policy: LiquidGlassBackdropPolicy,
): Backdrop {
    val empty = emptyBackdrop()
    val explicit = explicitBackdrop.takeIf { it != empty }
    val scene = sceneBackdrop?.takeIf { it != empty }
    return when (policy) {
        LiquidGlassBackdropPolicy.SceneFirst -> scene ?: explicit ?: empty
        LiquidGlassBackdropPolicy.ExplicitFirst -> explicit ?: scene ?: empty
    }
}

/** Resolves the backdrop through one shared precedence rule for every SDK component. */
@Composable
internal fun resolveLiquidGlassBackdrop(
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    policy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
): Backdrop =
    selectLiquidGlassBackdrop(
        explicitBackdrop = backdrop,
        sceneBackdrop = LocalLiquidGlassContentBackdrop.current,
        policy = policy,
    )
