package com.anto426.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.animatedswitcher.rememberLiquidContentTransition
import com.anto426.liquidmonet.components.layout.expandablecontent.LiquidExpandableContent
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.rememberLiquidAmbientPhase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LiquidSharedMotionTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    @Test fun existingHostUsesSdkRoutePolicyAndReducedMotion() {
        compose.mainClock.autoAdvance = false
        val page = mutableIntStateOf(0)
        val reduced = mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = if (reduced.value) 0f else 1f)) {
                    val spec = rememberLiquidContentTransition<Int>(
                        transition = LiquidSwitcherTransition.Crossfade,
                        transitionFor = { from, to -> if (from == 0 || to == 0) LiquidSwitcherTransition.None else LiquidSwitcherTransition.DirectionalHorizontal },
                        isForward = { from, to -> to > from },
                    )
                    AnimatedContent(page.intValue, transitionSpec = spec) { current ->
                        Box(Modifier.size(200.dp).testTag("page-$current")) { Text("Page $current") }
                    }
                }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { page.intValue = 1 }
        compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        compose.onNodeWithTag("page-1").assertExists()
        compose.onNodeWithTag("page-0").assertDoesNotExist()
        compose.runOnIdle { page.intValue = 2 }
        compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        compose.onNodeWithTag("page-1").assertExists()
        compose.mainClock.advanceTimeBy(2_000); compose.waitForIdle()
        compose.onNodeWithTag("page-2").assertExists()
        compose.onNodeWithTag("page-1").assertDoesNotExist()
        compose.runOnIdle { reduced.value = true; page.intValue = 3 }
        compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        compose.onNodeWithTag("page-3").assertExists()
        compose.onNodeWithTag("page-2").assertDoesNotExist()
    }

    @Test fun expandableControlsLeaveCompositionAfterExitAndImmediatelyWithReducedMotion() {
        compose.mainClock.autoAdvance = false
        val visible = mutableStateOf(true)
        val reduced = mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = if (reduced.value) 0f else 1f)) {
                    LiquidExpandableContent(visible.value) { Box(Modifier.size(100.dp).testTag("controls")) }
                }
            }
        }
        compose.waitForIdle()
        compose.runOnIdle { visible.value = false }
        compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        compose.onNodeWithTag("controls").assertExists()
        compose.mainClock.advanceTimeBy(2_000); compose.waitForIdle()
        compose.onNodeWithTag("controls").assertDoesNotExist()
        compose.runOnIdle { reduced.value = true; visible.value = true }
        compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        compose.onNodeWithTag("controls").assertExists()
        compose.runOnIdle { visible.value = false }
        compose.mainClock.advanceTimeBy(32); compose.waitForIdle()
        compose.onNodeWithTag("controls").assertDoesNotExist()
    }

    @Test fun inactiveOrReducedAmbientArtworkHasNoMovingPhase() {
        compose.mainClock.autoAdvance = false
        val active = mutableStateOf(false)
        val reduced = mutableStateOf(false)
        var phase = -1f
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(qualityTier = LiquidGlassQualityTier.entries.last(), motionScale = if (reduced.value) 0f else 1f)) {
                    val value = rememberLiquidAmbientPhase(active.value)
                    phase = value.value
                    Text("Phase $phase")
                }
            }
        }
        compose.mainClock.advanceTimeBy(5_000); compose.waitForIdle()
        assertEquals(.5f, phase, 0f)
        compose.runOnIdle { reduced.value = true; active.value = true }
        compose.mainClock.advanceTimeBy(5_000); compose.waitForIdle()
        assertEquals(.5f, phase, 0f)
    }
}
