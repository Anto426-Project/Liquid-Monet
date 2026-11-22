package com.anto426.app.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anto426.app.demo.DemoSettingsState
import com.anto426.app.demo.description
import com.anto426.app.demo.displayName
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.accordion.LiquidAccordionItem
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatar
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatarGroup
import com.anto426.liquidmonet.components.display.avatar.LiquidAvatarPresence
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.emptystate.LiquidEmptyState
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.display.swipetodismiss.LiquidSwipeToDismissBox
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.feedback.sheet.LiquidSheet
import com.anto426.liquidmonet.components.feedback.shimmer.LiquidShimmerBox
import com.anto426.liquidmonet.components.feedback.snackbar.LiquidSnackbar
import com.anto426.liquidmonet.components.feedback.snackbar.LiquidSnackbarType
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.components.feedback.tooltip.LiquidTooltipBox
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.expandablecontent.LiquidExpandableContent
import com.anto426.liquidmonet.components.layout.lazy.LiquidLazyColumn
import com.anto426.liquidmonet.components.layout.lazyfooter.LiquidLazyFooter
import com.anto426.liquidmonet.components.layout.lazyfooter.LiquidLazyFooterOrientation
import com.anto426.liquidmonet.components.layout.lazyfooter.LiquidLazyFooterState
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntrance
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.components.navigation.breadcrumbs.LiquidBreadcrumbItem
import com.anto426.liquidmonet.components.navigation.breadcrumbs.LiquidBreadcrumbs
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.pagination.LiquidPageIndicator
import com.anto426.liquidmonet.components.navigation.pagination.LiquidPagination
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.glass.overlay.LiquidGlassDropdownPlacement
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.RoundedRectangle
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OverlaysMotionScreen(
    settings: DemoSettingsState,
    requestedSubTab: Int,
    toastState: LiquidToastState,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    var currentSubTab by remember { mutableIntStateOf(requestedSubTab.coerceIn(0, 3)) }
    LaunchedEffect(requestedSubTab) {
        currentSubTab = requestedSubTab.coerceIn(0, 3)
    }

    val subTabs = remember {
        listOf(
            LiquidNavigationItem("Modali"),
            LiquidNavigationItem("Loading"),
            LiquidNavigationItem("Liste"),
            LiquidNavigationItem("Transizioni"),
        )
    }

    // Modal & Menu states
    var isDialogOpen by remember { mutableStateOf(false) }
    var isSheetOpen by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }
    var menuVerticalDir by remember { mutableIntStateOf(0) }
    var menuHorizAlign by remember { mutableIntStateOf(0) }
    var snackbarType by remember { mutableStateOf(LiquidSnackbarType.Info) }
    var snackbarVisible by remember { mutableStateOf(true) }

    // Loading state
    var demoProgress by remember { mutableFloatStateOf(0.68f) }

    // Navigation & Lists state
    var pagerIndicatorPage by remember { mutableIntStateOf(1) }
    var paginationPage by remember { mutableIntStateOf(2) }
    var accordion1Open by remember { mutableStateOf(true) }
    var accordion2Open by remember { mutableStateOf(false) }
    var inlineExpandVisible by remember { mutableStateOf(false) }
    var footerState by remember { mutableStateOf(LiquidLazyFooterState.Loading) }

    // Transitions lab state
    var labTransition by remember { mutableStateOf(settings.screenTransition) }
    var labPage by remember { mutableIntStateOf(0) }
    var labForward by remember { mutableStateOf(true) }
    var entrancePreviewAnim by remember { mutableStateOf(LiquidScreenEntranceAnimation.FadeUp) }
    var entranceReplayKey by remember { mutableIntStateOf(0) }

    LaunchedEffect(settings.screenTransition) {
        labTransition = settings.screenTransition
    }

    val selectedPlacement = when (menuVerticalDir) {
        0 -> when (menuHorizAlign) {
            0 -> LiquidGlassDropdownPlacement.BelowEnd
            1 -> LiquidGlassDropdownPlacement.BelowCenter
            else -> LiquidGlassDropdownPlacement.BelowStart
        }
        1 -> when (menuHorizAlign) {
            0 -> LiquidGlassDropdownPlacement.AboveEnd
            1 -> LiquidGlassDropdownPlacement.AboveCenter
            else -> LiquidGlassDropdownPlacement.AboveStart
        }
        else -> LiquidGlassDropdownPlacement.Auto
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LiquidTabBar(
            items = subTabs,
            selectedIndex = currentSubTab,
            onTabSelected = { currentSubTab = it },
            backdropState = backdropState,
        )

        LiquidAnimatedSwitcher(
            targetState = currentSubTab,
            transition = settings.screenTransition,
            label = "overlaysSubTabSwitcher",
        ) { tab ->
            when (tab) {
                0 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "Dialog, Bottom Sheet & Tooltip",
                        subtitle = "Overlay in vetro ottico con predictive back e pressione prolungata.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Apri Dialog",
                                    onClick = { isDialogOpen = true },
                                    variant = LiquidButtonVariant.Primary,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidButton(
                                    text = "Apri Sheet",
                                    onClick = { isSheetOpen = true },
                                    variant = LiquidButtonVariant.Secondary,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            LiquidHorizontalDivider()
                            LiquidTooltipBox(
                                tooltip = "Tooltip ottico in vetro liquido!",
                                backdropState = backdropState,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                LiquidPreferenceItem(
                                    title = "Tieni premuto per il Tooltip",
                                    subtitle = "Pressione prolungata su questa riga per visualizzare il popup",
                                    icon = LiquidIcons.Info,
                                    backdropState = backdropState,
                                )
                            }
                        }
                    }

                    LiquidSectionHeader(
                        title = "Dropdown Menu & Posizionamento",
                        subtitle = "Verifica apertura, rimbalzo e allineamento del menu rispetto all'ancora.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Direzione verticale", style = MaterialTheme.typography.labelMedium)
                            LiquidTabBar(
                                items = listOf(
                                    LiquidNavigationItem("Sotto"),
                                    LiquidNavigationItem("Sopra"),
                                    LiquidNavigationItem("Auto"),
                                ),
                                selectedIndex = menuVerticalDir,
                                onTabSelected = { menuVerticalDir = it },
                                backdropState = backdropState,
                            )
                            if (menuVerticalDir != 2) {
                                Text("Allineamento orizzontale", style = MaterialTheme.typography.labelMedium)
                                LiquidTabBar(
                                    items = listOf(
                                        LiquidNavigationItem("Destra"),
                                        LiquidNavigationItem("Centro"),
                                        LiquidNavigationItem("Sinistra"),
                                    ),
                                    selectedIndex = menuHorizAlign,
                                    onTabSelected = { menuHorizAlign = it },
                                    backdropState = backdropState,
                                )
                            }
                            LiquidHorizontalDivider()
                            val menuAnchorState = rememberLiquidGlassOverlayAnchorState()
                            Box(modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Apri Dropdown Menu",
                                    onClick = { isMenuOpen = !isMenuOpen },
                                    variant = LiquidButtonVariant.Tonal,
                                    backdropState = backdropState,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .liquidGlassOverlayAnchor(menuAnchorState),
                                )
                                LiquidDropdownMenu(
                                    expanded = isMenuOpen,
                                    onDismissRequest = { isMenuOpen = false },
                                    anchorState = menuAnchorState,
                                    placement = selectedPlacement,
                                    offset = DpOffset(0.dp, 6.dp),
                                    backdropState = backdropState,
                                ) {
                                    LiquidMenuItem(
                                        text = "Condividi",
                                        icon = LiquidIcons.Share,
                                        onClick = {
                                            isMenuOpen = false
                                            toastState.show("Condivisione avviata", icon = LiquidIcons.Share)
                                        },
                                    )
                                    LiquidMenuItem(
                                        text = "Aggiungi ai preferiti",
                                        icon = LiquidIcons.Star,
                                        selected = true,
                                        onClick = {
                                            isMenuOpen = false
                                            toastState.show("Salvato nei preferiti", icon = LiquidIcons.Star)
                                        },
                                    )
                                    LiquidMenuItem(
                                        text = "Elimina elemento",
                                        icon = LiquidIcons.Delete,
                                        onClick = {
                                            isMenuOpen = false
                                            toastState.show("Elemento rimosso", icon = LiquidIcons.Delete)
                                        },
                                    )
                                }
                            }
                        }
                    }

                    LiquidSectionHeader(
                        title = "Toast & Snackbar",
                        subtitle = "Notifiche fluttuanti in alto e banner interattivi con swipe-to-dismiss.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Successo",
                                    onClick = { toastState.show("Operazione riuscita", "Sincronizzazione completata", LiquidIcons.Check) },
                                    variant = LiquidButtonVariant.Primary,
                                    size = LiquidButtonSize.Small,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidButton(
                                    text = "Info",
                                    onClick = { toastState.show("Aggiornamento SDK", "Liquid Monet 2.0 attivo", LiquidIcons.Info) },
                                    variant = LiquidButtonVariant.Secondary,
                                    size = LiquidButtonSize.Small,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Avviso",
                                    onClick = { toastState.show("Attenzione", "Verifica i parametri ottici", LiquidIcons.Warning) },
                                    variant = LiquidButtonVariant.Tonal,
                                    size = LiquidButtonSize.Small,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidButton(
                                    text = "Errore",
                                    onClick = { toastState.show("Connessione assente", "Riprova tra qualche istante", LiquidIcons.Close) },
                                    variant = LiquidButtonVariant.Outlined,
                                    size = LiquidButtonSize.Small,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            LiquidHorizontalDivider()
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LiquidSnackbarType.entries.forEach { type ->
                                    LiquidChip(
                                        label = type.name,
                                        selected = snackbarVisible && snackbarType == type,
                                        onClick = {
                                            snackbarType = type
                                            snackbarVisible = true
                                        },
                                        backdropState = backdropState,
                                    )
                                }
                            }
                            LiquidSnackbar(
                                message = "Snackbar ${snackbarType.name}: scorri lateralmente per chiudere",
                                visible = snackbarVisible,
                                type = snackbarType,
                                actionLabel = "Azione",
                                onActionClick = { toastState.show("Azione Snackbar eseguita", icon = LiquidIcons.Check) },
                                onDismiss = { snackbarVisible = false },
                                backdropState = backdropState,
                            )
                        }
                    }
                }

                1 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "LiquidLoading & Indicatori Progressivi",
                        subtitle = "Controlla live l'avanzamento determinato e confronta gli stili animati.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Progresso simulato", style = MaterialTheme.typography.bodyMedium)
                                Text("${(demoProgress * 100f).roundToInt()}%", fontWeight = FontWeight.Bold)
                            }
                            LiquidSlider(
                                value = demoProgress,
                                onValueChange = { demoProgress = it },
                                backdropState = backdropState,
                            )
                            LiquidLinearProgressIndicator(
                                progress = demoProgress,
                                backdropState = backdropState,
                            )
                            LiquidLinearProgressIndicator(
                                progress = null,
                                backdropState = backdropState,
                            )
                            LiquidHorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LiquidCircularProgressIndicator(progress = demoProgress, backdropState = backdropState)
                                    Text("Determinato", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LiquidLoading(style = LiquidLoadingStyle.Circular, backdropState = backdropState)
                                    Text("Circular", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LiquidLoading(style = LiquidLoadingStyle.Dots, backdropState = backdropState)
                                    Text("Dots", style = MaterialTheme.typography.labelSmall)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LiquidLoading(style = LiquidLoadingStyle.Pulse, backdropState = backdropState)
                                    Text("Pulse", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    LiquidSectionHeader(
                        title = "Shimmer Skeleton",
                        subtitle = "Placeholder in vetro liquido animati nella sola fase di disegno.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                LiquidShimmerBox(
                                    modifier = Modifier.size(48.dp),
                                    shape = RoundedRectangle(24.dp),
                                    backdropState = backdropState,
                                )
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LiquidShimmerBox(modifier = Modifier.fillMaxWidth(0.75f).height(14.dp), backdropState = backdropState)
                                    LiquidShimmerBox(modifier = Modifier.fillMaxWidth(0.45f).height(12.dp), backdropState = backdropState)
                                }
                            }
                            LiquidShimmerBox(modifier = Modifier.fillMaxWidth().height(72.dp), backdropState = backdropState)
                        }
                    }
                }

                2 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "Breadcrumbs, Pager & Paginazione",
                        subtitle = "Percorsi gerarchici e controllo diretto delle pagine.",
                    )
                    LiquidBreadcrumbs(
                        items = listOf(
                            LiquidBreadcrumbItem("Studio") { toastState.show("Percorso: Studio", icon = LiquidIcons.Home) },
                            LiquidBreadcrumbItem("Catalogo") { toastState.show("Percorso: Catalogo", icon = LiquidIcons.MenuBook) },
                            LiquidBreadcrumbItem("Overlay") { toastState.show("Percorso: Overlay", icon = LiquidIcons.Notifications) },
                            LiquidBreadcrumbItem("Liste"),
                        ),
                        backdropState = backdropState,
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            LiquidPageIndicator(
                                pageCount = 5,
                                currentPage = pagerIndicatorPage,
                                onPageSelected = { pagerIndicatorPage = it },
                                backdropState = backdropState,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            LiquidHorizontalDivider()
                            LiquidPagination(
                                currentPage = paginationPage,
                                totalPages = 5,
                                onPageChange = { paginationPage = it },
                                backdropState = backdropState,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    LiquidSectionHeader(
                        title = "Accordion & ExpandableContent",
                        subtitle = "Espansioni fluide senza ritagli di ombre o bordi.",
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LiquidAccordionItem(
                            title = "Architettura Ottica Adattiva",
                            subtitle = "Essential, Bilanciato, Alto e Ultra",
                            leadingIcon = LiquidIcons.Info,
                            isExpanded = accordion1Open,
                            onExpandedChange = { accordion1Open = it },
                            backdropState = backdropState,
                        ) {
                            Text(
                                text = "Ogni superficie adatta blur, rifrazione e costo GPU al profilo attivo senza sacrificare contrasto o leggibilità.",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        LiquidAccordionItem(
                            title = "Transizioni Schermate Expressive",
                            subtitle = "Z-order, parallasse e curve Emphasized",
                            leadingIcon = LiquidIcons.Star,
                            isExpanded = accordion2Open,
                            onExpandedChange = { accordion2Open = it },
                            backdropState = backdropState,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Le 8 transizioni di LiquidAnimatedSwitcher sono condivise con Navigation 3 e supportano RTL e Reduce Motion.",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                LiquidButton(
                                    text = if (inlineExpandVisible) "Nascondi dettagli extra" else "Mostra dettagli extra",
                                    onClick = { inlineExpandVisible = !inlineExpandVisible },
                                    variant = LiquidButtonVariant.Tonal,
                                    size = LiquidButtonSize.Small,
                                    backdropState = backdropState,
                                )
                                LiquidExpandableContent(visible = inlineExpandVisible) {
                                    Text(
                                        text = "LiquidExpandableContent rimuove il contenuto dalla composizione al termine dell'uscita.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }

                    LiquidSectionHeader(
                        title = "Swipe, Avatar & Empty State",
                        subtitle = "Gesti orizzontali, presenze avatar e stati vuoti.",
                    )
                    LiquidSwipeToDismissBox(
                        onDismissLeft = { toastState.show("Archiviato", icon = LiquidIcons.Info) },
                        onDismissRight = { toastState.show("Preferito", icon = LiquidIcons.Star) },
                        backdropState = backdropState,
                    ) {
                        LiquidPreferenceItem(
                            title = "Scorri a destra o sinistra",
                            subtitle = "Swipe to Dismiss con azioni contestuali",
                            icon = LiquidIcons.Share,
                            backdropState = backdropState,
                        )
                    }
                    LiquidCard(backdropState = backdropState) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                LiquidAvatar(initials = "LM", presence = LiquidAvatarPresence.Online, backdropState = backdropState)
                                LiquidAvatar(initials = "AN", presence = LiquidAvatarPresence.Busy, backdropState = backdropState)
                                LiquidAvatar(presence = LiquidAvatarPresence.Away, backdropState = backdropState)
                            }
                            LiquidAvatarGroup(
                                avatars = listOf("AL", "MK", "ST", "DV", "RK", "PX"),
                                maxDisplay = 3,
                                backdropState = backdropState,
                            )
                        }
                    }
                    LiquidEmptyState(
                        title = "Tutto aggiornato",
                        description = "Nessun elemento in sospeso in questa vista dimostrativa.",
                        icon = LiquidIcons.Check,
                        actionButtonText = "Verifica di nuovo",
                        onActionClick = { toastState.show("Controllo completato", icon = LiquidIcons.Refresh) },
                        backdropState = backdropState,
                    )

                    LiquidSectionHeader(
                        title = "LiquidLazyColumn & LiquidLazyFooter",
                        subtitle = "Overscroll elastico e footer configurabile in 4 stati.",
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        listOf(
                            "Nascosto" to LiquidLazyFooterState.Hidden,
                            "Caricamento" to LiquidLazyFooterState.Loading,
                            "Fine" to LiquidLazyFooterState.End,
                            "Errore" to LiquidLazyFooterState.Error,
                        ).forEach { (label, state) ->
                            LiquidChip(
                                label = label,
                                selected = footerState == state,
                                onClick = { footerState = state },
                                backdropState = backdropState,
                            )
                        }
                    }
                    LiquidLazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(count = 4, key = { "lazy-item-$it" }) { idx ->
                            LiquidCard(
                                modifier = Modifier.fillMaxWidth(),
                                backdropState = backdropState,
                            ) {
                                Text(
                                    text = "Elemento lista #${idx + 1} · Scorri fino al footer",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        item(key = "lazy-footer") {
                            LiquidLazyFooter(
                                state = footerState,
                                orientation = LiquidLazyFooterOrientation.Vertical,
                                onRetry = {
                                    footerState = LiquidLazyFooterState.Loading
                                    toastState.show("Ricaricamento avviato", icon = LiquidIcons.Refresh)
                                },
                            )
                        }
                    }
                }

                else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "Laboratorio Transizioni Schermate",
                        subtitle = "Confronta tutti i 9 preset di LiquidSwitcherTransition con swipe, avanti/indietro e applicazione globale.",
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LiquidSwitcherTransition.entries.forEach { preset ->
                            LiquidChip(
                                label = preset.displayName(),
                                selected = labTransition == preset,
                                onClick = { labTransition = preset },
                                backdropState = backdropState,
                            )
                        }
                    }

                    LiquidCard(backdropState = backdropState) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = labTransition.displayName(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text(
                                        text = labTransition.description(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                LiquidBadge(
                                    text = "${labPage + 1} / 4",
                                    backdropState = backdropState,
                                )
                            }

                            LiquidHorizontalDivider()

                            LiquidAnimatedSwitcher(
                                targetState = labPage,
                                transition = labTransition,
                                isForward = { _, _ -> labForward },
                                onSwipeForward = {
                                    labForward = true
                                    labPage = (labPage + 1) % 4
                                },
                                onSwipeBackward = {
                                    labForward = false
                                    labPage = (labPage + 3) % 4
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 132.dp),
                                label = "transitionLabPreview",
                            ) { page ->
                                val (pageIcon, pageTitle, pageDesc) = when (page) {
                                    0 -> Triple(LiquidIcons.Home, "Schermata Panoramica", "Controlla parallasse, z-order e assenza di overshoot.")
                                    1 -> Triple(LiquidIcons.Analytics, "Schermata Statistiche", "Osserva la nitidezza del testo su sfondo glass trasparente.")
                                    2 -> Triple(LiquidIcons.Palette, "Schermata Tema Monet", "Curve EmphasizedDecelerate in ingresso e EmphasizedAccelerate in uscita.")
                                    else -> Triple(LiquidIcons.Settings, "Schermata Impostazioni", "Scorri a destra/sinistra o usa i pulsanti per testare avanti e indietro.")
                                }
                                LiquidCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backdropState = backdropState,
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = pageIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.liquidIconContainer(
                                                containerSize = 44.dp,
                                                iconSize = 22.dp,
                                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                                shape = RoundedRectangle(14.dp),
                                            ),
                                        )
                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(pageTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                            Text(pageDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Indietro",
                                    onClick = {
                                        labForward = false
                                        labPage = (labPage + 3) % 4
                                    },
                                    variant = LiquidButtonVariant.Outlined,
                                    size = LiquidButtonSize.Small,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidButton(
                                    text = "Avanti",
                                    onClick = {
                                        labForward = true
                                        labPage = (labPage + 1) % 4
                                    },
                                    variant = LiquidButtonVariant.Primary,
                                    size = LiquidButtonSize.Small,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            LiquidButton(
                                text = if (settings.screenTransition == labTransition) {
                                    "Già attiva su tutta l'app"
                                } else {
                                    "Applica \"${labTransition.displayName()}\" a tutta l'app"
                                },
                                enabled = settings.screenTransition != labTransition,
                                onClick = {
                                    settings.screenTransition = labTransition
                                    toastState.show(
                                        message = "Transizione globale aggiornata",
                                        subtitle = labTransition.displayName(),
                                        icon = LiquidIcons.Check,
                                    )
                                },
                                variant = LiquidButtonVariant.Tonal,
                                backdropState = backdropState,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }

                    LiquidSectionHeader(
                        title = "LiquidScreenEntrance",
                        subtitle = "Testa gli ingressi dedicati a schermate e sezioni indipendenti.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LiquidScreenEntranceAnimation.entries.forEach { anim ->
                                    LiquidChip(
                                        label = anim.displayName(),
                                        selected = entrancePreviewAnim == anim,
                                        onClick = {
                                            entrancePreviewAnim = anim
                                            entranceReplayKey++
                                        },
                                        backdropState = backdropState,
                                    )
                                }
                            }
                            key(entranceReplayKey, entrancePreviewAnim) {
                                LiquidScreenEntrance(
                                    animation = entrancePreviewAnim,
                                    delayMillis = 60,
                                ) {
                                    LiquidPreferenceItem(
                                        title = "Ingresso: ${entrancePreviewAnim.displayName()}",
                                        subtitle = "Curve EmphasizedDecelerate · Tocca Riproduci per rivedere",
                                        icon = LiquidIcons.PlayArrow,
                                        backdropState = backdropState,
                                    )
                                }
                            }
                            LiquidButton(
                                text = "Riproduci ingresso",
                                onClick = { entranceReplayKey++ },
                                variant = LiquidButtonVariant.Secondary,
                                size = LiquidButtonSize.Small,
                                backdropState = backdropState,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }

    if (isDialogOpen) {
        LiquidDialog(
            onDismissRequest = { isDialogOpen = false },
            title = "Conferma Operazione",
            text = "Questa finestra modale usa LiquidDialog con vetro ottico, predictive back e adattamento automatico alla qualità scelta.",
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = "Conferma",
                    onClick = {
                        isDialogOpen = false
                        toastState.show("Dialog confermato", icon = LiquidIcons.Check)
                    },
                    variant = LiquidButtonVariant.Primary,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            dismissButton = {
                LiquidButton(
                    text = "Annulla",
                    onClick = { isDialogOpen = false },
                    variant = LiquidButtonVariant.Text,
                    backdropState = backdropState,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        )
    }

    if (isSheetOpen) {
        LiquidSheet(
            onDismissRequest = { isSheetOpen = false },
            title = "Liquid Bottom Sheet",
            subtitle = "Trascina verso il basso o usa il gesto indietro per chiudere",
            backdropState = backdropState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LiquidPreferenceItem(
                    title = "Esporta configurazione",
                    subtitle = "Salva il profilo ottico corrente",
                    icon = LiquidIcons.Share,
                    onClick = {
                        isSheetOpen = false
                        toastState.show("Profilo esportato", icon = LiquidIcons.Share)
                    },
                    backdropState = backdropState,
                )
                LiquidPreferenceItem(
                    title = "Ricarica anteprima",
                    subtitle = "Riproduce gli ingressi delle sezioni",
                    icon = LiquidIcons.Refresh,
                    onClick = {
                        isSheetOpen = false
                        settings.replaySections()
                    },
                    backdropState = backdropState,
                )
            }
        }
    }
}
