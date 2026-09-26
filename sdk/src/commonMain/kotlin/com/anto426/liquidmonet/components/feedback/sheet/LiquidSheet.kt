package com.anto426.liquidmonet.components.feedback.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.feedback.sheet.motion.LiquidSheetMotion
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassModalOverlayState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.animateLiquidFloat
import com.anto426.liquidmonet.motion.rememberLiquidPredictiveBackState
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.abs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Floating bottom sheet with refractive glass and a restrained modal tone. */
@Composable
fun LiquidSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    backdropState: Backdrop = emptyBackdrop(),
    containerColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val modalOverlayState = LocalLiquidGlassModalOverlayState.current
    val effectiveBackdrop = LocalLiquidGlassContentBackdrop.current ?: backdropState
    val hostedContent: @Composable () -> Unit = {
        LiquidSheetLayer(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            title = title,
            subtitle = subtitle,
            backdropState = effectiveBackdrop,
            containerColor = containerColor,
            content = content,
        )
    }

    if (modalOverlayState == null) {
        hostedContent()
        return
    }

    val overlayKey = remember { Any() }
    val latestHostedContent by rememberUpdatedState(hostedContent)
    DisposableEffect(modalOverlayState, overlayKey) {
        modalOverlayState.show(overlayKey) { latestHostedContent() }
        onDispose { modalOverlayState.remove(overlayKey) }
    }
}

@Composable
private fun LiquidSheetLayer(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    title: String?,
    subtitle: String?,
    backdropState: Backdrop,
    containerColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val performance = LocalLiquidGlassPerformance.current
    val effectiveBackdrop = LocalLiquidGlassContentBackdrop.current ?: backdropState
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val contentColor = LiquidGlassDefaults.contentColorFor(containerColor, colorScheme)
    val dimColor = glassColors.scrim
    val shape = remember { RoundedRectangle(32.dp) }
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val dismissDistancePx = with(density) { 120.dp.toPx() }
    val dismissVelocityPx = with(density) { 800.dp.toPx() }
    val upwardResistanceLimitPx = with(density) { 24.dp.toPx() }

    val visibilityState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    var isDismissing by remember { mutableStateOf(false) }
    var dismissThresholdReached by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val animateDismiss: () -> Unit = dismiss@{
        if (isDismissing) return@dismiss
        isDismissing = true
        visibilityState.targetState = false
        coroutineScope.launch {
            snapshotFlow { visibilityState.isIdle && !visibilityState.currentState }.first { it }
            onDismissRequest()
        }
    }

    val predictiveBack =
        rememberLiquidPredictiveBackState(
            onPredictiveBack = animateDismiss,
            enabled = visibilityState.targetState,
        )
    val predictiveBackProgress = predictiveBack.progress
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val predictiveScale = 1f - predictiveBackProgress * 0.08f

    AnimatedVisibility(
        visibleState = visibilityState,
        enter = remember(performance) { LiquidSheetMotion.enter(performance) },
        exit = remember(performance) { LiquidSheetMotion.exit(performance) },
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        dimColor.copy(alpha = dimColor.alpha * (1f - predictiveBackProgress * 0.3f))
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = animateDismiss,
                    ),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier =
                    Modifier.graphicsLayer {
                            val predictiveOffset = predictiveBackProgress * 120.dp.toPx()
                            val dragProgress =
                                (dragOffsetY / dismissDistancePx).coerceIn(-0.20f, 1.20f)
                            val deformation = abs(dragProgress)
                            translationY = dragOffsetY + predictiveOffset
                            transformOrigin = TransformOrigin(0.5f, 1f)
                            scaleX = predictiveScale * (1f + deformation * 0.025f)
                            scaleY =
                                predictiveScale * (1f - dragProgress.coerceAtLeast(0f) * 0.018f)
                            alpha = 1f - predictiveBackProgress * 0.25f
                        }
                        .animateEnterExit(
                            enter =
                                remember(performance) {
                                    LiquidSheetMotion.surfaceEnter(performance)
                                },
                            exit =
                                remember(performance) {
                                    LiquidSheetMotion.surfaceExit(performance)
                                },
                        )
            ) {
                Column(
                    modifier =
                        modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .draggable(
                                orientation = Orientation.Vertical,
                                state =
                                    rememberDraggableState { delta ->
                                        val rawOffset = dragOffsetY + delta
                                        val resistedOffset =
                                            when {
                                                rawOffset < 0f ->
                                                    (rawOffset * 0.12f).coerceAtLeast(
                                                        -upwardResistanceLimitPx
                                                    )
                                                rawOffset > dismissDistancePx ->
                                                    dismissDistancePx +
                                                        (rawOffset - dismissDistancePx) * 0.28f
                                                else -> rawOffset
                                            }
                                        val reached = resistedOffset >= dismissDistancePx
                                        if (reached && !dismissThresholdReached) {
                                            hapticFeedback.performLiquidHaptic(
                                                LiquidHapticCue.Threshold
                                            )
                                        }
                                        dismissThresholdReached = reached
                                        // Pointer deltas are already delivered on the UI thread.
                                        // Writing
                                        // state directly preserves their order; launching one
                                        // coroutine
                                        // per pixel can replay stale offsets and make the sheet
                                        // jitter.
                                        dragOffsetY = resistedOffset
                                    },
                                onDragStopped = { velocity ->
                                    if (
                                        dragOffsetY > dismissDistancePx ||
                                            velocity > dismissVelocityPx
                                    ) {
                                        animateDismiss()
                                    } else {
                                        dismissThresholdReached = false
                                        coroutineScope.launch {
                                            animateLiquidFloat(
                                                initialValue = dragOffsetY,
                                                targetValue = 0f,
                                                animationSpec =
                                                    LiquidSheetMotion.returnSpring(performance),
                                            ) { value, _ ->
                                                dragOffsetY = value
                                            }
                                        }
                                    }
                                },
                            )
                            .liquidGlass(
                                backdrop = effectiveBackdrop,
                                shape = shape,
                                role = LiquidGlassRole.Sheet,
                                containerColor = containerColor,
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {},
                            )
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    CompositionLocalProvider(
                        LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                        LocalContentColor provides contentColor,
                        LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
                    ) {
                        Box(
                            modifier =
                                Modifier.align(Alignment.CenterHorizontally)
                                    .padding(bottom = 12.dp)
                                    .width(40.dp)
                                    .height(5.dp)
                                    .liquidGlass(
                                        backdrop = effectiveBackdrop,
                                        shape = Capsule(),
                                        role = LiquidGlassRole.Control,
                                        containerColor = contentColor.copy(alpha = 0.16f),
                                    )
                        )

                        if (!title.isNullOrBlank()) {
                            BasicText(
                                text = title,
                                style =
                                    TextStyle(
                                        color = contentColor,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                            )
                        }
                        if (!subtitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            BasicText(
                                text = subtitle,
                                style =
                                    TextStyle(
                                        color = contentColor.copy(alpha = 0.78f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                            )
                        }
                        if (!title.isNullOrBlank() || !subtitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        content()
                    }
                }
            }
        }
    }
}
