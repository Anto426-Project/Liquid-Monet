package com.anto426.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.anto426.app.demo.DemoTab
import com.anto426.app.demo.DemoThemeMode
import com.anto426.app.demo.demoCatalogEntries
import com.anto426.app.demo.displayName
import com.anto426.app.demo.rememberDemoSettingsState
import com.anto426.app.screens.ChartsSurfacesScreen
import com.anto426.app.screens.ControlsScreen
import com.anto426.app.screens.DemoSectionMotion
import com.anto426.app.screens.ExploreScreen
import com.anto426.app.screens.LocalDemoSectionMotion
import com.anto426.app.screens.OverlaysMotionScreen
import com.anto426.app.screens.QuickSettingsSheet
import com.anto426.app.screens.SettingsScreen
import com.anto426.liquidmonet.components.buttons.floatingactionbutton.LiquidFloatingActionButton
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.toast.LiquidToastHost
import com.anto426.liquidmonet.components.feedback.toast.rememberLiquidToastState
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationBar
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.topbar.LiquidTopBar
import com.anto426.liquidmonet.components.navigation.topbar.LiquidTopBarAction
import com.anto426.liquidmonet.glass.LiquidBackground
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidMonetTheme

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings = rememberDemoSettingsState()
            val systemDark = isSystemInDarkTheme()
            val isDark = settings.resolveDarkTheme(systemDark)

            LiquidMonetTheme(
                darkTheme = isDark,
                useMonetEngine = settings.useSystemDynamicColor,
                customMonetSeed = settings.activePalette.seed,
                liquidIntensity = settings.liquidIntensity,
                maximumGlassQuality = settings.glassQuality,
                reduceMotion = settings.reduceMotion,
            ) {
                val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                val toastState = rememberLiquidToastState()

                var currentTab by remember { mutableStateOf(DemoTab.Explore) }
                var controlsSubTab by remember { mutableIntStateOf(0) }
                var overlaysSubTab by remember { mutableIntStateOf(0) }

                var isSearchOpen by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }
                var isQuickSettingsOpen by remember { mutableStateOf(false) }
                var isNavBarVisible by remember { mutableStateOf(true) }
                var topBarHeight by remember { mutableStateOf(148.dp) }

                val navigateTo: (DemoTab, Int) -> Unit = { tab, subTab ->
                    when (tab) {
                        DemoTab.Controls -> controlsSubTab = subTab
                        DemoTab.Overlays -> overlaysSubTab = subTab
                        else -> Unit
                    }
                    currentTab = tab
                    isSearchOpen = false
                    searchQuery = ""
                    isNavBarVisible = true
                }

                LaunchedEffect(currentTab) {
                    scrollBehavior.state.heightOffset = 0f
                    scrollBehavior.state.contentOffset = 0f
                    isNavBarVisible = true
                }

                LaunchedEffect(settings.autoHideNavBar) {
                    if (!settings.autoHideNavBar) isNavBarVisible = true
                }

                val navBarScrollConnection = remember(settings.autoHideNavBar) {
                    object : NestedScrollConnection {
                        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                            if (!settings.autoHideNavBar) return Offset.Zero
                            if (available.y < -12f) {
                                isNavBarVisible = false
                            } else if (available.y > 12f) {
                                isNavBarVisible = true
                            }
                            return Offset.Zero
                        }
                    }
                }

                val sectionMotion = remember(settings.sectionEntrance, settings.sectionReplayCounter) {
                    DemoSectionMotion(
                        animation = settings.sectionEntrance,
                        onAnimationSelected = { settings.sectionEntrance = it },
                        replay = settings.sectionReplayCounter,
                        onReplay = settings::replaySections,
                    )
                }

                val navItems = remember {
                    DemoTab.entries.map { tab ->
                        LiquidNavigationItem(label = tab.label, icon = tab.icon)
                    }
                }

                CompositionLocalProvider(LocalDemoSectionMotion provides sectionMotion) {
                    LiquidGlassScene(
                        modifier = Modifier.fillMaxSize(),
                        background = {
                            LiquidBackground(
                                effect = settings.backgroundEffect,
                                isDark = isDark,
                                speedFactor = settings.backgroundSpeed,
                                intensity = settings.backgroundIntensity,
                                showVignette = settings.showVignette,
                            )
                        },
                        topBar = { contentBackdrop ->
                            LiquidTopBar(
                                title = currentTab.title,
                                subtitle = currentTab.subtitle,
                                backdrop = contentBackdrop,
                                scrollBehavior = scrollBehavior,
                                isSearchActive = isSearchOpen,
                                searchQuery = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearchActiveChange = { active ->
                                    isSearchOpen = active
                                    if (!active) searchQuery = ""
                                },
                                searchPlaceholder = "Cerca componenti, grafici, impostazioni...",
                                onHeightChanged = { topBarHeight = it },
                                actionItems = listOf(
                                    LiquidTopBarAction(
                                        icon = LiquidIcons.Search,
                                        label = "Cerca nel catalogo",
                                        onClick = { isSearchOpen = !isSearchOpen },
                                    ),
                                    LiquidTopBarAction(
                                        icon = LiquidIcons.MoreVert,
                                        label = "Azioni rapide",
                                        subItems = listOf(
                                            LiquidTopBarAction(
                                                icon = LiquidIcons.Visibility,
                                                label = if (isDark) "Passa a tema Chiaro" else "Passa a tema Scuro",
                                                onClick = {
                                                    settings.themeMode = if (isDark) DemoThemeMode.Light else DemoThemeMode.Dark
                                                },
                                            ),
                                            LiquidTopBarAction(
                                                icon = LiquidIcons.Star,
                                                label = "Cambia qualitÃ  vetro (${settings.glassQuality.displayName()})",
                                                onClick = {
                                                    val cycle = listOf(
                                                        LiquidGlassQualityTier.MINIMAL,
                                                        LiquidGlassQualityTier.BALANCED,
                                                        LiquidGlassQualityTier.HIGH,
                                                        LiquidGlassQualityTier.ULTRA,
                                                    )
                                                    val next = cycle[(cycle.indexOf(settings.glassQuality) + 1) % cycle.size]
                                                    settings.glassQuality = next
                                                    toastState.show("QualitÃ  vetro", next.displayName(), LiquidIcons.Star)
                                                },
                                            ),
                                            LiquidTopBarAction(
                                                icon = LiquidIcons.PlayArrow,
                                                label = "Riproduci ingressi sezioni",
                                                onClick = {
                                                    settings.replaySections()
                                                    toastState.show("Animazioni riprodotte", settings.sectionEntrance.displayName(), LiquidIcons.PlayArrow)
                                                },
                                            ),
                                            LiquidTopBarAction(
                                                icon = LiquidIcons.Refresh,
                                                label = "Ripristina predefiniti",
                                                onClick = {
                                                    settings.resetDefaults()
                                                    toastState.show("Impostazioni ripristinate", icon = LiquidIcons.Refresh)
                                                },
                                            ),
                                            LiquidTopBarAction(
                                                icon = LiquidIcons.Info,
                                                label = "Info Liquid Monet SDK",
                                                onClick = {
                                                    toastState.show(
                                                        message = "Liquid Monet 2.0",
                                                        subtitle = "Transizione: ${settings.screenTransition.displayName()} Â· Vetro: ${settings.glassQuality.displayName()}",
                                                        icon = LiquidIcons.Info,
                                                    )
                                                },
                                            ),
                                        ),
                                    ),
                                ),
                            )
                        },
                        bottomBar = { contentBackdrop ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth(),
                            ) {
                                LiquidNavigationBar(
                                    selectedIndex = currentTab.index,
                                    onItemSelected = { selectedIndex ->
                                        currentTab = DemoTab.entries[selectedIndex]
                                    },
                                    items = navItems,
                                    visible = isNavBarVisible && !isSearchOpen,
                                    backdrop = contentBackdrop,
                                )
                            }
                        },
                        overlay = { contentBackdrop ->
                            if (settings.showFab) {
                                LiquidFloatingActionButton(
                                    onClick = { isQuickSettingsOpen = true },
                                    visible = isNavBarVisible && !isSearchOpen,
                                    backdrop = contentBackdrop,
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .navigationBarsPadding()
                                        .padding(end = 16.dp, bottom = 92.dp),
                                ) {
                                    Icon(
                                        imageVector = LiquidIcons.Palette,
                                        contentDescription = "Controlli rapidi SDK",
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                            }

                            LiquidToastHost(
                                state = toastState,
                                backdrop = contentBackdrop,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 84.dp),
                            )

                            if (isQuickSettingsOpen) {
                                QuickSettingsSheet(
                                    settings = settings,
                                    onDismiss = { isQuickSettingsOpen = false },
                                    onOpenFullSettings = { navigateTo(DemoTab.Settings, 0) },
                                    backdrop = contentBackdrop,
                                )
                            }
                        },
                    ) { backdrop ->
                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(scrollBehavior.nestedScrollConnection)
                                .nestedScroll(navBarScrollConnection),
                            containerColor = Color.Transparent,
                        ) { _ ->
                            val showingSearch = isSearchOpen && searchQuery.isNotBlank()
                            if (showingSearch) {
                                val normalized = searchQuery.trim().lowercase()
                                val matches = remember(normalized) {
                                    demoCatalogEntries.filter { entry ->
                                        entry.title.lowercase().contains(normalized) ||
                                            entry.description.lowercase().contains(normalized) ||
                                            entry.keywords.lowercase().contains(normalized)
                                    }
                                }
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Spacer(modifier = Modifier.height(topBarHeight + 8.dp))
                                    LiquidSectionHeader(
                                        title = "Risultati ricerca (${matches.size})",
                                        subtitle = "Tocca un risultato per aprire direttamente la sezione corrispondente.",
                                    )
                                    if (matches.isEmpty()) {
                                        LiquidEmptyState(
                                            title = "Nessun componente trovato",
                                            description = "Prova con parole come switch, slider, dialog, grafici, essential o transizioni.",
                                            icon = LiquidIcons.Search,
                                            actionButtonText = "Cancella ricerca",
                                            onActionClick = { searchQuery = "" },
                                            backdrop = backdrop,
                                        )
                                    } else {
                                        matches.forEach { entry ->
                                            LiquidPreferenceItem(
                                                title = entry.title,
                                                subtitle = "${entry.tab.label} Â· ${entry.description}",
                                                icon = entry.icon,
                                                onClick = { navigateTo(entry.tab, entry.subTab) },
                                                backdrop = backdrop,
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(116.dp))
                                }
                            } else {
                                LiquidAnimatedSwitcher(
                                    targetState = currentTab,
                                    transition = settings.screenTransition,
                                    isForward = { from, to -> to.index >= from.index },
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.TopCenter,
                                    label = "mainDemoTabSwitcher",
                                ) { tab ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState())
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                    ) {
                                        Spacer(modifier = Modifier.height(topBarHeight + 8.dp))
                                        when (tab) {
                                            DemoTab.Explore -> ExploreScreen(
                                                settings = settings,
                                                toastState = toastState,
                                                backdrop = backdrop,
                                                onNavigate = navigateTo,
                                                onOpenQuickSettings = { isQuickSettingsOpen = true },
                                            )
                                            DemoTab.Controls -> ControlsScreen(
                                                settings = settings,
                                                requestedSubTab = controlsSubTab,
                                                toastState = toastState,
                                                backdrop = backdrop,
                                            )
                                            DemoTab.Overlays -> OverlaysMotionScreen(
                                                settings = settings,
                                                requestedSubTab = overlaysSubTab,
                                                toastState = toastState,
                                                backdrop = backdrop,
                                            )
                                            DemoTab.Charts -> ChartsSurfacesScreen(
                                                toastState = toastState,
                                                backdrop = backdrop,
                                            )
                                            DemoTab.Settings -> SettingsScreen(
                                                settings = settings,
                                                toastState = toastState,
                                                backdrop = backdrop,
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(116.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
