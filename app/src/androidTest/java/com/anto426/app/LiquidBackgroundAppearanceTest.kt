package com.anto426.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.glass.LiquidBackground
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.monet.LiquidMonetEngine
import com.anto426.liquidmonet.theme.monet.LiquidMonetSeed
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LiquidBackgroundAppearanceTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    private val mint = LiquidMonetSeed.fromColor(Color(0xFF359D77))
    private val violet = LiquidMonetSeed.fromColor(Color(0xFF9A61D2))

    @Test fun eachBackgroundKeepsTheSelectedLightTint() {
        val intensity = mutableFloatStateOf(1f)
        val dark = mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(
                LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f),
            ) {
                Column(Modifier.testTag("backgrounds").background(Color(0xFFE9E9EE))) {
                    for (effect in LiquidBackgroundEffect.entries) {
                        Row {
                            for ((name, seed) in listOf("mint" to mint, "violet" to violet)) {
                                val scheme = LiquidMonetEngine.generateColorScheme(dark.value, false, seed)
                                MaterialTheme(colorScheme = scheme) {
                                    Box(Modifier.size(180.dp, 148.dp).testTag("$effect-$name")) {
                                        LiquidBackground(
                                            effect = effect, isDark = dark.value, monetSeed = seed,
                                            intensity = intensity.floatValue, showVignette = false,
                                        )
                                        Text(
                                            when (effect) {
                                                LiquidBackgroundEffect.Aurora -> "Aurora"
                                                LiquidBackgroundEffect.MeshGlow -> "Mesh Glow"
                                                LiquidBackgroundEffect.OrbitalPulse -> "Orbital Pulse"
                                                LiquidBackgroundEffect.RadiantBeam -> "Radiant Beam"
                                            },
                                            Modifier.align(Alignment.BottomStart).padding(12.dp),
                                            color = scheme.onBackground,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        capture("backgrounds", "light-backgrounds")
        compose.runOnIdle { intensity.floatValue = 0f }
        for (effect in LiquidBackgroundEffect.entries) {
            val mintPixels = compose.onNodeWithTag("$effect-mint").captureToImage().toPixelMap()
            val violetPixels = compose.onNodeWithTag("$effect-violet").captureToImage().toPixelMap()
            val mintTop = mintPixels[mintPixels.width / 3, 3]
            val violetTop = violetPixels[violetPixels.width / 3, 3]
            assertTrue("$effect should still show a mint base without the animated fields",
                mintTop.green > mintTop.red + 0.035f && mintTop.luminance() > 0.5f,
            )
            assertTrue("$effect should keep a distinct violet base",
                violetTop.blue > violetTop.green + 0.035f && violetTop.luminance() > 0.5f,
            )
        }
        compose.runOnIdle {
            dark.value = true
            intensity.floatValue = 1f
        }
        capture("backgrounds", "dark-backgrounds")
    }

    @Test fun pressingLightGlassPreservesTheChosenTint() {
        compose.setContent {
            val scheme = LiquidMonetEngine.generateColorScheme(false, false, mint)
            MaterialTheme(colorScheme = scheme) {
                LiquidGlassScene(background = { LiquidBackground(monetSeed = mint) }) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LiquidCard(
                            modifier = Modifier.size(260.dp, 160.dp).testTag("card"),
                            colors = LiquidCardDefaults.colors(containerColor = Color(0xFFE4F5EA)),
                            onClick = {},
                        ) {}
                    }
                }
            }
        }
        val card = compose.onNodeWithTag("card")
        compose.mainClock.autoAdvance = false
        card.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(240)
        capture("card", "light-pressed-card")
        val image = card.captureToImage().toPixelMap()
        val center = image[image.width / 2, image.height / 2]
        assertTrue("The held glass should remain colored, was $center",
            center.green > center.red + 0.03f && center.luminance() < 0.96f,
        )
        card.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1400)
    }

    private fun capture(tag: String, name: String) {
        val image = compose.onNodeWithTag(tag).captureToImage().asAndroidBitmap()
        val directory = File(compose.activity.getExternalFilesDir(null), "background-captures").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
