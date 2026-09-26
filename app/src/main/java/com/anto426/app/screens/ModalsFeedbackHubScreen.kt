package com.anto426.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.feedback.dialog.LiquidDialog
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoading
import com.anto426.liquidmonet.components.feedback.loading.LiquidLoadingStyle
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.feedback.sheet.LiquidSheet
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.glass.overlay.LiquidGlassDropdownPlacement
import com.anto426.liquidmonet.glass.overlay.liquidGlassOverlayAnchor
import com.anto426.liquidmonet.glass.overlay.rememberLiquidGlassOverlayAnchorState
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop

@Composable
fun ModalsFeedbackHubScreen(
    toastState: LiquidToastState,
    backdropState: Backdrop,
    modifier: Modifier = Modifier
) {
    var currentSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        LiquidNavigationItem("Dialog & Sheet"),
        LiquidNavigationItem("Menu"),
        LiquidNavigationItem("Toast"),
        LiquidNavigationItem("Caricamento")
    )

    var isDialogOpen by remember { mutableStateOf(false) }
    var isSheetOpen by remember { mutableStateOf(false) }
    var isMenuOpen by remember { mutableStateOf(false) }

    var menuVerticalDir by remember { mutableIntStateOf(0) }
    var menuHorizAlign by remember { mutableIntStateOf(0) }
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-Navigation Liquid Tabs
        LiquidTabBar(
            items = subTabs,
            selectedIndex = currentSubTab,
            onTabSelected = { currentSubTab = it },
            backdropState = backdropState
        )

        when (currentSubTab) {
                0 -> {
                    // Sotto-Schermata 1: Dialog & Sheet
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LiquidSectionHeader(
                            title = "Finestre Modali & Bottom Sheet",
                            subtitle = "Presenta decisioni e dettagli temporanei sopra il contenuto corrente."
                        )
                        LiquidCard(backdropState = backdropState) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Finestre in Vetro Ottico con Rifrazione Snell", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                    LiquidButton(text = "Apri Dialog", onClick = { isDialogOpen = true }, variant = LiquidButtonVariant.Primary, backdropState = backdropState, modifier = Modifier.weight(1f))
                                    LiquidButton(text = "Apri Sheet", onClick = { isSheetOpen = true }, variant = LiquidButtonVariant.Secondary, backdropState = backdropState, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Sotto-Schermata 2: Dropdown Menu
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LiquidSectionHeader(
                            title = "Menu a Tendina (Dropdown)",
                            subtitle = "Verifica apertura, direzione e allineamento del menu rispetto all'ancora."
                        )
                        LiquidCard(backdropState = backdropState) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Direzione Verticale", style = MaterialTheme.typography.labelMedium)
                                LiquidTabBar(
                                    items = listOf(
                                        LiquidNavigationItem("Sotto"),
                                        LiquidNavigationItem("Sopra"),
                                        LiquidNavigationItem("Auto")
                                    ),
                                    selectedIndex = menuVerticalDir,
                                    onTabSelected = { menuVerticalDir = it },
                                    backdropState = backdropState
                                )

                                if (menuVerticalDir != 2) {
                                    Text("Allineamento Orizzontale", style = MaterialTheme.typography.labelMedium)
                                    LiquidTabBar(
                                        items = listOf(
                                            LiquidNavigationItem("Destra"),
                                            LiquidNavigationItem("Centro"),
                                            LiquidNavigationItem("Sinistra")
                                        ),
                                        selectedIndex = menuHorizAlign,
                                        onTabSelected = { menuHorizAlign = it },
                                        backdropState = backdropState
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
                                            .liquidGlassOverlayAnchor(menuAnchorState)
                                    )

                                    LiquidDropdownMenu(
                                        expanded = isMenuOpen,
                                        onDismissRequest = { isMenuOpen = false },
                                        anchorState = menuAnchorState,
                                        placement = selectedPlacement,
                                        offset = DpOffset(0.dp, 6.dp),
                                        backdropState = backdropState
                                    ) {
                                        LiquidMenuItem(text = "Condividi Elemento", icon = LiquidIcons.Share, onClick = { isMenuOpen = false; toastState.show("Condivisione avviata!", icon = LiquidIcons.Share) })
                                        LiquidMenuItem(text = "Salva nei Preferiti", icon = LiquidIcons.Star, onClick = { isMenuOpen = false; toastState.show("Aggiunto ai preferiti!", icon = LiquidIcons.Star) })
                                        LiquidMenuItem(text = "Elimina", icon = LiquidIcons.Delete, onClick = { isMenuOpen = false; toastState.show("Elemento rimosso", icon = LiquidIcons.Delete) })
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Sotto-Schermata 3: Notifiche Toast
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LiquidSectionHeader(
                            title = "Notifiche Toast in Cristallo Rifrattivo",
                            subtitle = "Comunica esito, avvisi ed errori senza interrompere il flusso."
                        )
                        LiquidCard(backdropState = backdropState) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Tocca un pulsante per mostrare un Toast in puro vetro liquido:", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    LiquidButton(
                                        text = "Successo",
                                        onClick = { toastState.show("Operazione completata!", "I dati sono stati sincronizzati su cloud", icon = LiquidIcons.Check) },
                                        variant = LiquidButtonVariant.Primary,
                                        size = LiquidButtonSize.Small,
                                        backdropState = backdropState,
                                        modifier = Modifier.weight(1f)
                                    )
                                    LiquidButton(
                                        text = "Informazione",
                                        onClick = { toastState.show("Aggiornamento pronto", "Versione Liquid Monet 2.0.0 scaricata", icon = LiquidIcons.Info) },
                                        variant = LiquidButtonVariant.Secondary,
                                        size = LiquidButtonSize.Small,
                                        backdropState = backdropState,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    LiquidButton(
                                        text = "Avviso",
                                        onClick = { toastState.show("Batteria scarica", "Meno del 15% di carica residua", icon = LiquidIcons.Warning) },
                                        variant = LiquidButtonVariant.Tonal,
                                        size = LiquidButtonSize.Small,
                                        backdropState = backdropState,
                                        modifier = Modifier.weight(1f)
                                    )
                                    LiquidButton(
                                        text = "Errore",
                                        onClick = { toastState.show("Errore di rete", "Impossibile contattare il server remoto", icon = LiquidIcons.Close) },
                                        variant = LiquidButtonVariant.Outlined,
                                        size = LiquidButtonSize.Small,
                                        backdropState = backdropState,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Sotto-Schermata 4: Caricamento & Shimmer
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LiquidSectionHeader(
                            title = "Indicatori di Avanzamento & Skeleton",
                            subtitle = "Mostra progresso determinato, attese brevi e contenuti in preparazione."
                        )
                        LiquidCard(backdropState = backdropState) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                LiquidLoading(style = LiquidLoadingStyle.Linear, progress = 0.68f, message = "Avanzamento ondulatorio fluido (68%)", backdropState = backdropState)
                                LiquidHorizontalDivider()
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Spinner Circolare Monet", style = MaterialTheme.typography.bodyMedium)
                                    LiquidLoading(style = LiquidLoadingStyle.Circular, backdropState = backdropState)
                                }
                                LiquidHorizontalDivider()
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Gocce di Cristallo Liquide", style = MaterialTheme.typography.bodyMedium)
                                    LiquidLoading(style = LiquidLoadingStyle.Dots, backdropState = backdropState)
                                }
                                LiquidHorizontalDivider()
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Impulso Radiale Prismatico", style = MaterialTheme.typography.bodyMedium)
                                    LiquidLoading(style = LiquidLoadingStyle.Pulse, backdropState = backdropState)
                                }
                                LiquidHorizontalDivider()
                                Text("Scheletro Shimmer in Vetro", style = MaterialTheme.typography.bodyMedium)
                                LiquidLoading(style = LiquidLoadingStyle.Shimmer, modifier = Modifier.fillMaxWidth().height(36.dp), backdropState = backdropState)
                            }
                        }
                    }
                }
        }
    }

    // Modal Dialogs & Sheets
    if (isDialogOpen) {
        LiquidDialog(
            onDismissRequest = { isDialogOpen = false },
            title = "Conferma Operazione",
            text = "Vuoi applicare e salvare le nuove impostazioni di sistema?",
            backdropState = backdropState,
            confirmButton = {
                LiquidButton(
                    text = "Conferma",
                    onClick = {
                        isDialogOpen = false
                        toastState.show("Impostazioni salvate!", icon = LiquidIcons.Check)
                    },
                    variant = LiquidButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                LiquidButton(
                    text = "Annulla",
                    onClick = { isDialogOpen = false },
                    variant = LiquidButtonVariant.Secondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        )
    }

    if (isSheetOpen) {
        LiquidSheet(
            onDismissRequest = { isSheetOpen = false },
            title = "Informazioni di Sistema",
            subtitle = "Dettagli runtime grafico e architettura UI.",
            backdropState = backdropState
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                LiquidPreferenceItem(title = "Accelerazione Grafica", subtitle = "AGSL Skia Hardware Pipeline", icon = LiquidIcons.Info)
                LiquidPreferenceItem(title = "Design System", subtitle = "Material 3 Expressive & Monet", icon = LiquidIcons.Star)
                LiquidButton(text = "Chiudi", onClick = { isSheetOpen = false }, variant = LiquidButtonVariant.Primary, backdropState = backdropState, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
