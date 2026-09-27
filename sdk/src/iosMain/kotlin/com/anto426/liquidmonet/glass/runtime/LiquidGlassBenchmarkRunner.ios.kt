package com.anto426.liquidmonet.glass.runtime

import com.kyant.backdrop.RuntimeShaderPrewarm
import kotlin.math.sqrt
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSProcessInfoThermalState.NSProcessInfoThermalStateFair
import platform.Foundation.NSProcessInfoThermalState.NSProcessInfoThermalStateNominal
import platform.Foundation.lowPowerModeEnabled
import platform.Foundation.thermalState

internal fun liquidGlassIosCanBenchmark(): Boolean {
    val process = NSProcessInfo.processInfo
    return !process.lowPowerModeEnabled &&
        (process.thermalState == NSProcessInfoThermalStateNominal ||
            process.thermalState == NSProcessInfoThermalStateFair)
}

/** Worker-only CPU/memory measurements followed by the actual renderer backend, Skia on Metal. */
internal object LiquidGlassIosBenchmarkRunner {
    suspend fun measure(
        hardware: LiquidGlassIosHardware,
        checkSafe: () -> Unit,
    ): LiquidGlassCalibration {
        checkSafe()
        RuntimeShaderPrewarm.prepare()
        val coroutine = currentCoroutineContext()
        val cpu = LiquidGlassIosCpuProbe.measure { coroutine.ensureActive() }
        val ceiling =
            minOf(
                hardware.qualityTier,
                LiquidGlassCalibrationPolicy.measuredCpuCeiling(cpu.first, cpu.second),
            )
        val device = hardware.device
        val pixels = device.displayWidthPixels.toLong() * device.displayHeightPixels
        check(pixels > 0L)
        val maxPixels =
            if (device.totalMemoryBytes >= 4L * 1024 * 1024 * 1024) 4_194_304.0 else 1_048_576.0
        val scale =
            minOf(
                1.0,
                sqrt(maxPixels / pixels),
                4096.0 / maxOf(device.displayWidthPixels, device.displayHeightPixels),
            )
        val width = (device.displayWidthPixels * scale).toInt().coerceAtLeast(1)
        val height = (device.displayHeightPixels * scale).toInt().coerceAtLeast(1)
        val density = (device.displayDensity * scale.toFloat()).coerceAtLeast(0.5f)
        val deadline = liquidGlassIosNanoTime() + 2_500_000_000L
        var result = liquidGlassCapabilityEstimate(ceiling)
        // No suspension while a context is alive: coroutine dispatch must not migrate its thread.
        LiquidGlassIosOffscreenRenderSession.open(width, height).use { session ->
            for (tier in LiquidGlassCalibrationPolicy.candidates(ceiling)) {
                val samples = ArrayList<Long>(5)
                for (frame in 0 until 7) {
                    coroutine.ensureActive()
                    checkSafe()
                    if (liquidGlassIosNanoTime() >= deadline) break
                    val nanos = session.render(tier, frame, density)
                    if (frame >= 2) samples += nanos
                }
                if (samples.size < 5) break
                val candidate =
                    LiquidGlassCalibration(
                        tier,
                        LiquidGlassCalibrationSource.MEASURED,
                        cpu.first,
                        cpu.second,
                        liquidGlassP90(samples),
                        samples.size,
                        width,
                        height,
                    )
                val accepted =
                    LiquidGlassCalibrationPolicy.acceptsRenderSample(
                        device,
                        candidate.renderP90Nanos,
                        samples.size,
                        width,
                        height,
                    )
                // Failed high candidates must never be published as successful measurements.
                if (accepted || tier == LiquidGlassQualityTier.MINIMAL) {
                    result = candidate
                    break
                }
                result =
                    liquidGlassCapabilityEstimate(LiquidGlassCalibrationPolicy.lowerCandidate(tier))
            }
            session.verifyOutput()
        }
        return result
    }
}
