package com.anto426.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.controlcentertile.LiquidControlCenterTile
import com.anto426.liquidmonet.components.cards.controlcentertile.LiquidControlCenterCompactTile
import com.anto426.liquidmonet.components.cards.controlcenterslider.LiquidControlCenterSlider
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.components.cards.mediacontroller.LiquidMediaController
import com.anto426.liquidmonet.components.selection.backgroundselector.LiquidBackgroundSelector
import com.anto426.liquidmonet.components.selection.slider.LiquidSlider
import com.anto426.liquidmonet.components.selection.switch.LiquidSwitch
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun StudioHubScreen(
    sliderVal: Float,
    onSliderChange: (Float) -> Unit,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
    selectedEffect: LiquidBackgroundEffect = LiquidBackgroundEffect.Aurora,
    onSelectEffect: (LiquidBackgroundEffect) -> Unit = {},
    isDark: Boolean = true,
    onToggleDark: (Boolean) -> Unit = {},
    backgroundSpeed: Float = 1.0f,
    onSpeedChange: (Float) -> Unit = {},
    backgroundIntensity: Float = 1.0f,
    onIntensityChange: (Float) -> Unit = {},
    useDynamicColor: Boolean = true,
    onToggleDynamicColor: (Boolean) -> Unit = {}
) {
    var currentSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        LiquidNavigationItem("Panoramica"),
        LiquidNavigationItem("Media Player"),
        LiquidNavigationItem("Control Center")
    )

    var wifiActive by remember { mutableStateOf(true) }
    var bluetoothActive by remember { mutableStateOf(false) }
    var airplaneActive by remember { mutableStateOf(false) }
    var torchActive by remember { mutableStateOf(false) }
    var hotspotActive by remember { mutableStateOf(false) }
    var dndActive by remember { mutableStateOf(false) }
    var brightnessLevel by remember { mutableFloatStateOf(0.75f) }
    var volumeLevel by remember { mutableFloatStateOf(0.60f) }

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
                    // Sotto-Schermata 1: Panoramica & Monet
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LiquidSectionHeader(
                            title = "Personalizzazione Cromatica Monet",
                            subtitle = "Colori dinamici di sistema, sfondo e intensità dell'ottica di vetro."
                        )
                        LiquidCard(backdropState = backdropState) {
                            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Colori Dinamici da Wallpaper", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(
                                            if (useDynamicColor) "Armonizzazione attiva con lo sfondo del dispositivo Android" else "Colori dinamici disattivati (modalità neutra)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    LiquidSwitch(
                                        checked = useDynamicColor,
                                        onCheckedChange = onToggleDynamicColor,
                                        backdropState = backdropState
                                    )
                                }

                                LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Modalità Scura OLED", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(
                                            if (isDark) "Nero cosmico per massimo contrasto ottico" else "Cristallo bianco perlato luminoso",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    LiquidSwitch(
                                        checked = isDark,
                                        onCheckedChange = onToggleDark,
                                        backdropState = backdropState
                                    )
                                }

                                LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Text(
                                    text = "Sfondo Ottico Dinamico",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                )
                                LiquidBackgroundSelector(
                                    selectedEffect = selectedEffect,
                                    onEffectSelected = onSelectEffect,
                                    backdropState = backdropState
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Intensità Bagliore Sfondo", style = MaterialTheme.typography.bodyMedium)
                                    Text("${(backgroundIntensity * 100).toInt()}%", fontWeight = FontWeight.Bold)
                                }
                                LiquidSlider(
                                    value = backgroundIntensity,
                                    onValueChange = onIntensityChange,
                                    valueRange = 0.2f..1.5f,
                                    backdropState = backdropState
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Velocità Dinamica Fluida", style = MaterialTheme.typography.bodyMedium)
                                    Text(String.format(Locale.getDefault(), "%.1fx", backgroundSpeed), fontWeight = FontWeight.Bold)
                                }
                                LiquidSlider(
                                    value = backgroundSpeed,
                                    onValueChange = onSpeedChange,
                                    valueRange = 0.2f..3.0f,
                                    backdropState = backdropState
                                )

                                LiquidHorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Rifrazione Vetro (Snell AGSL)", style = MaterialTheme.typography.bodyMedium)
                                    Text("${(sliderVal * 100).toInt()}%", fontWeight = FontWeight.Bold)
                                }
                                LiquidSlider(
                                    value = sliderVal,
                                    onValueChange = onSliderChange,
                                    valueRange = 0f..1f,
                                    backdropState = backdropState
                                )
                            }
                        }

                        LiquidSectionHeader(
                            title = "Stato dei Servizi",
                            subtitle = "Controlla in un colpo d'occhio rendering, sincronizzazione e risorse."
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            LiquidStatusCard(
                                title = "Pipeline Grafica",
                                description = "Shaders AGSL e accelerazione hardware attivi a 120 FPS.",
                                statusType = LiquidStatusType.Success,
                                backdropState = backdropState
                            )
                            LiquidStatusCard(
                                title = "Cache Shader",
                                description = "Ottimizzazione rendering completata con successo.",
                                statusType = LiquidStatusType.Info,
                                backdropState = backdropState
                            )
                        }
                    }
                }

                1 -> {
                    // Sotto-Schermata 2: Media Player
                    StudioMediaPlayerDemo(backdropState = backdropState)
                }

                else -> {
                    // Sotto-Schermata 3: Control Center
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        LiquidSectionHeader(
                            title = "Control Center Rapido",
                            subtitle = "Connettività a 2 colonne, griglia azioni rapide a bolla e cursori ottici in vetro liquido."
                        )

                        // 1. Gruppo Connettività Principale (2 colonne affiancate)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LiquidControlCenterTile(
                                title = "Rete Wi-Fi",
                                subtitle = if (wifiActive) "Liquid-5G" else "Spento",
                                icon = LiquidIcons.Phone,
                                active = wifiActive,
                                onClick = { wifiActive = !wifiActive },
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                            LiquidControlCenterTile(
                                title = "Bluetooth",
                                subtitle = if (bluetoothActive) "Dispositivi" else "Non attivo",
                                icon = LiquidIcons.Settings,
                                active = bluetoothActive,
                                onClick = { bluetoothActive = !bluetoothActive },
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 2. Griglia Azioni Rapide (4 Tessere Compatte a Bolla)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LiquidControlCenterCompactTile(
                                title = "Torcia",
                                icon = LiquidIcons.Star,
                                active = torchActive,
                                onClick = { torchActive = !torchActive },
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                            LiquidControlCenterCompactTile(
                                title = "Aereo",
                                icon = LiquidIcons.Info,
                                active = airplaneActive,
                                onClick = { airplaneActive = !airplaneActive },
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                            LiquidControlCenterCompactTile(
                                title = "Hotspot",
                                icon = LiquidIcons.Share,
                                active = hotspotActive,
                                onClick = { hotspotActive = !hotspotActive },
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                            LiquidControlCenterCompactTile(
                                title = "Non Disturbare",
                                icon = LiquidIcons.Notifications,
                                active = dndActive,
                                onClick = { dndActive = !dndActive },
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 3. Cursori Capsulari Ottici (Luminosità & Volume)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LiquidControlCenterSlider(
                                value = brightnessLevel,
                                onValueChange = { brightnessLevel = it },
                                icon = LiquidIcons.Star,
                                title = "Luminosità",
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                            LiquidControlCenterSlider(
                                value = volumeLevel,
                                onValueChange = { volumeLevel = it },
                                icon = LiquidIcons.PlayArrow,
                                title = "Volume",
                                backdropState = backdropState,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
        }
    }
}

@Composable
private fun StudioMediaPlayerDemo(backdropState: Backdrop) {
    var isPlayingMusic by remember { mutableStateOf(true) }
    val progressState = remember { mutableFloatStateOf(0.35f) }
    val currentTimeState = remember { mutableStateOf(formatMediaTime(progressState.floatValue)) }

    // Keep the original smooth media-player update cadence for the demo.
    LaunchedEffect(isPlayingMusic) {
        if (isPlayingMusic) {
            while (true) {
                delay(60L)
                progressState.floatValue =
                    if (progressState.floatValue >= 1f) 0f else progressState.floatValue + 0.0025f
            }
        }
    }

    LaunchedEffect(isPlayingMusic) {
        if (isPlayingMusic) {
            while (true) {
                delay(1000L)
                currentTimeState.value = formatMediaTime(progressState.floatValue)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        LiquidSectionHeader(
            title = "Player Multimediale Liquid Glass",
            subtitle = "Riproduzione, avanzamento e controlli riuniti in una superficie rifrattiva."
        )
        LiquidMediaController(
            title = "Cosmic Aurora",
            artist = "Electronic Soundscape • Lossless",
            isPlaying = isPlayingMusic,
            progress = 0.35f,
            currentTime = "1:18",
            totalTime = "3:45",
            onPlayPauseClick = { isPlayingMusic = !isPlayingMusic },
            onPreviousClick = {
                progressState.floatValue = 0.0f
                currentTimeState.value = formatMediaTime(0.0f)
            },
            onNextClick = {
                val nextProgress = (progressState.floatValue + 0.25f).coerceAtMost(0.95f)
                progressState.floatValue = nextProgress
                currentTimeState.value = formatMediaTime(nextProgress)
            },
            progressState = progressState,
            currentTimeState = currentTimeState,
            backdropState = backdropState
        )

        LiquidCard(backdropState = backdropState) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Dettagli Traccia & Audio Engine",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = "Codec: FLAC 24-bit / 96 kHz • Uscita: Audio Spaziale Prismatico con riverbero in vetro liquido.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun formatMediaTime(progress: Float): String {
    val elapsedSeconds = (progress * 225).toInt()
    return String.format(
        Locale.getDefault(),
        "%d:%02d",
        elapsedSeconds / 60,
        elapsedSeconds % 60
    )
}
