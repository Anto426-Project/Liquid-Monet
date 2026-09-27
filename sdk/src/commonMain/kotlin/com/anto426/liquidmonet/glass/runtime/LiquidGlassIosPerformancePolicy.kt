package com.anto426.liquidmonet.glass.runtime

/**
 * Initial iOS sampling budget inferred from stable capabilities, not a measured calibration. Kept
 * platform-free so the policy can be regression-tested without an Apple test runner.
 */
internal object LiquidGlassIosPerformancePolicy {
    fun qualityTier(device: LiquidGlassDeviceProfile, appleGpuFamily: Int): LiquidGlassQualityTier {
        val hardwareCeiling = LiquidGlassCalibrationPolicy.startupTier(device)
        // Feature families are supporting ceilings, not GPU timings. New Apple GPUs retain
        // support for older families, so they do not need a phone-model allowlist update.
        val gpuCeiling =
            when {
                !device.supportsRenderEffect -> LiquidGlassQualityTier.MINIMAL
                !device.supportsRuntimeShader -> LiquidGlassQualityTier.BALANCED
                appleGpuFamily >= 4 -> LiquidGlassQualityTier.ULTRA
                appleGpuFamily >= 2 -> LiquidGlassQualityTier.HIGH
                else -> LiquidGlassQualityTier.BALANCED
            }
        return minOf(hardwareCeiling, gpuCeiling)
    }
}
