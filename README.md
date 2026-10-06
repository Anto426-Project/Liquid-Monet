# Liquid Monet SDK

**Liquid Monet** is a Compose Multiplatform UI library that combines adaptive Material 3 controls,
Monet color and real optical glass (blur, lens refraction, chromatic aberration and highlights).
The public API lives under `com.anto426.liquidmonet`; renderer implementation details are internal.
SDK 2.0 groups each component with its state and motion: see [API migration](docs/API_MIGRATION.md).

---

## Features

- **Component-owned optical glass**: cards, controls, menus, bars, dialogs, sheets, toast and media surfaces sample the active scene themselves.
- **Adaptive rendering engine**: quality, refraction and motion respond to the device/performance profile.
- **Monet dynamic color**: real-time palette harmonization with animated seed changes.
- **Material-style controls with liquid interaction**:
  - `LiquidTopBar` & `LiquidSearchBar`
  - `LiquidNavigationBar` & `LiquidTabBar`
  - `LiquidDialog`, `LiquidSheet`, `LiquidToast` & `LiquidSnackbar`
  - `LiquidCard`, `LiquidButton`, `LiquidFloatingActionButton`, `LiquidSwitch`, `LiquidSlider` & `LiquidRangeSlider`
  - `LiquidLoading`, `LiquidLinearProgressIndicator` & `LiquidCircularProgressIndicator`
  - `LiquidMediaController`, `LiquidControlCenterTile`, `LiquidChip` & `LiquidShimmerBox`
  - `LiquidAnimatedSwitcher`, `LiquidChipSelectionGroup` & `LiquidLazyFooter`
  - one `LiquidTextField` API with `LiquidTextFieldType` for text, email, phone, number, password and text area

---

## Getting started

### Theme and glass scene

```kotlin
LiquidMonetTheme(
    useMonetEngine = true,
    customMonetSeed = LiquidMonetPresets.Sapphire
) {
    LiquidGlassScene(
        modifier = Modifier.fillMaxSize(),
        background = {
            LiquidBackground(
                effect = LiquidBackgroundEffect.RadiantBeam
            )
        }
    ) { backdrop ->
        LiquidCard(backdropState = backdrop) {
            Text("Powered by Liquid Monet")
        }
    }
}
```

Components inside a `LiquidGlassScene` resolve its backdrop automatically. The explicit
`backdropState` parameter remains useful for standalone surfaces and advanced layer composition.

### Floating navigation bar

```kotlin
LiquidNavigationBar(
    selectedIndex = selectedTab,
    onItemSelected = { selectedTab = it },
    items = listOf(
        LiquidNavigationItem(label = "Home", icon = LiquidIcons.Home),
        LiquidNavigationItem(label = "Componenti", icon = LiquidIcons.Star),
        LiquidNavigationItem(label = "Impostazioni", icon = LiquidIcons.Settings)
    ),
    backdropState = backdropState
)
```

### Swipe-driven animated content

```kotlin
LiquidAnimatedSwitcher(
    targetState = page,
    transition = LiquidSwitcherTransition.LiquidMorph,
    onSwipeForward = { page++ },
    onSwipeBackward = { page-- }
) { currentPage ->
    Page(currentPage)
}
```

---

## Build and run

Build the Android SDK and demo showcase:

```bash
./gradlew :sdk:assembleAndroidMain :app:assembleDebug
```

Install the demo with `./gradlew :app:installDebug`. Run device interaction tests with
`./gradlew :app:connectedDebugAndroidTest` and validate shared Kotlin metadata with
`./gradlew :sdk:compileCommonMainKotlinMetadata`.

See [SDK architecture](docs/ARCHITECTURE.md) for source ownership and verification boundaries.
Android and iOS use a [persistent device calibration](docs/DEVICE_CALIBRATION.md) with CPU, memory and
graphics measurements on first launch. Later launches reuse the same profile.
An offline [processor-family table](docs/PROCESSOR_FAMILIES.md) adds generation-aware ceilings to
new calibrations, including Apple A/M chips on iOS. Native iOS probes use Skia on Metal and GPU
completion; unavailable probes retain an explicitly labeled hardware estimate. `LiquidCard` uses
a modestly lighter material; other surfaces retain their styles.
Run `python scripts/check_sdk_structure.py` to check the complete source layout.

## Build indipendente e consumo dei binari

Questo repository compila e pubblica lo SDK autonomamente; UniApp non include i suoi sorgenti.
La toolchain richiede JDK 21, Gradle 9.7.1, Kotlin 2.4.20 e AGP 9.4.0 (Android API 37).
La versione Maven della pubblicazione è `1.0.<github.run_number>`, oppure il valore esplicito
passato con `-PsdkVersion=<versione>`. Una Release già pubblicata non viene sovrascritta.

Verifica locale e pubblicazione nel solo repository Maven di staging:

```sh
./gradlew -PsdkVersion=1.0.0-local :sdk:testAndroidHostTest :sdk:publishAllPublicationsToStagingRepository
```

Il pacchetto contiene Android AAR, metadati Maven/KMP e KLIB per iOS arm64 e simulator arm64.
Il collegamento del framework e i test sul simulatore richiedono macOS/Xcode.
Dopo la pubblicazione di una nuova Release, il progetto consumatore deve aggiornare esplicitamente
la versione della dipendenza; le modifiche ai sorgenti locali non cambiano i binari già pubblicati.
