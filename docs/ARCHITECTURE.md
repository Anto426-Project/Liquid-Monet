# SDK ownership and source layout

The SDK is a Kotlin Multiplatform library. Public package names stay under
`com.anto426.liquidmonet`; moving a file between source directories does not change its Kotlin API.

| Location | Responsibility |
| --- | --- |
| `sdk/src/commonMain/kotlin/.../components` | Public UI components, grouped by function |
| `components/internal` | Shared control implementation, highlights, haptics and input normalization |
| `components/menu` | Menu API, dropdown lifecycle, menu surface, rows and drag selection |
| `glass` | Scene ownership, spatial groups, stable glass rendering and semantic roles |
| `glass/internal` | Optical rendering for moving thumbs, lenses and masks |
| `glass/overlay` | Positioning and lifecycle of scene-hosted menus and modals |
| `glass/runtime` | Fixed device profiles, calibration policy, effect budgets and tokens |
| `motion` | Gesture helpers and animation specifications |
| `theme` | Material/Monet palette generation and semantic colors |
| `icons` | Domain-neutral vector assets |
| `com/kyant` | Adapted backdrop and shape primitives; retain upstream license |
| `sdk/src/androidMain/kotlin` | Android monitoring and graphics bridges |
| `sdk/src/iosMain/kotlin` | iOS monitoring and Skia graphics bridges |
| `app/src/main` | Android showcase and examples |
| `app/src/androidTest` | Consumer-level gesture regression tests |
| `app/src/debug` | Empty, release-excluded activity used by interaction tests |

## Composition and rendering

Use one `LiquidGlassScene` per window/app shell. It owns overlay and modal hosts.
`LiquidBackground` draws the background; it does not own scenes or overlays.
`LiquidGlassGroup` groups nearby, functionally related controls behind a shared surface.

Stable surfaces use `liquidGlass`. Moving lenses and masks use `liquidGlassDynamic`.
Both consume the shared effect policy. Their different rendering contracts are deliberate:
merging their names would not remove an optical pass. Components must not call the low-level
`drawBackdrop` functions directly.

On Android, the theme selects and persists one calibrated profile before composing app content.
CPU, memory traffic, graphics work and device capabilities provide independent limits; live
pressure diagnostics do not switch profiles or effects. The measured budget controls backdrop
sampling resolution; the caller's optical fidelity remains separate. Hardware probing, storage,
diagnostics and shader preparation have dedicated worker owners. Render nodes retain bounded
shader/geometry caches and reuse static optical masks. See [device calibration and caches](DEVICE_CALIBRATION.md)
for execution boundaries, invalidation, recovery, workload limits and platform validation.

`LiquidGlassContainer`, `LiquidGlassMotionSpecs`, `LiquidMenu` and `LiquidMonet` retain existing
entry points. Compatibility wrappers delegate to canonical implementations; do not copy the
renderer or overlay lifecycle into each entry point.

## Input and motion

The gesture owner must remain in stable layout coordinates. Draw the material, content,
outline and touch highlight inside the same transformed layer. Drawing the highlight outside
that layer leaves a stationary halo when the material moves.

An open menu owns one drag selection. Rows register live bounds and current callbacks.
Crossing touch slop cancels the original row click; releasing activates the enabled row at
the final pointer position. Cancellation and additional pointers clear selection. Ordinary
taps and semantic actions continue through `clickable`.

Pointer tracking updates state directly. Springs animate press/release and return, rather
than launching a new pointer-position coroutine for every motion event. A new press cancels
the previous release. Reduced motion must preserve selection and highlighting while disabling
elastic scale and displacement.

`LiquidIconButton` owns all actionable icons, including top-bar actions, dropdown anchors,
media controls, text-field actions and dismissal controls. Its `Standard`, `TopBarAction`,
`Ghost` and `DropdownAnchor` variants share one interaction implementation. A preference
row owns its single spring; the leading icon changes color without starting a second spring.

`liquidControlInteractive` centralizes click semantics, enabled state, highlight and the optical
surface. Input modifiers precede the graphics layer so pointer coordinates remain stationary.
`LiquidControlDefaults` sets deformation and translation factors to 0.35. Press uses the tactile
spring and release/return use the Snappy spring; reduced motion clears elastic transforms.

Surface tokens, theme tint, effect policy and backdrop providers are remembered with their
actual inputs. Geometry and density remain draw-time inputs. Backdrop recording bounds the
optical sample to about two million pixels while leaving foreground content at full resolution.
The shared C11 wave kernel generates one periodic table; Android JNI and iOS cinterop use the
same source, with Kotlin fallback. Animation phases are read during drawing instead of
recomposing the background on every frame. See [native kernel](../native/README.md).

## Verification

Run `python scripts/check_sdk_structure.py` to check the complete Kotlin source tree for
platform leakage, misplaced packages, nested component scenes and renderer bypasses.
This is a structural check, not a substitute for compilation or interaction tests.

Run Android and common metadata builds separately from iOS native verification. Android success
does not validate iOS. Linux cannot run iOS simulator tests.

The menu interaction suite exercises hosted and popup menus, drag release targets, normal taps,
semantic clicks, disabled rows, changed callbacks, cancellation and multiple pointers.
Optical appearance and frame cost also require device checks with effects enabled; fallback
rendering and successful compilation do not establish visual or performance parity.

The Android device suite covers icon variants, callback replacement, disabled input, the single
preference-row action and clear-button input with reduced motion. It also checks menu dragging,
backdrop recording, resolution, card optics and loading animation. Run
`./gradlew :app:connectedDebugAndroidTest` with an unlocked ADB device.
