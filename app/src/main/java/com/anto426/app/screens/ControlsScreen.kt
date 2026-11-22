package com.anto426.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.app.demo.DemoSettingsState
import com.anto426.app.demo.demoPaletteOptions
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.buttons.floatingactionbutton.LiquidFloatingActionButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.components.inputs.otpinput.LiquidOtpInput
import com.anto426.liquidmonet.components.inputs.searchbar.LiquidSearchBar
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextField
import com.anto426.liquidmonet.components.inputs.textfield.LiquidTextFieldType
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherMode
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.pickers.datepicker.LiquidDatePickerDialog
import com.anto426.liquidmonet.components.pickers.datepicker.LiquidDatePickerField
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteSelector
import com.anto426.liquidmonet.components.pickers.timepicker.LiquidTimePickerDialog
import com.anto426.liquidmonet.components.pickers.timepicker.LiquidTimePickerField
import com.anto426.liquidmonet.components.selection.checkbox.LiquidCheckbox
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.chipselectiongroup.LiquidChipSelectionGroup
import com.anto426.liquidmonet.components.selection.radiobutton.LiquidRadioButton
import com.anto426.liquidmonet.components.selection.rangeslider.LiquidRangeSlider
import com.anto426.liquidmonet.components.selection.ratingbar.LiquidRatingBar
import com.anto426.liquidmonet.components.selection.select.LiquidSelect
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.components.selection.stepper.LiquidStepper
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt
import kotlin.time.Clock

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlsScreen(
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
            LiquidNavigationItem("Pulsanti"),
            LiquidNavigationItem("Input"),
            LiquidNavigationItem("Selettori"),
            LiquidNavigationItem("Picker"),
        )
    }

    // Input & Form state
    var searchVal by remember { mutableStateOf("") }
    var textVal by remember { mutableStateOf("Liquid Monet 2.0") }
    var passwordVal by remember { mutableStateOf("Glass2026!") }
    var emailVal by remember { mutableStateOf("studio@liquidmonet.dev") }
    var phoneVal by remember { mutableStateOf("+39 345 123 4567") }
    var numberVal by remember { mutableStateOf("120.00") }
    var notesVal by remember { mutableStateOf("Superficie ottica adattiva con bordi bilanciati e feedback tattile.") }
    var readOnlyFields by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("4268") }
    var stepperVal by remember { mutableIntStateOf(6) }

    // Selection & Sliders state
    var switchVal by remember { mutableStateOf(true) }
    var switchDisabled by remember { mutableStateOf(false) }
    var checkboxVal by remember { mutableStateOf(true) }
    var radioVal by remember { mutableIntStateOf(0) }
    var sliderVal by remember { mutableFloatStateOf(0.65f) }
    var rangeSliderVal by remember { mutableStateOf(0.20f..0.80f) }
    var selectedDropdownOption by remember { mutableStateOf("Ottica Bilanciata") }
    var selectedChips by remember { mutableStateOf(setOf("Monet M3", "AGSL")) }
    var starRating by remember { mutableIntStateOf(4) }
    var badgeCount by remember { mutableIntStateOf(7) }

    // Pickers state
    var isDatePickerOpen by remember { mutableStateOf(false) }
    var selectedDate by remember {
        mutableStateOf<LocalDate?>(Clock.System.todayIn(TimeZone.currentSystemDefault()))
    }
    var isTimePickerOpen by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableIntStateOf(15) }
    var selectedMinute by remember { mutableIntStateOf(30) }

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
            mode = LiquidSwitcherMode.Tab,
            label = "controlsSubTabSwitcher",
        ) { tab ->
            when (tab) {
                0 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "Varianti & Dimensioni Pulsanti",
                        subtitle = "Tutte le 6 varianti LiquidButton e le 3 scale dimensionali.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Primary",
                                    onClick = { toastState.show("Pulsante Primary", icon = LiquidIcons.Check) },
                                    variant = LiquidButtonVariant.Primary,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidButton(
                                    text = "Secondary",
                                    onClick = { toastState.show("Pulsante Secondary", icon = LiquidIcons.Star) },
                                    variant = LiquidButtonVariant.Secondary,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Tonal",
                                    onClick = { toastState.show("Pulsante Tonal") },
                                    variant = LiquidButtonVariant.Tonal,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidButton(
                                    text = "Glass",
                                    onClick = { toastState.show("Pulsante Glass") },
                                    variant = LiquidButtonVariant.Glass,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidButton(
                                    text = "Outlined",
                                    onClick = { toastState.show("Pulsante Outlined") },
                                    variant = LiquidButtonVariant.Outlined,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidButton(
                                    text = "Text",
                                    onClick = { toastState.show("Pulsante Text") },
                                    variant = LiquidButtonVariant.Text,
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            LiquidHorizontalDivider()
                            Text(
                                text = "Dimensioni (Small · Medium · Large)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LiquidButton(text = "Small", onClick = {}, size = LiquidButtonSize.Small, variant = LiquidButtonVariant.Tonal, backdropState = backdropState)
                                LiquidButton(text = "Medium", onClick = {}, size = LiquidButtonSize.Medium, variant = LiquidButtonVariant.Primary, backdropState = backdropState)
                                LiquidButton(text = "Large", onClick = {}, size = LiquidButtonSize.Large, variant = LiquidButtonVariant.Secondary, backdropState = backdropState)
                            }
                        }
                    }

                    LiquidSectionHeader(
                        title = "IconButton, FAB & Badge",
                        subtitle = "Varianti Standard, TopBarAction, Ghost, DropdownAnchor e contatori.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                LiquidIconButton(
                                    icon = LiquidIcons.PlayArrow,
                                    contentDescription = "Standard",
                                    variant = LiquidIconButtonVariant.Standard,
                                    onClick = { toastState.show("IconButton Standard") },
                                    backdropState = backdropState,
                                )
                                LiquidIconButton(
                                    icon = LiquidIcons.Star,
                                    contentDescription = "TopBarAction",
                                    variant = LiquidIconButtonVariant.TopBarAction,
                                    onClick = { toastState.show("IconButton TopBarAction") },
                                    backdropState = backdropState,
                                )
                                LiquidIconButton(
                                    icon = LiquidIcons.Share,
                                    contentDescription = "Ghost",
                                    variant = LiquidIconButtonVariant.Ghost,
                                    onClick = { toastState.show("IconButton Ghost") },
                                    backdropState = backdropState,
                                )
                                LiquidIconButton(
                                    icon = LiquidIcons.MoreVert,
                                    contentDescription = "DropdownAnchor",
                                    variant = LiquidIconButtonVariant.DropdownAnchor,
                                    onClick = { toastState.show("IconButton DropdownAnchor") },
                                    backdropState = backdropState,
                                )
                                LiquidFloatingActionButton(
                                    onClick = { badgeCount++ },
                                    backdropState = backdropState,
                                ) {
                                    Icon(LiquidIcons.Add, contentDescription = "Incrementa badge", modifier = Modifier.size(22.dp))
                                }
                            }
                            LiquidHorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    LiquidBadge(count = badgeCount, backdropState = backdropState)
                                    LiquidBadge(text = "Nuovo", backdropState = backdropState)
                                    LiquidBadge(text = "2.0", backdropState = backdropState)
                                }
                                LiquidButton(
                                    text = "Azzera",
                                    size = LiquidButtonSize.Small,
                                    variant = LiquidButtonVariant.Text,
                                    onClick = { badgeCount = 0 },
                                    backdropState = backdropState,
                                )
                            }
                        }
                    }

                    LiquidSectionHeader(
                        title = "Valutazione a Stelle",
                        subtitle = "LiquidRatingBar interattivo con risposta elastica.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LiquidRatingBar(
                                rating = starRating,
                                onRatingChanged = { starRating = it },
                                backdropState = backdropState,
                            )
                            Text(
                                text = "Valutazione selezionata: $starRating / 5",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                1 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "Barra di Ricerca & Campi di Testo",
                        subtitle = "Tutti i 6 tipi di LiquidTextField con bordi adattivi e modalità sola lettura.",
                    )
                    LiquidSearchBar(
                        query = searchVal,
                        onQueryChange = { searchVal = it },
                        placeholderText = "Prova la barra di ricerca locale...",
                        backdropState = backdropState,
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Modalità sola lettura (readOnly)", style = MaterialTheme.typography.bodyMedium)
                                LiquidSwitch(
                                    checked = readOnlyFields,
                                    onCheckedChange = { readOnlyFields = it },
                                    backdropState = backdropState,
                                )
                            }
                            LiquidHorizontalDivider()
                            LiquidTextField(
                                value = textVal,
                                onValueChange = { textVal = it },
                                label = "Testo standard",
                                leadingIcon = LiquidIcons.AccountCircle,
                                readOnly = readOnlyFields,
                                backdropState = backdropState,
                            )
                            LiquidTextField(
                                value = passwordVal,
                                onValueChange = { passwordVal = it },
                                type = LiquidTextFieldType.Password,
                                label = "Password",
                                readOnly = readOnlyFields,
                                backdropState = backdropState,
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidTextField(
                                    value = emailVal,
                                    onValueChange = { emailVal = it },
                                    type = LiquidTextFieldType.Email,
                                    label = "Email",
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidTextField(
                                    value = phoneVal,
                                    onValueChange = { phoneVal = it },
                                    type = LiquidTextFieldType.Phone,
                                    label = "Telefono",
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            LiquidTextField(
                                value = numberVal,
                                onValueChange = { numberVal = it },
                                type = LiquidTextFieldType.Number,
                                label = "Importo numerico",
                                backdropState = backdropState,
                            )
                            LiquidTextField(
                                value = notesVal,
                                onValueChange = { notesVal = it },
                                type = LiquidTextFieldType.TextArea,
                                label = "Note multilinea",
                                maxLength = 200,
                                backdropState = backdropState,
                            )
                        }
                    }

                    LiquidSectionHeader(
                        title = "OTP & Stepper Numerico",
                        subtitle = "Inserimento codici a 4 cifre e contatori con limiti configurabili.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            LiquidStepper(
                                value = stepperVal,
                                onValueChange = { stepperVal = it },
                                label = "Quantità elementi",
                                unit = "pz",
                                minValue = 1,
                                maxValue = 24,
                                backdropState = backdropState,
                            )
                            LiquidHorizontalDivider()
                            Text("Codice di verifica OTP", style = MaterialTheme.typography.labelMedium)
                            LiquidOtpInput(
                                otpValue = otpCode,
                                onOtpChange = { otpCode = it },
                                length = 4,
                                backdropState = backdropState,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                2 -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "Switch, Checkbox & Radio",
                        subtitle = "Controlli booleani ed esclusivi con transizioni elastiche.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("LiquidSwitch attivo", style = MaterialTheme.typography.bodyMedium)
                                LiquidSwitch(
                                    checked = switchVal,
                                    onCheckedChange = { switchVal = it },
                                    backdropState = backdropState,
                                )
                            }
                            LiquidHorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("LiquidSwitch disabilitato", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                LiquidSwitch(
                                    checked = switchDisabled,
                                    onCheckedChange = { switchDisabled = it },
                                    enabled = false,
                                    backdropState = backdropState,
                                )
                            }
                            LiquidHorizontalDivider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("LiquidCheckbox sincronizzazione", style = MaterialTheme.typography.bodyMedium)
                                LiquidCheckbox(
                                    checked = checkboxVal,
                                    onCheckedChange = { checkboxVal = it },
                                    backdropState = backdropState,
                                )
                            }
                            LiquidHorizontalDivider()
                            listOf("Profilo Bilanciato", "Profilo Prestazioni", "Profilo Risparmio").forEachIndexed { idx, label ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                    LiquidRadioButton(
                                        selected = radioVal == idx,
                                        onClick = { radioVal = idx },
                                        backdropState = backdropState,
                                    )
                                }
                            }
                        }
                    }

                    LiquidSectionHeader(
                        title = "Slider, Range & Select",
                        subtitle = "Cursori continui, intervalli e menu di selezione ancorato.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Slider continuo", style = MaterialTheme.typography.bodyMedium)
                                Text("${(sliderVal * 100f).roundToInt()}%", fontWeight = FontWeight.Bold)
                            }
                            LiquidSlider(
                                value = sliderVal,
                                onValueChange = { sliderVal = it },
                                backdropState = backdropState,
                            )
                            LiquidHorizontalDivider()
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Range Slider", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    "${(rangeSliderVal.start * 100f).roundToInt()} – ${(rangeSliderVal.endInclusive * 100f).roundToInt()}",
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                            LiquidRangeSlider(
                                value = rangeSliderVal,
                                onValueChange = { rangeSliderVal = it },
                                backdropState = backdropState,
                            )
                            LiquidHorizontalDivider()
                            LiquidSelect(
                                items = listOf("Ottica Essential", "Ottica Bilanciata", "Ottica Alta", "Ottica Ultra"),
                                selectedItem = selectedDropdownOption,
                                onItemSelected = { selectedDropdownOption = it },
                                label = "Selettore Opzioni (LiquidSelect)",
                                leadingIcon = LiquidIcons.Star,
                                backdropState = backdropState,
                            )
                        }
                    }

                    LiquidSectionHeader(
                        title = "Chip & Selezione Multipla",
                        subtitle = "LiquidChip singolo e gruppo multi-selezione con toggle globale.",
                    )
                    LiquidChipSelectionGroup(
                        items = listOf("Monet M3", "AGSL", "Snell", "120Hz"),
                        selectedItems = selectedChips,
                        onSelectionChange = { selectedChips = it },
                        label = { it },
                        leadingIcon = {
                            when (it) {
                                "Monet M3" -> LiquidIcons.Palette
                                "AGSL" -> LiquidIcons.Star
                                "Snell" -> LiquidIcons.Visibility
                                else -> LiquidIcons.Refresh
                            }
                        },
                        allLabel = "Tutti",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                else -> Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LiquidSectionHeader(
                        title = "Selettori di Data & Ora",
                        subtitle = "Campi in vetro collegati ai rispettivi dialog modali.",
                    )
                    LiquidCard(backdropState = backdropState) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                LiquidDatePickerField(
                                    selectedDate = selectedDate,
                                    onClick = { isDatePickerOpen = true },
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                                LiquidTimePickerField(
                                    selectedHour = selectedHour,
                                    selectedMinute = selectedMinute,
                                    onClick = { isTimePickerOpen = true },
                                    backdropState = backdropState,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Text(
                                text = "Data: ${selectedDate ?: "—"} · Ora: ${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    LiquidSectionHeader(
                        title = "Selettore Palette Monet",
                        subtitle = "Cambia subito il seed cromatico dell'intera app (disattiva automaticamente Monet dinamico).",
                    )
                    LiquidCard(backdropState = backdropState) {
                        LiquidPaletteSelector(
                            options = demoPaletteOptions,
                            selectedIndex = settings.selectedPaletteIndex,
                            onSelectIndex = { index ->
                                settings.selectedPaletteIndex = index
                                settings.useSystemDynamicColor = false
                                toastState.show(
                                    message = "Palette ${demoPaletteOptions[index].name}",
                                    subtitle = "Seed cromatico applicato a tutta la demo",
                                    icon = LiquidIcons.Palette,
                                )
                            },
                            backdropState = backdropState,
                        )
                    }
                }
            }
        }
    }

    LiquidDatePickerDialog(
        isOpen = isDatePickerOpen,
        onDismissRequest = { isDatePickerOpen = false },
        onDateSelected = { selectedDate = it },
        initialDate = selectedDate ?: Clock.System.todayIn(TimeZone.currentSystemDefault()),
        backdropState = backdropState,
    )

    LiquidTimePickerDialog(
        isOpen = isTimePickerOpen,
        onDismissRequest = { isTimePickerOpen = false },
        onTimeSelected = { h, m ->
            selectedHour = h
            selectedMinute = m
        },
        initialHour = selectedHour,
        initialMinute = selectedMinute,
        backdropState = backdropState,
    )
}
