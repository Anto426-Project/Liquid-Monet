package com.anto426.liquidmonet.components.internal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.util.fastCoerceIn
import com.anto426.liquidmonet.components.internal.motion.LiquidPressMotion
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.inspectDragGestures
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.isRuntimeShaderSupported
import kotlinx.coroutines.CoroutineScope

@androidx.compose.runtime.Stable
internal class InteractiveHighlight(
    val animationScope: CoroutineScope,
    private val performance: () -> LiquidGlassPerformanceState = {
        LiquidGlassPerformanceState.Fallback
    },
    val position: (size: Size, offset: Offset) -> Offset = { _, offset -> offset },
) {

    private val motion = LiquidPressMotion(animationScope, performance)
    val motionEnabled: Boolean
        get() = motion.motionEnabled

    val pressProgress: Float
        get() = motion.pressProgress

    val offset: Offset
        get() = motion.offset

    private val clipPath = Path()
    private var lastClipSize: Size = Size.Unspecified
    private var lastClipShape: Shape? = null

    private val shader by lazy {
        if (isRuntimeShaderSupported()) {
            RuntimeShader(
                """
uniform float2 size;
layout(color) uniform half4 color;
uniform float radius;
uniform float2 position;

half4 main(float2 coord) {
    float dist = distance(coord, position);
    float intensity = smoothstep(radius, radius * 0.5, dist);
    return color * intensity;
}"""
            )
        } else {
            null
        }
    }

    internal fun modifier(
        highlightColor: Color = Color.Unspecified,
        clipShape: Shape? = null,
    ): Modifier = Modifier.drawWithContent {
        drawContent()

        val progress = motion.pressProgress
        if (progress > 0f) {
            val resolvedColor = if (highlightColor.isSpecified) highlightColor else Color.White

            val drawHighlight: () -> Unit = {
                val shader = shader
                if (shader != null) {
                    drawRect(
                        resolvedColor.copy(0.08f * progress),
                        blendMode = BlendMode.Plus,
                    )
                    shader.apply {
                        val position = position(size, motion.currentPosition)
                        setFloatUniform("size", size.width, size.height)
                        setColorUniform("color", resolvedColor.copy(0.15f * progress))
                        setFloatUniform("radius", size.minDimension * 1.5f)
                        setFloatUniform(
                            "position",
                            position.x.fastCoerceIn(0f, size.width),
                            position.y.fastCoerceIn(0f, size.height),
                        )
                    }
                    drawRect(
                        ShaderBrush(shader.asComposeShader()),
                        blendMode = BlendMode.Plus,
                    )
                } else {
                    drawRect(
                        resolvedColor.copy(0.25f * progress),
                        blendMode = BlendMode.Plus,
                    )
                }
            }

            if (clipShape != null) {
                if (lastClipSize != size || lastClipShape != clipShape) {
                    clipPath.reset()
                    clipPath.addOutline(clipShape.createOutline(size, layoutDirection, this))
                    lastClipSize = size
                    lastClipShape = clipShape
                }
                clipPath(clipPath) {
                    drawHighlight()
                }
            } else {
                drawHighlight()
            }
        }
    }

    internal val modifier: Modifier
        get() = modifier(Color.Unspecified, null)

    internal fun press(position: Offset) = motion.press(position)

    internal fun move(position: Offset) = motion.move(position)

    internal fun release() = motion.release()

    internal val gestureModifier: Modifier =
        Modifier.pointerInput(animationScope) {
            inspectDragGestures(
                onDragStart = { down ->
                    press(down.position)
                },
                onDragEnd = { release() },
                onDragCancel = ::release,
            ) { change, _ ->
                move(change.position)
            }
        }
}
