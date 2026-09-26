package com.anto426.app

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Verify the public card and ordinary surface pipelines, not just the policy booleans. */
class LiquidCardMaterialTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()
    private var useCard by mutableStateOf(true)
    private var blurStrength by mutableFloatStateOf(1f)
    private var lensStrength by mutableFloatStateOf(1f)

    private fun showMaterial() {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        compose.setContent {
            val performance = LiquidGlassPerformanceState.Fallback.copy(
                qualityTier = LiquidGlassQualityTier.ULTRA,
                opticalQualityTier = LiquidGlassQualityTier.ULTRA,
                blurScale = blurStrength, refractionScale = lensStrength,
                chromaticAberrationScale = 1f
            )
            MaterialTheme {
                CompositionLocalProvider(LocalLiquidGlassPerformance provides performance) {
                    val backdrop = rememberLayerBackdrop()
                    Box(Modifier.size(220.dp, 180.dp)) {
                        Canvas(Modifier.size(220.dp, 180.dp).layerBackdrop(backdrop)) {
                            drawRect(Color.Yellow)
                            repeat(22) { stripe ->
                                drawRect(Color.Blue, Offset(stripe * 10.dp.toPx(), 0f),
                                    Size(5.dp.toPx(), size.height))
                            }
                        }
                        val panel = Modifier.offset(43.dp, 37.dp).size(140.dp, 110.dp).testTag("material")
                        val shape = RoundedCornerShape(20.dp)
                        if (useCard) {
                            LiquidCard(modifier = panel, backdropState = backdrop, shape = shape) {}
                        } else {
                            Box(panel.liquidGlass(backdrop = backdrop, shape = shape))
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
    }

    @Test fun onlyTheCardSkipsBlur() {
        showMaterial()
        val card = capture()
        compose.runOnIdle { blurStrength = 0f }
        assertTrue("Card still responds to the removed blur pass", difference(card, capture()) < 0.002)
        compose.runOnIdle { useCard = false }
        val unblurredSurface = capture()
        compose.runOnIdle { blurStrength = 1f }
        assertTrue("Ordinary surfaces lost their blur", difference(unblurredSurface, capture()) > 0.015)
    }

    @Test fun lighterCardStillHasVisibleRefraction() {
        showMaterial()
        val refracted = capture()
        compose.runOnIdle { lensStrength = 0f }
        assertTrue("The lighter card lost lens depth", difference(refracted, capture()) > 0.015)
    }

    private fun capture() = compose.onNodeWithTag("material").captureToImage()

    private fun difference(first: ImageBitmap, second: ImageBitmap): Double {
        val a = first.toPixelMap()
        val b = second.toPixelMap()
        var error = 0.0
        for (y in 0 until a.height) for (x in 0 until a.width) {
            error += abs(a[x, y].red - b[x, y].red) + abs(a[x, y].green - b[x, y].green) +
                abs(a[x, y].blue - b[x, y].blue)
        }
        return error / (a.width * a.height * 3)
    }
}
