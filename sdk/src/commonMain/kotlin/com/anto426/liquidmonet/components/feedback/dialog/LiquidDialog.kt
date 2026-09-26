package com.anto426.liquidmonet.components.feedback.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.feedback.dialog.motion.LiquidDialogMotion
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassModalOverlayState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.motion.rememberLiquidPredictiveBackState
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun LiquidDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    text: String? = null,
    backdropState: Backdrop = emptyBackdrop(),
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    scrimColor: Color? = null,
    containerColor: Color? = null,
    content: (@Composable () -> Unit)? = null,
) {
    val modalOverlayState = LocalLiquidGlassModalOverlayState.current
    val effectiveBackdrop = LocalLiquidGlassContentBackdrop.current ?: backdropState
    val dialogContent: @Composable () -> Unit =
        content
            ?: {
                val contentColor = LocalContentColor.current
                BasicText(
                    text = text.orEmpty(),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    style =
                        TextStyle(
                            color = contentColor.copy(alpha = 0.78f),
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        ),
                    maxLines = 6,
                )
            }
    val hostedContent: @Composable () -> Unit = {
        LiquidDialogLayer(
            onDismissRequest = onDismissRequest,
            title = title,
            modifier = modifier,
            backdropState = effectiveBackdrop,
            confirmButton = confirmButton,
            dismissButton = dismissButton,
            scrimColor = scrimColor,
            containerColor = containerColor,
            content = dialogContent,
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
private fun LiquidDialogLayer(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    backdropState: Backdrop,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    scrimColor: Color? = null,
    containerColor: Color? = null,
    content: @Composable () -> Unit,
) {
    val performance = LocalLiquidGlassPerformance.current
    val effectiveBackdrop = LocalLiquidGlassContentBackdrop.current ?: backdropState
    val dimColor = scrimColor ?: LiquidGlassTheme.colors.scrim

    val visibilityState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    var isDismissing by remember { mutableStateOf(false) }

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
    val predictiveScale = 1f - predictiveBackProgress * 0.10f

    AnimatedVisibility(
        visibleState = visibilityState,
        enter = remember(performance) { LiquidDialogMotion.enter(performance) },
        exit = remember(performance) { LiquidDialogMotion.exit(performance) },
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
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier.graphicsLayer {
                            scaleX = predictiveScale
                            scaleY = predictiveScale
                            translationY = (predictiveBackProgress * 12.dp.toPx())
                            alpha = 1f - predictiveBackProgress * 0.35f
                        }
                        .animateEnterExit(
                            enter =
                                remember(performance) {
                                    LiquidDialogMotion.panelEnter(performance)
                                },
                            exit =
                                remember(performance) { LiquidDialogMotion.panelExit(performance) },
                        )
            ) {
                LiquidDialogPanel(
                    title = title,
                    backdrop = effectiveBackdrop,
                    modifier =
                        modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {},
                        ),
                    containerColor = containerColor,
                    confirmButton = confirmButton,
                    dismissButton = dismissButton,
                    content = content,
                )
            }
        }
    }
}

/** Single liquid-glass panel used by every dialog presentation. */
@Composable
private fun LiquidDialogPanel(
    title: String,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    confirmButton: (@Composable () -> Unit)? = null,
    dismissButton: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val contentColor =
        LiquidGlassDefaults.contentColorFor(
            containerColor,
            MaterialTheme.colorScheme,
        )
    val shape = remember { RoundedRectangle(32.dp) }

    Column(
        modifier =
            modifier
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .widthIn(min = 280.dp, max = 360.dp)
                .fillMaxWidth()
                .liquidGlass(
                    backdrop = backdrop,
                    shape = shape,
                    role = LiquidGlassRole.Dialog,
                    containerColor = containerColor,
                )
                .padding(horizontal = 20.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CompositionLocalProvider(
            LocalLiquidGlassContentBackdrop provides backdrop,
            LocalContentColor provides contentColor,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
        ) {
            // Centered Title
            BasicText(
                text = title,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                style =
                    TextStyle(
                        color = contentColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    ),
            )

            // Centered Body Content
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }

            // Action Buttons Row
            if (dismissButton != null || confirmButton != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dismissButton != null) {
                        Box(modifier = Modifier.weight(1f)) { dismissButton() }
                    }
                    if (confirmButton != null) {
                        Box(modifier = Modifier.weight(1f)) { confirmButton() }
                    }
                }
            }
        }
    }
}
