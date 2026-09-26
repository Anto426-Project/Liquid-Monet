package com.anto426.liquidmonet.glass.runtime

/** Shared material settings: platform sampling budgets must not flatten the requested optics. */
internal fun liquidGlassPerformanceState(
    device: LiquidGlassDeviceProfile,
    qualityTier: LiquidGlassQualityTier,
    liquidIntensity: Float,
    maximumQuality: LiquidGlassQualityTier,
    reduceMotion: Boolean,
    calibration: LiquidGlassCalibration? = null,
    thermalStatus: LiquidGlassThermalStatus = LiquidGlassThermalStatus.UNKNOWN,
    isPowerSaveMode: Boolean = false,
    isMemoryPressureHigh: Boolean = false,
    availableMemoryBytes: Long = 0L,
): LiquidGlassPerformanceState {
    val scales = scalesFor(maximumQuality)
    val intensity = normalizeLiquidGlassIntensity(liquidIntensity)
    return LiquidGlassPerformanceState(
        device = device,
        qualityTier = qualityTier,
        thermalStatus = thermalStatus,
        isPowerSaveMode = isPowerSaveMode,
        isMemoryPressureHigh = isMemoryPressureHigh,
        availableMemoryBytes = availableMemoryBytes,
        liquidIntensity = intensity,
        blurScale = if (device.supportsRenderEffect) scales.blur * intensity else 0f,
        refractionScale = if (device.supportsRuntimeShader) scales.refraction * intensity else 0f,
        motionScale = if (reduceMotion) 0f else scales.motion,
        chromaticAberrationScale =
            if (device.supportsRuntimeShader) scales.chromaticAberration * intensity else 0f,
        calibration = calibration,
        opticalQualityTier = maximumQuality,
        renderResolutionScale = qualityTier.renderResolutionScale,
    )
}

internal fun normalizeLiquidGlassIntensity(value: Float): Float =
    if (value.isFinite()) value.coerceIn(0f, 1f) else 1f

private data class PerformanceScales(
    val blur: Float,
    val refraction: Float,
    val motion: Float,
    val chromaticAberration: Float,
)

private fun scalesFor(tier: LiquidGlassQualityTier): PerformanceScales =
    when (tier) {
        LiquidGlassQualityTier.MINIMAL -> PerformanceScales(0.4f, 0.4f, 0.5f, 0f)
        LiquidGlassQualityTier.BALANCED -> PerformanceScales(0.7f, 0.7f, 0.75f, 0.35f)
        LiquidGlassQualityTier.HIGH -> PerformanceScales(0.9f, 0.95f, 0.9f, 0.75f)
        LiquidGlassQualityTier.ULTRA -> PerformanceScales(1f, 1f, 1f, 1f)
    }
