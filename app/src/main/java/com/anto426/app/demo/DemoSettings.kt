package com.anto426.app.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation
import com.anto426.liquidmonet.components.pickers.paletteselector.LiquidPaletteOption
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.monet.LiquidMonetPresets
import com.anto426.liquidmonet.theme.monet.LiquidMonetSeed

enum class DemoThemeMode(val label: String, val description: String) {
    System("Sistema", "Segue il tema del dispositivo"),
    Light("Chiaro", "Cristallo luminoso perlato"),
    Dark("Scuro", "Superficie OLED ad alto contrasto"),
}

enum class DemoTab(
    val index: Int,
    val label: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
) {
    Explore(
        index = 0,
        label = "Esplora",
        title = "Liquid Monet",
        subtitle = "Studio interattivo e telemetria ottica",
        icon = LiquidIcons.Home,
    ),
    Controls(
        index = 1,
        label = "Controlli",
        title = "Controlli & Input",
        subtitle = "Pulsanti, form, selettori, slider e picker",
        icon = LiquidIcons.Edit,
    ),
    Overlays(
        index = 2,
        label = "Overlay",
        title = "Overlay & Motion",
        subtitle = "Modali, menu, feedback, liste e transizioni",
        icon = LiquidIcons.Notifications,
    ),
    Charts(
        index = 3,
        label = "Grafici",
        title = "Grafici & Superfici",
        subtitle = "Visualizzazione dati e card in vetro liquido",
        icon = LiquidIcons.Analytics,
    ),
    Settings(
        index = 4,
        label = "Impostazioni",
        title = "Impostazioni SDK",
        subtitle = "Tema, qualità vetro, sfondo e animazioni",
        icon = LiquidIcons.Settings,
    ),
}

@Immutable
data class DemoPalettePreset(
    val name: String,
    val swatch: Color,
    val seed: LiquidMonetSeed,
)

@Immutable
data class DemoCatalogEntry(
    val title: String,
    val description: String,
    val tab: DemoTab,
    val subTab: Int = 0,
    val icon: ImageVector,
    val keywords: String,
)

val demoPalettes: List<DemoPalettePreset> = listOf(
    DemoPalettePreset("Zaffiro", Color(0xFF0061A4), LiquidMonetPresets.Sapphire),
    DemoPalettePreset("Smeraldo", Color(0xFF006C4C), LiquidMonetPresets.Emerald),
    DemoPalettePreset("Tramonto", Color(0xFFB86B00), LiquidMonetPresets.Sunset),
    DemoPalettePreset("Viola", Color(0xFF6B4EA2), LiquidMonetPresets.Violet),
    DemoPalettePreset("Rubino", Color(0xFFD81B60), LiquidMonetSeed.fromColor(Color(0xFFD81B60))),
    DemoPalettePreset("Ciano", Color(0xFF00838F), LiquidMonetSeed.fromColor(Color(0xFF00838F))),
)

val demoPaletteOptions: List<LiquidPaletteOption> =
    demoPalettes.map { LiquidPaletteOption(name = it.name, color = it.swatch) }

@Stable
class DemoSettingsState {
    var themeMode by mutableStateOf(DemoThemeMode.System)
    var useSystemDynamicColor by mutableStateOf(true)
    var selectedPaletteIndex by mutableIntStateOf(0)

    var glassQuality by mutableStateOf<LiquidGlassQualityTier?>(LiquidGlassQualityTier.ULTRA)
    var liquidIntensity by mutableFloatStateOf(0.85f)

    var backgroundEffect by mutableStateOf(LiquidBackgroundEffect.Aurora)
    var backgroundIntensity by mutableFloatStateOf(0.95f)
    var backgroundSpeed by mutableFloatStateOf(1.0f)
    var showVignette by mutableStateOf(true)

    var screenTransition by mutableStateOf(LiquidSwitcherTransition.DirectionalHorizontal)
    var sectionEntrance by mutableStateOf(LiquidScreenEntranceAnimation.FadeUp)
    var sectionReplayCounter by mutableIntStateOf(0)
    var reduceMotion by mutableStateOf(false)

    var autoHideNavBar by mutableStateOf(true)
    var showFab by mutableStateOf(true)

    val activePalette: DemoPalettePreset
        get() = demoPalettes[selectedPaletteIndex.coerceIn(demoPalettes.indices)]

    fun resolveDarkTheme(systemDark: Boolean): Boolean =
        when (themeMode) {
            DemoThemeMode.System -> systemDark
            DemoThemeMode.Light -> false
            DemoThemeMode.Dark -> true
        }

    fun replaySections() {
        sectionReplayCounter++
    }

    fun resetDefaults() {
        themeMode = DemoThemeMode.System
        useSystemDynamicColor = true
        selectedPaletteIndex = 0
        glassQuality = LiquidGlassQualityTier.ULTRA
        liquidIntensity = 0.85f
        backgroundEffect = LiquidBackgroundEffect.Aurora
        backgroundIntensity = 0.95f
        backgroundSpeed = 1.0f
        showVignette = true
        screenTransition = LiquidSwitcherTransition.DirectionalHorizontal
        sectionEntrance = LiquidScreenEntranceAnimation.FadeUp
        reduceMotion = false
        autoHideNavBar = true
        showFab = true
        sectionReplayCounter++
    }
}

@Composable
fun rememberDemoSettingsState(): DemoSettingsState = remember { DemoSettingsState() }

fun LiquidGlassQualityTier?.displayName(): String =
    when (this) {
        null -> "Auto"
        LiquidGlassQualityTier.MINIMAL -> "Essential"
        LiquidGlassQualityTier.BALANCED -> "Bilanciato"
        LiquidGlassQualityTier.HIGH -> "Alto"
        LiquidGlassQualityTier.ULTRA -> "Ultra"
    }

fun LiquidGlassQualityTier?.description(): String =
    when (this) {
        null -> "Selezione automatica calibrata sull'hardware"
        LiquidGlassQualityTier.MINIMAL -> "Vetro satinato pulito e leggerissimo"
        LiquidGlassQualityTier.BALANCED -> "Sfocatura ottica fluida a consumo bilanciato"
        LiquidGlassQualityTier.HIGH -> "Rifrazione Snell e dispersione cromatica"
        LiquidGlassQualityTier.ULTRA -> "Massima fedeltà ottica AGSL e animazioni"
    }

fun LiquidSwitcherTransition.displayName(): String =
    when (this) {
        LiquidSwitcherTransition.DirectionalHorizontal -> "Asse orizzontale"
        LiquidSwitcherTransition.DirectionalVertical -> "Asse verticale"
        LiquidSwitcherTransition.LiquidMorph -> "Morfosi liquida"
        LiquidSwitcherTransition.SlideHorizontal -> "Slide parallasse X"
        LiquidSwitcherTransition.SlideVertical -> "Slide parallasse Y"
        LiquidSwitcherTransition.SharedAxisDepth -> "Profondità Z"
        LiquidSwitcherTransition.FadeThrough -> "Fade-through"
        LiquidSwitcherTransition.Crossfade -> "Dissolvenza"
        LiquidSwitcherTransition.None -> "Nessuna"
    }

fun LiquidSwitcherTransition.description(): String =
    when (this) {
        LiquidSwitcherTransition.DirectionalHorizontal -> "Scorrimento laterale fluido senza rimbalzo"
        LiquidSwitcherTransition.DirectionalVertical -> "Continuità verticale per flussi a step"
        LiquidSwitcherTransition.LiquidMorph -> "Scala organica direzionale in vetro liquido"
        LiquidSwitcherTransition.SlideHorizontal -> "Push/pop nativo a schermo intero con parallasse"
        LiquidSwitcherTransition.SlideVertical -> "Presentazione verticale modale con parallasse"
        LiquidSwitcherTransition.SharedAxisDepth -> "Zoom spaziale di profondità (drill-in/out)"
        LiquidSwitcherTransition.FadeThrough -> "Separazione netta uscita-entrata Material 3"
        LiquidSwitcherTransition.Crossfade -> "Dissolvenza asimmetrica pulita"
        LiquidSwitcherTransition.None -> "Cambio istantaneo senza animazione"
    }

fun LiquidScreenEntranceAnimation.displayName(): String =
    when (this) {
        LiquidScreenEntranceAnimation.FadeUp -> "Dal basso"
        LiquidScreenEntranceAnimation.FadeDown -> "Dall'alto"
        LiquidScreenEntranceAnimation.Scale -> "Scala"
        LiquidScreenEntranceAnimation.Fade -> "Dissolvenza"
        LiquidScreenEntranceAnimation.None -> "Nessuna"
    }

fun LiquidBackgroundEffect.displayName(): String =
    when (this) {
        LiquidBackgroundEffect.Aurora -> "Aurora"
        LiquidBackgroundEffect.MeshGlow -> "Mesh Glow"
        LiquidBackgroundEffect.OrbitalPulse -> "Orbital Pulse"
        LiquidBackgroundEffect.RadiantBeam -> "Radiant Beam"
    }

val demoCatalogEntries: List<DemoCatalogEntry> = listOf(
    DemoCatalogEntry("Telemetria Ottica", "Stato live di blur, rifrazione e tier hardware", DemoTab.Explore, 0, LiquidIcons.Info, "telemetria gpu performance tier blur"),
    DemoCatalogEntry("Media Player", "LiquidMediaController con controlli tattili e playlist", DemoTab.Explore, 0, LiquidIcons.MusicNote, "media player musica audio play pause"),
    DemoCatalogEntry("Pulsanti & Varianti", "Primary, Secondary, Tonal, Glass, Outlined, Text, FAB, IconButton", DemoTab.Controls, 0, LiquidIcons.Add, "button pulsanti fab iconbutton badge rating stelle"),
    DemoCatalogEntry("Input, Form & OTP", "LiquidSearchBar, LiquidTextField (6 tipi), OTP e Stepper", DemoTab.Controls, 1, LiquidIcons.Edit, "textfield input password email telefono textarea otp stepper ricerca"),
    DemoCatalogEntry("Selettori & Slider", "Switch, Checkbox, Radio, Slider, RangeSlider, Select e Chip", DemoTab.Controls, 2, LiquidIcons.Check, "switch checkbox radio slider range select dropdown chip"),
    DemoCatalogEntry("Data, Ora & Palette", "LiquidDatePicker, LiquidTimePicker e LiquidPaletteSelector", DemoTab.Controls, 3, LiquidIcons.Calendar, "date time picker calendario orologio palette colori"),
    DemoCatalogEntry("Dialog, Sheet & Menu", "Finestre modali, bottom sheet, dropdown menu, tooltip, toast e snackbar", DemoTab.Overlays, 0, LiquidIcons.Notifications, "dialog sheet modal dropdown menu tooltip toast snackbar"),
    DemoCatalogEntry("Loading & Shimmer", "Linear, Circular, Dots, Pulse, ProgressIndicator e ShimmerBox", DemoTab.Overlays, 1, LiquidIcons.Refresh, "loading caricamento spinner progress shimmer skeleton"),
    DemoCatalogEntry("Navigazione & Liste", "Breadcrumbs, PageIndicator, Pagination, Accordion, Swipe, Avatar, Footer", DemoTab.Overlays, 2, LiquidIcons.MenuBook, "breadcrumbs pagination pager accordion expandable swipe dismiss avatar empty lazy footer"),
    DemoCatalogEntry("Laboratorio Transizioni", "Testa tutte le 8 animazioni schermate, swipe e ScreenEntrance", DemoTab.Overlays, 3, LiquidIcons.PlayArrow, "transizioni animazioni switcher motion entrance slide morph"),
    DemoCatalogEntry("Grafici Interattivi", "LiquidLineChart, LiquidBarChart e LiquidDonutChart con scrub", DemoTab.Charts, 0, LiquidIcons.Analytics, "grafici chart line bar donut torta statistiche"),
    DemoCatalogEntry("Superfici & Status Card", "LiquidCard, LiquidStatusCard, LiquidPreferenceItem e IconContainer", DemoTab.Charts, 0, LiquidIcons.Star, "card status preference divider icon container superfici"),
    DemoCatalogEntry("Tema & Motore Monet", "Chiaro/Scuro, colori dinamici Android e 6 palette seed", DemoTab.Settings, 0, LiquidIcons.Palette, "tema dark light scuro chiaro monet colori palette"),
    DemoCatalogEntry("Qualità Vetro & Ottica", "Essential, Bilanciato, Alto, Ultra e intensità rifrazione", DemoTab.Settings, 0, LiquidIcons.Visibility, "essential bilanciato alto ultra vetro glass qualita intensita"),
    DemoCatalogEntry("Sfondo Dinamico", "Aurora, MeshGlow, OrbitalPulse, RadiantBeam, velocità e vignetta", DemoTab.Settings, 0, LiquidIcons.Star, "sfondo background aurora mesh orbital beam velocita vignetta"),
    DemoCatalogEntry("Impostazioni Transizioni", "Cambia la transizione tra schermate su tutta l'app + Riduci animazioni", DemoTab.Settings, 0, LiquidIcons.Refresh, "transizione schermate animazioni riduci motion accessibilita"),
)
