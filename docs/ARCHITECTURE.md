# SDK ownership and source layout

The SDK is a Kotlin Multiplatform library. Public entry points stay under `com.anto426.liquidmonet`. Each component now owns its
UI, motion and optional state packages; this release intentionally changes import paths.
See [API migration](API_MIGRATION.md) before updating a consumer.

| Location | Responsibility |
| --- | --- |
| `sdk/src/commonMain/kotlin/.../components` | Public UI components, grouped by family and component |
| `components/internal` | Shared control implementation, highlights, haptics and input normalization |
| `components/menu` | Menu API, dropdown lifecycle, menu surface, rows and drag selection |
| `glass` | Scene ownership, spatial groups, stable glass rendering and semantic roles |
| `glass/internal` | Optical rendering for moving thumbs, lenses and masks |
| `glass/overlay` | Positioning and lifecycle of scene-hosted menus and modals |
| `glass/runtime` | Fixed device profiles, calibration policy, effect budgets and tokens |
| `components/<family>/<component>/motion` | Internal component animation bindings and transitions |
| `components/<family>/<component>/state` | Hoistable state and validated commands, without composables |
| `motion` | Public shared motion specifications/bindings and internal gesture controllers |
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

`LiquidGlassScene`, `LiquidMonetTheme`, `liquidGlass` and the component APIs are the
supported entry points. The unused `LiquidMonet` facade and `LiquidGlassMotionSpecs`
alias have been removed. Renderer internals, component animation owners and gesture
controllers use internal visibility; do not recreate their lifecycle in consumers.

## UI and application state

SDK controls receive application values and emit events: `value/onValueChange`,
`checked/onCheckedChange`, `selectedIndex/onSelection`, data and `onClick`.
The consumer's ViewModel owns business state, network calls, persistence and application
coroutines. SDK composables do not instantiate ViewModels or depend on repositories.
The structure checker rejects these dependencies under `components`.

UI-local state covers focus, pointer tracking, animation, menu visibility and picker drafts.
Date/time picker and toast state holders live separately from their composables and can be hoisted by
callers. Color conversion and input parsing have their own file without composables. Rendering,
platform calibration and the C11 kernel belong to the glass/runtime and platform layers;
they do not belong in an application ViewModel. The Android showcase uses local example
state to demonstrate controls and does not represent a production MVVM application.

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
`./gradlew :app:connectedDebugAndroidTest` with an unlocked ADB device, then
`python3 scripts/check_android_test_results.py --minimum-tests 30`. A successful Gradle task
with an empty report does not count as device acceptance. The debug variant retains the
classes required by instrumentation; release still enables R8 and resource shrinking.

All component animation specifications and transitions belong to their `motion` package.
UI files host `AnimatedVisibility`/`AnimatedContent` and read animation state; they do not
construct springs, tweens or infinite loops. Shared bindings are in `LiquidAnimatedState`,
popup transitions in `LiquidPopupMotion`, and press transforms in the internal motion owner.
Specifications and transition lambdas are remembered with their actual inputs.

State commands validate calendar coordinates before updating them. Clock values wrap into
valid ranges. Toast commands are serialized in the supplied lifecycle scope, suppress duplicate
content, bound the queue to 32 pending items and clamp duration to 900–30000 ms. Consumer
ViewModels can own these state holders; SDK UI does not create application ViewModels.


## Gesture feedback and component presentation

Lazy columns/rows use one common liquid `OverscrollEffect` that consumes only edge drag
and releases with the navigation panel return spring. Ordinary list scroll and fling are
handled by Compose. Direct drag state is updated synchronously; no animation job is launched
per pointer event. Swipe-to-dismiss uses the same tracking/return controller, and pointer
input stays outside the moving content layer. Reduced motion keeps swipe actions and list
scrolling while suppressing ornamental deformation. See the [Compose overscroll contract](https://developer.android.com/reference/kotlin/androidx/compose/foundation/OverscrollEffect).

Control-center tiles share their material and icon presentation, use theme foregrounds, and
have one press spring. The calendar uses one glass surface with aligned weekday/day columns,
six stable rows, a contrasting selected day and an outline for today. Page indicators use
the same selection controller and press/deformation spring as the navbar without allocating gradient brushes per frame. Clear-button
entry/exit uses one visibility progress for slot opening, deformation and a transient droplet
neck; the canonical icon control remains responsible for press input.
