package com.anto426.liquidmonet.components.internal

import androidx.compose.ui.graphics.Shape
import com.kyant.shapes.Capsule

/** Shared visual and interaction rules for compact Liquid Monet controls. */
internal object LiquidControlDefaults {
    val shape: Shape = Capsule()

    const val pressedScale: Float = 0.96f
    const val deformationFactor: Float = 0.35f
}
