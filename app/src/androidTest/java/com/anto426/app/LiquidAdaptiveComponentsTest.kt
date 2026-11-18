package com.anto426.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.inputs.otpinput.LiquidOtpInput
import com.anto426.liquidmonet.components.inputs.searchbar.LiquidSearchBar
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationBar
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.icons.LiquidIcons
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LiquidAdaptiveComponentsTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    private fun content(dark: Boolean = false, fontScale: Float = 2f, body: @Composable () -> Unit) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                        LiquidGlassScene { body() }
                    }
                }
            }
        }
    }

    private fun capture(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val directory = File(compose.activity.getExternalFilesDir(null), "adaptive-captures").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use {
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    @Test fun largeTextControlsGrowWithinCompactWidth() {
        var iconClicks = 0
        content {
            Column(Modifier.width(320.dp).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidButton({}, text = "Conferma scelta", modifier = Modifier.width(180.dp).testTag("button"))
                LiquidChip("Preferenze account", {}, modifier = Modifier.testTag("chip"))
                LiquidSearchBar("Ricerca", {}, modifier = Modifier.testTag("search"))
                LiquidIconButton(LiquidIcons.Star, { iconClicks++ }, modifier = Modifier.testTag("icon"), contentDescription = "Preferito")
            }
        }
        compose.onNodeWithTag("button").assertHeightIsAtLeast(72.dp)
        compose.onNodeWithTag("chip").assertHeightIsAtLeast(48.dp)
        compose.onNodeWithTag("search").assertHeightIsAtLeast(56.dp)
        // The 40 dp visual icon reserves a 48 dp touch target, including the outer margin.
        compose.onNodeWithTag("icon").performTouchInput { click(androidx.compose.ui.geometry.Offset(-2f, center.y)) }
        compose.runOnIdle { assertTrue(iconClicks == 1) }
        capture("compact-large-text-light")
    }

    @Test fun compactTabsExposeSelectionAndKeepLargeLabelsInsidePanel() {
        content(dark = true) {
            var selected by remember { mutableIntStateOf(0) }
            LiquidTabBar(
                listOf("Andamento", "Simula", "Obiettivo", "Proiezioni").map { LiquidNavigationItem(it) },
                selected, { selected = it }, Modifier.width(320.dp).testTag("tabs"),
            )
        }
        compose.onNodeWithText("Andamento").assertIsSelected()
        compose.onNodeWithText("Proiezioni").performScrollTo().performClick().assertIsSelected()
        val panel = compose.onNodeWithTag("tabs").fetchSemanticsNode().boundsInRoot
        for (label in listOf("Andamento", "Simula", "Obiettivo", "Proiezioni")) {
            compose.onNodeWithText(label).performScrollTo()
            val bounds = compose.onNodeWithText(label).fetchSemanticsNode().boundsInRoot
            assertTrue("$label must fit inside tabs", bounds.top >= panel.top && bounds.bottom <= panel.bottom)
            val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            compose.onNodeWithText(label).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("$label must remain readable without ellipsis", layouts.isNotEmpty() && layouts.none { layout ->
                layout.didOverflowHeight || (0 until layout.lineCount).any { layout.isLineEllipsized(it) }
            })
        }
        capture("compact-tabs-large-text-dark")
    }

    @Test fun sixDigitOtpWrapsWithoutLosingInputOrRunningOffscreen() {
        content {
            var value by remember { mutableStateOf("") }
            Box(Modifier.width(280.dp).testTag("viewport")) {
                LiquidOtpInput(value, { value = it }, length = 6)
            }
        }
        compose.onNode(hasSetTextAction()).performTextReplacement("123456")
        val viewport = compose.onNodeWithTag("viewport").fetchSemanticsNode().boundsInRoot
        for (digit in '1'..'6') {
            val bounds = compose.onNodeWithText(digit.toString(), useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
            assertTrue("OTP digit must stay inside the field", bounds.left >= viewport.left && bounds.right <= viewport.right)
        }
        capture("compact-otp-large-text")
    }

    @Test fun longDialogKeepsActionsReachableAtLargeFontScale() {
        var confirmed = false
        content {
            LiquidDialog(
                {}, "Conferma operazione", text = List(30) { "Informazioni da leggere." }.joinToString(" "),
                confirmButton = { LiquidButton({ confirmed = true }, text = "Conferma", modifier = Modifier.testTag("confirm")) },
                dismissButton = { LiquidButton({}, text = "Annulla", modifier = Modifier.testTag("dismiss")) },
            )
        }
        compose.onNodeWithTag("confirm").assertIsDisplayed().performClick()
        compose.onNodeWithTag("dismiss").assertIsDisplayed()
        compose.runOnIdle { assertTrue(confirmed) }
        capture("dialog-large-text")
    }

    @Test fun expandedTabsUseAvailableWidthWithoutChangingSelection() {
        content(fontScale = 1f) {
            LiquidTabBar(
                listOf("Sostenuti", "Piano di studio").map { LiquidNavigationItem(it, icon = LiquidIcons.Star) },
                1, {}, Modifier.fillMaxWidth().testTag("tabs"),
            )
        }
        compose.onNodeWithText("Piano di studio").assertIsSelected().assertIsDisplayed()
        capture("expanded-tabs")
    }

    @Test fun cardSupportsIndependentPaddingAndContentSizedWidth() {
        content(fontScale = 1f) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                LiquidCard(
                    contentPadding = PaddingValues(start = 24.dp, top = 12.dp, end = 8.dp, bottom = 4.dp),
                    modifier = Modifier.testTag("card"),
                    fillWidth = false,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Blue),
                    colors = LiquidCardDefaults.colors(contentColor = Color.Blue),
                    contentModifier = Modifier.testTag("content"),
                    clipContent = true,
                ) {
                    Box(Modifier.size(80.dp, 40.dp).testTag("body"))
                }
            }
        }
        compose.onNodeWithTag("card").assertWidthIsEqualTo(112.dp).assertHeightIsEqualTo(56.dp)
        compose.onNodeWithTag("body").assertLeftPositionInRootIsEqualTo(8.dp).assertTopPositionInRootIsEqualTo(12.dp)
        capture("card-custom-padding-rtl")
    }

    @Test fun disabledCardRetainsIndependentNestedControlAndContentColor() {
        var clicks = 0
        var inheritedColor = Color.Unspecified
        content(fontScale = 1f) {
            LiquidCard(
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.testTag("card"),
                enabled = false, onClick = { error("Disabled card must not handle clicks") },
                colors = LiquidCardDefaults.colors(contentColor = Color.Blue),
            ) {
                inheritedColor = LocalContentColor.current
                LiquidButton({ clicks++ }, text = "Azione", modifier = Modifier.testTag("child"))
            }
        }
        compose.onNodeWithTag("card").assertIsNotEnabled()
        compose.onNodeWithTag("child").performClick()
        compose.runOnIdle { assertTrue(clicks == 1 && inheritedColor == Color.Blue) }
    }

    @Test fun multipleNestedActionsDoNotAnimateOrClickTheirParentCard() {
        var parentClicks = 0
        var firstClicks = 0
        var secondClicks = 0
        content(fontScale = 1f) {
            LiquidCard(
                contentPadding = PaddingValues(24.dp),
                modifier = Modifier.width(300.dp).testTag("card"),
                onClick = { parentClicks++ },
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.Text("Azioni indipendenti", Modifier.testTag("anchor"))
                    LiquidButton({ firstClicks++ }, text = "Prima azione", modifier = Modifier.testTag("first"))
                    LiquidButton({ secondClicks++ }, text = "Seconda azione", modifier = Modifier.testTag("second"))
                }
            }
        }
        val anchorBefore = compose.onNodeWithTag("anchor", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("first").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(200)
        val anchorDuring = compose.onNodeWithTag("anchor", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        assertTrue("Nested press must not transform the parent card", anchorBefore == anchorDuring)
        compose.onNodeWithTag("first").performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1200)
        compose.onNodeWithTag("second").performClick()
        compose.runOnIdle { assertTrue(firstClicks == 1 && secondClicks == 1 && parentClicks == 0) }
        compose.onNodeWithTag("card").performTouchInput { click(androidx.compose.ui.geometry.Offset(width - 4f, height - 4f)) }
        compose.mainClock.advanceTimeBy(1200)
        compose.runOnIdle { assertTrue(parentClicks == 1) }
        capture("card-independent-actions")
    }

    @Test fun configurableCardHonorsAlignmentAtFixedHeight() {
        content(fontScale = 1f) {
            LiquidCard(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.size(200.dp),
                contentAlignment = androidx.compose.ui.Alignment.BottomEnd,
            ) { Box(Modifier.size(40.dp).testTag("aligned")) }
        }
        compose.onNodeWithTag("aligned").assertLeftPositionInRootIsEqualTo(144.dp).assertTopPositionInRootIsEqualTo(144.dp)
    }

    @Test fun tabBarSupportsAnUnboundedHorizontalParent() {
        content(fontScale = 1f) {
            Row {
                LiquidTabBar(listOf(LiquidNavigationItem("Prima"), LiquidNavigationItem("Seconda")), 0, {})
            }
        }
        compose.onNodeWithText("Prima").assertIsDisplayed().assertIsSelected()
    }

    @Test fun navigationLabelsAndBadgesFitAtLargeFontScale() {
        content(dark = true) {
            LiquidNavigationBar(
                listOf("Home", "Didattica", "Servizi", "Notizie").map {
                    LiquidNavigationItem(it, icon = LiquidIcons.Star, badge = if (it == "Notizie") "99+" else null)
                }, 0, {}, Modifier.width(320.dp),
            )
        }
        compose.onNodeWithText("Notizie").assertIsDisplayed()
        compose.onNodeWithText("99+").assertIsDisplayed()
        capture("navigation-large-text-dark")
    }
}
