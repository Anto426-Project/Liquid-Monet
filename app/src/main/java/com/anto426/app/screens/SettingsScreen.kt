package com.anto426.app.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.app.demo.DemoSettingsState
import com.anto426.app.demo.DemoThemeMode
import com.anto426.app.demo.demoPaletteOptions
import com.anto426.app.demo.description
import com.anto426.app.demo.displayName
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.sheet.LiquidSheet
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteSelector
import com.anto426.liquidmonet.components.selection.backgroundselector.LiquidBackgroundSelector
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.components.selection.select.LiquidSelect
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: DemoSettingsState,
    toastState: LiquidToastState,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // 1. Tema & Motore Monet
        DemoAnimatedSection(order = 0) {
            LiquidSectionHeader(
                title = "Tema & Motore Monet",
                subtitle = "Controlla modalitÃ  chiara/scura, colori dinamici di sistema e palette seed.",
            )
            LiquidCard(backdrop = backdrop) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Aspetto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    LiquidTabBar(
                        items = DemoThemeMode.entries.map { LiquidNavigationItem(it.label) },
                        selectedIndex = settings.themeMode.ordinal,
                        onTabSelected = { settings.themeMode = DemoThemeMode.entries[it] },
                        backdrop = backdrop,
                    )
                    Text(
                        text = settings.themeMode.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )

                    LiquidHorizontalDivider()

                    SettingsSwitchRow(
                        title = "Colori Dinamici Android (Monet)",
                        subtitle = if (settings.useSystemDynamicColor) {
                            "Estrae la palette direttamente dallo sfondo di sistema"
                        } else {
                            "Usa la palette personalizzata selezionata qui sotto"
                        },
                        checked = settings.useSystemDynamicColor,
                        onCheckedChange = { settings.useSystemDynamicColor = it },
                        backdrop = backdrop,
                    )

                    LiquidHorizontalDivider()

                    LiquidPaletteSelector(
                        options = demoPaletteOptions,
                        selectedIndex = settings.selectedPaletteIndex,
                        onSelectIndex = { index ->
                            settings.selectedPaletteIndex = index
                            settings.useSystemDynamicColor = false
                        },
                        title = "Palette Seed personalizzata",
                        backdrop = backdrop,
                    )
                }
            }
        }

        // 2. QualitÃ  Vetro & Ottica
        DemoAnimatedSection(order = 1) {
            LiquidSectionHeader(
                title = "QualitÃ  Vetro & Ottica AGSL",
                subtitle = "Cambia live tra Essential, Bilanciato, Alto, Ultra o selezione automatica.",
            )
            LiquidCard(backdrop = backdrop) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    val tiers = listOf(
                        null,
                        LiquidGlassQualityTier.MINIMAL,
                        LiquidGlassQualityTier.BALANCED,
                        LiquidGlassQualityTier.HIGH,
                        LiquidGlassQualityTier.ULTRA,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tiers.forEach { tier ->
                            LiquidChip(
                                label = tier.displayName(),
                                selected = settings.glassQuality == tier,
                                onClick = { settings.glassQuality = tier },
                                backdrop = backdrop,
                            )
                        }
                    }
                    Text(
                        text = settings.glassQuality.description(),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant,
                    )

                    LiquidHorizontalDivider()

                    SettingsSliderRow(
                        title = "IntensitÃ  Ottica Vetro (liquidIntensity)",
                        valueLabel = "${(settings.liquidIntensity * 100f).roundToInt()}%",
                        value = settings.liquidIntensity,
                        onValueChange = { settings.liquidIntensity = it },
                        valueRange = 0f..1f,
                        backdrop = backdrop,
                    )

                    LiquidHorizontalDivider()

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LiquidBadge(text = "Tier: ${(performance.opticalQualityTier ?: performance.qualityTier).displayName()}", backdrop = backdrop)
                        LiquidBadge(text = "Blur: ${(performance.blurScale * 100f).roundToInt()}%", backdrop = backdrop)
                        LiquidBadge(text = "Snell: ${(performance.refractionScale * 100f).roundToInt()}%", backdrop = backdrop)
                        LiquidBadge(text = "Cromatica: ${(performance.chromaticAberrationScale * 100f).roundToInt()}%", backdrop = backdrop)
                        LiquidBadge(text = "Risoluzione: ${(performance.renderResolutionScale * 100f).roundToInt()}%", backdrop = backdrop)
                    }
                }
            }
        }

        // 3. Sfondo Dinamico
        DemoAnimatedSection(order = 2) {
            LiquidSectionHeader(
                title = "Sfondo Dinamico (LiquidBackground)",
                subtitle = "Configura effetto cromatico, intensitÃ , velocitÃ  e vignettatura.",
            )
            LiquidCard(backdrop = backdrop) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    LiquidBackgroundSelector(
                        selectedEffect = settings.backgroundEffect,
                        onEffectSelected = { settings.backgroundEffect = it },
                        backdrop = backdrop,
                    )
                    SettingsSliderRow(
                        title = "IntensitÃ  Sfondo",
                        valueLabel = "${(settings.backgroundIntensity * 100f).roundToInt()}%",
                        value = settings.backgroundIntensity,
                        onValueChange = { settings.backgroundIntensity = it },
                        valueRange = 0f..1f,
                        backdrop = backdrop,
                    )
                    SettingsSliderRow(
                        title = "VelocitÃ  Movimento",
                        valueLabel = String.format(Locale.getDefault(), "%.1fx", settings.backgroundSpeed),
                        value = settings.backgroundSpeed,
                        onValueChange = { settings.backgroundSpeed = it },
                        valueRange = 0.1f..3.0f,
                        backdrop = backdrop,
                    )
                    LiquidHorizontalDivider()
                    SettingsSwitchRow(
                        title = "Vignettatura Periferica",
                        subtitle = "Leggero oscuramento ai bordi per profonditÃ  cinematografica",
                        checked = settings.showVignette,
                        onCheckedChange = { settings.showVignette = it },
                        backdrop = backdrop,
                    )
                }
            }
        }

        // 4. Animazioni & Transizioni Schermate
        DemoAnimatedSection(order = 3) {
            LiquidSectionHeader(
                title = "Animazioni & Transizioni Schermate",
                subtitle = "Scegli la transizione usata su tutte le schermate e sotto-tab dell'app.",
            )
            LiquidCard(backdrop = backdrop) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    LiquidSelect(
                        items = LiquidSwitcherTransition.entries,
                        selectedItem = settings.screenTransition,
                        onItemSelected = { settings.screenTransition = it },
                        label = "Transizione tra schermate",
                        itemLabel = { it.displayName() },
                        itemSubtitle = { it.description() },
                        leadingIcon = LiquidIcons.PlayArrow,
                        backdrop = backdrop,
                    )

                    LiquidHorizontalDivider()

                    Text(
                        text = "Ingresso sezioni allo scorrimento",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LiquidScreenEntranceAnimation.entries.forEach { anim ->
                            LiquidChip(
                                label = anim.displayName(),
                                selected = settings.sectionEntrance == anim,
                                onClick = {
                                    settings.sectionEntrance = anim
                                    settings.replaySections()
                                },
                                backdrop = backdrop,
                            )
                        }
                    }
                    LiquidButton(
                        text = "Riproduci animazioni sezioni",
                        onClick = {
                            settings.replaySections()
                            toastState.show("Ingressi riprodotti", settings.sectionEntrance.displayName(), LiquidIcons.Refresh)
                        },
                        variant = LiquidButtonVariant.Tonal,
                        size = LiquidButtonSize.Small,
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    LiquidHorizontalDivider()

                    SettingsSwitchRow(
                        title = "Riduci Animazioni (AccessibilitÃ )",
                        subtitle = "Azzera motionScale e disattiva le animazioni decorative continue",
                        checked = settings.reduceMotion,
                        onCheckedChange = { settings.reduceMotion = it },
                        backdrop = backdrop,
                    )
                }
            }

            // 5. Shell & Reset
            LiquidSectionHeader(
                title = "Comportamento Shell Demo",
                subtitle = "Controlla barra di navigazione, FAB flottante e reset completo.",
            )
            LiquidCard(backdrop = backdrop) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsSwitchRow(
                        title = "Nascondi Navbar allo scorrimento",
                        subtitle = "Ritrae automaticamente navbar e FAB durante lo scroll verso il basso",
                        checked = settings.autoHideNavBar,
                        onCheckedChange = { settings.autoHideNavBar = it },
                        backdrop = backdrop,
                    )
                    LiquidHorizontalDivider()
                    SettingsSwitchRow(
                        title = "Mostra FAB Controlli Rapidi",
                        subtitle = "Pulsante flottante sempre accessibile per aprire il pannello rapido",
                        checked = settings.showFab,
                        onCheckedChange = { settings.showFab = it },
                        backdrop = backdrop,
                    )
                    LiquidHorizontalDivider()
                    LiquidButton(
                        text = "Ripristina impostazioni predefinite",
                        onClick = {
                            settings.resetDefaults()
                            toastState.show("Impostazioni ripristinate", "Valori predefiniti attivi", LiquidIcons.Refresh)
                        },
                        variant = LiquidButtonVariant.Outlined,
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun QuickSettingsSheet(
    settings: DemoSettingsState,
    onDismiss: () -> Unit,
    onOpenFullSettings: () -> Unit,
    backdrop: Backdrop,
) {
    LiquidSheet(
        onDismissRequest = onDismiss,
        title = "Controlli Rapidi SDK",
        subtitle = "Modifica al volo qualitÃ  vetro, tema, sfondo e transizioni",
        backdrop = backdrop,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            LiquidTabBar(
                items = DemoThemeMode.entries.map { LiquidNavigationItem(it.label) },
                selectedIndex = settings.themeMode.ordinal,
                onTabSelected = { settings.themeMode = DemoThemeMode.entries[it] },
                backdrop = backdrop,
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("QualitÃ  vetro", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        null,
                        LiquidGlassQualityTier.MINIMAL,
                        LiquidGlassQualityTier.BALANCED,
                        LiquidGlassQualityTier.HIGH,
                        LiquidGlassQualityTier.ULTRA,
                    ).forEach { tier ->
                        LiquidChip(
                            label = tier.displayName(),
                            selected = settings.glassQuality == tier,
                            onClick = { settings.glassQuality = tier },
                            backdrop = backdrop,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Transizione schermate", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidSwitcherTransition.entries.forEach { preset ->
                        LiquidChip(
                            label = preset.displayName(),
                            selected = settings.screenTransition == preset,
                            onClick = { settings.screenTransition = preset },
                            backdrop = backdrop,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sfondo dinamico", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidBackgroundEffect.entries.forEach { effect ->
                        LiquidChip(
                            label = effect.displayName(),
                            selected = settings.backgroundEffect == effect,
                            onClick = { settings.backgroundEffect = effect },
                            backdrop = backdrop,
                        )
                    }
                }
            }

            SettingsSwitchRow(
                title = "Riduci animazioni",
                subtitle = "Disattiva motion e sfondi animati",
                checked = settings.reduceMotion,
                onCheckedChange = { settings.reduceMotion = it },
                backdrop = backdrop,
            )

            LiquidButton(
                text = "Apri tutte le impostazioni",
                onClick = {
                    onDismiss()
                    onOpenFullSettings()
                },
                variant = LiquidButtonVariant.Primary,
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    backdrop: Backdrop,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LiquidSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            backdrop = backdrop,
        )
    }
}

@Composable
private fun SettingsSliderRow(
    title: String,
    valueLabel: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    backdrop: Backdrop,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(valueLabel, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        LiquidSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            backdrop = backdrop,
        )
    }
}
