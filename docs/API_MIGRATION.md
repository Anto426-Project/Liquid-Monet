# SDK 2.0 migration

This SDK release intentionally reorganizes public imports. UniApp is outside this refactor;
its dependency and imports must be migrated separately when it adopts the 2.0 binary.

Every component owns its UI, optional `state` and internal `motion` packages:

```text
components/
  buttons/iconbutton/LiquidIconButton.kt
  buttons/iconbutton/motion/LiquidIconButtonMotion.kt
  pickers/datepicker/LiquidDatePicker.kt
  pickers/datepicker/state/LiquidDatePickerState.kt
  pickers/datepicker/motion/LiquidDatePickerMotion.kt
```

The component's Kotlin name and parameters remain recognizable, but package names change.
Update explicit imports rather than using broad wildcard imports. Representative mappings:

| Previous import | New import |
| --- | --- |
| `components.buttons.LiquidIconButton` | `components.buttons.iconbutton.LiquidIconButton` |
| `components.buttons.LiquidButtonVariant` | `components.buttons.button.LiquidButtonVariant` |
| `components.cards.LiquidPreferenceItem` | `components.cards.preferenceitem.LiquidPreferenceItem` |
| `components.cards.LiquidCard` | `components.cards.card.LiquidCard` |
| `components.navigation.LiquidTopBar` | `components.navigation.topbar.LiquidTopBar` |
| `components.navigation.LiquidPageIndicator` | `components.navigation.pagination.LiquidPageIndicator` |
| `components.pickers.LiquidDatePickerState` | `components.pickers.datepicker.state.LiquidDatePickerState` |
| `components.pickers.LiquidTimePickerState` | `components.pickers.timepicker.state.LiquidTimePickerState` |
| `components.feedback.LiquidToastState` | `components.feedback.toast.state.LiquidToastState` |
| `components.layout.LiquidLazyColumn` | `components.layout.lazy.LiquidLazyColumn` |
| `components.charts.LiquidBarChartData` | `components.charts.model.LiquidBarChartData` |

All entries are relative to `com.anto426.liquidmonet`.

The unused `LiquidMonet` facade and `LiquidGlassMotionSpecs` alias are removed. Use
`LiquidMonetTheme`, `LiquidGlassScene` and the public common `motion.LiquidMotion` builders.
Component-specific animation objects and gesture controllers are internal. Shared state
animation bindings and `rememberLiquidOverscrollEffect` are public for consumer reuse.

`LiquidToastType` and its type argument have been removed. Toasts use one neutral presentation
with optional icon/subtitle. The state holder owns a bounded, serialized queue in the lifecycle
scope supplied by its caller; `show` and `dismiss` execute in that scope. Date-picker displayed
month setters are private: call `showMonth(year, month)` to validate both coordinates atomically.

SDK controls accept values and callbacks from application ViewModels. Picker/toast state can
be owned by a ViewModel without instantiating ViewModels inside SDK composables.

`LiquidLazyColumn` and `LiquidLazyRow` now apply liquid edge feedback automatically. Pass
`liquidOverscroll = false` to disable it; reduced motion disables it automatically. Ordinary
scrolling, nested scrolling, reverse layout, RTL and fling stay owned by Compose. Custom lazy
layouts can pass `rememberLiquidOverscrollEffect(Orientation.Vertical/Horizontal)` to their
`overscrollEffect` parameter. One effect instance must be attached to one scrolling container.
