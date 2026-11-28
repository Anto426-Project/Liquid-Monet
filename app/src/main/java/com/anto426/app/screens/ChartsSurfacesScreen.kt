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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.preferenceitem.LiquidPreferenceItem
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusCard
import com.anto426.liquidmonet.components.cards.statuscard.LiquidStatusType
import com.anto426.liquidmonet.components.charts.barchart.LiquidBarChart
import com.anto426.liquidmonet.components.charts.donutchart.LiquidDonutChart
import com.anto426.liquidmonet.components.charts.linechart.LiquidLineChart
import com.anto426.liquidmonet.components.charts.model.LiquidBarChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidChartEntry
import com.anto426.liquidmonet.components.charts.model.LiquidDonutChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidLineChartStyle
import com.anto426.liquidmonet.components.charts.model.LiquidLineSeries
import com.anto426.liquidmonet.components.charts.model.LiquidLineStyle
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
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    var lineDatasetIndex by remember { mutableIntStateOf(0) }
    var smoothCurves by remember { mutableStateOf(true) }
    var showGrid by remember { mutableStateOf(true) }
    var showArea by remember { mutableStateOf(true) }
    var showLineSelection by remember { mutableStateOf(true) }
    var barDatasetIndex by remember { mutableIntStateOf(0) }
    var donutRoundCap by remember { mutableStateOf(true) }
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

    val lineSeries = remember(lineDatasetIndex, showArea, lineEntries, weightedEntries, arithmeticEntries) {
        listOf(
            LiquidLineSeries(
                label = if (lineDatasetIndex == 0) "Voto" else "Indice",
                entries = lineEntries,
                lineStyle = LiquidLineStyle.Solid,
                showArea = showArea,
                showPoints = true,
            ),
            LiquidLineSeries(
                label = "Ponderata",
                entries = weightedEntries,
                lineStyle = LiquidLineStyle.Dashed,
            ),
            LiquidLineSeries(
                label = "Aritmetica",
                entries = arithmeticEntries,
                lineStyle = LiquidLineStyle.Dotted,
            ),
        )
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
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        // 1. Containerless Multi-series Line Chart
        DemoAnimatedSection(order = 0) {
            LiquidSectionHeader(
                title = "LiquidLineChart Multi-Serie",
                subtitle = "Grafico senza contenitore: serie illimitate, stili personalizzabili e scrubbing fluido.",
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LiquidChip(
                        label = "Carriera (18-30)",
                        selected = lineDatasetIndex == 0,
                        onClick = { lineDatasetIndex = 0 },
                        backdrop = backdrop,
                    )
                    LiquidChip(
                        label = "Rendimenti (0-100)",
                        selected = lineDatasetIndex == 1,
                        onClick = { lineDatasetIndex = 1 },
                        backdrop = backdrop,
                    )
                    LiquidChip(
                        label = "Curve morbide",
                        selected = smoothCurves,
                        onClick = { smoothCurves = !smoothCurves },
                        backdrop = backdrop,
                    )
                    LiquidChip(
                        label = "Griglia",
                        selected = showGrid,
                        onClick = { showGrid = !showGrid },
                        backdrop = backdrop,
                    )
                    LiquidChip(
                        label = "Area",
                        selected = showArea,
                        onClick = { showArea = !showArea },
                        backdrop = backdrop,
                    )
                    LiquidChip(
                        label = "Dettaglio",
                        selected = showLineSelection,
                        onClick = { showLineSelection = !showLineSelection },
                        backdrop = backdrop,
                    )
                }
                LiquidLineChart(
                    series = lineSeries,
                    showLegend = true,
                    showSelection = showLineSelection,
                    style = LiquidLineChartStyle(
                        showGridLines = showGrid,
                        showYAxis = showGrid,
                        smoothCurves = smoothCurves,
                        dashedGridLines = true,
                    ),
                )
            }
        }

        // 2. Containerless Bar Chart
        DemoAnimatedSection(order = 1) {
            LiquidSectionHeader(
                title = "LiquidBarChart",
                subtitle = "Confronto per categoria senza contenitore, con stile barre e selezione personalizzabili.",
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LiquidChip(
                        label = "Distribuzione CFU",
                        selected = barDatasetIndex == 0,
                        onClick = { barDatasetIndex = 0 },
                        backdrop = backdrop,
                    )
                    LiquidChip(
                        label = "Trimestri",
                        selected = barDatasetIndex == 1,
                        onClick = { barDatasetIndex = 1 },
                        backdrop = backdrop,
                    )
                }
                LiquidBarChart(
                    entries = barEntries,
                    valueSuffix = if (barDatasetIndex == 0) "esami" else "pt",
                    showSelection = true,
                    style = LiquidBarChartStyle(
                        barWidth = 24.dp,
                        dashedGridLines = true,
                    ),
                )
            }
        }

        // 3. Containerless Donut Chart
        DemoAnimatedSection(order = 2) {
            LiquidSectionHeader(
                title = "LiquidDonutChart",
                subtitle = "Anello trasparente senza badge centrale o card, con legenda essenziale e cap arrotondati.",
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LiquidChip(
                    label = if (donutRoundCap) "EstremitÃ  arrotondate" else "EstremitÃ  nette",
                    selected = donutRoundCap,
                    onClick = { donutRoundCap = !donutRoundCap },
                    backdrop = backdrop,
                )
                LiquidDonutChart(
                    entries = donutEntries,
                    centerLabel = selectedDonutLabel ?: "Totale",
                    centerValue = if (selectedDonutLabel == null) "22" else {
                        donutEntries.firstOrNull { it.label == selectedDonutLabel }?.value?.toInt()?.toString() ?: "22"
                    },
                    showLegend = true,
                    style = LiquidDonutChartStyle(
                        strokeWidth = 20.dp,
                        strokeCap = if (donutRoundCap) StrokeCap.Round else StrokeCap.Butt,
                        sliceGapDegrees = if (donutRoundCap) 6f else 3f,
                    ),
                    onEntrySelected = { entry -> selectedDonutLabel = entry?.label },
                )
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
                    title = "Successo â€” Rendering Attivo",
                    description = "Shader e compositing hardware operativi senza frame persi.",
                    statusType = LiquidStatusType.Success,
                    backdrop = backdrop,
                )
                LiquidStatusCard(
                    title = "Informazione â€” Profilo Calibrato",
                    description = "I parametri ottici vengono adattati automaticamente al dispositivo.",
                    statusType = LiquidStatusType.Info,
                    backdrop = backdrop,
                )
                LiquidStatusCard(
                    title = "Avviso â€” Risparmio Energetico",
                    description = "In modalitÃ  batteria ridotta puoi selezionare il profilo Essential.",
                    statusType = LiquidStatusType.Warning,
                    backdrop = backdrop,
                )
                LiquidStatusCard(
                    title = "Errore â€” Esempio Stato Critico",
                    description = "Con pulsante di recupero integrato direttamente nella card.",
                    statusType = LiquidStatusType.Error,
                    actionLabel = "Riprova",
                    onAction = { toastState.show("Recupero eseguito", icon = LiquidIcons.Refresh) },
                    backdrop = backdrop,
                )
            }

            LiquidCard(backdrop = backdrop) {
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
                        backdrop = backdrop,
                    )
                }
            }
        }
    }
}
