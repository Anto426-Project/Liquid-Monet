package com.anto426.liquidmonet.glass.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import kotlinx.coroutines.flow.first

@Composable
internal actual fun rememberLiquidGlassPerformanceState(
    liquidIntensity: Float,
    maximumQuality: LiquidGlassQualityTier?,
    reduceMotion: Boolean,
): State<LiquidGlassPerformanceState?> {
    if (LocalInspectionMode.current) {
        return remember(liquidIntensity, reduceMotion) {
            mutableStateOf(
                LiquidGlassPerformanceState.Fallback.copy(
                    liquidIntensity =
                        if (liquidIntensity.isFinite()) liquidIntensity.coerceIn(0f, 1f) else 1f,
                    motionScale = if (reduceMotion) 0f else 1f,
                )
            )
        }
    }
    val applicationContext = LocalContext.current.applicationContext
    val windowInfo = LocalWindowInfo.current
    val calibratedDevice by
        produceState<LiquidGlassCalibratedDevice?>(null, applicationContext, windowInfo) {
            // A background/locked activity can have a composition before it is visible. Such a
            // launch is not a representative CPU/GPU environment for permanent calibration.
            snapshotFlow { windowInfo.isWindowFocused }.first { it }
            value = LiquidGlassDeviceCalibration.load(applicationContext)
        }
    val readyDevice = calibratedDevice ?: return remember { mutableStateOf(null) }
    val manager =
        remember(applicationContext, readyDevice, maximumQuality, reduceMotion) {
            LiquidGlassPerformanceManager(
                applicationContext,
                readyDevice,
                liquidIntensity,
                maximumQuality,
                reduceMotion,
            )
        }

    DisposableEffect(manager) {
        manager.start()
        onDispose(manager::close)
    }
    LaunchedEffect(manager, liquidIntensity) { manager.setLiquidIntensity(liquidIntensity) }
    return manager.state
}
