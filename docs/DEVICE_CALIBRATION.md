# Device calibration, preparation and rendering caches

Android and iOS select a device sampling budget once before `LiquidMonetTheme` composes application
content. Later launches load that same result. Material fidelity is a separate setting:
`maximumGlassQuality = null` automatically selects ULTRA for ULTRA-capable hardware and HIGH
for smaller hardware ceilings. Measured GPU sampling may be lower without attenuating that
material. An explicit tier still selects that optical fidelity; `liquidIntensity`
retains its optical meaning.
A slow CPU or small memory budget must not silently remove refraction, highlights or depth.

## Responsibilities and execution

| Owner | Responsibility | Execution |
| --- | --- | --- |
| Platform theme adapter | Await preparation, manage lifetime and publish Compose state | Main; suspending waits only |
| `LiquidGlassDeviceCalibration` | Share one process-owned operation; load/commit the permanent result | IO |
| `LiquidGlassHardwareProbe` | Read hardware facts, CPU clocks and stable device identity | IO |
| `LiquidGlassCpuProbe` | Timed arithmetic/transforms and memory transfers | Default |
| `LiquidGlassBenchmarkRunner` / `LiquidGlassRenderProbe` | Measure candidates within hardware limits | Worker; native drawing uses HWUI RenderThread/GPU |
| `LiquidGlassCalibrationStore` | Bounded reads and atomic profile writes | IO |
| `LiquidGlassShaderWarmup` | Prepare built-in shaders and a small offscreen render | Worker |
| `LiquidGlassDiagnosticsMonitor` | Register/remove callbacks and read system diagnostics | IO; callbacks only enqueue signals |
| `LiquidGlassPerformanceManager` | Convert immutable results and explicit settings to Compose state | Main, without system/disk queries |
| Backdrop modifiers | Draw the current scene and replay retained optical masks | Compose drawing pipeline |

`LiquidGlassOffscreenRenderSession` owns temporary HWUI resources for the benchmark and warm-up.
It does not own application content, a theme, a calibration decision or persistence.
Composition and draw callbacks still obey Compose's threading model; moving preparation to a
worker does not move arbitrary UI nodes or mutable graphics objects across threads.

The startup surface stays lightweight and animated while awaiting the result. Initial Android
calibration waits for a focused window. Rotation, theme changes and multiple windows await the
same process-owned operation, rather than cancelling and restarting a benchmark.

## Independent hardware limits

The decision takes the minimum of separate memory, CPU and rendering limits, never a weighted
average where abundant RAM could hide a slow processor.

| Signal | Use |
| --- | --- |
| Physical RAM, low-RAM flag, app heap class | Bound working memory |
| Arithmetic/transform workload | Single-core throughput: eight warm-ups, then five timed samples |
| Memory transfers | Four observable 1 MiB transfers per sample, separate from arithmetic |
| Core count, architecture, per-core maximum frequency | Supporting ceilings, not substitutes for measured speed |
| Processor family | Manual CPU ceiling, resolved once on IO; introduction year is metadata; see [family policy](PROCESSOR_FAMILIES.md) |
| SoC, manufacturer, model, hardware, graphics API support | Hashed device/backend identity; no serial number or IMEI |
| Resolution, density and maximum supported refresh | Account for pixels and frame budget |
| Hardware-rendered scene | Background and six filtered panels at each candidate sampling resolution |

CPU and memory-copy timings use thread CPU time, excluding time descheduled by unrelated work.
Unavailable frequency files mean unknown, not zero. Candidates retain the same complete optical
workload, including dispersion on Android 13+. Only their texture sampling resolution changes:
MINIMAL 0.50, BALANCED 0.67, HIGH 0.85, ULTRA 1.00 along each axis.

Policy version 3 uses more permissive independent ceilings:

| Signal | Sampling ceiling |
| --- | --- |
| RAM below 1 GiB / below 2 GiB / below 3 GiB / at least 3 GiB | MINIMAL / BALANCED / HIGH / ULTRA |
| Android low-RAM flag or known app heap below 96 MiB | At most BALANCED |
| Known app heap 96–127 MiB or 32-bit architecture | At most HIGH |
| At most 2 cores or known maximum clock below 1.2 GHz | At most HIGH |
| Both CPU and copy P90 at most 3 / 8 / 16 ms | ULTRA / HIGH / BALANCED |
| Either CPU or copy P90 above 16 ms, or invalid timings | MINIMAL |

Unknown heap size or clock does not invent a restriction. The highest allowed graphics candidate
is tested first, so a flagship can qualify for ULTRA without spending its deadline on lower tiers.
A complete rejection moves to the next lower candidate. An incomplete probe returns an explicitly
unmeasured capability estimate, bounded by any candidates already rejected.

Two warm-ups precede five measured frames per graphics candidate. Two output buffers are bounded
at about 1 MP on limited-memory devices or 4 MP on stronger-memory devices. Inter-frame pacing is
outside the timed sample. Smaller test surfaces are conservatively charged for the full display
pixel count, and a candidate must fit 90% of the refresh-rate frame interval.

Graphics timing includes recording, submission, completion and synchronization. It is an
end-to-end estimate, not a GPU hardware counter. A frame commit and the image fence synchronize
Android 13+; Android 12 uses a synchronized readback. Deferred HWUI submissions are awaited, not
mistaken for completed frames. See Android's [FrameRenderRequest contract](https://developer.android.com/reference/android/graphics/HardwareRenderer.FrameRenderRequest)
and [deferred-frame semantics](https://developer.android.com/reference/android/graphics/HardwareRenderer#SYNC_FRAME_DROPPED).
These initial thresholds and pixel extrapolation require validation on representative phones.
They do not establish an application frame-rate guarantee.

## Persistence and recovery

The atomic record lives at `noBackupFilesDir/liquid-glass-device-profile-v1`. The filename is stable;
its internal policy version is 3. Version 2 remains readable to preserve crash markers during
migration. The record contains the hashed device key, tier, reason and
measurements. It stays local and is excluded from backup/transfer.

A pending record is committed before benchmarking. If the process dies, the next launch selects
a stable capability estimate instead of repeating the native test. Unavailable tests, timeout and
startup power/thermal/memory constraints also produce `CAPABILITY_ESTIMATE` with zero measured
timings. A lower estimate after a rejected candidate is preserved. Missing rendering capabilities
remain hard limits. None of
these reasons lowers the caller's requested optical fidelity. Supported platform capabilities
still limit which shader APIs can actually run.

The coordinator waits at most 2.5 seconds for the benchmark. The render loop checks an additional
1.8-second deadline between frames. Native driver calls may ignore cancellation; late results
never overwrite the selected profile. Temporary graphics resources are released when those calls
return. Storage and hardware reads are outside the benchmark timeout and remain on IO.

The version-3 upgrade measures completed version-2 `MEASURED` profiles once under the new policy.
Old pending, failed or constrained profiles migrate directly to a capability estimate, preserving
the no-retry guard. This is an explicit policy migration, not reclassification on every app update.
Subsequent normal app updates, transient pressure, rotation and battery saver do not change the saved profile.
Clearing app data, changing hardware/backend identity or an explicitly incompatible record version
allows calibration again. There is no automatic reclassification during use.

`LocalLiquidGlassPerformance.current.calibration` exposes the saved diagnostics. `qualityTier`
is the measured sampling budget, `opticalQualityTier` is the caller's material setting, and
`renderResolutionScale` is the chosen texture scale. A null optical tier retains the original
behavior of manually supplied performance states. Live pressure fields are diagnostics only.
Loading feedback remains animated at every budget unless explicit `reduceMotion` disables motion.

Processor family rules participate in new calibrations only. An ordinary SDK table edit does not
change the device identity or invalidate a current-policy saved profile. Broad families with no
verified generation date retain an unknown year; neither the phone's release nor the Android
version is used as the chip's age. Introduction years add no ceiling or calendar-driven downgrade.

## Card-specific material

Only `LiquidCard` selects the lighter card treatment on every device: no blur pass,
chromatic dispersion or additional inner shadow. Lens dimensions, refraction
strength, outer shadow, highlight, tint and press interaction retain their existing values.
This is an intentional card design choice, independent of hardware classification.

The internal flag belongs to the card's background alone and is not inherited by its children.
Ordinary `Surface` glass, `LiquidGlassContainer`, menus, dialogs, sheets, bars, controls and the
dynamic renderer retain their original effect and animation policies. The public `liquidGlass`
signature is unchanged. Removing these passes does not establish a particular frame-time improvement.

## Shader preparation and cache ownership

Android 13+ prepares the four built-in programs: refraction, dispersion, directional highlight and
ambient highlight. Compilation runs on Default. A bounded reserve holds 2–8 independent instances
per program according to memory capacity; no mutable shader/uniform object is shared between live
render nodes. Reserve refills are coalesced in background. Unknown custom shaders and an exhausted
reserve retain synchronous on-demand construction; this mechanism does not promise zero first-use
cost for an arbitrary number of surfaces or custom programs.

After a successful measured profile, a 256×256 offscreen scene draws all four programs through
their actual RenderEffect/paint paths and waits for GPU completion. This also runs on launches
that reuse a saved profile: process-local objects cannot be restored by a persistent “warmed” flag.
The hardware benchmark is not repeated. Android/driver pipeline reuse is implementation-dependent;
there is no application-managed export/import of compiled AGSL binaries here.

Warm-up has a separate 1.5-second wait budget. A source-versioned pending marker prevents repeating
a warm-up that previously crashed, failed or exceeded its deadline. An interrupted or failed
calibration skips the GPU warm-up. The profile remains unchanged. Native cancellation limitations
are the same as for calibration.

On iOS, four immutable Skia RuntimeEffects are compiled once on Default and reused with separate
builders for each renderer. This prepares source programs; it does not precompile a Metal pipeline.

### iOS calibration and Metal preparation

iOS reads physical RAM and core count through Apple's [ProcessInfo](https://developer.apple.com/documentation/foundation/processinfo),
screen pixels/density/refresh through UIKit, and [Metal GPU family support](https://developer.apple.com/documentation/metal/mtlgpufamily).
The Metal device name identifies the actual Apple SoC when available (for example `Apple A19 Pro GPU`).
It populates `device.socModel` and the same `LiquidGlassProcessorFamilies` used by Android; see
[processor family policy](PROCESSOR_FAMILIES.md) for supported A/M generations and explicit ceilings.
It no longer publishes the Android-shaped `Fallback` with zero RAM, unsupported shaders and a
hard-coded MINIMAL tier. The Skia backend supplies the real effect-support flags. Android-only
API level and app heap class remain zero (unknown) and do not penalize the iOS decision.

`LiquidGlassIosPerformancePolicy` supplies independent upper limits for the benchmark:

| Signal | Sampling ceiling |
| --- | --- |
| RAM and topology | Shared version-3 ceilings above; unknown Android heap size adds no penalty |
| Apple GPU family 4 or later / family 2–3 / older or unknown family | ULTRA / HIGH / BALANCED |
| Recognized Apple CPU generation | Its explicit shared family ceiling, just as on Android |

An unknown CPU model adds no family penalty: available capabilities and actual timings remain in
charge. Generic GPU-family support never invents an exact CPU model. The family table contains
SDK policy limits, not Apple performance ratings. Current-policy profiles remain cached.

`LiquidGlassIosDeviceCalibration` shares one process-owned operation across themes/windows. Only
UIKit hardware/lifecycle reads run on Main; files, CPU probes and GPU work run on workers. Cancelling
a theme's await does not cancel calibration. The first active launch measures the same arithmetic
and four observable 1 MiB memory transfers as Android, with eight warm-ups and five samples timed by
the native thread CPU clock. CPU and memory P90 provide additional independent ceilings.

`LiquidGlassIosOffscreenRenderSession` creates a real Skia Metal `DirectContext` and GPU surfaces.
For each resolution candidate, it draws a changing background and six panels with the SDK's actual
blur and dispersion/refraction shader. Two untimed frames precede five timed frames. The timer
includes recording, filtering, submission and `flushAndSubmit(syncCpu = true)`, which waits for GPU
completion under [Skia's submission contract](https://api.skia.org/classGrDirectContext.html).
A one-pixel GPU readback outside the samples rejects missing output. All native resources are
created, used and closed on the same worker thread, with no coroutine suspension while a context
is alive. Textures are limited to about 1 or 4 MP, dimensions to 4096, and the Skia resource cache to
64 MiB. The shared acceptance policy charges smaller probes for display resolution and refresh.

The coordinator waits up to 4 seconds; the GPU loop checks a 2.5-second deadline between frames.
Native driver calls may ignore cancellation. Late results never write the profile, and resources
are released when the worker returns. Background entry, a memory warning, low power mode or serious
thermal pressure invalidates the current run. These are end-to-end estimates on a representative
SDK scene, not hardware GPU counters or a frame-rate guarantee for every app screen.

The atomic profile lives in `Application Support/liquid-glass/device-profile-v1`, with the directory
excluded from backup/transfer. Reads are bounded to 4 KiB. A local hash includes machine, SoC,
memory, renderer capabilities, normalized screen dimensions, refresh, density, simulator status
and an iOS workload version. It excludes serial numbers, app versions and caller theme settings.
The simulator measures its host GPU and has a separate key; it must not stand in for an iPhone.

A pending record is saved before GPU work. An interrupted process, timeout, failed/partial test,
or unavailable storage yields `CAPABILITY_ESTIMATE` with **zero measured timings**, keeping the
hardware estimate instead of permanently forcing every recent phone to MINIMAL. Successful probes
publish `MEASURED` with CPU/memory/render P90 and sample dimensions. Both completed results are
stable across subsequent launches; pending recovery does not repeat potentially crashing native
work. Version-2 measured profiles migrate once; older crash markers migrate without retry, using
the same shared policy as Android. Clearing the SDK profile or changing its hardware/workload key
permits a new calibration.

On measured-profile launches, a small 256 x 256 offscreen pass prepares all four actual SDK shaders
on Metal. It is process-owned and independent of the permanent score; Skia source programs are
also shared by the live renderers. A device/source signature in `shader-warmup.pending` prevents
repeating a crashed, failed or timed-out GPU preparation. The 1.5-second coordinator only removes
that marker after timely success. The offscreen context is released afterward: this warms actual
GPU work but does not expose or preload Compose's separate context-local pipeline cache, nor does
it remove the steady per-frame cost of blur/refraction.

Both platforms use the same state builder: `maximumGlassQuality` selects optical fidelity,
`liquidIntensity` scales the effects, `reduceMotion` disables motion, and the hardware budget only
selects texture resolution. iOS refreshes thermal/power diagnostics on system notifications and
foreground entry, removing observers when the theme leaves composition. These diagnostics never
reclassify hardware. Available memory remains unknown (zero); total RAM is not reported as free RAM.

Each local runtime shader cache keeps at most eight successful entries and eight rejected sources.
Changing a source under the same key invalidates that entry; an unchanged rejected source is not
compiled again every frame. Detachment clears the local cache.

## Other rendering caches

- Default backdrop recordings traverse the content subtree once and replay its display list.
  Custom recording callbacks retain their independent traversal.
- A surface with an exported backdrop records its sampled background once per draw, then replays
  that recording for the visible and exported layers. It still records anew on the next draw.
- Outer offscreen compositing is retained. Downsampling applies only to the filtered backdrop;
  foreground text, controls, highlights and shadows stay at layout resolution. Effect density,
  dimensions and padding scale together so the lens keeps its logical shape and depth.
- Highlight and shadow display lists are reused while geometry and paint remain unchanged.
  Alpha/blend updates use layer properties. Color, dimensions, density, style and offsets invalidate
  the relevant masks; inner-shadow blur updates its effect independently. Custom highlight styles
  and generic paths keep per-draw recording. Detachment resets caches and releases layers.
- Shape outlines include font scale in their cache key. Inverse transform matrices retain their
  allocation and coefficients only while rotation and scale match.
- Monet palettes and resolved glass colors are cached by their inputs. Android system colors use
  direct resource IDs and refresh on configuration changes.
- Shimmer observes animation during drawing, avoiding a full glass-modifier recomposition per frame.

No full-scene screenshot cache is introduced: moving backgrounds, controls, theme changes and
refraction must remain live. Caches are bounded or scoped to a node/process rather than retaining
an unbounded history of bitmaps, shaders or geometry.

## Verification

```sh
./gradlew :sdk:testAndroidHostTest :sdk:compileCommonMainKotlinMetadata :sdk:compileKotlinIosArm64 :app:assembleDebug :app:compileDebugAndroidTestKotlin
python3 scripts/check_sdk_structure.py
./gradlew :app:connectedDebugAndroidTest
```

Host tests cover independent CPU/RAM constraints, clocks, graphics budget, persistent records,
processor aliases/family ceilings, policy migration, card-only treatment, explicit optical-policy invariance, essential motion
and shader cache reuse/invalidation/bounds. Android
instrumentation covers nested recording, menu interaction, visible loading motion, backdrop
alignment and retained refraction under downsampling. Compilation of instrumentation tests is not
an execution result.

The iOS coordinator host tests additionally cover pending-before-measure ordering, cached restarts,
concurrent themes, caller cancellation, invalid samples, timeouts with late native completion,
warm-up crash guards and device-key invalidation. Apple CPU names and generation ceilings are
covered by the common processor tests. `LiquidGlassIosNativeCalibrationTest` exercises the native
thread clock, all four shaders on Metal with readback, and atomic Foundation persistence on Apple
hardware/simulator. Compile it with `:sdk:compileTestKotlinIosArm64` and
`:sdk:compileTestKotlinIosSimulatorArm64`; execute `:sdk:iosSimulatorArm64Test` on macOS. A Linux
compilation result does not execute these native tests.

For acceptance, verify a fresh calibration, process restart without reclassification, preparation
on a cached-profile launch, interrupted-record recovery, light/dark mode, rotation, moving
backgrounds, menus/sheets and scrolling. Inspect optical appearance and collect release-build
frame timing/memory on low-end hardware. Compilation and host tests cannot establish low-end frame
performance, native driver recovery or iOS visual behavior.
