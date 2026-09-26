package com.anto426.app

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.cards.LiquidPreferenceItem
import com.anto426.liquidmonet.components.inputs.LiquidSplitClearButton
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LiquidControlInteractionTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    @Test fun iconVariantsObserveNewCallbacksAndIgnoreDisabledTouches() {
        val enabled = mutableStateOf(true)
        val version = mutableIntStateOf(0)
        val calls = mutableListOf<Int>()
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    Column {
                        LiquidIconButtonVariant.entries.forEach { variant ->
                            val callbackVersion = version.intValue
                            LiquidIconButton(
                                icon = LiquidIcons.Close,
                                onClick = { calls += callbackVersion },
                                enabled = enabled.value,
                                variant = variant,
                                modifier = Modifier.testTag(variant.name)
                            )
                        }
                    }
                }
            }
        }
        LiquidIconButtonVariant.entries.forEach { variant ->
            compose.onNodeWithTag(variant.name).performTouchInput { click() }
        }
        compose.runOnIdle { version.intValue = 1 }
        LiquidIconButtonVariant.entries.forEach { variant ->
            compose.onNodeWithTag(variant.name).performTouchInput { click() }
        }
        compose.runOnIdle {
            assertEquals(listOf(0, 0, 0, 0, 1, 1, 1, 1), calls)
            enabled.value = false
        }
        LiquidIconButtonVariant.entries.forEach { variant ->
            compose.onNodeWithTag(variant.name).assertIsNotEnabled().performTouchInput { click() }
        }
        compose.runOnIdle { assertEquals(8, calls.size) }
    }

    @Test fun preferenceIconBelongsToTheRowsSingleInteraction() {
        var calls = 0
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    LiquidPreferenceItem(
                        title = "Preference",
                        icon = LiquidIcons.Settings,
                        onClick = { calls++ },
                        modifier = Modifier.testTag("preference")
                    )
                }
            }
        }
        compose.onAllNodes(hasClickAction(), useUnmergedTree = true).assertCountEquals(1)
        compose.onNodeWithTag("preference").performTouchInput { click(Offset(30f, center.y)) }
        compose.runOnIdle { assertEquals(1, calls) }
    }

    @Test fun clearButtonStillReactsWithReducedMotion() {
        var calls = 0
        compose.setContent {
            MaterialTheme {
                CompositionLocalProvider(
                    LocalLiquidGlassPerformance provides LiquidGlassPerformanceState.Fallback.copy(motionScale = 0f)
                ) {
                    LiquidGlassScene {
                        LiquidSplitClearButton(
                            visible = true,
                            onClick = { calls++ },
                            modifier = Modifier.testTag("clear")
                        )
                    }
                }
            }
        }
        compose.onNodeWithTag("clear").performTouchInput { click() }
        compose.runOnIdle { assertEquals(1, calls) }
    }
}
