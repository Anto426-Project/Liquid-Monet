package com.anto426.liquidmonet.glass.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class LiquidGlassIosCalibrationTest {
    @Test
    fun completedOldPolicyIsMeasuredOnceButOldCrashMarkersAreNotRetried() = runTest {
        val old = LiquidGlassCalibrationRecord("phone", measured, policyVersion = 2)
        val store = Store(LiquidGlassCalibrationRecord.decode(old.encode(), "phone"))
        var runs = 0
        val upgraded = measured.copy(qualityTier = LiquidGlassQualityTier.ULTRA)
        val controller =
            LiquidGlassIosCalibration(
                this,
                store,
                "phone",
                LiquidGlassQualityTier.ULTRA,
                {
                    runs++
                    assertNull(store.record?.calibration)
                    assertEquals(LiquidGlassCalibrationVersion, store.record?.policyVersion)
                    upgraded
                },
            )
        assertEquals(upgraded, controller.load())
        assertEquals(1, runs)
        assertEquals(
            upgraded,
            LiquidGlassIosCalibration(
                    this,
                    store,
                    "phone",
                    LiquidGlassQualityTier.ULTRA,
                    { error("Migrated results must remain cached") },
                )
                .load(),
        )
        for (oldResult in
            listOf(
                null,
                LiquidGlassCalibration(
                    LiquidGlassQualityTier.MINIMAL,
                    LiquidGlassCalibrationSource.INTERRUPTED,
                ),
            )) {
            val guarded = Store(LiquidGlassCalibrationRecord("phone", oldResult, policyVersion = 2))
            val result =
                LiquidGlassIosCalibration(
                        this,
                        guarded,
                        "phone",
                        LiquidGlassQualityTier.ULTRA,
                        { error("A policy upgrade must not repeat a crashed native probe") },
                    )
                    .load()
            assertEquals(liquidGlassCapabilityEstimate(LiquidGlassQualityTier.ULTRA), result)
            assertEquals(LiquidGlassCalibrationVersion, guarded.record?.policyVersion)
        }
    }

    @Test
    fun partialGpuRunPreservesTheLowerEstimateAfterARejectedHighCandidate() = runTest {
        val estimate = liquidGlassCapabilityEstimate(LiquidGlassQualityTier.HIGH)
        val store = Store()
        assertEquals(
            estimate,
            LiquidGlassIosCalibration(
                    this,
                    store,
                    "phone",
                    LiquidGlassQualityTier.ULTRA,
                    { estimate },
                )
                .load(),
        )
        assertEquals(estimate, store.record?.calibration)
    }

    private val measured =
        LiquidGlassCalibration(
            LiquidGlassQualityTier.HIGH,
            LiquidGlassCalibrationSource.MEASURED,
            500_000,
            600_000,
            2_000_000,
            5,
            1000,
            2000,
        )

    @Test
    fun firstRunCommitsPendingBeforeGpuAndPersistsMeasuredResult() = runTest {
        val store = Store()
        val controller =
            LiquidGlassIosCalibration(
                this,
                store,
                "phone",
                LiquidGlassQualityTier.ULTRA,
                {
                    assertEquals(LiquidGlassCalibrationRecord("phone", null), store.record)
                    measured
                },
            )
        assertEquals(measured, controller.load())
        assertEquals(listOf(null, measured), store.writes.map { it.calibration })
        assertEquals(measured, controller.load())
        assertEquals(2, store.writes.size)
    }

    @Test
    fun processRestartUsesSavedMeasurementWithoutRunningBenchmark() = runTest {
        val store = Store(LiquidGlassCalibrationRecord("phone", measured))
        val controller =
            LiquidGlassIosCalibration(
                this,
                store,
                "phone",
                LiquidGlassQualityTier.ULTRA,
                { error("A saved profile must not benchmark again") },
            )
        assertEquals(measured, controller.load())
        assertTrue(store.writes.isEmpty())
    }

    @Test
    fun concurrentThemesAndCancelledWaiterShareOneOperation() = runTest {
        val gate = CompletableDeferred<Unit>()
        var runs = 0
        val controller =
            LiquidGlassIosCalibration(
                this,
                Store(),
                "phone",
                LiquidGlassQualityTier.ULTRA,
                {
                    runs++
                    gate.await()
                    measured
                },
            )
        val first = async { controller.load() }
        val second = async { controller.load() }
        runCurrent()
        first.cancel()
        gate.complete(Unit)
        assertEquals(measured, second.await())
        assertEquals(1, runs)
    }

    @Test
    fun interruptedNativeWorkKeepsCapabilityEstimateAndDoesNotRepeat() = runTest {
        val store = Store(LiquidGlassCalibrationRecord("phone", null))
        val controller =
            LiquidGlassIosCalibration(
                this,
                store,
                "phone",
                LiquidGlassQualityTier.ULTRA,
                { error("Never repeat a native run interrupted by process death") },
            )
        val result = controller.load()
        assertEquals(LiquidGlassQualityTier.ULTRA, result.qualityTier)
        assertEquals(LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE, result.source)
        assertEquals(0L, result.renderP90Nanos)
        assertEquals(result, store.record?.calibration)
    }

    @Test
    fun unavailableStoragePreventsUnguardedNativeWork() = runTest {
        val store = Store().apply { writable = false }
        val controller =
            LiquidGlassIosCalibration(
                this,
                store,
                "phone",
                LiquidGlassQualityTier.HIGH,
                { error("Do not invoke Metal without a crash marker") },
            )
        assertEquals(LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE, controller.load().source)
        assertTrue(store.writes.isEmpty())
    }

    @Test
    fun timeoutAndLateNativeReturnCannotReplaceFallback() = runTest {
        val store = Store()
        val controller =
            LiquidGlassIosCalibration(
                this,
                store,
                "phone",
                LiquidGlassQualityTier.ULTRA,
                {
                    withContext(NonCancellable) { delay(500) }
                    measured
                },
                timeoutMillis = 100,
            )
        val result = controller.load()
        assertEquals(LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE, result.source)
        advanceUntilIdle()
        assertEquals(result, store.record?.calibration)
        assertEquals(result, controller.load())
        assertEquals(2, store.writes.size)
    }

    @Test
    fun exceptionOrIncompleteMeasurementCannotMasqueradeAsMeasured() = runTest {
        val invalid =
            listOf(
                measured.copy(cpuWorkP90Nanos = 0),
                measured.copy(memoryCopyP90Nanos = 0),
                measured.copy(renderP90Nanos = 0),
                measured.copy(renderSampleCount = 4),
                measured.copy(renderWidthPixels = 0),
                measured.copy(renderHeightPixels = 0),
                measured.copy(qualityTier = LiquidGlassQualityTier.ULTRA),
                measured.copy(source = LiquidGlassCalibrationSource.TEST_UNAVAILABLE),
            )
        for (report in invalid) {
            val result =
                LiquidGlassIosCalibration(
                        this,
                        Store(),
                        "phone",
                        LiquidGlassQualityTier.HIGH,
                        { report },
                    )
                    .load()
            assertEquals(LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE, result.source)
            assertEquals(0L, result.cpuWorkP90Nanos)
        }
        assertEquals(
            LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE,
            LiquidGlassIosCalibration(
                    this,
                    Store(),
                    "phone",
                    LiquidGlassQualityTier.HIGH,
                    { error("Metal failure") },
                )
                .load()
                .source,
        )
    }

    @Test
    fun capabilityFallbackHasDistinctEncodingAndCannotCarryFakeTimings() {
        val record =
            LiquidGlassCalibrationRecord(
                "phone",
                LiquidGlassCalibration(
                    LiquidGlassQualityTier.ULTRA,
                    LiquidGlassCalibrationSource.CAPABILITY_ESTIMATE,
                ),
            )
        assertEquals(record, LiquidGlassCalibrationRecord.decode(record.encode(), "phone"))
        val forged = record.copy(calibration = record.calibration!!.copy(renderP90Nanos = 1))
        assertNull(LiquidGlassCalibrationRecord.decode(forged.encode(), "phone"))
        assertNull(LiquidGlassCalibrationRecord.decode(record.encode(), "other-phone"))
    }

    @Test
    fun persistedProfilesAreInvalidatedByHardwareBackendAndWorkloadNotOrientation() {
        val device =
            LiquidGlassPerformanceState.Fallback.device.copy(
                totalMemoryBytes = 8L * 1024 * 1024 * 1024,
                cpuCoreCount = 6,
                displayWidthPixels = 1206,
                displayHeightPixels = 2622,
                displayDensity = 3f,
            )
        fun key(
            d: LiquidGlassDeviceProfile = device,
            machine: String = "iPhone18,1",
            gpu: String = "Apple A19 Pro",
            simulator: Boolean = false,
            version: Int = 1,
        ) = liquidGlassIosDeviceKey(machine, gpu, d, simulator, version)
        assertEquals(key(), key(device.copy(displayWidthPixels = 2622, displayHeightPixels = 1206)))
        assertNotEquals(key(), key(machine = "iPhone18,2"))
        assertNotEquals(key(), key(gpu = "Apple A18"))
        assertNotEquals(key(), key(device.copy(displayRefreshRateHz = 120f)))
        assertNotEquals(key(), key(device.copy(supportsRuntimeShader = true)))
        assertNotEquals(key(), key(simulator = true))
        assertNotEquals(key(), key(version = 2))
        assertFalse(key().contains("iPhone"))
        assertEquals(
            key(),
            key(
                device.copy(
                    processorFamily = LiquidGlassProcessorFamilies.identify("Apple A19 Pro")
                )
            ),
        )
    }

    @Test
    fun warmupIsSharedAndOnlySuccessfulCompletionClearsMarker() = runTest {
        val store = Store()
        var calls = 0
        val warmup =
            LiquidGlassIosShaderWarmup(
                this,
                store,
                "shaders",
                {
                    calls++
                    delay(20)
                },
            )
        val first = async { warmup.prepare() }
        val second = async { warmup.prepare() }
        assertTrue(first.await())
        assertTrue(second.await())
        assertEquals(1, calls)
        assertNull(store.marker)
        assertEquals(1, store.clears)
    }

    @Test
    fun failedOrTimedOutWarmupKeepsGuardAcrossRestartAndLateReturn() = runTest {
        val store = Store()
        assertFalse(
            LiquidGlassIosShaderWarmup(
                    this,
                    store,
                    "shaders",
                    { withContext(NonCancellable) { delay(500) } },
                    timeoutMillis = 100,
                )
                .prepare()
        )
        advanceUntilIdle()
        assertEquals("shaders", store.marker)
        assertEquals(0, store.clears)
        assertFalse(
            LiquidGlassIosShaderWarmup(
                    this,
                    store,
                    "shaders",
                    { error("Blocked by previous timeout") },
                )
                .prepare()
        )
        assertFalse(
            LiquidGlassIosShaderWarmup(this, store, "changed-shaders", { error("Driver error") })
                .prepare()
        )
        assertEquals("changed-shaders", store.marker)
        assertTrue(LiquidGlassIosShaderWarmup(this, store, "fixed-shaders", {}).prepare())
        assertNull(store.marker)
    }

    private class Store(var record: LiquidGlassCalibrationRecord? = null) :
        LiquidGlassIosCalibrationStore {
        var writable = true
        val writes = mutableListOf<LiquidGlassCalibrationRecord>()
        var marker: String? = null
        var clears = 0

        override fun read(key: String) =
            record?.let { LiquidGlassCalibrationRecord.decode(it.encode(), key) }

        override fun save(record: LiquidGlassCalibrationRecord): Boolean {
            if (!writable) return false
            this.record =
                assertNotNull(
                    LiquidGlassCalibrationRecord.decode(record.encode(), record.deviceKey)
                )
            writes += record
            return true
        }

        override fun beginWarmup(signature: String): Boolean {
            if (!writable || marker == signature) return false
            marker = signature
            return true
        }

        override fun finishWarmup() {
            marker = null
            clears++
        }
    }
}
