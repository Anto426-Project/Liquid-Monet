package com.anto426.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LiquidMenuInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()
    private val selections = mutableListOf<Int>()
    private val secondEnabled = mutableStateOf(true)
    private val callbackVersion = mutableStateOf(0)

    private fun openMenu(hosted: Boolean = true, motionScale: Float = 0f) {
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = motionScale)
                ) {
                    LiquidGlassScene {
                        var expanded by remember { mutableStateOf(false) }
                        val anchor = rememberLiquidGlassOverlayAnchorState()
                        Box(Modifier.padding(40.dp)) {
                            Text("Open", Modifier.size(48.dp).liquidGlassOverlayAnchor(anchor)
                                .testTag("trigger").clickable { expanded = true })
                            LiquidDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.testTag("menu"),
                                anchorState = if (hosted) anchor else null
                            ) {
                                repeat(3) { index ->
                                    val version = callbackVersion.value
                                    LiquidMenuItem(
                                        text = "Item $index",
                                        modifier = Modifier.testTag("item-$index"),
                                        enabled = index != 1 || secondEnabled.value,
                                        onClick = { selections += index + version * 10 }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        compose.waitUntil(timeoutMillis = 5_000) {
            compose.onAllNodesWithTag("trigger").fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
        compose.onNodeWithTag("trigger").performClick()
        compose.waitForIdle()
    }

    private fun itemPosition(index: Int): Offset {
        val menu = compose.onNodeWithTag("menu").fetchSemanticsNode().boundsInRoot
        return compose.onNodeWithTag("item-$index").fetchSemanticsNode().boundsInRoot.center - menu.topLeft
    }

    @Test fun dragSelectsReleaseRowExactlyOnce() {
        openMenu()
        val start = itemPosition(0)
        val end = itemPosition(2)
        compose.onNodeWithTag("menu").performTouchInput { swipe(start, end, 300) }
        compose.runOnIdle { assertEquals(listOf(2), selections) }
    }

    @Test fun popupAlsoSelectsReleaseRow() {
        openMenu(hosted = false)
        val start = itemPosition(0)
        val end = itemPosition(2)
        compose.onNodeWithTag("menu").performTouchInput { swipe(start, end, 300) }
        compose.runOnIdle { assertEquals(listOf(2), selections) }
    }

    @Test fun dragWithAnimatedMenuSelectsReleaseRow() {
        openMenu(motionScale = 1f)
        val start = itemPosition(0)
        val end = itemPosition(2)
        compose.onNodeWithTag("menu").performTouchInput { swipe(start, end, 300) }
        compose.runOnIdle { assertEquals(listOf(2), selections) }
    }

    @Test fun releaseOutsideDoesNotActivateOriginalRow() {
        openMenu()
        val start = itemPosition(0)
        compose.onNodeWithTag("menu").performTouchInput { swipe(start, Offset(-30f, start.y), 300) }
        compose.runOnIdle { assertEquals(emptyList<Int>(), selections) }
    }

    @Test fun returningToOriginalRowStillCommitsOnlyOnce() {
        openMenu()
        val start = itemPosition(0)
        val end = itemPosition(2)
        compose.onNodeWithTag("menu").performTouchInput {
            down(start)
            moveTo(end, delayMillis = 100)
            moveTo(start, delayMillis = 100)
            up()
        }
        compose.runOnIdle { assertEquals(listOf(0), selections) }
    }

    @Test fun disabledReleaseRowDoesNotActivate() {
        secondEnabled.value = false
        openMenu()
        val start = itemPosition(0)
        val end = itemPosition(1)
        compose.onNodeWithTag("menu").performTouchInput { swipe(start, end, 300) }
        compose.runOnIdle { assertEquals(emptyList<Int>(), selections) }
    }

    @Test fun cancellationDoesNotActivateAndNextTapWorks() {
        openMenu()
        val start = itemPosition(0)
        val end = itemPosition(2)
        compose.onNodeWithTag("menu").performTouchInput {
            down(start)
            moveTo(end, delayMillis = 100)
            cancel()
        }
        compose.runOnIdle { assertEquals(emptyList<Int>(), selections) }
        compose.onNodeWithTag("item-1").performTouchInput { click() }
        compose.runOnIdle { assertEquals(listOf(1), selections) }
    }

    @Test fun secondPointerCancelsSelection() {
        openMenu()
        val start = itemPosition(0)
        val end = itemPosition(2)
        compose.onNodeWithTag("menu").performTouchInput {
            down(0, start)
            moveTo(0, end, delayMillis = 100)
            down(1, start)
            up(1)
            up(0)
        }
        compose.runOnIdle { assertEquals(emptyList<Int>(), selections) }
    }

    @Test fun callbackChangesDuringDragAreObserved() {
        openMenu()
        val start = itemPosition(0)
        val end = itemPosition(2)
        compose.onNodeWithTag("menu").performTouchInput {
            down(start)
            moveTo(end, delayMillis = 100)
        }
        compose.runOnIdle { callbackVersion.value = 1 }
        compose.onNodeWithTag("menu").performTouchInput { up() }
        compose.runOnIdle { assertEquals(listOf(12), selections) }
    }

    @Test fun rowDisabledDuringDragDoesNotActivate() {
        openMenu()
        val start = itemPosition(0)
        val end = itemPosition(1)
        compose.onNodeWithTag("menu").performTouchInput {
            down(start)
            moveTo(end, delayMillis = 100)
        }
        compose.runOnIdle { secondEnabled.value = false }
        compose.onNodeWithTag("menu").performTouchInput { up() }
        compose.runOnIdle { assertEquals(emptyList<Int>(), selections) }
    }

    @Test fun tapAndSemanticsClickStillWork() {
        openMenu()
        compose.onNodeWithTag("item-0").performTouchInput { click() }
        compose.onNodeWithTag("item-2").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.runOnIdle { assertEquals(listOf(0, 2), selections) }
    }
}
