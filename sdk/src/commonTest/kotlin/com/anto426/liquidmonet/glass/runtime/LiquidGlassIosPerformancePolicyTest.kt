package com.anto426.liquidmonet.glass.runtime

import com.anto426.liquidmonet.glass.LiquidGlassRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LiquidGlassIosPerformancePolicyTest {
    private val GiB = 1024L * 1024L * 1024L
    private val modernDevice =
        LiquidGlassDeviceProfile(
            sdkInt = 0,
            supportsRenderEffect = true,
            supportsRuntimeShader = true,
            isLowRamDevice = false,
            totalMemoryBytes = 8L * GiB,
            appMemoryClassMb = 0,
            cpuCoreCount = 6,
            is64Bit = true,
            displayWidthPixels = 1206,
            displayHeightPixels = 2622,
            displayRefreshRateHz = 120f,
            displayDensity = 3f,
        )

    @Test
    fun modernAppleHardwareDoesNotUseAndroidFallback() {
        val state = state()
        assertEquals(LiquidGlassQualityTier.ULTRA, state.qualityTier)
        assertEquals(modernDevice, state.device)
        assertEquals(1f, state.renderResolutionScale)
        assertEquals(LiquidGlassQualityTier.HIGH, state.opticalQualityTier)
        assertTrue(state.blurScale > 0f)
        assertTrue(state.refractionScale > 0f)
        assertTrue(state.effectPolicy(LiquidGlassRole.Navigation, interactive = true).refraction)
        assertTrue(state.animateBackground)
        assertTrue(state.animateFunctionalContent)
        assertNull(state.calibration)
    }

    @Test
    fun unknownAndroidHeapClassDoesNotPenalizeIos() {
        assertEquals(LiquidGlassQualityTier.ULTRA, tier(modernDevice.copy(appMemoryClassMb = 0)))
        assertEquals(LiquidGlassQualityTier.ULTRA, tier(modernDevice.copy(appMemoryClassMb = 512)))
    }

    @Test
    fun newGpuFamiliesDoNotNeedAnIphoneModelAllowlist() {
        for (family in listOf(7, 8, 9, 10, 11)) {
            assertEquals(LiquidGlassQualityTier.ULTRA, tier(family = family))
        }
    }

    @Test
    fun actualAppleCpuModelAddsIndependentCeilingBeforeMeasurements() {
        assertEquals(
            LiquidGlassQualityTier.BALANCED,
            tier(
                modernDevice.copy(
                    processorFamily = LiquidGlassProcessorFamilies.identify("Apple A10X GPU")
                )
            ),
        )
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            tier(
                modernDevice.copy(
                    processorFamily = LiquidGlassProcessorFamilies.identify("Apple A14 GPU")
                )
            ),
        )
        val modern =
            modernDevice.copy(
                processorFamily = LiquidGlassProcessorFamilies.identify("Apple A19 Pro GPU")
            )
        assertEquals(LiquidGlassQualityTier.ULTRA, tier(modern))
        assertEquals(
            LiquidGlassQualityTier.MINIMAL,
            minOf(
                tier(modern),
                LiquidGlassCalibrationPolicy.measuredCpuCeiling(9_000_000, 500_000),
            ),
        )
    }

    @Test
    fun memoryStillBoundsSamplingOnOlderHardware() {
        assertEquals(
            LiquidGlassQualityTier.MINIMAL,
            tier(modernDevice.copy(totalMemoryBytes = GiB)),
        )
        assertEquals(
            LiquidGlassQualityTier.BALANCED,
            tier(modernDevice.copy(totalMemoryBytes = 3L * GiB)),
        )
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            tier(modernDevice.copy(totalMemoryBytes = 4L * GiB)),
        )
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            tier(modernDevice.copy(totalMemoryBytes = 6L * GiB)),
        )
        assertEquals(LiquidGlassQualityTier.MINIMAL, tier(modernDevice.copy(isLowRamDevice = true)))
    }

    @Test
    fun abundantMemoryCannotOverrideCpuOrGpuLimits() {
        assertEquals(LiquidGlassQualityTier.BALANCED, tier(modernDevice.copy(cpuCoreCount = 2)))
        assertEquals(LiquidGlassQualityTier.HIGH, tier(modernDevice.copy(cpuCoreCount = 4)))
        assertEquals(LiquidGlassQualityTier.HIGH, tier(family = 4))
        assertEquals(LiquidGlassQualityTier.HIGH, tier(family = 6))
        assertEquals(LiquidGlassQualityTier.BALANCED, tier(family = 0))
    }

    @Test
    fun missingRendererCapabilitiesLimitBudgetAndDisableUnsupportedEffects() {
        val noShaders = modernDevice.copy(supportsRuntimeShader = false)
        assertEquals(LiquidGlassQualityTier.BALANCED, tier(noShaders))
        val noShaderState = state(device = noShaders)
        assertEquals(0f, noShaderState.refractionScale)
        assertEquals(0f, noShaderState.chromaticAberrationScale)
        assertTrue(noShaderState.blurScale > 0f)
        val noEffects = noShaders.copy(supportsRenderEffect = false)
        assertEquals(LiquidGlassQualityTier.MINIMAL, tier(noEffects))
        assertEquals(0f, state(device = noEffects).blurScale)
    }

    @Test
    fun materialSettingsDoNotReclassifyHardware() {
        for (maximum in LiquidGlassQualityTier.entries) {
            val state = state(maximum = maximum)
            assertEquals(LiquidGlassQualityTier.ULTRA, state.qualityTier)
            assertEquals(maximum, state.opticalQualityTier)
            assertEquals(1f, state.renderResolutionScale)
        }
        assertFalse(
            state(maximum = LiquidGlassQualityTier.MINIMAL)
                .effectPolicy(LiquidGlassRole.Surface)
                .refraction
        )
        assertFalse(
            state(maximum = LiquidGlassQualityTier.BALANCED)
                .effectPolicy(LiquidGlassRole.Surface)
                .refraction
        )
        assertTrue(
            state(maximum = LiquidGlassQualityTier.HIGH)
                .effectPolicy(LiquidGlassRole.Surface)
                .refraction
        )
        assertTrue(
            state(maximum = LiquidGlassQualityTier.ULTRA)
                .effectPolicy(LiquidGlassRole.Surface)
                .chromaticAberration
        )
    }

    @Test
    fun constrainedIosHardwareKeepsRequestedMaterialAtLowerResolution() {
        val state = state(device = modernDevice.copy(totalMemoryBytes = 3L * GiB))
        assertEquals(LiquidGlassQualityTier.BALANCED, state.qualityTier)
        assertEquals(0.67f, state.renderResolutionScale)
        assertEquals(LiquidGlassQualityTier.HIGH, state.opticalQualityTier)
        assertTrue(state.effectPolicy(LiquidGlassRole.Surface).refraction)
    }

    @Test
    fun sharedScalesKeepStrongerRefractionIndependentOfTheHardwareBudget() {
        val expected =
            listOf(
                listOf(0.4f, 0.4f, 0.5f, 0f),
                listOf(0.7f, 0.7f, 0.75f, 0.35f),
                listOf(0.9f, 0.95f, 0.9f, 0.75f),
                listOf(1f, 1f, 1f, 1f),
            )
        for ((index, maximum) in LiquidGlassQualityTier.entries.withIndex()) {
            val state = state(maximum = maximum)
            assertEquals(
                expected[index],
                listOf(
                    state.blurScale,
                    state.refractionScale,
                    state.motionScale,
                    state.chromaticAberrationScale,
                ),
            )
        }
    }

    @Test
    fun intensityControlsAllOpticalScalesWithoutChangingMotionOrBudget() {
        val full = state()
        val half = state(intensity = 0.5f)
        assertEquals(full.blurScale / 2f, half.blurScale)
        assertEquals(full.refractionScale / 2f, half.refractionScale)
        assertEquals(full.chromaticAberrationScale / 2f, half.chromaticAberrationScale)
        assertEquals(full.motionScale, half.motionScale)
        assertEquals(full.qualityTier, half.qualityTier)
        val zero = state(intensity = 0f)
        assertEquals(0f, zero.blurScale)
        assertEquals(0f, zero.refractionScale)
        assertEquals(0f, zero.chromaticAberrationScale)
    }

    @Test
    fun minimumMaterialKeepsOnlyCompactTouchRefractionAtEveryHardwareBudget() {
        for (budget in LiquidGlassQualityTier.entries) {
            val light =
                liquidGlassPerformanceState(
                    modernDevice,
                    budget,
                    1f,
                    LiquidGlassQualityTier.MINIMAL,
                    reduceMotion = false,
                )
            for (role in listOf(LiquidGlassRole.Control, LiquidGlassRole.Navigation)) {
                val active = light.effectPolicy(role, interactive = true)
                assertTrue(active.refraction)
                assertFalse(active.blur)
                assertFalse(active.chromaticAberration)
                assertFalse(light.effectPolicy(role, interactive = false).refraction)
                assertFalse(
                    light
                        .copy(refractionScale = 0f)
                        .effectPolicy(role, interactive = true)
                        .refraction
                )
            }
            for (role in
                listOf(
                    LiquidGlassRole.Surface,
                    LiquidGlassRole.Dialog,
                    LiquidGlassRole.Sheet,
                    LiquidGlassRole.Menu,
                )) {
                assertFalse(light.effectPolicy(role, interactive = true).refraction)
            }
            assertEquals(budget.renderResolutionScale, light.renderResolutionScale)
            val noShader =
                liquidGlassPerformanceState(
                    modernDevice.copy(supportsRuntimeShader = false),
                    budget,
                    1f,
                    LiquidGlassQualityTier.MINIMAL,
                    reduceMotion = false,
                )
            assertFalse(
                noShader.effectPolicy(LiquidGlassRole.Navigation, interactive = true).refraction
            )
        }
        val balanced = state(maximum = LiquidGlassQualityTier.BALANCED)
        assertTrue(balanced.effectPolicy(LiquidGlassRole.TopBar).refraction)
        assertFalse(balanced.effectPolicy(LiquidGlassRole.Sheet).refraction)
    }

    @Test
    fun invalidIntensityIsNormalizedOnBothPlatforms() {
        assertEquals(state(intensity = 0f), state(intensity = -1f))
        assertEquals(state(), state(intensity = 2f))
        for (invalid in listOf(Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY)) {
            assertEquals(state(), state(intensity = invalid))
        }
    }

    @Test
    fun reduceMotionPreservesOpticsAndHardwareBudget() {
        val normal = state()
        val reduced = state(reduceMotion = true)
        assertEquals(normal.copy(motionScale = 0f), reduced)
        assertFalse(reduced.animateBackground)
        assertFalse(reduced.animateFunctionalContent)
    }

    @Test
    fun diagnosticsAndAndroidCalibrationSurviveSharedStateConstruction() {
        val calibration =
            LiquidGlassCalibration(
                LiquidGlassQualityTier.HIGH,
                LiquidGlassCalibrationSource.MEASURED,
            )
        val state =
            liquidGlassPerformanceState(
                device = modernDevice,
                qualityTier = calibration.qualityTier,
                liquidIntensity = 1f,
                maximumQuality = LiquidGlassQualityTier.HIGH,
                reduceMotion = false,
                calibration = calibration,
                thermalStatus = LiquidGlassThermalStatus.SEVERE,
                isPowerSaveMode = true,
                isMemoryPressureHigh = true,
                availableMemoryBytes = GiB,
            )
        assertEquals(calibration, state.calibration)
        assertEquals(LiquidGlassThermalStatus.SEVERE, state.thermalStatus)
        assertTrue(state.isPowerSaveMode)
        assertTrue(state.isMemoryPressureHigh)
        assertEquals(GiB, state.availableMemoryBytes)
        assertEquals(this.state().refractionScale, state.refractionScale)
        assertEquals(0.85f, state.renderResolutionScale)
    }

    private fun tier(device: LiquidGlassDeviceProfile = modernDevice, family: Int = 7) =
        LiquidGlassIosPerformancePolicy.qualityTier(device, family)

    private fun state(
        device: LiquidGlassDeviceProfile = modernDevice,
        maximum: LiquidGlassQualityTier = LiquidGlassQualityTier.HIGH,
        intensity: Float = 1f,
        reduceMotion: Boolean = false,
    ) = liquidGlassPerformanceState(device, tier(device), intensity, maximum, reduceMotion)
}
