package com.anto426.liquidmonet.components.feedback.tooltip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.anto426.liquidmonet.components.feedback.tooltip.motion.LiquidTooltipMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import kotlinx.coroutines.delay

/** LiquidTooltip - Contextual Glass Tooltip Popup anchored to components. */
@Composable
fun LiquidTooltipBox(
    tooltip: String,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable () -> Unit,
) {
    val performance = LocalLiquidGlassPerformance.current
    var showTooltip by remember { mutableStateOf(false) }

    val colorScheme = MaterialTheme.colorScheme
    val contentColor = colorScheme.onSurface
    val shape = Capsule()
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Box(
        modifier =
            modifier.pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        showTooltip = true
                    }
                )
            }
    ) {
        content()

        if (showTooltip) {
            LaunchedEffect(Unit) {
                delay(2200)
                showTooltip = false
            }

            Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -60),
                onDismissRequest = { showTooltip = false },
                properties = PopupProperties(focusable = false),
            ) {
                AnimatedVisibility(
                    visible = showTooltip,
                    enter = remember(performance) { LiquidTooltipMotion.enter(performance) },
                    exit = remember(performance) { LiquidTooltipMotion.exit(performance) },
                ) {
                    Box(
                        modifier =
                            Modifier.liquidGlass(
                                    backdrop = effectiveBackdrop,
                                    shape = shape,
                                    role = LiquidGlassRole.Control,
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        BasicText(
                            text = tooltip,
                            style =
                                TextStyle(
                                    color = contentColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                ),
                        )
                    }
                }
            }
        }
    }
}
