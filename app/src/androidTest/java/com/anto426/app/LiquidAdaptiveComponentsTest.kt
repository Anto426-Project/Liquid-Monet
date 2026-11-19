package com.anto426.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceGroup
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.inputs.otpinput.LiquidOtpInput
import com.anto426.liquidmonet.components.inputs.searchbar.LiquidSearchBar
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationBar
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatarPresence
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import androidx.compose.foundation.clickable
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
        // Activity recreation can finish attaching its first Compose root after setContent.
        compose.waitUntil(15_000) {
            runCatching { compose.onAllNodes(isRoot()).fetchSemanticsNodes().isNotEmpty() }.getOrDefault(false)
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

    @Test fun compactTabsKeepAllLargeLabelsOnOneLineWithoutScrolling() {
        val labels = listOf("Andamento", "Simula", "Obiettivo", "Proiezioni")
        content(dark = true) {
            var selected by remember { mutableIntStateOf(0) }
            LiquidTabBar(
                labels.map { LiquidNavigationItem(it) },
                selected, { selected = it }, Modifier.width(320.dp).testTag("tabs"),
            )
        }
        compose.onNodeWithText("Andamento").assertIsSelected()
        compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange), useUnmergedTree = true)
            .assertCountEquals(0)
        val panel = compose.onNodeWithTag("tabs").fetchSemanticsNode().boundsInRoot
        val boundsBefore = labels.map { label ->
            val tab = compose.onNodeWithText(label).assertIsDisplayed()
            val bounds = tab.fetchSemanticsNode().boundsInRoot
            assertTrue("$label must fit inside tabs", panel.contains(bounds.topLeft) &&
                bounds.right <= panel.right && bounds.bottom <= panel.bottom)
            val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            tab.performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("$label must fit on a single line", layouts.isNotEmpty() && layouts.all { layout ->
                layout.lineCount == 1 && layout.multiParagraph.height <= layout.size.height + 1f
            })
            bounds
        }
        assertTrue(boundsBefore.zipWithNext().all { (left, right) -> left.right <= right.left })
        compose.onNodeWithText("Proiezioni").performTouchInput { click() }.assertIsSelected()
        labels.forEachIndexed { index, label ->
            val bounds = compose.onNodeWithText(label).assertIsDisplayed().fetchSemanticsNode().boundsInRoot
            assertTrue("$label must keep its slot after selection", bounds == boundsBefore[index])
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

    @Test fun rtlTabsKeepTheLensDraggableAndSelectTheAdjacentTab() {
        content(dark = true) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                var selected by remember { mutableIntStateOf(3) }
                LiquidTabBar(
                    listOf("Andamento", "Simula", "Obiettivo", "Proiezioni").map { LiquidNavigationItem(it) },
                    selected, { selected = it }, Modifier.width(320.dp).testTag("tabs"),
                )
            }
        }
        val selected = compose.onNodeWithText("Proiezioni")
        selected.assertIsDisplayed().assertIsSelected()
        selected.performTouchInput {
            down(center)
            moveBy(androidx.compose.ui.geometry.Offset(width * 0.85f, 0f), 240)
            up()
        }
        compose.onNodeWithText("Obiettivo").assertIsSelected().assertIsDisplayed()
        capture("compact-tabs-rtl-after-drag")
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
        val clickableTab = hasAnyAncestor(hasClickAction())
        val badgeBounds = compose.onNode(hasText("99+") and clickableTab, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInRoot
        val labelBounds = listOf("Home", "Didattica", "Servizi", "Notizie").map {
            compose.onNode(hasText(it) and clickableTab, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        }
        assertTrue("Navigation labels must share a baseline below the badge", labelBounds.all {
            it.top > badgeBounds.bottom && kotlin.math.abs(it.top - labelBounds.first().top) <= 1f
        })
        for (label in listOf("Home", "Didattica", "Servizi", "Notizie")) {
            val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            compose.onNodeWithText(label).performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertTrue("$label must stay on one line: " + layouts.joinToString { "size=${it.size}, lines=${it.lineCount}, constraints=${it.layoutInput.constraints}" }, layouts.none { layout ->
                layout.lineCount != 1 || layout.multiParagraph.height > layout.size.height + 1f
            })
        }
        capture("navigation-large-text-dark")
    }

    @Test fun tabsCanExpandPastTheirRestingOutlineDuringPress() {
        content {
            Box(Modifier.size(400.dp, 220.dp).background(Color.Magenta).testTag("viewport")) {
                LiquidTabBar(
                    listOf("Andamento", "Simula", "Obiettivo", "Proiezioni").map { LiquidNavigationItem(it) },
                    0, {}, Modifier.offset(40.dp, 40.dp).width(320.dp).testTag("tabs"),
                )
            }
        }
        compose.onNodeWithTag("tabs").assertWidthIsEqualTo(320.dp)
        assertSurfaceExpandsOnPress("Andamento", "tabs-expanded-during-press")
    }

    @Test fun navigationBarCanExpandPastItsRestingOutlineDuringPress() {
        content {
            Box(Modifier.size(400.dp, 232.dp).background(Color.Magenta).testTag("viewport")) {
                LiquidNavigationBar(
                    listOf("Home", "Didattica", "Servizi", "Notizie").map {
                        LiquidNavigationItem(it, icon = LiquidIcons.Star)
                    },
                    0, {}, Modifier.offset(40.dp, 40.dp).width(320.dp),
                )
            }
        }
        assertSurfaceExpandsOnPress("Home", "navbar-expanded-during-press")
    }

    private fun assertSurfaceExpandsOnPress(label: String, captureName: String) {
        val viewport = compose.onNodeWithTag("viewport")
        val origin = viewport.fetchSemanticsNode().boundsInRoot.topLeft
        val selected = compose.onNodeWithText(label)
        val x = (selected.fetchSemanticsNode().boundsInRoot.center.x - origin.x).toInt()
        val atRest = viewport.captureToImage().toPixelMap()
        val restingTop = (0 until atRest.height).first { atRest[x, it] != Color.Magenta }

        compose.mainClock.autoAdvance = false
        selected.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(160)
        val pressed = viewport.captureToImage().toPixelMap()
        assertTrue("The lens should remain visible above the resting outline during its spring",
            (restingTop - 32).coerceAtLeast(0).until(restingTop - 2).any { y ->
                atRest[x, y] == Color.Magenta && pressed[x, y] != Color.Magenta
            },
        )
        capture(captureName)
        selected.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1200)
    }

    @Test fun cardFollowsItsOwnedDragAndReturnsOnCancellation() {
        content(fontScale = 1f) {
            LiquidCard(
                contentPadding = PaddingValues(24.dp),
                modifier = Modifier.size(340.dp, 220.dp).testTag("card"),
                clipContent = true,
                contentAlignment = androidx.compose.ui.Alignment.Center,
                onClick = {},
            ) { androidx.compose.material3.Text("Contenuto", Modifier.testTag("anchor")) }
        }
        val anchor = compose.onNodeWithTag("anchor", useUnmergedTree = true)
        val initial = anchor.fetchSemanticsNode().boundsInRoot.center
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("card").performTouchInput { down(androidx.compose.ui.geometry.Offset(center.x, height * .8f)) }
        compose.mainClock.advanceTimeBy(160)
        val held = anchor.fetchSemanticsNode().boundsInRoot.center
        compose.onNodeWithTag("card").performTouchInput { moveBy(androidx.compose.ui.geometry.Offset(80f, 0f), 100) }
        compose.mainClock.advanceTimeBy(80)
        val dragged = anchor.fetchSemanticsNode().boundsInRoot.center
        assertTrue("A large card must visibly follow its own drag: $held -> $dragged", dragged.x > held.x + 5f)
        compose.onNodeWithTag("card").performTouchInput { cancel() }
        compose.mainClock.advanceTimeBy(1600)
        val returned = anchor.fetchSemanticsNode().boundsInRoot.center
        assertTrue("Card must settle back after cancellation", (returned - initial).getDistance() < 1f)
    }

    @Test fun nestedCardDragsInsideTheRoundedParentWithoutMovingIt() {
        content(fontScale = 1f) {
            Box(Modifier.fillMaxWidth().height(250.dp).background(Color.White).testTag("viewport")) {
                LiquidCard(
                    modifier = Modifier.offset(32.dp, 32.dp).size(256.dp, 180.dp).testTag("parent"),
                    contentPadding = PaddingValues(0.dp),
                    onClick = {},
                ) {
                    LiquidCard(
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("child"),
                        clipContent = true,
                        onClick = {},
                    ) {
                        Box(Modifier.fillMaxSize().background(Color.Red))
                    }
                }
            }
        }
        val viewport = compose.onNodeWithTag("viewport")
        val origin = viewport.fetchSemanticsNode().boundsInRoot.topLeft
        val parent = compose.onNodeWithTag("parent", useUnmergedTree = true)
        val child = compose.onNodeWithTag("child", useUnmergedTree = true)
        val parentBounds = parent.fetchSemanticsNode().boundsInRoot
        val childBounds = child.fetchSemanticsNode().boundsInRoot
        val x = (parentBounds.right - origin.x).toInt()
        val y = (childBounds.center.y - origin.y).toInt()
        val resting = viewport.captureToImage().toPixelMap()
        compose.mainClock.autoAdvance = false
        child.performTouchInput {
            down(center)
            moveBy(Offset(width * 0.68f, 0f), 170)
        }
        compose.mainClock.advanceTimeBy(180)
        val dragged = viewport.captureToImage().toPixelMap()
        capture("nested-card-near-border")
        val justOutside = x + 4
        assertTrue("A nested card must leave pixels outside the parent's border unchanged",
            resting[justOutside, y] == dragged[justOutside, y],
        )
        assertTrue("The dragged card should recede on the opposite side of the gesture",
            resting[(parentBounds.left - origin.x).toInt() + 2, y] !=
                dragged[(parentBounds.left - origin.x).toInt() + 2, y],
        )
        assertTrue(parentBounds == parent.fetchSemanticsNode().boundsInRoot)
        child.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1200)
    }

    @Test fun largeCardShowsPressFeedbackEvenOnAQuickTap() {
        var clicks = 0
        content(fontScale = 1f) {
            LiquidCard(
                modifier = Modifier.offset(18.dp, 24.dp).size(340.dp, 220.dp).testTag("card"),
                contentPadding = PaddingValues(16.dp),
                onClick = { clicks++ },
            ) {
                Box(Modifier.fillMaxSize().background(Color.Cyan).testTag("body"))
            }
        }
        val card = compose.onNodeWithTag("card", useUnmergedTree = true)
        val body = compose.onNodeWithTag("body", useUnmergedTree = true)
        val widthAtRest = body.fetchSemanticsNode().boundsInRoot.width
        compose.mainClock.autoAdvance = false
        card.performTouchInput { click() }
        compose.mainClock.advanceTimeBy(64)
        capture("large-card-quick-tap")
        assertTrue("A quick tap must still draw a visible press frame",
            body.fetchSemanticsNode().boundsInRoot.width < widthAtRest - 2f,
        )
        compose.mainClock.advanceTimeBy(1200)
        assertTrue(body.fetchSemanticsNode().boundsInRoot.width == widthAtRest)
        compose.runOnIdle { assertTrue(clicks == 1) }
    }

    @Test fun cardReactsToTheInitialTouchInsideAScrollingList() {
        var clicks = 0
        val listState = androidx.compose.foundation.lazy.LazyListState()
        content(fontScale = 1f) {
            LazyColumn(Modifier.size(320.dp, 240.dp), state = listState, contentPadding = PaddingValues(16.dp)) {
                item {
                    LiquidCard(onClick = { clicks++ }, modifier = Modifier.testTag("card")) {
                        Box(Modifier.size(64.dp).testTag("anchor"))
                    }
                }
                item { Spacer(Modifier.height(400.dp)) }
            }
        }
        val card = compose.onNodeWithTag("card")
        val anchor = compose.onNodeWithTag("anchor", useUnmergedTree = true)
        val restingWidth = anchor.fetchSemanticsNode().boundsInRoot.width
        compose.mainClock.autoAdvance = false
        card.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(48)
        assertTrue("Press feedback should start before the scrollable tap timeout",
            anchor.fetchSemanticsNode().boundsInRoot.width < restingWidth,
        )
        val held = anchor.fetchSemanticsNode().boundsInRoot.center
        card.performTouchInput { moveBy(androidx.compose.ui.geometry.Offset(80f, 0f), 100) }
        compose.mainClock.advanceTimeBy(80)
        assertTrue("A horizontal drag must also move the card inside a list",
            anchor.fetchSemanticsNode().boundsInRoot.center.x > held.x + 1f,
        )
        card.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1200)
        compose.runOnIdle {
            assertTrue(clicks == 0)
            assertTrue(listState.firstVisibleItemScrollOffset == 0)
        }
        compose.mainClock.autoAdvance = true
        card.performTouchInput { swipeUp(endY = -100f) }
        compose.runOnIdle {
            assertTrue(listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0)
            assertTrue(clicks == 0)
        }
    }

    @Test fun nestedButtonKeepsElasticDragWithoutDraggingTheCard() {
        var cardClicks = 0
        var buttonClicks = 0
        content(fontScale = 1f) {
            LiquidCard(contentPadding = PaddingValues(24.dp), modifier = Modifier.width(300.dp), onClick = { cardClicks++ }) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.Text("Contenitore", Modifier.testTag("anchor"))
                    LiquidButton({ buttonClicks++ }, text = "Pulsante", modifier = Modifier.width(220.dp).testTag("button"))
                }
            }
        }
        val anchor = compose.onNodeWithTag("anchor", useUnmergedTree = true)
        val label = compose.onNodeWithText("Pulsante", useUnmergedTree = true)
        val parentBefore = anchor.fetchSemanticsNode().boundsInRoot
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("button").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(160)
        val held = label.fetchSemanticsNode().boundsInRoot.center
        compose.onNodeWithTag("button").performTouchInput { moveBy(androidx.compose.ui.geometry.Offset(80f, 0f), 100) }
        compose.mainClock.advanceTimeBy(80)
        val dragged = label.fetchSemanticsNode().boundsInRoot.center
        assertTrue("Button must keep its elastic drag: $held -> $dragged", dragged.x > held.x + 1f)
        assertTrue("Child drag must not move its parent", parentBefore == anchor.fetchSemanticsNode().boundsInRoot)
        compose.onNodeWithTag("button").performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1600)
        compose.runOnIdle {
            assertTrue("Releasing a drag inside the button must not click either control", cardClicks == 0 && buttonClicks == 0)
        }
        compose.onNodeWithTag("button").performTouchInput { click(center) }
        compose.runOnIdle { assertTrue(cardClicks == 0 && buttonClicks == 1) }
    }

    @Test fun avatarSupportsCustomGeometryColorsContentAndIndependentPresence() {
        var avatarClicks = 0
        var presenceClicks = 0
        var inherited = Color.Unspecified
        content(fontScale = 1f) {
            LiquidAvatar(
                shape = RoundedCornerShape(12.dp), size = 80.dp,
                modifier = Modifier.testTag("avatar"), initials = "AB",
                contentColor = Color.Blue, border = null, contentPadding = PaddingValues(8.dp),
                onClick = { avatarClicks++ }, presence = LiquidAvatarPresence.Online,
                presenceSize = 24.dp, presenceShape = RoundedCornerShape(4.dp),
                presenceAlignment = androidx.compose.ui.Alignment.TopEnd,
                presenceOffset = androidx.compose.ui.unit.DpOffset.Zero,
                presenceColor = Color.Green, presenceBorder = null,
                presenceContent = { Box(Modifier.fillMaxSize().testTag("presence").clickable { presenceClicks++ }) },
            ) {
                inherited = LocalContentColor.current
                Box(Modifier.fillMaxSize().testTag("avatar-content"))
            }
        }
        compose.onNodeWithTag("avatar-content", useUnmergedTree = true).assertWidthIsEqualTo(64.dp).assertHeightIsEqualTo(64.dp)
        compose.onNodeWithTag("presence").assertWidthIsEqualTo(24.dp).assertHeightIsEqualTo(24.dp).performTouchInput { click() }
        compose.runOnIdle { assertTrue(presenceClicks == 1 && avatarClicks == 0 && inherited == Color.Blue) }
        compose.onNodeWithTag("avatar").performTouchInput { click(androidx.compose.ui.geometry.Offset(width * .25f, height * .75f)) }
        compose.runOnIdle { assertTrue(avatarClicks == 1) }
        capture("custom-avatar-and-presence")
    }

    @Test fun settingsRowAndItsTrailingActionKeepIndependentPresses() {
        var rowClicks = 0
        var trailingClicks = 0
        content(fontScale = 1f) {
            var checked by remember { mutableStateOf(false) }
            LiquidPreferenceGroup(modifier = Modifier.width(320.dp)) {
                LiquidPreferenceItem(
                    title = "Impostazione",
                    onClick = { rowClicks++ },
                    trailingContent = {
                        LiquidSwitch(
                            checked = checked,
                            onCheckedChange = { checked = it; trailingClicks++ },
                            modifier = Modifier.testTag("action"),
                        )
                    },
                )
                LiquidPreferenceItem(title = "Altra impostazione", onClick = {})
            }
        }
        val row = compose.onNodeWithText("Impostazione", useUnmergedTree = true)
        val sibling = compose.onNodeWithText("Altra impostazione", useUnmergedTree = true)
        val action = compose.onNodeWithTag("action")
        val before = row.fetchSemanticsNode().boundsInRoot
        val siblingBefore = sibling.fetchSemanticsNode().boundsInRoot

        compose.mainClock.autoAdvance = false
        action.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(160)
        assertTrue("The trailing action must not animate its row", before == row.fetchSemanticsNode().boundsInRoot)
        action.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1200)
        action.assertIsOn()
        compose.runOnIdle { assertTrue(rowClicks == 0 && trailingClicks == 1) }
        row.performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(160)
        assertTrue("The row must animate its own press", before != row.fetchSemanticsNode().boundsInRoot)
        assertTrue("The sibling item must stay in place", siblingBefore == sibling.fetchSemanticsNode().boundsInRoot)
        row.performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1200)
        compose.runOnIdle { assertTrue(rowClicks == 1 && trailingClicks == 1) }
    }

    @Test fun badgeSupportsCustomShapePaddingTypographyAndDotSize() {
        content(fontScale = 1f) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LiquidBadge(
                    shape = RoundedCornerShape(4.dp), text = "Nuovo", modifier = Modifier.testTag("badge"),
                    minimumSize = 32.dp, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
                )
                LiquidBadge(shape = RoundedCornerShape(2.dp), text = null, dotSize = 12.dp, modifier = Modifier.testTag("dot"))
            }
        }
        compose.onNodeWithTag("badge").assertHeightIsAtLeast(32.dp).assertWidthIsAtLeast(48.dp)
        compose.onNodeWithTag("dot").assertWidthIsEqualTo(12.dp).assertHeightIsEqualTo(12.dp)
    }

    @Test fun iconContainerSupportsIndependentContentPadding() {
        content(fontScale = 1f) {
            Box(Modifier.liquidIconContainer(
                contentPadding = PaddingValues(start = 4.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
                containerSize = 48.dp, shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, Color.Blue),
            )) { Box(Modifier.fillMaxSize().testTag("icon-content")) }
        }
        compose.onNodeWithTag("icon-content").assertWidthIsEqualTo(32.dp).assertHeightIsEqualTo(32.dp)
    }

    @Test fun groupedControlsKeepPressFeedbackInsideTheirSharedShape() {
        var firstClicks = 0
        content(fontScale = 1f) {
            Box(Modifier.size(320.dp, 180.dp).background(Color.Magenta).testTag("viewport")) {
                LiquidCard(
                    contentPadding = PaddingValues(0.dp), clipContent = true,
                    modifier = Modifier.offset(20.dp, 20.dp).width(280.dp), shape = RoundedCornerShape(24.dp),
                ) {
                    Column {
                        LiquidCard(
                            modifier = Modifier.height(64.dp).testTag("first-row"),
                            shape = androidx.compose.ui.graphics.RectangleShape, contentPadding = 0.dp,
                            colors = LiquidCardDefaults.colors(containerColor = Color.Transparent), onClick = { firstClicks++ },
                        ) { androidx.compose.material3.Text("Primo controllo") }
                        LiquidCard(
                            modifier = Modifier.height(64.dp), contentPadding = 0.dp,
                            colors = LiquidCardDefaults.colors(containerColor = Color.Transparent), onClick = {},
                        ) { androidx.compose.material3.Text("Secondo controllo", Modifier.testTag("second-row")) }
                    }
                }
            }
        }
        val sibling = compose.onNodeWithTag("second-row", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot
        compose.mainClock.autoAdvance = false
        compose.onNodeWithTag("first-row").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(160)
        val bitmap = compose.onNodeWithTag("viewport").captureToImage()
        val pixels = bitmap.toPixelMap()
        val corner = bitmap.width * 21 / 320
        assertTrue("Press feedback leaked through the rounded group corner", pixels[corner, corner] == Color.Magenta)
        assertTrue("Sibling control must remain stable", sibling == compose.onNodeWithTag("second-row", useUnmergedTree = true).fetchSemanticsNode().boundsInRoot)
        compose.onNodeWithTag("first-row").performTouchInput { up() }
        compose.mainClock.advanceTimeBy(1200)
        compose.runOnIdle { assertTrue(firstClicks == 1) }
        capture("grouped-controls-clean-press")
    }

    @Test fun buttonSupportsCustomPaddingHeightColorsAndTypography() {
        var clicks = 0
        var inherited = Color.Unspecified
        content(fontScale = 1f) {
            LiquidButton(
                onClick = { clicks++ }, contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                minimumHeight = 72.dp, containerColor = Color.Blue, contentColor = Color.Yellow,
                shape = RoundedCornerShape(8.dp), border = null,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                modifier = Modifier.testTag("custom-button"),
            ) {
                inherited = LocalContentColor.current
                androidx.compose.material3.Text("Personalizzato")
            }
        }
        compose.onNodeWithTag("custom-button").assertHeightIsEqualTo(72.dp).performTouchInput { click() }
        val layouts = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        compose.onNodeWithText("Personalizzato").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult) { it(layouts) }
        compose.runOnIdle { assertTrue(clicks == 1 && inherited == Color.Yellow && layouts.single().layoutInput.style.fontSize == 20.sp) }
        capture("custom-button")
    }
}
