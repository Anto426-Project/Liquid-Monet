package com.anto426.app

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
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
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import kotlin.math.abs
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

/** Real layered coordinates and AGSL optics, exercised across the saved sampling budgets. */
class LiquidBackdropResolutionTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()
    private var sampling by mutableFloatStateOf(1f)
    private var lensStrength by mutableFloatStateOf(1f)
    private var lensHeight by mutableFloatStateOf(18f)
    private var lensAmount by mutableFloatStateOf(28f)
    private var dispersion by mutableStateOf(true)

    private fun showGlass(optical: Boolean) {
        assumeTrue(Build.VERSION.SDK_INT >= 33)
        compose.setContent {
            val backdrop = rememberLayerBackdrop()
            Box(Modifier.size(220.dp, 180.dp)) {
                Canvas(Modifier.size(220.dp, 180.dp).layerBackdrop(backdrop)) {
                    drawRect(Color.Yellow)
                    repeat(11) { stripe ->
                        drawRect(
                            Color.Blue,
                            Offset(stripe * 20.dp.toPx(), 0f),
                            Size(10.dp.toPx(), size.height),
                        )
                    }
                }
                Box(
                    Modifier.offset(43.dp, 37.dp)
                        .size(140.dp, 110.dp)
                        .testTag("glass")
                        .drawBackdrop(
                            backdrop,
                            { RoundedCornerShape(20.dp) },
                            resolutionScale = sampling,
                            effects = {
                                if (optical) {
                                    blur(1.dp.toPx())
                                    lens(
                                        lensHeight.dp.toPx(),
                                        lensAmount.dp.toPx() * lensStrength,
                                        depthEffect = true,
                                chromaticAberration = dispersion,
                                    )
                                }
                            },
                            highlight = null,
                            shadow = null,
                        )
                        .drawBehind {
                            // Foreground content must remain sharp and at its original location.
                            drawRect(
                                Color.Red,
                                Offset(size.width / 2, size.height / 2),
                                Size(2.dp.toPx(), 12.dp.toPx()),
                            )
                        }
                )
            }
        }
        compose.waitForIdle()
    }

    @Test
    fun reducedResolutionPreservesBackdropPositionAndForeground() {
        showGlass(optical = false)
        val original = compose.onNodeWithTag("glass").captureToImage()
        for (scale in listOf(0.5f, 0.67f, 0.85f)) {
            compose.runOnIdle { sampling = scale }
            val reduced = compose.onNodeWithTag("glass").captureToImage()
            assertTrue(
                "Backdrop alignment changed at $scale",
                difference(original, reduced) < 0.035,
            )
        }
    }

    @Test
    fun halfResolutionRetainsLensDepth() {
        showGlass(optical = true)
        val original = compose.onNodeWithTag("glass").captureToImage()
        compose.runOnIdle { sampling = 0.5f }
        val reduced = compose.onNodeWithTag("glass").captureToImage()
        assertTrue("Downsampling changed lens geometry", difference(original, reduced) < 0.10)
        compose.runOnIdle { lensStrength = 0f }
        val flat = compose.onNodeWithTag("glass").captureToImage()
        assertTrue("Refraction disappeared at half resolution", difference(reduced, flat) > 0.015)
    }

    @Test
    fun minimumAndMaximumRefractionKeepTheirGeometryAndSharpForegroundAtHalfResolution() {
        showGlass(optical = true)
        val extremes =
            listOf(
                LiquidGlassPresets.Interactive to 0.4f,
                LiquidGlassPresets.Immersive to 1f,
            )
        for ((preset, strength) in extremes) {
            compose.runOnIdle {
                sampling = 1f
                lensStrength = 1f
                lensHeight = preset.refractionHeight.value * strength
                lensAmount = preset.refractionAmount.value * strength
                dispersion = strength == 1f
            }
            val full = compose.onNodeWithTag("glass").captureToImage()
            compose.runOnIdle { sampling = 0.5f }
            val half = compose.onNodeWithTag("glass").captureToImage()
            assertTrue("Lens geometry changed at strength $strength", difference(full, half) < 0.10)
            val pixels = half.toPixelMap()
            val foreground = pixels[pixels.width / 2 + 1, pixels.height / 2 + 1]
            assertTrue(
                "Foreground was distorted",
                foreground.red > 0.95f && foreground.green < 0.05f && foreground.blue < 0.05f,
            )
            compose.runOnIdle { lensStrength = 0f }
            val flat = compose.onNodeWithTag("glass").captureToImage()
            assertTrue("Lens disappeared at strength $strength", difference(half, flat) > 0.003)
        }
    }

    private fun difference(first: ImageBitmap, second: ImageBitmap): Double {
        val a = first.toPixelMap()
        val b = second.toPixelMap()
        var error = 0.0
        for (y in 0 until a.height) for (x in 0 until a.width) {
            error +=
                abs(a[x, y].red - b[x, y].red) +
                    abs(a[x, y].green - b[x, y].green) +
                    abs(a[x, y].blue - b[x, y].blue)
        }
        return error / (a.width * a.height * 3)
    }
}
