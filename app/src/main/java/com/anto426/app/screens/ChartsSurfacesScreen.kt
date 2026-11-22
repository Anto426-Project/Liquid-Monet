package com.anto426.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart
import com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.LiquidChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidPieEntry
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.display.sectionheader.LiquidSectionHeader
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.RoundedRectangle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChartsSurfacesScreen(
    toastState: LiquidToastState,
    backdropState: Backdrop,
    modifier: Modifier = Modifier,
) {
    var lineDatasetIndex by remember { mutableIntStateOf(0) }
    var smoothCurves by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var showArea by remember { mutableStateOf(true) }
    var barDatasetIndex by remember { mutableIntStateOf(0) }
    var selectedDonutLabel by remember { mutableStateOf<String?>(null) }

    val lineEntries = remember(lineDatasetIndex) {
        if (lineDatasetIndex == 0) {
            listOf(
                LiquidChartEntry("Gen", 25f),
                LiquidChartEntry("Feb", 27f),
                LiquidChartEntry("Mar", 26f),
                LiquidChartEntry("Apr", 29f),
                LiquidChartEntry("Mag", 28f),
                LiquidChartEntry("Giu", 30f),
            )
        } else {
            listOf(
                LiquidChartEntry("Lun", 62f),
                LiquidChartEntry("Mar", 78f),
                LiquidChartEntry("Mer", 74f),
                LiquidChartEntry("Gio", 91f),
                LiquidChartEntry("Ven", 88f),
                LiquidChartEntry("Sab", 96f),
            )
        }
    }
    val weightedEntries = remember(lineDatasetIndex) {
        if (lineDatasetIndex == 0) {
            listOf(
                LiquidChartEntry("Gen", 25.0f),
                LiquidChartEntry("Feb", 26.1f),
                LiquidChartEntry("Mar", 26.0f),
                LiquidChartEntry("Apr", 27.0f),
                LiquidChartEntry("Mag", 27.3f),
                LiquidChartEntry("Giu", 27.9f),
            )
        } else {
            listOf(
                LiquidChartEntry("Lun", 62f),
                LiquidChartEntry("Mar", 70f),
                LiquidChartEntry("Mer", 71.3f),
                LiquidChartEntry("Gio", 76.2f),
                LiquidChartEntry("Ven", 78.6f),
                LiquidChartEntry("Sab", 81.5f),
            )
        }
    }
    val arithmeticEntries = remember(lineDatasetIndex) {
        if (lineDatasetIndex == 0) {
            listOf(
                LiquidChartEntry("Gen", 25.0f),
                LiquidChartEntry("Feb", 26.0f),
                LiquidChartEntry("Mar", 26.0f),
                LiquidChartEntry("Apr", 26.75f),
                LiquidChartEntry("Mag", 27.0f),
                LiquidChartEntry("Giu", 27.5f),
            )
        } else {
            listOf(
                LiquidChartEntry("Lun", 62f),
                LiquidChartEntry("Mar", 70f),
                LiquidChartEntry("Mer", 71.3f),
                LiquidChartEntry("Gio", 76.25f),
                LiquidChartEntry("Ven", 78.6f),
                LiquidChartEntry("Sab", 81.5f),
            )
        }
    }

    val barEntries = remember(barDatasetIndex) {
        if (barDatasetIndex == 0) {
            listOf(
                LiquidChartEntry("6 CFU", 8f),
                LiquidChartEntry("9 CFU", 6f),
                LiquidChartEntry("12 CFU", 3f),
                LiquidChartEntry("3 CFU", 4f),
            )
        } else {
            listOf(
                LiquidChartEntry("Q1", 42f),
                LiquidChartEntry("Q2", 58f),
                LiquidChartEntry("Q3", 51f),
                LiquidChartEntry("Q4", 74f),
            )
        }
    }

    val donutEntries = remember {
        listOf(
            LiquidPieEntry("30 / 30L", 9f),
            LiquidPieEntry("27 - 29", 7f),
            LiquidPieEntry("24 - 26", 4f),
            LiquidPieEntry("18 - 23", 2f),
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        // 1. Multi-series Line Chart
        DemoAnimatedSection(order = 0) {
            LiquidSectionHeader(
                title = "LiquidLineChart Multi-Serie",
                subtitle = "Trascina il dito sul grafico per ispezionare i singoli punti e attiva/disattiva le serie dalla legenda.",
            )
            LiquidCard(backdropState = backdropState) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LiquidChip(
                            label = "Carriera (18-30)",
                            selected = lineDatasetIndex == 0,
                            onClick = { lineDatasetIndex = 0 },
                            backdropState = backdropState,
                        )
                        LiquidChip(
                            label = "Rendimenti (0-100)",
                            selected = lineDatasetIndex == 1,
                            onClick = { lineDatasetIndex = 1 },
                            backdropState = backdropState,
                        )
                        LiquidChip(
                            label = "Curve morbide",
                            selected = smoothCurves,
                            onClick = { smoothCurves = !smoothCurves },
                            backdropState = backdropState,
                        )
                        LiquidChip(
                            label = "Griglia",
                            selected = showGrid,
                            onClick = { showGrid = !showGrid },
                            backdropState = backdropState,
                        )
                        LiquidChip(
                            label = "Area",
                            selected = showArea,
                            onClick = { showArea = !showArea },
                            backdropState = backdropState,
                        )
                    }
                    LiquidLineChart(
                        entries = lineEntries,
                        weightedAverageEntries = weightedEntries,
                        arithmeticAverageEntries = arithmeticEntries,
                        primarySeriesLabel = if (lineDatasetIndex == 0) "Voto" else "Indice",
                        secondarySeriesLabel = "Ponderata",
                        tertiarySeriesLabel = "Aritmetica",
                        style = LiquidChartStyle(
                            showGridLines = showGrid,
                            showRefractiveArea = showArea,
                            smoothCurves = smoothCurves,
                        ),
                        backdropState = backdropState,
                    )
                }
            }
        }

        // 2. Bar Chart
        DemoAnimatedSection(order = 1) {
            LiquidSectionHeader(
                title = "LiquidBarChart",
                subtitle = "Confronto per categoria con scrub continuo e selezione interattiva.",
            )
            LiquidCard(backdropState = backdropState) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LiquidChip(
                            label = "Distribuzione CFU",
                            selected = barDatasetIndex == 0,
                            onClick = { barDatasetIndex = 0 },
                            backdropState = backdropState,
                        )
                        LiquidChip(
                            label = "Trimestri",
                            selected = barDatasetIndex == 1,
                            onClick = { barDatasetIndex = 1 },
                            backdropState = backdropState,
                        )
                    }
                    LiquidBarChart(
                        entries = barEntries,
                        valueSuffix = if (barDatasetIndex == 0) "esami" else "pt",
                        backdropState = backdropState,
                    )
                }
            }
        }

        // 3. Donut Chart
        DemoAnimatedSection(order = 2) {
            LiquidSectionHeader(
                title = "LiquidDonutChart",
                subtitle = "Tocca uno spicchio o una voce in legenda per evidenziare la quota.",
            )
            LiquidCard(backdropState = backdropState) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidDonutChart(
                        entries = donutEntries,
                        centerLabel = selectedDonutLabel ?: "Totale",
                        centerValue = if (selectedDonutLabel == null) "22" else {
                            donutEntries.firstOrNull { it.label == selectedDonutLabel }?.value?.toInt()?.toString() ?: "22"
                        },
                        onEntrySelected = { entry -> selectedDonutLabel = entry?.label },
                        backdropState = backdropState,
                    )
                }
            }
        }

        // 4. Status Cards & Surfaces
        DemoAnimatedSection(order = 3) {
            LiquidSectionHeader(
                title = "Status Card & Superfici Glass",
                subtitle = "Tutti e 4 i tipi semantici di LiquidStatusCard, PreferenceItem e IconContainer.",
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiquidStatusCard(
                    title = "Successo — Rendering Attivo",
                    description = "Shader e compositing hardware operativi senza frame persi.",
                    statusType = LiquidStatusType.Success,
                    backdropState = backdropState,
                )
                LiquidStatusCard(
                    title = "Informazione — Profilo Calibrato",
                    description = "I parametri ottici vengono adattati automaticamente al dispositivo.",
                    statusType = LiquidStatusType.Info,
                    backdropState = backdropState,
                )
                LiquidStatusCard(
                    title = "Avviso — Risparmio Energetico",
                    description = "In modalità batteria ridotta puoi selezionare il profilo Essential.",
                    statusType = LiquidStatusType.Warning,
                    backdropState = backdropState,
                )
                LiquidStatusCard(
                    title = "Errore — Esempio Stato Critico",
                    description = "Con pulsante di recupero integrato direttamente nella card.",
                    statusType = LiquidStatusType.Error,
                    actionLabel = "Riprova",
                    onAction = { toastState.show("Recupero eseguito", icon = LiquidIcons.Refresh) },
                    backdropState = backdropState,
                )
            }

            LiquidCard(backdropState = backdropState) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        listOf(
                            LiquidIcons.Palette to MaterialTheme.colorScheme.primary,
                            LiquidIcons.Star to MaterialTheme.colorScheme.tertiary,
                            LiquidIcons.Analytics to MaterialTheme.colorScheme.secondary,
                            LiquidIcons.Check to MaterialTheme.colorScheme.primary,
                        ).forEach { (icon, tint) ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.liquidIconContainer(
                                    containerSize = 42.dp,
                                    iconSize = 20.dp,
                                    containerColor = tint.copy(alpha = 0.14f),
                                    shape = RoundedRectangle(14.dp),
                                ),
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("liquidIconContainer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Contenitori icona coerenti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    LiquidHorizontalDivider()
                    LiquidPreferenceItem(
                        title = "LiquidPreferenceItem interattivo",
                        subtitle = "Tocca per testare risposta elastica e feedback",
                        icon = LiquidIcons.Settings,
                        onClick = { toastState.show("Elemento cliccato", icon = LiquidIcons.Check) },
                        backdropState = backdropState,
                    )
                }
            }
        }
    }
}
