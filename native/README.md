# Shared C11 numeric kernels

`liquid_wave_fill_sine` builds a periodic lookup table once per process.
The animated background then interpolates this table during drawing instead
of calling trigonometric functions for every gradient on every frame.

Android builds `libliquidwave.so` for arm64-v8a, armeabi-v7a, and x86_64
with the NDK and packages it into the SDK AAR. iOS builds the same C source
as a static library for device and simulator and links it through cinterop.
Both platforms retain the same Kotlin fallback if native initialization fails.
Pixel rendering remains on Compose/GPU.

`liquid_spring_step` advances a packed batch of spring channels using the analytic
underdamped, critical or overdamped solution. Navigation, sliders and page indicators
share one frame coroutine per active control and one native call for all five channels.
Retargeting changes the destination without resetting position or velocity. Coefficients
are prepared when the motion policy changes, the buffer is reused, and the frame loop
stops at equilibrium. The same equations run in Kotlin when native loading is unavailable.

The ABI accepts 1–64 channels and checks lengths, finite values, modes and coefficients
before updating the batch. Android uses the existing `libliquidwave.so` package and JNI
keep rules; iOS links both C sources into the same archive through cinterop. No vendor
library is loaded or shipped.

The C tests run in CI with `-O3 -Wall -Wextra -Werror -pedantic`. They cover invalid input,
analytic reference values, equilibrium and subdivision invariance. Common tests compare
the portable solver to Compose at millisecond boundaries (Compose's reference truncates
nanoseconds); the native solver retains sub-millisecond frame timing. iOS and Android
instrumentation also exercise their actual native entry points.
