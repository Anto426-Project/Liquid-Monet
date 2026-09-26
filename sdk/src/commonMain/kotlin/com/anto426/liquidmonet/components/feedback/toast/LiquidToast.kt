package com.anto426.liquidmonet.components.feedback.toast

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.anto426.liquidmonet.components.feedback.toast.motion.LiquidToastMotion
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastData
import com.anto426.liquidmonet.components.feedback.toast.state.LiquidToastState
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LiquidGlassZIndex
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidFloatMotion
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlinx.coroutines.launch

@Composable
fun rememberLiquidToastState(): LiquidToastState {
    val scope = rememberCoroutineScope()
    return remember(scope) { LiquidToastState(scope) }
}

/**
 * LiquidToastHost - Overlay container that renders active [LiquidToast] notifications with camera
 * punch-hole dewdrop physics.
 */
@Composable
fun LiquidToastHost(
    state: LiquidToastState,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopCenter,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val toast = state.currentToast
    val performance = LocalLiquidGlassPerformance.current

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                // Toasts are scene overlays, not navigation content. Keep them above top bars,
                // floating controls and every ordinary interactive z-index.
                .zIndex(LiquidGlassZIndex.Toast)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .graphicsLayer(clip = false),
        contentAlignment = alignment,
    ) {
        AnimatedContent(
            targetState = toast,
            modifier = Modifier.graphicsLayer(clip = false),
            transitionSpec = LiquidToastMotion.contentTransition(performance),
            contentAlignment = alignment,
            label = "liquidToastTransition",
        ) { currentToastItem ->
            if (currentToastItem != null) {
                LiquidToast(
                    data = currentToastItem,
                    modifier =
                        Modifier.fillMaxWidth().widthIn(max = 520.dp).graphicsLayer(clip = false),
                    onDismiss = { state.dismiss() },
                    backdropState = backdropState,
                )
            }
        }
    }
}

/** LiquidToast - compact Material-proportioned Liquid Glass notification. */
@Composable
fun LiquidToast(
    data: LiquidToastData,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val performance = LocalLiquidGlassPerformance.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val offsetXAnim = remember(data.id) { LiquidFloatMotion(0f) }
    val offsetYAnim = remember(data.id) { LiquidFloatMotion(0f) }
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val toastHighlight = rememberLiquidControlHighlight()

    val accentColor = colorScheme.primary
    val iconVector: ImageVector = data.icon ?: LiquidIcons.Info

    val shape = RoundedRectangle(20.dp)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val currentOffsetX = offsetXAnim.value
    val currentOffsetY = offsetYAnim.value
    val dismissThresholdPx = with(density) { 90.dp.toPx() }

    Box(
        modifier =
            modifier
                .offset { IntOffset(currentOffsetX.roundToInt(), currentOffsetY.roundToInt()) }
                .graphicsLayer {
                    rotationZ = (currentOffsetX / with(density) { 32.dp.toPx() }).coerceIn(-3f, 3f)
                    val totalDist =
                        kotlin.math.sqrt(
                            currentOffsetX * currentOffsetX + currentOffsetY * currentOffsetY
                        )
                    alpha = 1f - (totalDist / with(density) { 280.dp.toPx() }).coerceIn(0f, 0.55f)
                    cameraDistance = 16f
                    clip = false
                }
                .pointerInput(dismissThresholdPx) {
                    detectDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                if (
                                    abs(offsetXAnim.value) > dismissThresholdPx ||
                                        offsetYAnim.value < -dismissThresholdPx
                                ) {
                                    onDismiss?.invoke()
                                } else {
                                    launch {
                                        offsetXAnim.animateTo(
                                            targetValue = 0f,
                                            animationSpec =
                                                LiquidToastMotion.returnSpring(performance),
                                        )
                                    }
                                    launch {
                                        offsetYAnim.animateTo(
                                            targetValue = 0f,
                                            animationSpec =
                                                LiquidToastMotion.returnSpring(performance),
                                        )
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                launch {
                                    offsetXAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = LiquidToastMotion.returnSpring(performance),
                                    )
                                }
                                launch {
                                    offsetYAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = LiquidToastMotion.returnSpring(performance),
                                    )
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                val newRawX = offsetXAnim.value + dragAmount.x
                                val dampedX =
                                    if (abs(newRawX) > dismissThresholdPx) {
                                        val excess = abs(newRawX) - dismissThresholdPx
                                        val dampedExcess =
                                            60.dp.toPx() * (1f - exp(-excess / (80.dp.toPx())))
                                        newRawX.sign * (dismissThresholdPx + dampedExcess)
                                    } else {
                                        newRawX
                                    }
                                offsetXAnim.snapTo(dampedX)

                                val newRawY = offsetYAnim.value + dragAmount.y
                                val dampedY =
                                    if (newRawY > 0f) {
                                        48.dp.toPx() * (1f - exp(-newRawY / (64.dp.toPx())))
                                    } else if (newRawY < -dismissThresholdPx) {
                                        val excess = abs(newRawY) - dismissThresholdPx
                                        val dampedExcess =
                                            40.dp.toPx() * (1f - exp(-excess / (60.dp.toPx())))
                                        -(dismissThresholdPx + dampedExcess)
                                    } else {
                                        newRawY
                                    }
                                offsetYAnim.snapTo(dampedY)
                            }
                        },
                    )
                }
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Surface,
                    containerColor =
                        accentColor.copy(
                            alpha = (glassColors.neutralContainer.alpha * 1.5f).coerceAtMost(0.28f)
                        ),
                )
                .clickable(
                    interactionSource = null,
                    indication = null,
                    role = Role.Button,
                    onClick = { onDismiss?.invoke() },
                )
                .defaultMinSize(minHeight = 56.dp)
                .padding(start = 14.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier.size(36.dp)
                        // The toast shell already owns the refractive pass. The icon capsule is a
                        // restrained tint, avoiding a recursive-looking double lens.
                        .background(
                            accentColor.copy(
                                alpha = (glassColors.neutralContainer.alpha * 1.8f).coerceAtMost(1f)
                            ),
                            Capsule(),
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = data.message,
                    style = MaterialTheme.typography.labelLarge,
                    color = glassColors.content,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (data.subtitle != null) {
                    Text(
                        text = data.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.secondaryContent,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
