# Screen motion

Screen transitions and content entrances are shared Compose APIs in `commonMain`, available to
Android, iOS and desktop consumers. They use `LiquidMotion` and the current
`LocalLiquidGlassPerformance` profile. The existing switcher API and its default `LiquidMorph`
preset remain unchanged.

## Changing screens

Use `LiquidAnimatedSwitcher` for a route, tab or other changing state. Render the state passed
to its content lambda, so outgoing content keeps its own identity during the transition.

| `LiquidSwitcherTransition` | Behavior | Typical use |
| --- | --- | --- |
| `DirectionalHorizontal` | One-third-width slide and fade | Neighboring tabs |
| `DirectionalVertical` | One-third-height slide and fade | Ordered vertical steps |
| `LiquidMorph` | Elastic scale and fade | Content at the same level |
| `None` | Immediate replacement, including layout size | Motion-free presentation |
| `Crossfade` | Simultaneous opacity blend | Unrelated peer screens |
| `FadeThrough` | Old content fades out before new content fades in | Switching app areas |
| `SlideHorizontal` | Full-container horizontal slide | Forward/back page navigation |
| `SlideVertical` | Full-container vertical slide | Vertical page navigation |
| `SharedAxisDepth` | Directional zoom and fade | Entering/leaving a hierarchy |

```kotlin
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition

LiquidAnimatedSwitcher(
    targetState = route,
    modifier = Modifier.fillMaxSize(),
    transition = LiquidSwitcherTransition.SharedAxisDepth,
    isForward = { initial, target -> target.depth > initial.depth },
) { currentRoute ->
    Screen(currentRoute)
}
```

Numbers and comparable states infer direction automatically. For routes, unordered states and
wrapping pagers, supply `isForward` from the navigation event. Both horizontal presets follow
`LocalLayoutDirection`; forward motion reverses in RTL. Full-container slides account for
different incoming/outgoing sizes. Size changes use adaptive motion and keep clipping disabled,
consistent with the existing switcher. An application's enclosing viewport can clip page motion
with `Modifier.clipToBounds()` when needed; keep scene overlay hosts outside that viewport.

The switcher retains swipe callbacks, accessibility actions and predictive-back support. It
does not own a back stack or register routes. `None` and a zero motion scale suppress decorative
drag/predictive-back transforms while preserving their actions.

## Entering a screen or its sections

`LiquidScreenEntrance` plays an entrance on first composition. Its presets are `None`, `Fade`,
`FadeUp`, `FadeDown` and `Scale`. Vertical entrances move 24 dp; the scale preset starts at 94%.
All animated presets also fade. Changing `visible` plays enter/exit again, and removes hidden
content from composition after exit completes.

```kotlin
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntrance
import com.anto426.liquidmonet.components.layout.screenentrance.LiquidScreenEntranceAnimation

key(screenId) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.forEachIndexed { index, section ->
            LiquidScreenEntrance(
                animation = LiquidScreenEntranceAnimation.FadeUp,
                delayMillis = index * 90,
            ) {
                Section(section)
            }
        }
    }
}
```

Ordinary recomposition preserves entrance progress. Use a stable outer `key(screenId)` or a
replay key to start a new entrance. Delays must be non-negative and are capped at 10 seconds
before performance scaling. Exit animations have no entrance delay, so hiding content stays
responsive. These wrappers also work with ordinary Compose content and create no glass scene,
backdrop recordings, blur passes or overlay hosts.

When combining the two APIs, use a screen transition for navigation and entrances for its
sections. Avoid applying two spatial transforms to the entire screen at once. Keep bars and
overlay hosts in the existing `LiquidGlassScene`, outside the changing content.

## Sections revealed by scrolling

`LiquidSectionEntrance` reserves the section's space and keeps child state while waiting for it
to reach the viewport. It uses the same five entrance styles. Scroll back to a revealed section
without repeating the entrance; change `replayKey` to replay without resetting its controls.
Progress is read in the drawing layer rather than recomposing the content every frame.

```kotlin
import com.anto426.liquidmonet.components.layout.sectionentrance.LiquidSectionEntrance

LiquidSectionEntrance(
    animation = LiquidScreenEntranceAnimation.FadeUp,
    delayMillis = 120,
    viewportBoundsInWindow = contentViewport, // optional Rect excluding scene-owned bars
) {
    StatisticsSection()
}
```

The delay starts when the section becomes visible, rather than when the screen is composed.
Lazy lists should give items stable keys. Offscreen items disposed by a lazy list may enter again
when recreated; keep an application's persistent reveal policy outside the SDK if needed.
Reduced motion shows the content immediately, including sections waiting for scroll.

## Reduced motion and verification

The profile scales timings and delays together. At `motionScale = 0f`, screen replacement,
layout size and entrances are immediate; entrance delays are removed. Standard Compose motion
duration scaling also applies. The SDK wrappers do not add infinite animation loops.

Focused `LiquidScreenMotionTest` instrumentation covers interrupted transitions with differing
sizes, reduced motion, delayed entrance pixels, swipe callbacks and RTL forward/back navigation.
Run it with:

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.anto426.app.LiquidScreenMotionTest
```

`LiquidDemoSectionTest` covers delayed reveal on scrolling, reserved layout space, replay without
recreating child state, immediate reduced motion, and reveal after viewport bounds change.

The underlying content/visibility transitions follow the
[Compose animation APIs](https://developer.android.com/develop/ui/compose/animation/composables-modifiers).
Android interaction tests and shared/desktop compilation do not establish native iOS behavior
or frame-time performance on every device.
