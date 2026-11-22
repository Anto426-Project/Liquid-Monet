package com.anto426.app

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.swipetodismiss.LiquidSwipeToDismissBox
import com.anto426.liquidmonet.components.inputs.splitclearbutton.LiquidSplitClearButton
import com.anto426.liquidmonet.components.layout.lazy.LiquidLazyColumn
import com.anto426.liquidmonet.components.navigation.pagination.*
import com.anto426.liquidmonet.components.pickers.datepicker.LiquidDatePicker
import com.anto426.liquidmonet.components.pickers.datepicker.state.LiquidDatePickerState
import com.anto426.liquidmonet.components.selection.rangeslider.LiquidRangeSlider
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidMonetTheme
import java.io.File
import kotlin.math.abs
import kotlinx.datetime.LocalDate
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LiquidRefactoredComponentsTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    @Test
    fun slidersKeepSemanticsAndFollowExternalValuesInsideExpressiveTheme() {
        val value = mutableFloatStateOf(0.25f)
        val range = mutableStateOf(0.25f..0.5f)
        compose.setContent {
            LiquidMonetTheme(useMonetEngine = false, reduceMotion = true) {
                LiquidGlassScene {
                    Column {
                        LiquidSlider(
                            value = value.floatValue,
                            onValueChange = { value.floatValue = it },
                            modifier = Modifier.testTag("single"),
                            steps = 3,
                        )
                        LiquidRangeSlider(
                            value = range.value,
                            onValueChange = { range.value = it },
                            modifier = Modifier.testTag("range"),
                            steps = 3,
                        )
                    }
                }
            }
        }
        val adjustable = SemanticsMatcher.keyIsDefined(SemanticsActions.SetProgress)
        val single = adjustable and hasAnyAncestor(hasTestTag("single"))
        val rangeThumbs = adjustable and hasAnyAncestor(hasTestTag("range"))
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodes(single, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(single, useUnmergedTree = true).performSemanticsAction(SemanticsActions.SetProgress) {
            assertTrue(it(0.75f))
        }
        compose.runOnIdle {
            assertEquals(0.75f, value.floatValue)
            range.value = 0.75f..1f
        }
        val thumbValues = compose.onAllNodes(rangeThumbs, useUnmergedTree = true).fetchSemanticsNodes()
            .map { it.config[SemanticsProperties.ProgressBarRangeInfo].current }
            .sorted()
        assertEquals(listOf(0.75f, 1f), thumbValues)
    }

    @Test
    fun calendarSelectionAndMonthNavigationRemainIndependent() {
        val state = LiquidDatePickerState(LocalDate(2026, 9, 25))
        compose.setContent { MaterialTheme { LiquidGlassScene { LiquidDatePicker(state) } } }
        compose.onNodeWithText("15").performClick()
        compose.runOnIdle { assertEquals(LocalDate(2026, 9, 15), state.selectedDate) }
        compose.onNodeWithContentDescription("Mese successivo").performClick()
        compose.runOnIdle {
            assertEquals(10, state.displayedMonth)
            assertEquals(LocalDate(2026, 9, 15), state.selectedDate)
        }
    }

    @Test
    fun pageSelectionExpandsOnHoldAndReturnsWithNavigationPhysics() {
        val page = mutableIntStateOf(1)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color.Blue)) {
                LiquidGlassScene {
                    LiquidPageIndicator(
                        3,
                        page.intValue,
                        Modifier.testTag("indicator"),
                        { page.intValue = it },
                    )
                }
            }
        }
        compose.mainClock.advanceTimeBy(32)
        fun activePixels(): Int {
            val pixels = compose.onNodeWithTag("indicator").captureToImage().toPixelMap()
            var count = 0
            for (y in 0 until pixels.height) for (x in 0 until pixels.width) {
                val color = pixels[x, y]
                if (color.blue > .8f && color.red < .1f && color.green < .1f) count++
            }
            return count
        }
        val resting = activePixels()
        assertTrue(resting > 0)
        compose.onNodeWithTag("indicator").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(400)
        val pressed = activePixels()
        assertTrue(
            "Selection should grow like the navbar: $resting -> $pressed",
            pressed > resting * 1.5f,
        )
        compose.onNodeWithTag("indicator").performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1800)
        val released = activePixels()
        assertTrue(
            "Selection must return after release: $released",
            abs(released - resting) < resting * .2f,
        )
    }

    @Test
    fun numberedPagesKeepBoundaryActionsDisabled() {
        val page = mutableIntStateOf(1)
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    LiquidPagination(
                        page.intValue,
                        5,
                        { page.intValue = it },
                        Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        compose.onNodeWithContentDescription("Pagina precedente").assertIsNotEnabled()
        compose.onNodeWithText("5").performClick()
        compose.runOnIdle { assertEquals(5, page.intValue) }
        compose.onNodeWithContentDescription("Pagina successiva").assertIsNotEnabled()
    }

    @Test
    fun cancelledSwipeDoesNotInvokeAnActionAndTheNextSwipeStillWorks() {
        var calls = 0
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    LiquidSwipeToDismissBox(
                        Modifier.testTag("swipe"),
                        onDismissRight = { calls++ },
                    ) {
                        Text("Swipe", Modifier.fillMaxWidth().height(72.dp))
                    }
                }
            }
        }
        compose.onNodeWithTag("swipe").performTouchInput {
            down(Offset(width * .2f, center.y))
            moveTo(Offset(width * .8f, center.y), 150)
            cancel()
        }
        compose.runOnIdle { assertEquals(0, calls) }
        compose.onNodeWithTag("swipe").performTouchInput { swipeRight(durationMillis = 300) }
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun lazyEdgePullMovesTheViewportAndReleasesWithoutScrollingItsFirstItem() {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme {
                LiquidLazyColumn(Modifier.height(240.dp).fillMaxWidth().testTag("list")) {
                    items(20) { index ->
                        Text(
                            "Item $index",
                            Modifier.height(64.dp).fillMaxWidth().testTag("item$index"),
                        )
                    }
                }
            }
        }
        compose.mainClock.advanceTimeBy(32)
        val before = compose.onNodeWithTag("item0").fetchSemanticsNode().boundsInRoot.top
        compose.onNodeWithTag("list").performTouchInput {
            down(center)
            moveBy(Offset(0f, 80f), 100)
            moveBy(Offset(0f, 100f), 100)
        }
        compose.mainClock.advanceTimeBy(32)
        val pulled = compose.onNodeWithTag("item0").fetchSemanticsNode().boundsInRoot.top
        assertTrue("Viewport should follow edge pull: $before -> $pulled", pulled > before + 5f)
        compose.onNodeWithTag("list").performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1800)
        val released = compose.onNodeWithTag("item0").fetchSemanticsNode().boundsInRoot.top
        assertTrue("Viewport must return: $released", abs(released - before) < 2f)
    }

    @Test
    fun dropletExitDisablesClickImmediatelyAndRapidTypingReversesIt() {
        val visible = mutableStateOf(false)
        var calls = 0
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    Row {
                        Text("Field", Modifier.weight(1f))
                        LiquidSplitClearButton(
                            visible.value,
                            { calls++ },
                            Modifier.testTag("clear"),
                        )
                    }
                }
            }
        }
        compose.runOnIdle { visible.value = true }
        compose.mainClock.advanceTimeBy(96)
        compose.runOnIdle { visible.value = false }
        compose.mainClock.advanceTimeBy(16)
        compose.onNodeWithTag("clear").assertIsNotEnabled()
        compose.runOnIdle { visible.value = true }
        compose.mainClock.advanceTimeBy(1200)
        compose.onNodeWithTag("clear").performClick()
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test
    fun recordCalendarAndControlsInBothThemes() {
        val dark = mutableStateOf(false)
        val calendar = mutableStateOf(false)
        val state = LiquidDatePickerState(LocalDate(2026, 9, 25))
        compose.setContent {
            MaterialTheme(colorScheme = if (dark.value) darkColorScheme() else lightColorScheme()) {
                LiquidGlassScene(
                    background = {
                        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface))
                    }
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp).testTag("gallery"),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        if (calendar.value) {
                            LiquidDatePicker(state)
                        } else {
                            LiquidPagination(2, 5, {}, Modifier.fillMaxWidth())
                            LiquidPageIndicator(5, 2)
                        }
                    }
                }
            }
        }
        for (isDark in listOf(false, true)) for (isCalendar in listOf(false, true)) {
            compose.runOnIdle {
                dark.value = isDark
                calendar.value = isCalendar
            }
            val name =
                "liquid-${if (isCalendar) "calendar" else "controls"}-${if (isDark) "dark" else "light"}.png"
            val bitmap = compose.onNodeWithTag("gallery").captureToImage().asAndroidBitmap()
            File(compose.activity.filesDir, name).outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
    }
}
