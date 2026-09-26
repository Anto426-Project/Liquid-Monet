package com.anto426.liquidmonet.components.selection.backgroundselector

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.components.selection.select.LiquidSelect
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

/** Selects one of the Liquid Monet scene background effects through the canonical select menu. */
@Composable
fun LiquidBackgroundSelector(
    selectedEffect: LiquidBackgroundEffect,
    onEffectSelected: (LiquidBackgroundEffect) -> Unit,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val effects =
        listOf(
            Triple(
                LiquidBackgroundEffect.Aurora,
                "Sfumature Fluide (Aurora)",
                "Campi cromatici e sfumature morbide in moto organico",
            ),
            Triple(
                LiquidBackgroundEffect.MeshGlow,
                "Mesh Cromatica (MeshGlow)",
                "5 poli di colore fusi in una transizione fluida continua",
            ),
            Triple(
                LiquidBackgroundEffect.OrbitalPulse,
                "Pulsazione Radiale (Pulse)",
                "Sfumature concentriche ad espansione e respirazione cromatica",
            ),
            Triple(
                LiquidBackgroundEffect.RadiantBeam,
                "Gradiente Zenitale (Beam)",
                "Sfumatura atmosferica verticale con luce zenitale diffusa",
            ),
        )

    LiquidSelect(
        items = effects.map { it.first },
        selectedItem = selectedEffect,
        onItemSelected = onEffectSelected,
        label = "Effetto di Sfondo Dinamico",
        itemLabel = { effect -> effects.first { it.first == effect }.second },
        itemSubtitle = { effect -> effects.first { it.first == effect }.third },
        itemIcon = { effect ->
            when (effect) {
                LiquidBackgroundEffect.RadiantBeam -> LiquidIcons.Star
                LiquidBackgroundEffect.Aurora -> LiquidIcons.Visibility
                LiquidBackgroundEffect.MeshGlow -> LiquidIcons.Palette
                LiquidBackgroundEffect.OrbitalPulse -> LiquidIcons.Analytics
            }
        },
        modifier = modifier,
        backdropState = backdropState,
    )
}
