# Shared C11 wave kernel

`liquid_wave_fill_sine` builds a periodic lookup table once per process.
The animated background then interpolates this table during drawing instead
of calling trigonometric functions for every gradient on every frame.

Android builds `libliquidwave.so` for arm64-v8a, armeabi-v7a, and x86_64
with the NDK and packages it into the SDK AAR. iOS builds the same C source
as a static library for device and simulator and links it through cinterop.
Both platforms retain the same Kotlin fallback if native initialization fails.
Pixel rendering remains on Compose/GPU.
