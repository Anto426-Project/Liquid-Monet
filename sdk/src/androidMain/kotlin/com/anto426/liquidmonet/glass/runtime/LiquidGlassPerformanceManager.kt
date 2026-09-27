package com.anto426.liquidmonet.glass.runtime

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Publishes a calibrated profile and asynchronously collected diagnostics as Compose [State].
 *
 * The manager retains only [Context.getApplicationContext] and owns every callback it registers.
 * Call [start] when the state becomes visible and [close] when its lifecycle ends.
 * `LiquidMonetTheme` does this automatically.
 */
@Stable
internal class LiquidGlassPerformanceManager(
    context: Context,
    private val calibratedDevice: LiquidGlassCalibratedDevice,
    @FloatRange(from = 0.0, to = 1.0) liquidIntensity: Float = 1f,
    private val maximumQuality: LiquidGlassQualityTier? = null,
    private val reduceMotion: Boolean = false,
) : AutoCloseable {

    private val applicationContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private val deviceProfile = calibratedDevice.device
    // A caller's explicit cap never overwrites the device calibration on disk.
    private val selectedQualityTier = calibratedDevice.calibration.qualityTier
    private val diagnosticsMonitor =
        LiquidGlassDiagnosticsMonitor(applicationContext, deviceProfile)

    private var normalizedLiquidIntensity = normalizeLiquidGlassIntensity(liquidIntensity)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observation: Job? = null
    private var diagnostics = LiquidGlassDiagnostics()
    private val mutableState = mutableStateOf(readState())

    val state: State<LiquidGlassPerformanceState>
        get() = mutableState

    /** The platform observer owns its callbacks and all system reads on an IO dispatcher. */
    fun start() = runOnMain {
        if (observation?.isActive == true) return@runOnMain
        observation =
            scope.launch {
                diagnosticsMonitor.observe().collect { snapshot ->
                    diagnostics = snapshot
                    updateStateNow()
                }
            }
    }

    /**
     * Changes the global liquid strength. Non-finite input falls back to `1f`; all other values are
     * clamped to `0f..1f`.
     */
    fun setLiquidIntensity(@FloatRange(from = 0.0, to = 1.0) value: Float) {
        val normalized = normalizeLiquidGlassIntensity(value)
        runOnMain {
            if (normalizedLiquidIntensity == normalized) return@runOnMain
            normalizedLiquidIntensity = normalized
            updateStateNow()
        }
    }

    /** Requests a background diagnostic read without querying system services here. */
    fun refresh() = diagnosticsMonitor.refresh()

    override fun close() = runOnMain {
        observation?.cancel()
        observation = null
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    private fun updateStateNow() {
        mutableState.value = readState()
    }

    private fun readState(): LiquidGlassPerformanceState =
        liquidGlassPerformanceState(
            device = deviceProfile,
            qualityTier = selectedQualityTier,
            liquidIntensity = normalizedLiquidIntensity,
            maximumQuality = maximumQuality,
            reduceMotion = reduceMotion,
            calibration = calibratedDevice.calibration,
            thermalStatus = diagnostics.thermalStatus,
            isPowerSaveMode = diagnostics.powerSaveMode,
            isMemoryPressureHigh = diagnostics.memoryPressureHigh,
            availableMemoryBytes = diagnostics.availableMemoryBytes,
        )
}
