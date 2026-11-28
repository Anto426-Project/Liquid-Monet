package com.anto426.liquidmonet.glass.runtime

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.util.AtomicFile
import android.util.Log
import androidx.annotation.RequiresApi
import com.kyant.backdrop.RuntimeShaderPrewarm
import com.kyant.backdrop.internal.BuiltinRuntimeShader
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/** Startup preparation only: never scores hardware or changes the saved device profile. */
internal object LiquidGlassShaderWarmup {
    suspend fun prepare(context: Context, calibrated: LiquidGlassCalibratedDevice) {
        if (Build.VERSION.SDK_INT < 33 || !calibrated.device.supportsRuntimeShader) return
        val marker = AtomicFile(File(context.noBackupFilesDir, "liquid-glass-shader-warmup.pending"))
        val signature = BuiltinRuntimeShader.entries.joinToString(":") { it.source.hashCode().toString() }
        try {
            // Source changes allow another attempt; activity recreation and process restarts do not
            // repeatedly execute a warm-up that previously crashed or exceeded its deadline.
            val unfinished = try {
                marker.openRead().use { String(it.readNBytes(256), Charsets.UTF_8) }
            } catch (_: Exception) { null }
            if (unfinished == signature) return
            val output = marker.startWrite()
            try {
                output.write(signature.toByteArray())
                marker.finishWrite(output)
            } catch (error: Throwable) {
                marker.failWrite(output)
                throw error
            }
            val preparedCount = RuntimeShaderPrewarm.prepare(
                if (LiquidGlassCalibrationPolicy.memoryCeiling(calibrated.device) >= LiquidGlassQualityTier.HIGH) 8 else 2)
            // An interrupted/failed graphics test must not be immediately repeated by warm-up.
            val gpuReady = calibrated.calibration.source == LiquidGlassCalibrationSource.MEASURED
            if (gpuReady) prepareGpu()
            currentCoroutineContext().ensureActive()
            marker.delete()
            Log.i("LiquidGlassCalibration", "Shader warm-up: $preparedCount/${BuiltinRuntimeShader.entries.size} programs prepared; GPU=$gpuReady")
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Log.w("LiquidGlassCalibration", "Shader warm-up unavailable", error)
        } catch (_: LinkageError) {
            // Keep the pending marker, so an unsupported driver entry point is not retried.
        } catch (_: OutOfMemoryError) {
            // Warm-up is optional. The already selected profile remains usable.
        }
    }

    @RequiresApi(33)
    private suspend fun prepareGpu() {
        val nodes = ArrayList<RenderNode>(BuiltinRuntimeShader.entries.size)
        try {
            LiquidGlassOffscreenRenderSession(256, 384).use { session ->
                BuiltinRuntimeShader.entries.forEachIndexed { index, kind ->
                    val shader = RuntimeShaderPrewarm.obtain(kind.source)
                    when (kind) {
                        BuiltinRuntimeShader.Refraction,
                        BuiltinRuntimeShader.Dispersion -> {
                            shader.setFloatUniform("size", 128f, 128f)
                            shader.setFloatUniform("cornerRadii", 16f, 16f, 16f, 16f)
                            shader.setFloatUniform("offset", 0f, 0f)
                            shader.setFloatUniform("refractionHeight", 16f)
                            shader.setFloatUniform("refractionAmount", -24f)
                            shader.setFloatUniform("depthEffect", 1f)
                            if (kind == BuiltinRuntimeShader.Dispersion) {
                                shader.setFloatUniform("chromaticAberration", 1f)
                            }
                        }
                        BuiltinRuntimeShader.AdaptiveLegibility -> {
                            shader.setFloatUniform("darkSurface", 1f)
                            shader.setFloatUniform("threshold", 0.22f)
                            shader.setFloatUniform("targetLuma", 0.22f)
                            shader.setFloatUniform("strength", 0.85f)
                            shader.setFloatUniform("tintRgb", 0.08f, 0.09f, 0.11f)
                        }
                        BuiltinRuntimeShader.Highlight,
                        BuiltinRuntimeShader.Ambient -> {
                            shader.setFloatUniform("size", 128f, 128f)
                            shader.setFloatUniform("cornerRadii", 16f, 16f, 16f, 16f)
                            shader.setFloatUniform("angle", 0.7853982f)
                            shader.setFloatUniform("falloff", 1f)
                            if (kind == BuiltinRuntimeShader.Highlight) {
                                shader.setColorUniform("color", Color.WHITE)
                            }
                        }
                    }
                    val node = RenderNode("Warm${kind.name}").also { nodes += it }
                    val x = (index % 2) * 128
                    val y = (index / 2) * 128
                    node.setPosition(x, y, x + 128, y + 128)
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                    if (kind.hasBackdropInput) {
                        node.setRenderEffect(RenderEffect.createChainEffect(
                            RenderEffect.createRuntimeShaderEffect(shader, "content"),
                            RenderEffect.createBlurEffect(4f, 4f, Shader.TileMode.CLAMP)))
                        paint.shader = LinearGradient(0f, 0f, 128f, 128f, Color.BLUE, Color.YELLOW, Shader.TileMode.CLAMP)
                    } else paint.shader = shader
                    val canvas = node.beginRecording()
                    try { canvas.drawRoundRect(0f, 0f, 128f, 128f, 16f, 16f, paint) }
                    finally { node.endRecording() }
                }
                repeat(2) {
                    session.render { canvas ->
                        canvas.drawColor(Color.BLACK)
                        nodes.forEach(canvas::drawRenderNode)
                    }
                }
            }
        } finally { nodes.forEach { it.discardDisplayList() } }
    }
}
