package com.anto426.liquidmonet.glass

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import com.kyant.backdrop.Backdrop
import kotlin.test.Test
import kotlin.test.assertSame

class LiquidGlassBackdropTest {
    private class StubBackdrop(val name: String) : Backdrop {
        override val isCoordinatesDependent: Boolean = false
        override fun DrawScope.drawBackdrop(
            density: Density,
            coordinates: androidx.compose.ui.layout.LayoutCoordinates?,
            layerBlock: (androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit)?,
        ) {}
    }

    @Test
    fun sceneFirstPrefersSceneBackdropOverExplicit() {
        val scene = StubBackdrop("scene")
        val explicit = StubBackdrop("explicit")

        val resolved =
            selectLiquidGlassBackdrop(
                explicitBackdrop = explicit,
                sceneBackdrop = scene,
                policy = LiquidGlassBackdropPolicy.SceneFirst,
            )

        assertSame(scene, resolved)
    }

    @Test
    fun sceneFirstFallsBackToExplicitWhenSceneIsMissingOrEmpty() {
        val explicit = StubBackdrop("explicit")

        assertSame(
            explicit,
            selectLiquidGlassBackdrop(
                explicitBackdrop = explicit,
                sceneBackdrop = null,
                policy = LiquidGlassBackdropPolicy.SceneFirst,
            ),
        )
        assertSame(
            explicit,
            selectLiquidGlassBackdrop(
                explicitBackdrop = explicit,
                sceneBackdrop = emptyLiquidGlassBackdrop(),
                policy = LiquidGlassBackdropPolicy.SceneFirst,
            ),
        )
    }

    @Test
    fun explicitFirstPrefersExplicitBackdropOverScene() {
        val scene = StubBackdrop("scene")
        val explicit = StubBackdrop("explicit")

        val resolved =
            selectLiquidGlassBackdrop(
                explicitBackdrop = explicit,
                sceneBackdrop = scene,
                policy = LiquidGlassBackdropPolicy.ExplicitFirst,
            )

        assertSame(explicit, resolved)
    }

    @Test
    fun explicitFirstFallsBackToSceneWhenExplicitIsEmpty() {
        val scene = StubBackdrop("scene")

        val resolved =
            selectLiquidGlassBackdrop(
                explicitBackdrop = emptyLiquidGlassBackdrop(),
                sceneBackdrop = scene,
                policy = LiquidGlassBackdropPolicy.ExplicitFirst,
            )

        assertSame(scene, resolved)
    }

    @Test
    fun returnsEmptySentinelWhenBothBackdropsAreAbsent() {
        val resolved =
            selectLiquidGlassBackdrop(
                explicitBackdrop = emptyLiquidGlassBackdrop(),
                sceneBackdrop = null,
                policy = LiquidGlassBackdropPolicy.ExplicitFirst,
            )

        assertSame(emptyLiquidGlassBackdrop(), resolved)
    }
}
