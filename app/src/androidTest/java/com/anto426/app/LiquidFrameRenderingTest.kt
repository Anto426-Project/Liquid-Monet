package com.anto426.app

import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.navigation.navigationbar.LiquidNavigationItem
import com.anto426.liquidmonet.components.navigation.tabbar.LiquidTabBar
import com.anto426.liquidmonet.glass.LiquidBackground
import com.anto426.liquidmonet.glass.LiquidBackgroundEffect
import com.anto426.liquidmonet.glass.LiquidGlassScene
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import java.io.File
import kotlin.math.abs
import kotlin.math.exp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class LiquidFrameRenderingTest {
    @get:Rule val compose = createAndroidComposeRule<LiquidTestActivity>()

    @Test
    fun nativeSpringEntryPointAdvancesThePackagedLibrary() {
        System.loadLibrary("liquidwave")
        val type = Class.forName("com.anto426.liquidmonet.motion.LiquidNativeSpring")
        val instance = type.getField("INSTANCE").get(null)
        val step =
            type.getDeclaredMethod(
                "stepBatch",
                FloatArray::class.java,
                Float::class.javaPrimitiveType,
            )
        step.isAccessible = true
        val values = floatArrayOf(0f, 0f, 1f, -10f, 0f, 0.000001f, 1f)
        assertEquals(1, step.invoke(instance, values, 0.1f))
        assertEquals((1.0 - 2.0 * exp(-1.0)).toFloat(), values[0], 0.000001f)
        assertEquals((10.0 * exp(-1.0)).toFloat(), values[1], 0.000001f)
        assertEquals(-1, step.invoke(instance, FloatArray(6), 0.1f))
    }

    @Test
    fun navbarDragStillCommitsTheEndSelection() {
        val selected = mutableIntStateOf(0)
        compose.setContent {
            MaterialTheme {
                LiquidGlassScene {
                    LiquidTabBar(
                        listOf(
                            LiquidNavigationItem("Uno"),
                            LiquidNavigationItem("Due"),
                            LiquidNavigationItem("Tre"),
                        ),
                        selected.intValue,
                        { selected.intValue = it },
                        Modifier.size(320.dp, 64.dp).testTag("tabs"),
                    )
                }
            }
        }
        compose.onNodeWithTag("tabs").performTouchInput {
            down(Offset(width / 6f, center.y))
            moveTo(Offset(width * 5f / 6f, center.y), 300)
            up()
        }
        compose.runOnIdle { assertEquals(2, selected.intValue) }
    }

    @Test
    fun everyBackgroundKeepsItsPaletteAndContinuesMoving() {
        val dark = mutableStateOf(false)
        val effect = mutableStateOf(LiquidBackgroundEffect.Aurora)
        val animated = mutableStateOf(false)
        compose.mainClock.autoAdvance = false
        compose.setContent {
            MaterialTheme(colorScheme = if (dark.value) darkColorScheme() else lightColorScheme()) {
                CompositionLocalProvider(
                    LocalLiquidGlassPerformance provides
                        LiquidGlassPerformanceState.Fallback.copy(
                            qualityTier = LiquidGlassQualityTier.HIGH,
                            opticalQualityTier = LiquidGlassQualityTier.HIGH,
                            motionScale = if (animated.value) 1f else 0f,
                        )
                ) {
                    LiquidBackground(
                        Modifier.size(240.dp, 320.dp).testTag("background"),
                        effect.value,
                    )
                }
            }
        }
        for (isDark in listOf(false, true)) for (background in LiquidBackgroundEffect.entries) {
            compose.runOnIdle {
                dark.value = isDark
                effect.value = background
                animated.value = false
            }
            compose.mainClock.advanceTimeBy(64)
            val still = compose.onNodeWithTag("background").captureToImage()
            val path =
                File(
                    compose.activity.filesDir,
                    "background-${background.name}-${if (isDark) "dark" else "light"}.png",
                )
            path.outputStream().use {
                still.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            compose.runOnIdle { animated.value = true }
            compose.mainClock.advanceTimeBy(64)
            val before = compose.onNodeWithTag("background").captureToImage().toPixelMap()
            compose.mainClock.advanceTimeBy(4_000)
            val after = compose.onNodeWithTag("background").captureToImage().toPixelMap()
            var changes = 0
            for (y in 0 until before.height step 4) for (x in 0 until before.width step 4) {
                val a = before[x, y]
                val b = after[x, y]
                if (abs(a.red - b.red) + abs(a.green - b.green) + abs(a.blue - b.blue) > .005f)
                    changes++
            }
            assertTrue(
                "$background / dark=$isDark must keep moving",
                changes > before.width * before.height / 1600,
            )
        }
    }
}
