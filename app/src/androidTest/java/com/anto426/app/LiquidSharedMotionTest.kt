package com.anto426.app

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.cards.accordion.LiquidExpandableCard
import com.anto426.liquidmonet.components.cards.accordion.LiquidExpandableCardStyle
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.animatedswitcher.rememberLiquidContentTransition
import com.anto426.liquidmonet.components.layout.expandablecontent.LiquidExpandableContent
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.rememberLiquidAmbientPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test fun expandableCardSeparatesTheDisclosureFromItsChildAction() {
        var changes = 0
        var childClicks = 0
        compose.setContent {
            MaterialTheme {
                var expanded by remember { mutableStateOf(false) }
                LiquidExpandableCard(
                    title = "Exam details",
                    style = LiquidExpandableCardStyle(
                        headerPadding = PaddingValues(horizontal = 12.dp),
                        showDivider = false,
                    ),
                    isExpanded = expanded,
                    onExpandedChange = {
                        expanded = it
                        changes++
                    },
                ) {
                    LiquidButton(text = "Save grade", onClick = { childClicks++ })
                }
            }
        }

        val header = compose.onNode(hasText("Exam details") and hasClickAction())
        val button = compose.onNode(hasText("Save grade") and hasClickAction())
        button.assertDoesNotExist()
        header.performSemanticsAction(SemanticsActions.Expand) { it() }
        header.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.Collapse))
        button.performTouchInput { click(center) }
        compose.runOnIdle {
            assertEquals(1, childClicks)
            assertEquals(1, changes)
        }
        header.performTouchInput { click(center) }
        button.assertDoesNotExist()
        header.assert(SemanticsMatcher.keyIsDefined(SemanticsActions.Expand))
        compose.runOnIdle { assertEquals(2, changes) }
    }

    @Test fun expandableCardRevealsItsBodyGraduallyAndRespectsReducedMotion() {
        compose.mainClock.autoAdvance = false
        val expanded = mutableStateOf(false)
        val reduceMotion = mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                val performance = LiquidGlassPerformanceState.Fallback.copy(
                    motionScale = if (reduceMotion.value) 0f else 1f,
                )
                CompositionLocalProvider(LocalLiquidGlassPerformance provides performance) {
                    LiquidExpandableCard(
                        title = "Details",
                        modifier = Modifier.testTag("expandable-card"),
                        isExpanded = expanded.value,
                    ) {
                        Box(Modifier.size(width = 120.dp, height = 160.dp).testTag("expandable-body"))
                    }
                }
            }
        }

        val card = compose.onNodeWithTag("expandable-card")
        val body = compose.onNodeWithTag("expandable-body")
        fun cardHeight() = card.fetchSemanticsNode().boundsInRoot.height
        val collapsedHeight = cardHeight()
        compose.runOnIdle { expanded.value = true }
        compose.mainClock.advanceTimeBy(96)
        compose.waitForIdle()
        val openingHeight = cardHeight()

        compose.mainClock.advanceTimeBy(1_000)
        compose.waitForIdle()
        val expandedHeight = cardHeight()
        assertTrue(
            "Expected an intermediate height, got $collapsedHeight, $openingHeight, $expandedHeight",
            openingHeight > collapsedHeight && openingHeight < expandedHeight,
        )

        compose.runOnIdle { reduceMotion.value = true }
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle { expanded.value = false }
        compose.mainClock.advanceTimeBy(80)
        compose.waitForIdle()
        body.assertDoesNotExist()
        assertEquals(collapsedHeight, cardHeight(), 1f)
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
