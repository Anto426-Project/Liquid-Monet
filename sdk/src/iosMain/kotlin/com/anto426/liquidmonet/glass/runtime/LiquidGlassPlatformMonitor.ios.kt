package com.anto426.liquidmonet.glass.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSProcessInfoPowerStateDidChangeNotification
import platform.Foundation.NSProcessInfoThermalState.NSProcessInfoThermalStateCritical
import platform.Foundation.NSProcessInfoThermalState.NSProcessInfoThermalStateFair
import platform.Foundation.NSProcessInfoThermalState.NSProcessInfoThermalStateNominal
import platform.Foundation.NSProcessInfoThermalState.NSProcessInfoThermalStateSerious
import platform.Foundation.NSProcessInfoThermalStateDidChangeNotification
import platform.Foundation.lowPowerModeEnabled
import platform.Foundation.thermalState
import platform.UIKit.UIApplicationDidBecomeActiveNotification

@Composable
internal actual fun rememberLiquidGlassPerformanceState(
    liquidIntensity: Float,
    maximumQuality: LiquidGlassQualityTier?,
    reduceMotion: Boolean,
): State<LiquidGlassPerformanceState?> {
    return produceState(null, liquidIntensity, maximumQuality, reduceMotion) {
        val calibrated = LiquidGlassIosDeviceCalibration.load()
        fun publishState() {
            val process = NSProcessInfo.processInfo
            value =
                liquidGlassPerformanceState(
                    device = calibrated.device,
                    qualityTier = calibrated.calibration.qualityTier,
                    liquidIntensity = liquidIntensity,
                    maximumQuality = maximumQuality,
                    reduceMotion = reduceMotion,
                    calibration = calibrated.calibration,
                    thermalStatus =
                        when (process.thermalState) {
                            NSProcessInfoThermalStateNominal -> LiquidGlassThermalStatus.NONE
                            NSProcessInfoThermalStateFair -> LiquidGlassThermalStatus.LIGHT
                            NSProcessInfoThermalStateSerious -> LiquidGlassThermalStatus.SEVERE
                            NSProcessInfoThermalStateCritical -> LiquidGlassThermalStatus.CRITICAL
                            else -> LiquidGlassThermalStatus.NONE
                        },
                    isPowerSaveMode = process.lowPowerModeEnabled,
                )
        }
        val notifications = NSNotificationCenter.defaultCenter
        val observers =
            listOfNotNull(
                    NSProcessInfoThermalStateDidChangeNotification,
                    NSProcessInfoPowerStateDidChangeNotification,
                    UIApplicationDidBecomeActiveNotification,
                )
                .map { name ->
                    notifications.addObserverForName(name, null, NSOperationQueue.mainQueue) {
                        publishState()
                    }
                }
        publishState()
        awaitDispose { observers.forEach { notifications.removeObserver(it) } }
    }
}
