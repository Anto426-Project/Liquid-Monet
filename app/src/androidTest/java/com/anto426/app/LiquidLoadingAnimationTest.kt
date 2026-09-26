package com.anto426.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Pixel changes verify visible motion, including the MINIMAL budget that previously froze it. */
class LiquidLoadingAnimationTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    @Test fun everyInlineLoadingStyleMovesAtMinimalBudget() {
        val styles = LiquidLoadingStyle.entries.filter { it != LiquidLoadingStyle.Overlay }
        // Compose's test policy cancels infinite animations when autoAdvance is true.
        // Disable it before composition; changing it after waitForIdle cannot restart them.
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLiquidGlassPerformance provides
                    LiquidGlassPerformanceState.Fallback.copy(
                        qualityTier = LiquidGlassQualityTier.MINIMAL,
                        opticalQualityTier = LiquidGlassQualityTier.HIGH,
                        renderResolutionScale = 0.5f, motionScale = 1f)) {
                    Column(Modifier.background(Color.White)) {
                        styles.forEach { style ->
                            Box(Modifier.size(160.dp, 64.dp).testTag(style.name)) {
                                LiquidLoading(style = style, modifier = Modifier.size(120.dp, 48.dp), tint = Color.Blue)
                            }
                        }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(160)
        val first = styles.associateWith { compose.onNodeWithTag(it.name).captureToImage() }
        compose.mainClock.advanceTimeBy(640)
        for (style in styles) {
            val second = compose.onNodeWithTag(style.name).captureToImage()
            assertTrue("$style must visibly move on MINIMAL", differentPixels(first.getValue(style), second) > 10)
        }
    }

    private fun differentPixels(first: ImageBitmap, second: ImageBitmap): Int {
        val a = first.toPixelMap()
        val b = second.toPixelMap()
        var different = 0
        for (y in 0 until a.height) for (x in 0 until a.width) {
            if (a[x, y] != b[x, y]) different++
        }
        return different
    }
}
