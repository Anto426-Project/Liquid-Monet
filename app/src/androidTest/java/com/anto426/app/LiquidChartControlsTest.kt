package com.anto426.app

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart
import com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.charts.model.LiquidBarChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.LiquidLineSeries
import com.anto426.liquidmonet.components.charts.model.LiquidLineStyle
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.glass.LiquidGlassScene
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LiquidChartControlsTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    @Test
    fun lineLegendTogglesSeriesIndependently() {
        val values = listOf(LiquidChartEntry("First", 24f), LiquidChartEntry("Last", 28f))
        val weightedValues = listOf(LiquidChartEntry("First", 22f), LiquidChartEntry("Last", 25f))
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    LiquidLineChart(
                        series = listOf(
                            LiquidLineSeries(label = "Grades", entries = values, showArea = true),
                            LiquidLineSeries(
                                label = "Weighted",
                                entries = weightedValues,
                                lineStyle = LiquidLineStyle.Dashed,
                            ),
                        ),
                        modifier = Modifier.width(120.dp),
                        showLegend = true,
                        showSelection = true,
                    )
                }
            }
        }

        val grades = compose.onNode(hasText("Grades") and hasClickAction())
        val weighted = compose.onNode(hasText("Weighted") and hasClickAction())
        grades.assertIsSelected().performClick().assertIsNotSelected()
        weighted.assertIsSelected()
        assertTrue(weighted.fetchSemanticsNode().boundsInRoot.top > grades.fetchSemanticsNode().boundsInRoot.top)
        compose.onNodeWithText("28").assertExists()
    }

    @Test
    fun donutLegendSelectsAndClearsTheSlice() {
        val entries = listOf(LiquidPieEntry("Completed", 18f), LiquidPieEntry("Remaining", 12f))
        val changes = mutableListOf<LiquidPieEntry?>()
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    LiquidDonutChart(
                        entries = entries,
                        centerLabel = "Credits",
                        showLegend = true,
                        onEntrySelected = { changes += it },
                    )
                }
            }
        }

        val legend = compose.onNode(hasText("Completed") and SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected))
        compose.onNodeWithText("18 · 60%").assertExists()
        compose.onNodeWithText("12 · 40%").assertExists()
        legend.assertIsNotSelected().performClick()
        compose.runOnIdle { assertEquals(listOf(entries[0]), changes) }
        legend.assertIsSelected().performClick().assertIsNotSelected()
        compose.runOnIdle { assertEquals(listOf(entries[0], null), changes) }
    }

    @Test
    fun barPlotStillSelectsTheMatchingEntry() {
        val entries = listOf(LiquidChartEntry("First", 14f), LiquidChartEntry("Last", 26f))
        val changes = mutableListOf<LiquidChartEntry?>()
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    LiquidBarChart(
                        entries = entries,
                        modifier = Modifier.testTag("bars"),
                        style = LiquidBarChartStyle(showXAxisLabels = false),
                        showSelection = true,
                        onEntrySelected = { _, entry -> changes += entry },
                    )
                }
            }
        }

        compose.onNodeWithTag("bars").performTouchInput {
            click(Offset(width / 4f, height / 2f))
        }
        compose.runOnIdle { assertEquals(entries[0], changes.lastOrNull()) }
        compose.onNodeWithText("First").assertExists()
        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        compose.runOnIdle { assertEquals(entries.last(), changes.lastOrNull()) }
    }

    @Test
    fun linePlotAllowsVerticalPageScrollWithoutSelectingAPoint() {
        val entries = listOf(LiquidChartEntry("First", 14f), LiquidChartEntry("Last", 26f))
        val changes = mutableListOf<LiquidChartEntry>()
        val scroll = ScrollState(0)
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    Column(Modifier.height(320.dp).verticalScroll(scroll)) {
                        LiquidLineChart(
                            entries = entries,
                            onEntrySelected = { _, entry -> changes += entry },
                        )
                        Spacer(Modifier.height(400.dp))
                    }
                }
            }
        }

        compose.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.ProgressBarRangeInfo))
            .performTouchInput { swipeUp(startY = height * 0.75f, endY = height * 0.25f) }
        compose.runOnIdle {
            assertTrue(scroll.value > 0)
            assertTrue(changes.isEmpty())
        }
    }

    @Test
    fun heldButtonDragStaysWithTheButtonWithoutTriggeringItsAction() {
        var clicks = 0
        val scroll = ScrollState(0)
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    Column(Modifier.height(220.dp).verticalScroll(scroll)) {
                        LiquidButton(text = "Action", onClick = { clicks++ }, modifier = Modifier.testTag("button"))
                        Spacer(Modifier.height(400.dp))
                    }
                }
            }
        }
        val button = compose.onNodeWithTag("button")
        button.performTouchInput {
            down(center)
            advanceEventTime(150)
            moveBy(Offset(0f, -20.dp.toPx()))
            up()
        }
        compose.runOnIdle {
            assertEquals(0, clicks)
            assertEquals(0, scroll.value)
        }
        button.performTouchInput { click(center) }
        compose.runOnIdle { assertEquals(1, clicks) }
    }
}
