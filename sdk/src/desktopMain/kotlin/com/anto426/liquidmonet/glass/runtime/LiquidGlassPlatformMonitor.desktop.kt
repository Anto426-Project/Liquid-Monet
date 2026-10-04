package com.anto426.liquidmonet.glass.runtime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import java.lang.management.ManagementFactory
@Composable
internal actual fun rememberLiquidGlassPerformanceState(
    liquidIntensity: Float, maximumQuality: LiquidGlassQualityTier?, reduceMotion: Boolean,
): State<LiquidGlassPerformanceState?> {
    val device = remember {
        val bean = ManagementFactory.getOperatingSystemMXBean() as? com.sun.management.OperatingSystemMXBean
        val memory = bean?.totalMemorySize ?: Runtime.getRuntime().maxMemory()
        LiquidGlassDeviceProfile(
            sdkInt = 0, supportsRenderEffect = true, supportsRuntimeShader = true,
            isLowRamDevice = memory < 4L * 1024 * 1024 * 1024,
            totalMemoryBytes = memory, appMemoryClassMb = 0,
            cpuCoreCount = Runtime.getRuntime().availableProcessors(),
            is64Bit = System.getProperty("os.arch").contains("64"),
            socModel = System.getProperty("os.arch"),
        )
    }
    val tier = if (device.isLowRamDevice || device.cpuCoreCount <= 2) LiquidGlassQualityTier.BALANCED else LiquidGlassQualityTier.HIGH
    return rememberUpdatedState(liquidGlassPerformanceState(device, tier, liquidIntensity, maximumQuality, reduceMotion))
}
