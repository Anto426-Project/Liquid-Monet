package com.anto426.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.anto426.app.screens.DemoAnimatedSection
import com.anto426.app.screens.DemoSectionMotion
import com.anto426.app.screens.LocalDemoSectionMotion
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.layout.sectionentrance.LiquidSectionEntrance
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LiquidDemoSectionTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    private fun redPixels(): Int {
        val pixels = compose.onNodeWithTag("viewport").captureToImage().toPixelMap()
        var count = 0
        for (y in 0 until pixels.height step 3) for (x in 0 until pixels.width step 3) {
            val c = pixels[x, y]
            if (c.red > .8f && c.green < .2f && c.blue < .2f) count++
        }
        return count
    }

    @Test fun entranceWaitsForScrollingIntoViewAndKeepsItsLayoutSpace() {
        compose.mainClock.autoAdvance = false
        val scroll = ScrollState(0)
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = 1f)) {
                    Column(Modifier.size(240.dp, 200.dp).background(Color.Black).verticalScroll(scroll).testTag("viewport")) {
                        Spacer(Modifier.height(300.dp))
                        DemoAnimatedSection(Modifier.testTag("late-section"), order = 2) {
                            Box(Modifier.fillMaxWidth().height(80.dp).background(Color.Red))
                        }
                        Spacer(Modifier.height(200.dp))
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(1_000); compose.waitForIdle()
        assertEquals(0, redPixels())
        val height = compose.onNodeWithTag("late-section").fetchSemanticsNode().size.height
        assertTrue("Invisible sections must reserve their measured height", height > 0)
        compose.runOnUiThread { scroll.dispatchRawDelta(compose.activity.resources.displayMetrics.density * 300f) }
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        assertEquals("Offscreen time must not consume the delayed entrance", 0, redPixels())
        compose.mainClock.advanceTimeBy(700); compose.waitForIdle()
        assertTrue(redPixels() > 500)
        assertEquals(height, compose.onNodeWithTag("late-section").fetchSemanticsNode().size.height)
        compose.runOnUiThread { scroll.dispatchRawDelta(-compose.activity.resources.displayMetrics.density * 300f) }
        compose.waitForIdle()
        compose.runOnUiThread { scroll.dispatchRawDelta(compose.activity.resources.displayMetrics.density * 300f) }
        compose.waitForIdle()
        assertTrue("Already revealed sections stay visible", redPixels() > 500)
    }

    @Test fun replayAndStyleChangesPreserveChildStateAndReducedMotionIsImmediate() {
        compose.mainClock.autoAdvance = false
        val replay = mutableIntStateOf(0)
        val reduced = mutableStateOf(false)
        val style = mutableStateOf(LiquidScreenEntranceAnimation.Scale)
        var creations = 0
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = if (reduced.value) 0f else 1f),
                    LocalDemoSectionMotion provides DemoSectionMotion(animation = style.value, replay = replay.intValue),
                ) {
                    Box(Modifier.size(240.dp, 200.dp).background(Color.Black).testTag("viewport")) {
                        DemoAnimatedSection(order = 3) {
                            val value = remember { creations++; mutableIntStateOf(42) }
                            Box(Modifier.fillMaxWidth().height(80.dp).background(Color.Red)) { Text("Value ${value.intValue}") }
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(1_000); compose.waitForIdle()
        compose.onNodeWithText("Value 42").assertExists()
        assertTrue(redPixels() > 500)
        compose.runOnIdle { replay.intValue++ }
        compose.waitForIdle(); compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        assertEquals(0, redPixels())
        compose.runOnIdle { reduced.value = true }
        compose.waitForIdle(); compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        assertTrue(redPixels() > 500)
        compose.runOnIdle { style.value = LiquidScreenEntranceAnimation.FadeDown; replay.intValue++ }
        compose.waitForIdle(); compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        assertTrue(redPixels() > 500)
        assertEquals("Replay must not recreate child state", 1, creations)
    }

    @Test fun coveredSectionsWaitAndReactToViewportChangesWithoutScrolling() {
        compose.mainClock.autoAdvance = false
        val viewport = mutableStateOf<Rect?>(Rect.Zero)
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = 1f)) {
                    Box(Modifier.size(240.dp, 200.dp).background(Color.Black).testTag("viewport")) {
                        LiquidSectionEntrance(
                            animation = LiquidScreenEntranceAnimation.Fade,
                            delayMillis = 120,
                            viewportBoundsInWindow = viewport.value,
                        ) {
                            Box(Modifier.fillMaxWidth().height(80.dp).background(Color.Red))
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(1_000); compose.waitForIdle()
        assertEquals("Covered content must wait for its viewport", 0, redPixels())
        compose.runOnIdle { viewport.value = null }
        compose.waitForIdle(); compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        assertEquals(0, redPixels())
        compose.mainClock.advanceTimeBy(700); compose.waitForIdle()
        assertTrue("Updating the viewport must reveal content without a scroll", redPixels() > 500)
    }
}
