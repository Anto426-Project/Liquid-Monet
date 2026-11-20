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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.util.fastCoerceIn
import com.anto426.liquidmonet.components.internal.motion.LiquidPressMotion
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPerformanceState
import com.anto426.liquidmonet.motion.inspectDragGestures
import com.kyant.backdrop.RuntimeShader
import com.kyant.backdrop.asComposeShader
import com.kyant.backdrop.isRuntimeShaderSupported
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope

private const val ControlDragHoldMillis = 120L

@androidx.compose.runtime.Stable
internal class InteractiveHighlight(
    val animationScope: CoroutineScope,
    private val performance: () -> LiquidGlassPerformanceState = {
        LiquidGlassPerformanceState.Fallback
    },
    private val lightTint: () -> Color = { Color.Unspecified },
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
            val tint = lightTint()
            val isTinted = tint.isSpecified
            val resolvedColor = when {
                highlightColor.isSpecified -> highlightColor
                isTinted -> tint
                else -> Color.White
            }
            val blendMode = if (isTinted) BlendMode.SrcOver else BlendMode.Plus

            val drawHighlight: () -> Unit = {
                val shader = shader
                if (shader != null) {
                    drawRect(
                        resolvedColor.copy((if (isTinted) 0.035f else 0.08f) * progress),
                        blendMode = blendMode,
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
                        blendMode = blendMode,
                    )
                } else {
                    drawRect(
                        resolvedColor.copy((if (isTinted) 0.13f else 0.25f) * progress),
                        blendMode = blendMode,
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

    /** Observe the owned press between clickable and the moving graphics layer. */
    internal fun ownedGestureModifier(): Modifier =
        Modifier.pointerInput(animationScope) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (down.isConsumed) return@awaitEachGesture
                val touchSlop = viewConfiguration.touchSlop
                val canOwnDrag = motionEnabled
                var dragged = false
                var tapped = false
                var heldBeforeMoving: Boolean? = null
                var ownsDrag = false
                press(down.position)
                try {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (change.isConsumed) break
                        val offset = change.position - down.position
                        val distance = offset.getDistance()
                        dragged = dragged || distance > touchSlop
                        if (!change.pressed) {
                            // Suppress the click even when the elastic drag ends inside the button.
                            if (dragged) change.consume()
                            else tapped = true
                            break
                        }
                        // A short stationary press gives the control priority on either axis.
                        // A drag already in progress must not become a hold just because time passed.
                        if (canOwnDrag && heldBeforeMoving == null && distance > touchSlop / 2f) {
                            heldBeforeMoving = change.uptimeMillis - down.uptimeMillis >= ControlDragHoldMillis
                        }
                        if (heldBeforeMoving == true && dragged) ownsDrag = true

                        if (ownsDrag && change.position != change.previousPosition) {
                            change.consume()
                        } else {
                            // A scrollable ancestor can claim this move later in the Main pass.
                            awaitPointerEvent(PointerEventPass.Final)
                            if (change.isConsumed) break
                            // An accepted horizontal drag can turn vertically without handing off.
                            if (canOwnDrag && abs(offset.x) > touchSlop && abs(offset.x) > abs(offset.y)) {
                                ownsDrag = true
                            }
                        }
                        move(change.position)
                    }
                } finally {
                    motion.release(finishTap = tapped)
                }
            }
        }

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
