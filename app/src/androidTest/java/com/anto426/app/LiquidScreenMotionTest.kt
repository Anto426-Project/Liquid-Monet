package com.anto426.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntrance
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LiquidScreenMotionTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    @Test fun everyTransitionSettlesOnTheLatestScreenAfterInterruption() {
        val page = mutableIntStateOf(0)
        val preset = mutableStateOf(LiquidSwitcherTransition.LiquidMorph)
        var clickedPage = -1
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback) {
                LiquidAnimatedSwitcher(targetState = page.intValue, transition = preset.value) { current ->
                    Button(
                        modifier = Modifier.testTag("page-$current").size(if (current % 2 == 0) 100.dp else 140.dp),
                        onClick = { clickedPage = current },
                    ) { Text("Page $current") }
                }
            }
        }
        LiquidSwitcherTransition.entries.forEach { transition ->
            compose.runOnIdle { preset.value = transition; page.intValue++ }
            compose.mainClock.advanceTimeBy(64)
            compose.runOnIdle { page.intValue++ }
            compose.mainClock.advanceTimeBy(2_000)
            compose.waitForIdle()
            val current = page.intValue
            compose.onNodeWithTag("page-${current - 1}").assertDoesNotExist()
            compose.onNodeWithTag("page-$current").performClick()
            compose.runOnIdle { assertEquals(current, clickedPage) }
        }
    }

    @Test fun reducedMotionRemovesScreenAndEntranceDelays() {
        val page = mutableIntStateOf(0)
        val visible = mutableStateOf(true)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(
                LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f)
            ) {
                Column {
                    LiquidSwitcherTransition.entries.forEach { preset ->
                        LiquidAnimatedSwitcher(targetState = page.intValue, transition = preset) { current ->
                            Text("Page $current", Modifier.testTag("${preset.name}-$current"))
                        }
                    }
                    LiquidScreenEntranceAnimation.entries.forEach { preset ->
                        LiquidScreenEntrance(animation = preset, visible = visible.value, delayMillis = 10_000) {
                            Text("Section", Modifier.testTag("entrance-${preset.name}"))
                        }
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(64)
        compose.waitForIdle()
        LiquidScreenEntranceAnimation.entries.forEach { preset ->
            compose.onNodeWithTag("entrance-${preset.name}").assertExists()
        }
        compose.runOnIdle { page.intValue = 1; visible.value = false }
        compose.mainClock.advanceTimeBy(64)
        compose.waitForIdle()
        LiquidSwitcherTransition.entries.forEach { preset ->
            compose.onNodeWithTag("${preset.name}-0").assertDoesNotExist()
            compose.onNodeWithTag("${preset.name}-1").assertExists()
        }
        LiquidScreenEntranceAnimation.entries.forEach { preset ->
            compose.onNodeWithTag("entrance-${preset.name}").assertDoesNotExist()
        }
    }

    @Test fun entranceDelayHoldsPixelsThenRevealsContent() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback) {
                Box(Modifier.size(100.dp).background(Color.Black).testTag("preview")) {
                    LiquidScreenEntrance(animation = LiquidScreenEntranceAnimation.Fade, delayMillis = 500) {
                        Box(Modifier.size(100.dp).background(Color.Red))
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(200)
        compose.waitForIdle()
        val before = compose.onNodeWithTag("preview").captureToImage().toPixelMap()
        assertTrue(before[before.width / 2, before.height / 2].red < 0.05f)
        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        val after = compose.onNodeWithTag("preview").captureToImage().toPixelMap()
        assertTrue(after[after.width / 2, after.height / 2].red > 0.95f)
    }

    @Test fun reducedMotionPreservesSwipeActionsAndUpdatedCallbacks() {
        val callbackVersion = mutableIntStateOf(0)
        val calls = mutableListOf<String>()
        compose.setContent {
            CompositionLocalProvider(
                LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f)
            ) {
                val version = callbackVersion.intValue
                LiquidAnimatedSwitcher(
                    targetState = 0,
                    transition = LiquidSwitcherTransition.SlideHorizontal,
                    onSwipeForward = { calls += "forward-$version" },
                    onSwipeBackward = { calls += "backward-$version" },
                    modifier = Modifier.size(300.dp, 100.dp).testTag("swipe"),
                ) { Text("Swipe") }
            }
        }
        compose.onNodeWithTag("swipe").performTouchInput { swipeLeft() }
        compose.runOnIdle { callbackVersion.intValue = 1 }
        compose.onNodeWithTag("swipe").performTouchInput { swipeRight() }
        compose.runOnIdle { assertEquals(listOf("forward-0", "backward-1"), calls) }
    }

    @Test fun horizontalScreensFollowRtlLayoutInBothDirections() {
        val page = mutableIntStateOf(0)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            CompositionLocalProvider(
                LocalLayoutDirection provides LayoutDirection.Rtl,
                LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback,
            ) {
                LiquidAnimatedSwitcher(
                    targetState = page.intValue,
                    transition = LiquidSwitcherTransition.SlideHorizontal,
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.TopStart,
                ) { current ->
                    Box(Modifier.size(200.dp).testTag("rtl-$current"))
                }
            }
        }
        compose.runOnIdle { page.intValue = 1 }
        compose.mainClock.advanceTimeBy(64)
        compose.waitForIdle()
        assertTrue(compose.onNodeWithTag("rtl-1").fetchSemanticsNode().positionInRoot.x < 0f)
        compose.mainClock.advanceTimeBy(2_000)
        compose.waitForIdle()
        compose.runOnIdle { page.intValue = 0 }
        compose.mainClock.advanceTimeBy(64)
        compose.waitForIdle()
        assertTrue(compose.onNodeWithTag("rtl-0").fetchSemanticsNode().positionInRoot.x > 0f)
        compose.mainClock.advanceTimeBy(2_000)
        compose.waitForIdle()
        compose.onNodeWithTag("rtl-1").assertDoesNotExist()
    }
}
