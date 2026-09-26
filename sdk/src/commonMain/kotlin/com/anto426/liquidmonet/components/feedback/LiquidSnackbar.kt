package com.anto426.liquidmonet.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import com.anto426.liquidmonet.motion.LiquidMotion
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.anto426.liquidmonet.components.buttons.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.LiquidIconButtonVariant
import com.anto426.liquidmonet.icons.LiquidIcons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

enum class LiquidSnackbarType {
    Info, Success, Warning, Error
}

/**
 * LiquidSnackbar - Floating Liquid Glass Notification Banner with Snell Lensing and Swipe-to-Dismiss.
 */
@Composable
fun LiquidSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    type: LiquidSnackbarType = LiquidSnackbarType.Info,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop()
) {
    val performance = LocalLiquidGlassPerformance.current
    val offsetX = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 96.dp.toPx() }

    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val contentColor = glassColors.content
    val icon: ImageVector = when (type) {
        LiquidSnackbarType.Info -> LiquidIcons.Info
        LiquidSnackbarType.Success -> LiquidIcons.Check
        LiquidSnackbarType.Warning -> LiquidIcons.Warning
        LiquidSnackbarType.Error -> LiquidIcons.Close
    }
    val accentColor = when (type) {
        LiquidSnackbarType.Info -> colorScheme.primary
        LiquidSnackbarType.Success -> glassColors.success
        LiquidSnackbarType.Warning -> glassColors.warning
        LiquidSnackbarType.Error -> glassColors.error
    }

    val shape = RoundedRectangle(16.dp)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    AnimatedVisibility(
        visible = visible,
        enter = LiquidMotion.snackbarEnter(performance),
        exit = LiquidMotion.snackbarExit(performance),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .graphicsLayer {
                    rotationZ = (offsetX.value / with(density) { 40.dp.toPx() }).coerceIn(-2.5f, 2.5f)
                    alpha = 1f - (abs(offsetX.value) / with(density) { 320.dp.toPx() }).coerceIn(0f, 0.45f)
                }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            offsetX.snapTo(offsetX.value + delta)
                        }
                    },
                    enabled = onDismiss != null,
                    onDragStopped = {
                        if (abs(offsetX.value) > dismissThresholdPx) {
                            onDismiss?.invoke()
                        }
                        offsetX.animateTo(
                            targetValue = 0f,
                            animationSpec = LiquidMotion.interactiveSpring(performance)
                        )
                    }
                )
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Surface,
                    containerColor = accentColor.copy(alpha = glassColors.neutralContainer.alpha)
                )
                .defaultMinSize(minHeight = 56.dp)
                .padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(Capsule())
                        .background(
                            accentColor.copy(
                                alpha = (glassColors.neutralContainer.alpha * 1.8f).coerceAtMost(1f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )

                if (actionLabel != null && onActionClick != null) {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clickable(
                                role = Role.Button,
                                onClick = onActionClick
                            )
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = accentColor
                        )
                    }
                }

                if (onDismiss != null) {
                    LiquidIconButton(
                        icon = LiquidIcons.Close,
                        contentDescription = "Chiudi",
                        onClick = onDismiss,
                        iconSize = 18.dp,
                        variant = LiquidIconButtonVariant.Ghost,
                        contentColor = glassColors.secondaryContent
                    )
                }
            }
        }
    }
}
