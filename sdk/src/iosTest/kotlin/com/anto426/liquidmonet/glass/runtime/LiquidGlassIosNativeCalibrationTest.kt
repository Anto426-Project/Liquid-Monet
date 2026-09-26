package com.anto426.liquidmonet.glass.runtime

import com.kyant.backdrop.RuntimeShaderPrewarm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Metal.MTLCreateSystemDefaultDevice

/** Runs on Apple hardware/simulator; compiling this suite on Linux is not native execution. */
@OptIn(ExperimentalForeignApi::class)
class LiquidGlassIosNativeCalibrationTest {
    @Test fun nativeThreadClockMeasuresObservableCpuAndMemoryWork() = runTest {
        withContext(Dispatchers.Default) {
            val coroutine = currentCoroutineContext()
            val (cpu, memory) = LiquidGlassIosCpuProbe.measure { coroutine.ensureActive() }
            assertTrue(cpu > 0L)
            assertTrue(memory > 0L)
        }
    }

    @Test fun allSdkShadersRenderToRealMetalTextureAndSurviveReadback() = runTest {
        // Hosted macOS runners can compile and execute iOS tests without a Metal GPU.
        // Keep the readback assertion active on simulators/devices that expose Metal.
        if (MTLCreateSystemDefaultDevice() == null) {
            println("Skipping Metal readback: this test host has no Metal device")
            return@runTest
        }
        RuntimeShaderPrewarm.prepare()
        withContext(Dispatchers.Default) {
            LiquidGlassIosOffscreenRenderSession.open(256, 512).use { session ->
                assertTrue(session.render(LiquidGlassQualityTier.ULTRA, 0, 1f, warmup = true) > 0L)
                session.verifyOutput()
                for (tier in LiquidGlassQualityTier.entries) {
                    assertTrue(session.render(tier, 1, 1f) > 0L)
                    session.verifyOutput()
                }
            }
        }
    }

    @Test fun nativeAtomicStoreRoundTripsProfileAndCrashGuards() = runTest {
        withContext(Dispatchers.Default) {
            val directory = NSURL.fileURLWithPath(NSTemporaryDirectory() + "liquid-calibration-test-" + NSUUID().UUIDString)
            try {
                val store = LiquidGlassIosFileCalibrationStore(directory)
                assertNull(store.read("device"))
                val pending = LiquidGlassCalibrationRecord("device", null)
                assertTrue(store.save(pending))
                assertEquals(pending, store.read("device"))
                val result = pending.copy(calibration = LiquidGlassCalibration(LiquidGlassQualityTier.HIGH,
                    LiquidGlassCalibrationSource.MEASURED, 500_000, 500_000, 2_000_000, 5, 1000, 2000))
                assertTrue(store.save(result))
                assertEquals(result, LiquidGlassIosFileCalibrationStore(directory).read("device"))
                assertNull(store.read("different-device"))
                assertTrue(store.beginWarmup("source-a"))
                assertFalse(LiquidGlassIosFileCalibrationStore(directory).beginWarmup("source-a"))
                assertTrue(store.beginWarmup("source-b"))
                store.finishWarmup()
                assertTrue(store.beginWarmup("source-b"))
            } finally {
                NSFileManager.defaultManager.removeItemAtURL(directory, error = null)
            }
        }
    }
}
