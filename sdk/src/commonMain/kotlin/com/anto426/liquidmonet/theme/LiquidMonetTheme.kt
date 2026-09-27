package com.anto426.liquidmonet.theme

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.rememberLiquidGlassPerformanceState
import com.anto426.liquidmonet.theme.monet.LiquidMonetEngine
import com.anto426.liquidmonet.theme.monet.LiquidMonetPresets
import com.anto426.liquidmonet.theme.monet.LiquidMonetSeed

/**
 * LiquidMonetTheme - Official Google Material 3 Expressive Theme with MonetEngine Dynamic Color.
 * Fuses Material 3 Expressive color schemes & motion with AGSL Snell Glass Backdrops.
 *
 * On Android and iOS the first launch measures CPU, memory traffic and graphics rendering off the
 * UI thread, then saves a permanent device profile. Content starts only after a profile is ready;
 * later launches reuse it. Calibration controls sampling resolution, preserving material depth. iOS
 * uses Skia/Metal and retains an explicit capability estimate if native calibration is unavailable.
 * A null [maximumGlassQuality] chooses ULTRA for ULTRA-capable hardware and HIGH otherwise. An
 * explicit quality preserves the caller's optical choice independently of sampling resolution.
 * [liquidIntensity] controls the strength of that material. [reduceMotion] is an
 * accessibility-level policy: components receive zero motion scale and continuous decorative
 * animation is disabled without exposing renderer internals to users.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiquidMonetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    useMonetEngine: Boolean = true,
    customMonetSeed: LiquidMonetSeed = LiquidMonetPresets.Sapphire,
    @FloatRange(from = 0.0, to = 1.0) liquidIntensity: Float = 1f,
    glassColors: LiquidGlassColors? = null,
    maximumGlassQuality: LiquidGlassQualityTier? = null,
    reduceMotion: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        LiquidMonetEngine.generateColorScheme(
            darkTheme = darkTheme,
            useSystemDynamic = useMonetEngine,
            customSeed = customMonetSeed,
        )

    val glassPerformance by
        rememberLiquidGlassPerformanceState(
            liquidIntensity = liquidIntensity,
            maximumQuality = maximumGlassQuality,
            reduceMotion = reduceMotion,
        )
    val readyPerformance = glassPerformance
    if (readyPerformance == null) {
        // Keep application content at one profile from its first composition. This lightweight
        // startup surface also avoids racing a glass-heavy screen against the graphics probe.
        MaterialExpressiveTheme(colorScheme = colorScheme) {
            Box(
                Modifier.fillMaxSize().background(colorScheme.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }
    val resolvedGlassColors =
        glassColors ?: remember(colorScheme) { LiquidGlassDefaults.colors(colorScheme) }
    CompositionLocalProvider(
        LocalLiquidGlassPerformance provides readyPerformance,
        LocalLiquidGlassColors provides resolvedGlassColors,
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}
