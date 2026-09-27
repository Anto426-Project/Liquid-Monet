package com.anto426.liquidmonet.glass.runtime

import com.anto426.liquidmonet.glass.LiquidGlassRole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LiquidGlassCalibrationTest {
    @Test
    fun aggressiveBudgetsStillRejectMissingSamplesAndRealOverruns() {
        val highRefresh = capable.copy(displayRefreshRateHz = 120f)
        assertTrue(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(highRefresh, 7_000_000, 5, 1080, 2400)
        )
        assertFalse(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(highRefresh, 8_000_000, 5, 1080, 2400)
        )
        assertFalse(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(highRefresh, 7_000_000, 4, 1080, 2400)
        )
        assertEquals(
            LiquidGlassQualityTier.ULTRA,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 2_500_000, 2_500_000),
        )
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 7_000_000, 500_000),
        )
        assertEquals(
            LiquidGlassQualityTier.BALANCED,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 12_000_000, 500_000),
        )
        assertEquals(
            LiquidGlassQualityTier.MINIMAL,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 17_000_000, 500_000),
        )
    }

    private val capable =
        LiquidGlassDeviceProfile(
            sdkInt = 35,
            supportsRenderEffect = true,
            supportsRuntimeShader = true,
            isLowRamDevice = false,
            totalMemoryBytes = 12L * 1024 * 1024 * 1024,
            appMemoryClassMb = 512,
            cpuCoreCount = 8,
            is64Bit = true,
            cpuMaxFrequenciesKhz = listOf(1_800_000L, 2_400_000L, 3_000_000L),
            displayWidthPixels = 1080,
            displayHeightPixels = 2400,
            displayRefreshRateHz = 60f,
        )

    @Test
    fun abundantRamCannotPromoteSlowCpu() {
        assertEquals(
            LiquidGlassQualityTier.MINIMAL,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 17_000_000, 500_000),
        )
    }

    @Test
    fun fastCpuCannotOverrideMemoryLimit() {
        val lowMemory =
            capable.copy(totalMemoryBytes = 2L * 1024 * 1024 * 1024, appMemoryClassMb = 192)
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            LiquidGlassCalibrationPolicy.cpuCeiling(lowMemory, 500_000, 500_000),
        )
    }

    @Test
    fun slowMemoryTrafficAlsoLimitsFastArithmetic() {
        assertEquals(
            LiquidGlassQualityTier.MINIMAL,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 500_000, 17_000_000),
        )
    }

    @Test
    fun fastClocksCannotMaskPoorMeasuredThroughput() {
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 4_000_000, 500_000),
        )
    }

    @Test
    fun unavailableFrequencyIsUnknownInsteadOfZero() {
        assertEquals(
            LiquidGlassQualityTier.ULTRA,
            LiquidGlassCalibrationPolicy.cpuCeiling(
                capable.copy(cpuMaxFrequenciesKhz = emptyList()),
                500_000,
                500_000,
            ),
        )
    }

    @Test
    fun fewCoresAndLowClocksProvideIndependentCeilings() {
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            LiquidGlassCalibrationPolicy.cpuCeiling(
                capable.copy(cpuCoreCount = 2),
                500_000,
                500_000,
            ),
        )
        assertEquals(
            LiquidGlassQualityTier.HIGH,
            LiquidGlassCalibrationPolicy.cpuCeiling(
                capable.copy(cpuMaxFrequenciesKhz = listOf(1_100_000)),
                500_000,
                500_000,
            ),
        )
    }

    @Test
    fun unsupportedShadersNeverSelectShaderTier() {
        assertEquals(
            LiquidGlassQualityTier.BALANCED,
            LiquidGlassCalibrationPolicy.cpuCeiling(
                capable.copy(supportsRuntimeShader = false),
                500_000,
                500_000,
            ),
        )
        assertEquals(
            LiquidGlassQualityTier.MINIMAL,
            LiquidGlassCalibrationPolicy.capabilityCeiling(
                capable.copy(supportsRenderEffect = false)
            ),
        )
    }

    @Test
    fun fastCpuAndPlentyOfRamStillNeedFastRendering() {
        assertEquals(
            LiquidGlassQualityTier.ULTRA,
            LiquidGlassCalibrationPolicy.cpuCeiling(capable, 500_000, 500_000),
        )
        assertFalse(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(capable, 20_000_000, 5, 1080, 2400)
        )
        assertTrue(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(capable, 5_000_000, 5, 1080, 2400)
        )
    }

    @Test
    fun resolutionAndRefreshRateConsumeRenderingBudget() {
        assertTrue(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(capable, 8_000_000, 5, 1080, 2400)
        )
        assertFalse(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(
                capable.copy(displayRefreshRateHz = 120f),
                8_000_000,
                5,
                1080,
                2400,
            )
        )
        assertFalse(
            LiquidGlassCalibrationPolicy.acceptsRenderSample(capable, 8_000_000, 5, 540, 1200)
        )
    }

    @Test
    fun incompleteAndInvalidSamplesCannotPromoteDevice() {
        assertFalse(LiquidGlassCalibrationPolicy.acceptsRenderSample(capable, 1, 4, 1080, 2400))
        assertFalse(LiquidGlassCalibrationPolicy.acceptsRenderSample(capable, 0, 5, 1080, 2400))
        assertFalse(LiquidGlassCalibrationPolicy.acceptsRenderSample(capable, 1, 5, 0, 2400))
        assertEquals(0L, liquidGlassP90(emptyList()))
        assertEquals(0L, liquidGlassP90(listOf(1, -1)))
        assertEquals(90L, liquidGlassP90((1L..10L).map { it * 10 }))
    }

    private val report =
        LiquidGlassCalibration(
            LiquidGlassQualityTier.HIGH,
            LiquidGlassCalibrationSource.MEASURED,
            1_000_000,
            500_000,
            2_000_000,
            5,
            640,
            1280,
        )

    @Test
    fun savedProfileAndMeasurementsSurviveRoundTrip() {
        val saved = LiquidGlassCalibrationRecord("device-a", report)
        assertEquals(saved, LiquidGlassCalibrationRecord.decode(saved.encode(), "device-a"))
    }

    @Test
    fun unfinishedCalibrationSurvivesRestartAsPending() {
        val pending = LiquidGlassCalibrationRecord("device-a", null)
        val restored =
            assertNotNull(LiquidGlassCalibrationRecord.decode(pending.encode(), "device-a"))
        assertNull(restored.calibration)
    }

    @Test
    fun differentDeviceAndIncompatibleVersionDoNotReuseProfile() {
        val text = LiquidGlassCalibrationRecord("device-a", report).encode()
        assertNull(LiquidGlassCalibrationRecord.decode(text, "device-b"))
        assertNull(
            LiquidGlassCalibrationRecord.decode(
                "999" + text.substringAfter('\n').let { "\n$it" },
                "device-a",
            )
        )
    }

    @Test
    fun corruptOrIncompleteReportsAreRejected() {
        val text = LiquidGlassCalibrationRecord("device-a", report).encode()
        assertNull(LiquidGlassCalibrationRecord.decode(text.replace("HIGH", "BROKEN"), "device-a"))
        assertNull(LiquidGlassCalibrationRecord.decode(text.replace("1000000", "-1"), "device-a"))
        assertNull(LiquidGlassCalibrationRecord.decode(text.substringBeforeLast('\n'), "device-a"))
        val incomplete =
            LiquidGlassCalibrationRecord("device-a", report.copy(renderSampleCount = 0))
        assertNull(LiquidGlassCalibrationRecord.decode(incomplete.encode(), "device-a"))
    }

    @Test
    fun diagnosticsCannotChangeAnyProfilesEffectsOrAnimation() {
        for (tier in LiquidGlassQualityTier.entries) {
            val stable =
                LiquidGlassPerformanceState.Fallback.copy(
                    device = capable,
                    qualityTier = tier,
                    blurScale = 1f,
                    refractionScale = 1f,
                    motionScale = 1f,
                )
            for (thermal in LiquidGlassThermalStatus.entries) {
                val stressed =
                    stable.copy(
                        thermalStatus = thermal,
                        isMemoryPressureHigh = true,
                        isPowerSaveMode = true,
                    )
                for (role in LiquidGlassRole.entries) {
                    for (interactive in listOf(false, true)) {
                        assertEquals(
                            stable.effectPolicy(role, interactive),
                            stressed.effectPolicy(role, interactive),
                        )
                    }
                }
                assertEquals(stable.animateBackground, stressed.animateBackground)
                assertEquals(stable.animateFunctionalContent, stressed.animateFunctionalContent)
                assertEquals(stable.renderDetailedBackground, stressed.renderDetailedBackground)
            }
        }
    }

    @Test
    fun explicitReduceMotionStillAppliesWithoutChangingQuality() {
        val state =
            LiquidGlassPerformanceState.Fallback.copy(
                qualityTier = LiquidGlassQualityTier.HIGH,
                motionScale = 0f,
            )
        assertFalse(state.animateBackground)
        assertFalse(state.animateFunctionalContent)
        assertEquals(LiquidGlassQualityTier.HIGH, state.qualityTier)
    }

    @Test
    fun minimalProfileKeepsLoadingFeedbackAnimated() {
        val minimal = LiquidGlassPerformanceState.Fallback.copy(motionScale = 0.5f)
        assertTrue(minimal.animateFunctionalContent)
        assertFalse(minimal.animateBackground)
        assertTrue(
            minimal
                .copy(isPowerSaveMode = true, isMemoryPressureHigh = true)
                .animateFunctionalContent
        )
    }

    @Test
    fun samplingBudgetCannotFlattenTheRequestedMaterial() {
        val reference =
            LiquidGlassPerformanceState.Fallback.copy(
                device = capable,
                qualityTier = LiquidGlassQualityTier.ULTRA,
                opticalQualityTier = LiquidGlassQualityTier.ULTRA,
                blurScale = 1f,
                refractionScale = 1f,
                chromaticAberrationScale = 1f,
                motionScale = 1f,
            )
        for (budget in LiquidGlassQualityTier.entries) {
            val sampled =
                reference.copy(
                    qualityTier = budget,
                    renderResolutionScale = budget.renderResolutionScale,
                )
            for (role in LiquidGlassRole.entries) {
                for (interactive in listOf(false, true)) {
                    assertEquals(
                        reference.effectPolicy(role, interactive),
                        sampled.effectPolicy(role, interactive),
                    )
                }
            }
            assertTrue(sampled.effectPolicy(LiquidGlassRole.Surface).refraction)
            assertTrue(sampled.effectPolicy(LiquidGlassRole.Surface).innerShadow)
            assertTrue(sampled.animateBackground)
            assertTrue(sampled.animateFunctionalContent)
            assertTrue(sampled.renderDetailedBackground)
        }
    }

    @Test
    fun cardTreatmentIsLocalAndKeepsLensAndOuterDepthOnEveryDevice() {
        for (budget in LiquidGlassQualityTier.entries) {
            val state =
                LiquidGlassPerformanceState.Fallback.copy(
                    device = capable,
                    qualityTier = budget,
                    opticalQualityTier = LiquidGlassQualityTier.ULTRA,
                    blurScale = 1f,
                    refractionScale = 1f,
                    chromaticAberrationScale = 1f,
                )
            val surface = state.effectPolicy(LiquidGlassRole.Surface)
            val card = state.effectPolicy(LiquidGlassRole.Surface, isCardSurface = true)
            assertFalse(card.blur)
            assertFalse(card.chromaticAberration)
            assertFalse(card.innerShadow)
            assertTrue(card.refraction)
            assertEquals(surface.refraction, card.refraction)
            assertEquals(surface.highlight, card.highlight)
            assertEquals(surface.shadow, card.shadow)
            assertTrue(surface.blur)
            assertTrue(surface.chromaticAberration)
            assertTrue(surface.innerShadow)
            for (role in LiquidGlassRole.entries) {
                val regular = state.effectPolicy(role, interactive = true)
                assertTrue(regular.blur)
                assertTrue(regular.refraction)
                assertTrue(regular.chromaticAberration)
                if (role != LiquidGlassRole.TopBar) assertTrue(regular.innerShadow)
            }
        }
    }
}
