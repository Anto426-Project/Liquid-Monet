package com.anto426.app.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.anto426.app.demo.DemoTab
import com.anto426.app.demo.displayName
import com.anto426.liquidmonet.components.buttons.button.LiquidButton
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonSize
import com.anto426.liquidmonet.components.buttons.button.LiquidButtonVariant
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.mediacontroller.LiquidMediaController
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.components.display.badge.LiquidBadge
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    settings: DemoSettingsState,
    toastState: LiquidToastState,
    backdropState: Backdrop,
    onNavigate: (DemoTab, Int) -> Unit,
    onOpenQuickSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // 1. Live Studio Hero & Optical Telemetry
        DemoAnimatedSection(order = 0) {
            LiquidCard(backdropState = backdropState) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = "Liquid Monet SDK 2.0",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Ottica adattiva, Monet dinamico e transizioni fluide",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant,
                            )
                        }
                        LiquidBadge(
                            text = (performance.opticalQualityTier ?: performance.qualityTier).displayName(),
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.onPrimaryContainer,
                            backdropState = backdropState,
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TelemetryPill(
                            label = "Blur",
                            value = "${(performance.blurScale * 100f).roundToInt()}%",
                            backdropState = backdropState,
                        )
                        TelemetryPill(
                            label = "Rifrazione",
                            value = "${(performance.refractionScale * 100f).roundToInt()}%",
                            backdropState = backdropState,
                        )
                        TelemetryPill(
                            label = "Motion",
                            value = "${(performance.motionScale * 100f).roundToInt()}%",
                            backdropState = backdropState,
                        )
                        TelemetryPill(
                            label = "Scala GPU",
                            value = "${(performance.renderResolutionScale * 100f).roundToInt()}%",
                            backdropState = backdropState,
                        )
                    }

                    LiquidHorizontalDivider()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Profilo vetro rapido",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
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
                                    backdropState = backdropState,
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Transizione schermate attiva: ${settings.screenTransition.displayName()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                LiquidSwitcherTransition.DirectionalHorizontal,
                                LiquidSwitcherTransition.SlideHorizontal,
                                LiquidSwitcherTransition.LiquidMorph,
                                LiquidSwitcherTransition.SharedAxisDepth,
                                LiquidSwitcherTransition.FadeThrough,
                            ).forEach { preset ->
                                LiquidChip(
                                    label = preset.displayName(),
                                    selected = settings.screenTransition == preset,
                                    onClick = { settings.screenTransition = preset },
                                    backdropState = backdropState,
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LiquidButton(
                            text = "Controlli rapidi",
                            onClick = onOpenQuickSettings,
                            variant = LiquidButtonVariant.Primary,
                            size = LiquidButtonSize.Small,
                            backdropState = backdropState,
                            modifier = Modifier.weight(1f),
                        )
                        LiquidButton(
                            text = "Lab transizioni",
                            onClick = { onNavigate(DemoTab.Overlays, 3) },
                            variant = LiquidButtonVariant.Tonal,
                            size = LiquidButtonSize.Small,
                            backdropState = backdropState,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // 2. Quick Hub Shortcuts
        DemoAnimatedSection(order = 1) {
            LiquidSectionHeader(
                title = "Esplora il Catalogo",
                subtitle = "Salta direttamente a controlli, overlay, grafici e impostazioni complete.",
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidPreferenceItem(
                    title = "Controlli & Input",
                    subtitle = "Pulsanti, campi di testo, OTP, slider, selettori e picker",
                    icon = LiquidIcons.Edit,
                    onClick = { onNavigate(DemoTab.Controls, 0) },
                    backdropState = backdropState,
                )
                LiquidPreferenceItem(
                    title = "Overlay & Motion",
                    subtitle = "Dialog, sheet, dropdown menu, toast, liste e lab transizioni",
                    icon = LiquidIcons.Notifications,
                    onClick = { onNavigate(DemoTab.Overlays, 0) },
                    backdropState = backdropState,
                )
                LiquidPreferenceItem(
                    title = "Grafici & Superfici",
                    subtitle = "Line, Bar, Donut chart e status card in vetro liquido",
                    icon = LiquidIcons.Analytics,
                    onClick = { onNavigate(DemoTab.Charts, 0) },
                    backdropState = backdropState,
                )
                LiquidPreferenceItem(
                    title = "Impostazioni SDK",
                    subtitle = "Tema, palette Monet, qualità vetro, sfondo e animazioni",
                    icon = LiquidIcons.Settings,
                    onClick = { onNavigate(DemoTab.Settings, 0) },
                    backdropState = backdropState,
                )
            }
        }

        // 3. Interactive Media Controller
        DemoAnimatedSection(order = 2) {
            LiquidSectionHeader(
                title = "Media Controller",
                subtitle = "Riproduzione continua in vetro liquido con stato live e controllo brani.",
            )
            ExploreMediaPlayerWidget(
                toastState = toastState,
                backdropState = backdropState,
            )
        }

        // 4. System Pipeline Status
        DemoAnimatedSection(order = 3) {
            LiquidStatusCard(
                title = "Pipeline Rendering Attiva",
                description = "Profilo ${(performance.opticalQualityTier ?: performance.qualityTier).displayName()} · Sfondo ${settings.backgroundEffect.displayName()} · Transizione ${settings.screenTransition.displayName()}.",
                statusType = LiquidStatusType.Success,
                actionLabel = "Apri Impostazioni",
                onAction = { onNavigate(DemoTab.Settings, 0) },
                backdropState = backdropState,
            )
        }
    }
}

@Composable
private fun TelemetryPill(
    label: String,
    value: String,
    backdropState: Backdrop,
) {
    LiquidBadge(
        text = "$label: $value",
        backdropState = backdropState,
    )
}

@Composable
private fun ExploreMediaPlayerWidget(
    toastState: LiquidToastState,
    backdropState: Backdrop,
) {
    val tracks = remember {
        listOf(
            Triple("Crystal Refraction", "Liquid Monet Ensemble", 225),
            Triple("Aurora Nocturne", "AGSL Synth Lab", 198),
            Triple("Velvet Glass 120Hz", "Monet Acoustics", 242),
        )
    }
    var trackIndex by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(false) }
    val elapsedState = remember { mutableFloatStateOf(64f) }

    val (title, artist, totalSec) = tracks[trackIndex]
    val progressState = remember(totalSec) {
        derivedStateOf { (elapsedState.floatValue / totalSec.toFloat()).coerceIn(0f, 1f) }
    }
    val currentTimeState = remember {
        derivedStateOf { formatSeconds(elapsedState.floatValue.roundToInt()) }
    }

    LaunchedEffect(isPlaying, trackIndex) {
        while (isPlaying) {
            delay(1000L)
            val next = elapsedState.floatValue + 1f
            if (next >= totalSec) {
                trackIndex = (trackIndex + 1) % tracks.size
                elapsedState.floatValue = 0f
            } else {
                elapsedState.floatValue = next
            }
        }
    }

    LiquidMediaController(
        title = title,
        artist = artist,
        isPlaying = isPlaying,
        progress = progressState.value,
        progressState = progressState,
        currentTime = currentTimeState.value,
        currentTimeState = currentTimeState,
        totalTime = formatSeconds(totalSec),
        onPlayPauseClick = { isPlaying = !isPlaying },
        onPreviousClick = {
            trackIndex = (trackIndex + tracks.size - 1) % tracks.size
            elapsedState.floatValue = 0f
        },
        onNextClick = {
            trackIndex = (trackIndex + 1) % tracks.size
            elapsedState.floatValue = 0f
        },
        onContainerClick = {
            toastState.show(
                message = title,
                subtitle = "$artist · ${formatSeconds(totalSec)}",
                icon = LiquidIcons.MusicNote,
            )
        },
        backdropState = backdropState,
    )
}

private fun formatSeconds(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val minutes = safe / 60
    val seconds = safe % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
