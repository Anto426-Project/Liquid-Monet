package com.anto426.liquidmonet.glass.runtime

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kyant.backdrop.internal.RoundedRectRefractionShaderString
import com.kyant.backdrop.internal.RoundedRectRefractionWithDispersionShaderString
import kotlin.math.sqrt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Actual HWUI rendering, including GPU completion; no CPU-only shader compilation score. */
@RequiresApi(31)
internal object LiquidGlassRenderProbe {

    @androidx.annotation.WorkerThread
    suspend fun measure(
        device: LiquidGlassDeviceProfile,
        ceiling: LiquidGlassQualityTier,
    ): LiquidGlassCalibration {
        val pixels = device.displayWidthPixels.toLong() * device.displayHeightPixels
        if (pixels <= 0L) return unavailable()
        // Strong-memory devices can measure near native resolution, avoiding exaggerated
        // extrapolation of fixed submission costs. Small-memory devices retain a 1 MP budget.
        val maxPixels =
            if (LiquidGlassCalibrationPolicy.memoryCeiling(device) >= LiquidGlassQualityTier.HIGH) {
                4_194_304.0
            } else {
                1_048_576.0
            }
        val scale = minOf(1.0, sqrt(maxPixels / pixels), 4096.0 / device.displayHeightPixels)
        val width = (device.displayWidthPixels * scale).toInt().coerceAtLeast(1)
        val height = (device.displayHeightPixels * scale).toInt().coerceAtLeast(1)
        var session: LiquidGlassOffscreenRenderSession? = null
        val nodes = ArrayList<RenderNode>()
        var result = liquidGlassCapabilityEstimate(ceiling)
        val deadline = System.nanoTime() + 1_800_000_000L
        try {
            val renderSession =
                LiquidGlassOffscreenRenderSession(width, height).also { session = it }
            val background = RenderNode("CalibrationBackdrop").also { nodes += it }
            background.setPosition(0, 0, width, height)
            val cards =
                List(6) { index -> RenderNode("CalibrationGlass$index").also { nodes += it } }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val cardWidth = (width * 0.43f).toInt().coerceAtLeast(1)
            val cardHeight = (height * 0.24f).toInt().coerceAtLeast(1)
            val radius = minOf(cardWidth, cardHeight) * 0.12f
            val density = (device.displayDensity * scale.toFloat()).coerceAtLeast(0.5f)
            for (tier in LiquidGlassCalibrationPolicy.candidates(ceiling)) {
                currentCoroutineContext().ensureActive()
                if (System.nanoTime() >= deadline) break
                val samplingScale = tier.renderResolutionScale
                val sampledWidth = (cardWidth * samplingScale).toInt().coerceAtLeast(1)
                val sampledHeight = (cardHeight * samplingScale).toInt().coerceAtLeast(1)
                val sampledRadius = radius * samplingScale
                val sampledDensity = density * samplingScale
                var effect =
                    RenderEffect.createBlurEffect(
                        LiquidGlassPresets.Standard.blurRadius.value * sampledDensity,
                        LiquidGlassPresets.Standard.blurRadius.value * sampledDensity,
                        Shader.TileMode.CLAMP,
                    )
                if (Build.VERSION.SDK_INT >= 33) {
                    // Every resolution candidate preserves the same complete optical workload.
                    effect =
                        RenderEffect.createChainEffect(
                            lens(
                                LiquidGlassQualityTier.ULTRA,
                                sampledWidth,
                                sampledHeight,
                                sampledRadius,
                                sampledDensity,
                            ),
                            effect,
                        )
                }
                cards.forEachIndexed { index, card ->
                    val left = ((0.04f + (index % 2) * 0.49f) * width).toInt()
                    val top = ((0.07f + (index / 2) * 0.30f) * height).toInt()
                    card.setPosition(left, top, left + sampledWidth, top + sampledHeight)
                    card.pivotX = 0f
                    card.pivotY = 0f
                    card.scaleX = 1f / samplingScale
                    card.scaleY = 1f / samplingScale
                    card.setOutline(
                        Outline().apply {
                            setRoundRect(0, 0, sampledWidth, sampledHeight, sampledRadius)
                        }
                    )
                    card.clipToOutline = true
                    card.elevation = 4f * density
                    card.setRenderEffect(effect)
                    val canvas = card.beginRecording()
                    canvas.scale(samplingScale, samplingScale)
                    canvas.translate(-left.toFloat(), -top.toFloat())
                    canvas.drawRenderNode(background)
                    card.endRecording()
                }
                val samples = ArrayList<Long>(5)
                // Two unmeasured warm-ups per candidate separate first-use compilation from steady
                // rendering.
                for (frame in 0 until 7) {
                    currentCoroutineContext().ensureActive()
                    if (System.nanoTime() >= deadline) break
                    val duration =
                        renderSession.render { rootCanvas ->
                            val canvas = background.beginRecording()
                            canvas.drawColor(Color.rgb(24, 28, 42))
                            repeat(3) { band ->
                                paint.shader =
                                    LinearGradient(
                                        frame + band * 37f,
                                        0f,
                                        width.toFloat(),
                                        height.toFloat(),
                                        Color.rgb(40 + band * 30, 75 + frame, 150),
                                        Color.rgb(120, 30 + band * 25, 70),
                                        Shader.TileMode.MIRROR,
                                    )
                                canvas.drawRect(
                                    0f,
                                    band * height / 4f,
                                    width.toFloat(),
                                    height.toFloat(),
                                    paint,
                                )
                            }
                            background.endRecording()
                            rootCanvas.drawRenderNode(background)
                            cards.forEach(rootCanvas::drawRenderNode)
                        }
                    if (frame >= 2) samples += duration
                }
                val p90 = liquidGlassP90(samples)
                Log.i(
                    "LiquidGlassCalibration",
                    "Render $tier: p90=${p90}ns, samples=${samples.size}, ${width}x$height",
                )
                if (samples.size < 5 || p90 <= 0L) break
                val candidate =
                    LiquidGlassCalibration(
                        tier,
                        LiquidGlassCalibrationSource.MEASURED,
                        renderP90Nanos = p90,
                        renderSampleCount = samples.size,
                        renderWidthPixels = width,
                        renderHeightPixels = height,
                    )
                if (
                    LiquidGlassCalibrationPolicy.acceptsRenderSample(
                        device,
                        p90,
                        samples.size,
                        width,
                        height,
                    ) || tier == LiquidGlassQualityTier.MINIMAL
                ) {
                    result = candidate
                    break
                }
                result =
                    liquidGlassCapabilityEstimate(LiquidGlassCalibrationPolicy.lowerCandidate(tier))
            }
            return result
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("LiquidGlassCalibration", "Render candidate unavailable", e)
            return result
        } finally {
            try {
                session?.close()
            } finally {
                nodes.forEach { it.discardDisplayList() }
            }
        }
    }

    @RequiresApi(33)
    private fun lens(
        tier: LiquidGlassQualityTier,
        width: Int,
        height: Int,
        radius: Float,
        density: Float,
    ): RenderEffect {
        val dispersion = tier == LiquidGlassQualityTier.ULTRA
        val shader =
            RuntimeShader(
                if (dispersion) RoundedRectRefractionWithDispersionShaderString
                else RoundedRectRefractionShaderString
            )
        shader.setFloatUniform("size", width.toFloat(), height.toFloat())
        shader.setFloatUniform("offset", 0f, 0f)
        shader.setFloatUniform("cornerRadii", radius, radius, radius, radius)
        shader.setFloatUniform(
            "refractionHeight",
            LiquidGlassPresets.Standard.refractionHeight.value * density,
        )
        shader.setFloatUniform(
            "refractionAmount",
            -LiquidGlassPresets.Standard.refractionAmount.value * density,
        )
        shader.setFloatUniform("depthEffect", 1f)
        if (dispersion) shader.setFloatUniform("chromaticAberration", 1f)
        return RenderEffect.createRuntimeShaderEffect(shader, "content")
    }

    private fun unavailable() =
        LiquidGlassCalibration(
            LiquidGlassQualityTier.MINIMAL,
            LiquidGlassCalibrationSource.TEST_UNAVAILABLE,
        )
}
