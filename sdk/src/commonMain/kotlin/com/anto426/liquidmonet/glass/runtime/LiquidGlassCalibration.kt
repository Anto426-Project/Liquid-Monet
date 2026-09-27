package com.anto426.liquidmonet.glass.runtime

import androidx.compose.runtime.Immutable
import kotlin.math.ceil

/** Why a permanent device profile was selected. No benchmark data leaves the device. */
enum class LiquidGlassCalibrationSource {
    MEASURED,
    CAPABILITY_LIMIT,
    INTERRUPTED,
    TEST_UNAVAILABLE,
    CONSTRAINED_START,
    /** Capability fallback when a native benchmark cannot safely complete; contains no timings. */
    CAPABILITY_ESTIMATE,
}

/** Local diagnostics for the one-time calibration; times are nanoseconds, not synthetic scores. */
@Immutable
data class LiquidGlassCalibration(
    val qualityTier: LiquidGlassQualityTier,
    val source: LiquidGlassCalibrationSource,
    val cpuWorkP90Nanos: Long = 0L,
    val memoryCopyP90Nanos: Long = 0L,
    val renderP90Nanos: Long = 0L,
    val renderSampleCount: Int = 0,
    val renderWidthPixels: Int = 0,
    val renderHeightPixels: Int = 0,
)

/** Version of the workload/selection policy. Previous crash markers remain readable. */
internal const val LiquidGlassCalibrationVersion = 3

/** Texture budget only: optical thickness, tint, lighting and interaction stay unchanged. */
internal val LiquidGlassQualityTier.renderResolutionScale: Float
    get() =
        when (this) {
            LiquidGlassQualityTier.MINIMAL -> 0.5f
            LiquidGlassQualityTier.BALANCED -> 0.67f
            LiquidGlassQualityTier.HIGH -> 0.85f
            LiquidGlassQualityTier.ULTRA -> 1f
        }

internal object LiquidGlassCalibrationPolicy {
    private const val GiB = 1024L * 1024L * 1024L

    // RAM and real renderer capabilities remain hard limits; unknown Android heap size is not zero
    // capacity.
    fun memoryCeiling(device: LiquidGlassDeviceProfile): LiquidGlassQualityTier =
        when {
            device.totalMemoryBytes < GiB -> LiquidGlassQualityTier.MINIMAL
            device.isLowRamDevice ||
                device.totalMemoryBytes < 2L * GiB ||
                device.appMemoryClassMb in 1..95 -> LiquidGlassQualityTier.BALANCED
            device.totalMemoryBytes < 3L * GiB ||
                device.appMemoryClassMb in 96..127 ||
                !device.is64Bit -> LiquidGlassQualityTier.HIGH
            else -> LiquidGlassQualityTier.ULTRA
        }

    fun capabilityCeiling(device: LiquidGlassDeviceProfile): LiquidGlassQualityTier =
        minOf(
            memoryCeiling(device),
            when {
                !device.supportsRenderEffect -> LiquidGlassQualityTier.MINIMAL
                !device.supportsRuntimeShader -> LiquidGlassQualityTier.BALANCED
                else -> LiquidGlassQualityTier.ULTRA
            },
        )

    /**
     * Highest candidate first: a flagship must not exhaust startup time testing three lower tiers.
     */
    fun candidates(ceiling: LiquidGlassQualityTier): List<LiquidGlassQualityTier> =
        LiquidGlassQualityTier.entries.filter { it <= ceiling }.reversed()

    fun lowerCandidate(tier: LiquidGlassQualityTier): LiquidGlassQualityTier =
        LiquidGlassQualityTier.entries[(tier.ordinal - 1).coerceAtLeast(0)]

    fun startupTier(device: LiquidGlassDeviceProfile): LiquidGlassQualityTier {
        val maxClock = device.cpuMaxFrequenciesKhz.maxOrNull()
        val topologyCeiling =
            when {
                device.cpuCoreCount <= 2 ||
                    !device.is64Bit ||
                    (maxClock != null && maxClock < 1_200_000L) -> LiquidGlassQualityTier.HIGH
                else -> LiquidGlassQualityTier.ULTRA
            }
        val familyCeiling =
            device.processorFamily?.effectiveCpuQualityCeiling ?: LiquidGlassQualityTier.ULTRA
        return minOf(capabilityCeiling(device), topologyCeiling, familyCeiling)
    }

    fun cpuCeiling(
        device: LiquidGlassDeviceProfile,
        cpuP90Nanos: Long,
        memoryCopyP90Nanos: Long,
    ): LiquidGlassQualityTier =
        minOf(startupTier(device), measuredCpuCeiling(cpuP90Nanos, memoryCopyP90Nanos))

    fun measuredCpuCeiling(cpuP90Nanos: Long, memoryCopyP90Nanos: Long): LiquidGlassQualityTier =
        when {
            cpuP90Nanos <= 0 || memoryCopyP90Nanos <= 0 -> LiquidGlassQualityTier.MINIMAL
            cpuP90Nanos > 16_000_000 || memoryCopyP90Nanos > 16_000_000 ->
                LiquidGlassQualityTier.MINIMAL
            cpuP90Nanos > 8_000_000 || memoryCopyP90Nanos > 8_000_000 ->
                LiquidGlassQualityTier.BALANCED
            cpuP90Nanos > 3_000_000 || memoryCopyP90Nanos > 3_000_000 -> LiquidGlassQualityTier.HIGH
            else -> LiquidGlassQualityTier.ULTRA
        }

    fun acceptsRenderSample(
        device: LiquidGlassDeviceProfile,
        p90Nanos: Long,
        sampleCount: Int,
        width: Int,
        height: Int,
    ): Boolean {
        if (p90Nanos <= 0L || sampleCount < 5 || width <= 0 || height <= 0) return false
        val displayPixels = device.displayWidthPixels.toLong() * device.displayHeightPixels
        if (displayPixels <= 0L) return false
        // Benchmark textures are bounded. Charge the smaller workload for the full display;
        // otherwise a high-resolution tablet could be promoted by a tiny offscreen test.
        val pixelRatio = (displayPixels.toDouble() / (width.toLong() * height)).coerceAtLeast(1.0)
        val refreshRate = device.displayRefreshRateHz.takeIf { it.isFinite() && it > 0f } ?: 60f
        val frameBudget = 1_000_000_000.0 / refreshRate.coerceIn(60f, 240f)
        return p90Nanos * pixelRatio <= frameBudget * 0.90
    }
}

internal fun liquidGlassP90(samples: List<Long>): Long {
    if (samples.isEmpty() || samples.any { it <= 0L }) return 0L
    val sorted = samples.sorted()
    return sorted[(ceil(sorted.size * 0.9).toInt() - 1).coerceIn(sorted.indices)]
}

/** Pure encoding shared by persistence and host tests; no device identifiers beyond a hash. */
internal data class LiquidGlassCalibrationRecord(
    val deviceKey: String,
    val calibration: LiquidGlassCalibration?,
    val policyVersion: Int = LiquidGlassCalibrationVersion,
) {
    /** Reprobe only completed old measurements; never repeat a failed or interrupted native run. */
    fun reusableCalibration(capabilityTier: LiquidGlassQualityTier): LiquidGlassCalibration? =
        when {
            policyVersion == LiquidGlassCalibrationVersion && calibration != null -> calibration
            calibration?.source == LiquidGlassCalibrationSource.MEASURED -> null
            else -> liquidGlassCapabilityEstimate(capabilityTier)
        }

    fun encode(): String =
        listOf(
                policyVersion,
                deviceKey,
                calibration?.qualityTier?.name ?: "PENDING",
                calibration?.source?.name ?: "PENDING",
                calibration?.cpuWorkP90Nanos ?: 0,
                calibration?.memoryCopyP90Nanos ?: 0,
                calibration?.renderP90Nanos ?: 0,
                calibration?.renderSampleCount ?: 0,
                calibration?.renderWidthPixels ?: 0,
                calibration?.renderHeightPixels ?: 0,
            )
            .joinToString("\n")

    companion object {
        fun decode(text: String, expectedDeviceKey: String): LiquidGlassCalibrationRecord? {
            val fields = text.lines()
            val version = fields.firstOrNull()?.toIntOrNull() ?: return null
            if (
                fields.size != 10 ||
                    version !in 2..LiquidGlassCalibrationVersion ||
                    fields[1] != expectedDeviceKey
            )
                return null
            if (fields[2] == "PENDING" && fields[3] == "PENDING") {
                return LiquidGlassCalibrationRecord(expectedDeviceKey, null, version)
            }
            val tier = LiquidGlassQualityTier.entries.find { it.name == fields[2] } ?: return null
            val source =
                LiquidGlassCalibrationSource.entries.find { it.name == fields[3] } ?: return null
            if (
                source != LiquidGlassCalibrationSource.MEASURED &&
                    source != LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE &&
                    tier != LiquidGlassQualityTier.MINIMAL
            )
                return null
            val numbers =
                fields.drop(4).map { it.toLongOrNull()?.takeIf { n -> n >= 0 } ?: return null }
            if (numbers.drop(3).any { it > Int.MAX_VALUE }) return null
            if (
                source == LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE &&
                    numbers.any { it != 0L }
            )
                return null
            if (
                source == LiquidGlassCalibrationSource.MEASURED &&
                    tier != LiquidGlassQualityTier.MINIMAL &&
                    (numbers[0] == 0L ||
                        numbers[1] == 0L ||
                        numbers[2] == 0L ||
                        numbers[3] < 5 ||
                        numbers[4] == 0L ||
                        numbers[5] == 0L)
            )
                return null
            return LiquidGlassCalibrationRecord(
                expectedDeviceKey,
                LiquidGlassCalibration(
                    tier,
                    source,
                    numbers[0],
                    numbers[1],
                    numbers[2],
                    numbers[3].toInt(),
                    numbers[4].toInt(),
                    numbers[5].toInt(),
                ),
                version,
            )
        }
    }
}

internal fun liquidGlassCapabilityEstimate(tier: LiquidGlassQualityTier) =
    LiquidGlassCalibration(tier, LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE)
