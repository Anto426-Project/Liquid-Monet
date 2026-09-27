package com.anto426.liquidmonet.glass.runtime

import com.kyant.backdrop.RuntimeShaderPrewarm
import com.kyant.backdrop.internal.BuiltinRuntimeShader
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.objcPtr
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color4f
import org.jetbrains.skia.DirectContext
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.Gradient
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.ImageInfo
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeShaderBuilder
import org.jetbrains.skia.Shader
import org.jetbrains.skia.Surface
import org.jetbrains.skia.impl.use
import platform.Metal.MTLCommandQueueProtocol
import platform.Metal.MTLCreateSystemDefaultDevice
import platform.Metal.MTLDeviceProtocol

/**
 * Owns a real Skia/Metal context. Create, draw, synchronize and close on the same worker thread.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class LiquidGlassIosOffscreenRenderSession
private constructor(
    // Retain native owners for at least as long as the Skia context holding their pointers.
    private val device: MTLDeviceProtocol,
    private val queue: MTLCommandQueueProtocol,
    private val context: DirectContext,
    private val output: Surface,
    private val width: Int,
    private val height: Int,
) : AutoCloseable {
    /** End-to-end rendering time includes recording, filters, submission and GPU completion. */
    fun render(
        tier: LiquidGlassQualityTier,
        frame: Int,
        density: Float,
        warmup: Boolean = false,
    ): Long {
        val scale = tier.renderResolutionScale
        val cardWidth = (width * 0.43f * scale).toInt().coerceAtLeast(1)
        val cardHeight = (height * 0.24f * scale).toInt().coerceAtLeast(1)
        val radius = minOf(cardWidth, cardHeight) * 0.12f
        val start = liquidGlassIosNanoTime()
        val canvas = output.canvas
        canvas.clear(0xff181c2a.toInt())
        Paint().use { paint ->
            repeat(3) { band ->
                val gradient =
                    Gradient(
                        Gradient.Colors(
                            arrayOf(
                                Color4f(0.16f + band * 0.1f, 0.3f + frame * 0.005f, 0.6f, 1f),
                                Color4f(0.47f, 0.12f + band * 0.1f, 0.27f, 1f),
                            ),
                            tileMode = FilterTileMode.MIRROR,
                        )
                    )
                Shader.makeLinearGradient(
                        frame + band * 37f,
                        0f,
                        width.toFloat(),
                        height.toFloat(),
                        gradient,
                    )
                    .use { shader ->
                        paint.shader = shader
                        canvas.drawRect(
                            Rect.makeXYWH(
                                0f,
                                band * height / 4f,
                                width.toFloat(),
                                height.toFloat(),
                            ),
                            paint,
                        )
                        paint.shader = null
                    }
            }
        }
        output.makeImageSnapshot().use { background ->
            Surface.makeRenderTarget(context, true, ImageInfo.makeN32Premul(cardWidth, cardHeight))
                .use { sampled ->
                    repeat(6) { index ->
                        val left = (0.04f + (index % 2) * 0.49f) * width
                        val top = (0.07f + (index / 2) * 0.30f) * height
                        val source = sampled.canvas
                        source.clear(0)
                        source.save()
                        source.scale(scale, scale)
                        source.translate(-left, -top)
                        source.drawImage(background, 0f, 0f)
                        source.restore()
                        val kind =
                            if (warmup)
                                BuiltinRuntimeShader.entries[
                                        index % BuiltinRuntimeShader.entries.size]
                            else BuiltinRuntimeShader.Dispersion
                        RuntimeShaderBuilder(RuntimeShaderPrewarm.obtain(kind.source)).use { builder
                            ->
                            builder.uniform("size", cardWidth.toFloat(), cardHeight.toFloat())
                            builder.uniform("cornerRadii", radius, radius, radius, radius)
                            Paint().use { paint ->
                                paint.isAntiAlias = true
                                canvas.save()
                                try {
                                    canvas.translate(left, top)
                                    canvas.scale(1f / scale, 1f / scale)
                                    canvas.clipRRect(
                                        0f,
                                        0f,
                                        cardWidth.toFloat(),
                                        cardHeight.toFloat(),
                                        floatArrayOf(radius, radius, radius, radius),
                                        true,
                                    )
                                    if (kind.hasBackdropInput) {
                                        builder.uniform("offset", 0f, 0f)
                                        builder.uniform(
                                            "refractionHeight",
                                            LiquidGlassPresets.Standard.refractionHeight.value *
                                                density *
                                                scale,
                                        )
                                        builder.uniform(
                                            "refractionAmount",
                                            -LiquidGlassPresets.Standard.refractionAmount.value *
                                                density *
                                                scale,
                                        )
                                        builder.uniform("depthEffect", 1f)
                                        if (kind == BuiltinRuntimeShader.Dispersion)
                                            builder.uniform("chromaticAberration", 1f)
                                        ImageFilter.makeBlur(
                                                LiquidGlassPresets.Standard.blurRadius.value *
                                                    density *
                                                    scale,
                                                LiquidGlassPresets.Standard.blurRadius.value *
                                                    density *
                                                    scale,
                                                FilterTileMode.CLAMP,
                                            )
                                            .use { blur ->
                                                ImageFilter.makeRuntimeShader(
                                                        builder,
                                                        "content",
                                                        blur,
                                                    )
                                                    .use { lens ->
                                                        paint.imageFilter = lens
                                                        sampled.makeImageSnapshot().use { image ->
                                                            canvas.drawImage(image, 0f, 0f, paint)
                                                        }
                                                        paint.imageFilter = null
                                                    }
                                            }
                                    } else {
                                        builder.uniform("angle", 0.7853982f)
                                        builder.uniform("falloff", 1f)
                                        if (kind == BuiltinRuntimeShader.Highlight)
                                            builder.uniform("color", 1f, 1f, 1f, 1f)
                                        builder.makeShader().use { shader ->
                                            paint.shader = shader
                                            canvas.drawRect(
                                                Rect.makeWH(
                                                    cardWidth.toFloat(),
                                                    cardHeight.toFloat(),
                                                ),
                                                paint,
                                            )
                                            paint.shader = null
                                        }
                                    }
                                } finally {
                                    canvas.restore()
                                }
                            }
                        }
                    }
                }
        }
        // Skiko's syncCpu=true waits until submitted GPU work has finished. Asynchronous
        // flush() alone would classify only command encoding and incorrectly promote devices.
        output.flushAndSubmit(syncCpu = true)
        return (liquidGlassIosNanoTime() - start).also { check(it > 0L) }
    }

    /** Bounded readback outside timed samples rejects a context that did not produce pixels. */
    fun verifyOutput() {
        Bitmap().use { pixel ->
            check(pixel.allocPixels(ImageInfo.makeN32Premul(1, 1)))
            check(output.readPixels(pixel, width / 2, height / 2)) { "Metal readback failed" }
            check(pixel.getColor(0, 0) ushr 24 != 0) { "Metal produced no opaque output" }
        }
    }

    override fun close() {
        try {
            output.close()
        } finally {
            context.close()
        }
    }

    companion object {
        fun open(width: Int, height: Int): LiquidGlassIosOffscreenRenderSession {
            require(width in 1..4096 && height in 1..4096)
            val device = checkNotNull(MTLCreateSystemDefaultDevice()) { "Metal unavailable" }
            val queue = checkNotNull(device.newCommandQueue()) { "Metal queue unavailable" }
            val context = DirectContext.makeMetal(device.objcPtr(), queue.objcPtr())
            try {
                context.resourceCacheLimit = 64L * 1024 * 1024
                val surface =
                    Surface.makeRenderTarget(context, true, ImageInfo.makeN32Premul(width, height))
                return LiquidGlassIosOffscreenRenderSession(
                    device,
                    queue,
                    context,
                    surface,
                    width,
                    height,
                )
            } catch (error: Throwable) {
                context.close()
                throw error
            }
        }
    }
}
