package com.anto426.liquidmonet.components.cards.controlcenterslider

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.controlcenterslider.motion.LiquidControlCenterSliderMotion
import com.anto426.liquidmonet.components.cards.internal.LiquidControlCenterDefaults
import com.anto426.liquidmonet.components.cards.internal.LiquidControlCenterIcon
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.selection.slider.requireLiquidSliderRange
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import kotlin.math.roundToInt

/** Quick-setting slider with a restrained fill and accessible, direction-aware input. */
@Composable
fun LiquidControlCenterSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    requireLiquidSliderRange(valueRange)
    val colors = LiquidGlassTheme.colors
    val scheme = MaterialTheme.colorScheme
    val safeValue = value.takeIf { it.isFinite() }?.coerceIn(valueRange) ?: valueRange.start
    val span = valueRange.endInclusive - valueRange.start
    val fraction = (safeValue - valueRange.start) / span
    val fill =
        LiquidControlCenterSliderMotion.animateFloat(
            fraction,
            LocalLiquidGlassPerformance.current,
            "controlCenterFill",
        )
    val callback = rememberUpdatedState(onValueChange)
    val currentValue = rememberUpdatedState(safeValue)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val highlight = rememberLiquidControlHighlight()
    val layer = liquidControlLayerBlock(enabled, highlight)
    val shape = LiquidControlCenterDefaults.shape
    val fillColor = if (enabled) colors.accentContainer else colors.neutralContainer
    Row(
        modifier
            .heightIn(min = LiquidControlCenterDefaults.minimumHeight)
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = title
                progressBarRangeInfo = ProgressBarRangeInfo(safeValue, valueRange)
                stateDescription = "${(fraction * 100).roundToInt()}%"
                if (!enabled) disabled()
                if (enabled)
                    setProgress { requested ->
                        val target =
                            requested.takeIf { it.isFinite() }?.coerceIn(valueRange)
                                ?: return@setProgress false
                        if (target == currentValue.value) false
                        else {
                            callback.value(target)
                            true
                        }
                    }
            }
            .pointerInput(enabled, valueRange, rtl) {
                if (enabled)
                    detectTapGestures { position ->
                        val physical = (position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                        callback.value(
                            valueRange.start + (if (rtl) 1f - physical else physical) * span
                        )
                    }
            }
            .pointerInput(enabled, valueRange, rtl) {
                if (enabled)
                    detectHorizontalDragGestures(
                        onDragStart = { position ->
                            val physical =
                                (position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                            callback.value(
                                valueRange.start + (if (rtl) 1f - physical else physical) * span
                            )
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val physical =
                                (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                            callback.value(
                                valueRange.start + (if (rtl) 1f - physical else physical) * span
                            )
                        },
                    )
            }
            .then(if (enabled) highlight.gestureModifier else Modifier)
            .then(if (layer != null) Modifier.graphicsLayer(layer) else Modifier)
            .liquidGlass(
                resolveLiquidGlassBackdrop(backdropState),
                shape,
                role = LiquidGlassRole.Control,
                containerColor = colors.neutralContainer,
            )
            .drawBehind {
                val width = size.width * fill.value.coerceIn(0f, 1f)
                drawRoundRect(
                    fillColor,
                    topLeft = Offset(if (rtl) size.width - width else 0f, 0f),
                    size = Size(width, size.height),
                    cornerRadius = CornerRadius(24.dp.toPx()),
                )
            }
            .then(if (enabled) highlight.modifier(clipShape = shape) else Modifier)
            .padding(LiquidControlCenterDefaults.spacing),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LiquidControlCenterDefaults.spacing),
    ) {
        LiquidControlCenterIcon(
            icon,
            if (enabled) scheme.primary else colors.neutralContainer,
            if (enabled) scheme.onPrimary else colors.disabledContent,
        )
        Text(
            title,
            Modifier.weight(1f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) colors.content else colors.disabledContent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            "${(fraction * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) colors.secondaryContent else colors.disabledContent,
        )
    }
}
