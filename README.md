# Liquid Monet SDK

[![License](https://img.shields.io/badge/License-Apache--2.0-blue.svg)](LICENSE)

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
  - Nine screen-transition presets and five entrance styles via `LiquidScreenEntrance` / `LiquidSectionEntrance`
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

### Custom cards with independent actions

The original `contentPadding = 16.dp` API remains available. Use `PaddingValues` for independent
edges and the configurable container API. `modifier` sizes the complete card; `contentModifier`
customizes its inner box. Cards can wrap content (`fillWidth = false`), draw a border, change
content alignment and optionally clip their children to the chosen shape.

```kotlin
LiquidCard(
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    modifier = Modifier.widthIn(max = 560.dp),
    shape = RoundedCornerShape(20.dp),
    colors = LiquidCardDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    contentAlignment = Alignment.TopStart,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Gestisci il tuo percorso")
        LiquidButton(onClick = onOpenCareer, text = "Carriera")
        LiquidButton(onClick = onOpenSimulator, text = "Simulatore")
    }
}
```

Nested Liquid controls own their click, press animation and enabled state independently and reuse
the card's optical backdrop. An optional card `onClick` handles the remaining card area; pressing
a child does not animate or click its parent. Set the card's `enabled = false` to disable only
that outer action. `clipContent` keeps children inside the card shape by default.
Nested cards ease toward the edges of their own space as you drag them.
Clickable cards and buttons keep their elastic pointer tracking; releasing or cancelling a drag
returns the control to its original position without moving the surrounding container.

Text-bearing controls grow with system font scale. Tabs preserve selection semantics, OTP cells
wrap into balanced rows on narrow containers, and dialogs scroll long content while keeping
their actions visible. Android native kernels are linked for 16 KB pages on every supported ABI;
SDK packaging rejects incompatible ELF LOAD segments or RELRO boundaries.
The segmented tab bar shows all options in fixed slots within the available width.
Dragging the glass indicator changes the selection. The glass can expand beyond its resting
outline during a press. Tab and navigation labels use a single line with ellipsis when necessary,
while their full accessible labels remain available.

### Custom avatars, badges, buttons and icon containers

Pass a `shape` to the extended `LiquidAvatar` and `LiquidBadge` overloads. Existing calls keep
their original defaults. Avatar configuration includes foreground/background colors, optional
border, padding, initials typography, icon size and a separate presence shape, color, size,
position and content slot. A clickable presence control keeps its own action and animation.

```kotlin
LiquidAvatar(
    shape = RoundedCornerShape(16.dp),
    initials = "AB",
    size = 64.dp,
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    border = null,
    presence = LiquidAvatarPresence.Online,
    presenceShape = RoundedCornerShape(4.dp),
    presenceSize = 16.dp,
    initialsTextStyle = MaterialTheme.typography.titleLarge,
)
LiquidBadge(
    shape = RoundedCornerShape(6.dp),
    text = "Nuovo",
    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
    textStyle = MaterialTheme.typography.labelMedium,
)
Icon(
    imageVector = LiquidIcons.Star,
    contentDescription = "Preferito",
    modifier = Modifier.liquidIconContainer(
        contentPadding = PaddingValues(8.dp),
        containerSize = 44.dp,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ),
)
LiquidButton(
    onClick = onConfirm,
    text = "Conferma",
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    minimumHeight = 64.dp,
    shape = RoundedCornerShape(16.dp),
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary,
    textStyle = MaterialTheme.typography.titleSmall,
    border = null,
)
```

For a flush group of clickable rows, use `contentPadding = PaddingValues(0.dp)` and
`clipContent = true` on the outer card. `LiquidPreferenceItem` or transparent nested cards share
its optical surface and animate their own presses; the outer shape contains their feedback.

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

### Screen and section entrances

```kotlin
LiquidScreenEntrance(
    animation = LiquidScreenEntranceAnimation.FadeUp,
    delayMillis = 90,
) {
    ScreenSection()
}
```

Select `Crossfade`, `FadeThrough`, `SlideHorizontal`, `SlideVertical`, `SharedAxisDepth` or
`None` on `LiquidAnimatedSwitcher`, alongside its existing three presets. Entrances support
`None`, `Fade`, `FadeUp`, `FadeDown` and `Scale`, with optional delays for staggered sections.
Use `LiquidSectionEntrance` for scrollable sections that should keep their space and child state
while waiting to reach the viewport. See [screen motion](docs/SCREEN_MOTION.md) for imports,
direction, replay, viewport bounds and reduced-motion behavior.

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
La toolchain usa il Gradle Wrapper del repository e richiede JDK 21, Kotlin 2.4.21 e AGP 9.5.1 (Android API 37).
Compose Multiplatform 1.12.2, Compose Android 1.12.1 e Material3 Multiplatform 1.12.0-alpha03
sono allineati al catalogo di UniApp; la versione Android di Material3 è 1.5.0-rc01.
La versione Maven della pubblicazione è `2.0.<github.run_number>`, oppure il valore esplicito
passato con `-PsdkVersion=<versione>`. Una Release già pubblicata non viene sovrascritta.

Verifica locale e pubblicazione nel solo repository Maven di staging:

```sh
./gradlew -PsdkVersion=2.0.0-local :sdk:testAndroidHostTest :sdk:publishAllPublicationsToStagingRepository
```

Il pacchetto contiene Android AAR, metadati Maven/KMP e KLIB per iOS arm64 e simulator arm64.
Il collegamento del framework e i test sul simulatore richiedono macOS/Xcode.
Dopo la pubblicazione di una nuova Release, il progetto consumatore deve aggiornare esplicitamente
la versione della dipendenza; le modifiche ai sorgenti locali non cambiano i binari già pubblicati.

## Licenza

Questo SDK è distribuito con licenza [Apache-2.0](LICENSE).
Copyright 2026 Anto426. Le attribuzioni sono riportate in [NOTICE](NOTICE).

Le implementazioni derivate da Kyant0/AndroidLiquidGlass conservano la relativa
[licenza Apache-2.0 e attribuzione originale](licenses/AndroidLiquidGlass-Apache-2.0.txt).
Le modifiche sono descritte nelle [note di adattamento](docs/KYANT_REFERENCE_RESTORATION.md).

I metadati Maven dichiarano la licenza Apache-2.0. Ogni pubblicazione comprende
un archivio con classifier `licenses`; il pacchetto della Release include `LICENSE`,
`NOTICE` e le eventuali licenze di terze parti, con i relativi SHA-256 in `sdk-info.json`.
