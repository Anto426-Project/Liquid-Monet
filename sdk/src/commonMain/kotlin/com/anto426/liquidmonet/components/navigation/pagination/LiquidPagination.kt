package com.anto426.liquidmonet.components.navigation.pagination

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.navigation.pagination.motion.LiquidPaginationMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.liquidNavigationSelectionLayer
import com.anto426.liquidmonet.motion.liquidNavigationSelectionScaleX
import com.anto426.liquidmonet.motion.liquidNavigationSelectionScaleY
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import kotlin.math.min
import kotlin.math.roundToInt

/** Compact page beads with one fluid, shared navigation selection. Page indices are zero-based. */
@Composable
fun LiquidPageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    onPageSelected: ((Int) -> Unit)? = null,
    dotSize: Dp = 8.dp,
    dotSpacing: Dp = 10.dp,
    backdropState: Backdrop = emptyBackdrop(),
) {
    LiquidInputNormalization.positive(pageCount, "LiquidPageIndicator pageCount")
    LiquidInputNormalization.positive(dotSize, "LiquidPageIndicator dotSize")
    LiquidInputNormalization.nonNegative(dotSpacing, "LiquidPageIndicator dotSpacing")
    val page = currentPage.coerceIn(0, pageCount - 1)
    val colors = LiquidGlassTheme.colors
    val primary = MaterialTheme.colorScheme.primary
    val callback = rememberUpdatedState(onPageSelected)
    val motion =
        LiquidPaginationMotion.rememberIndicator(
            page,
            pageCount,
            LocalLiquidGlassPerformance.current,
        )
    val visibleCount = min(pageCount, 7)
    var heldWindowStart by remember { mutableStateOf<Int?>(null) }
    val start = heldWindowStart ?: (page - visibleCount / 2).coerceIn(0, pageCount - visibleCount)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val density = LocalDensity.current
    val diameter = with(density) { dotSize.toPx() }
    val pitch = with(density) { (dotSize + dotSpacing).toPx() }
    val inset = with(density) { 16.dp.toPx() }
    val width = dotSize * visibleCount + dotSpacing * (visibleCount - 1) + 32.dp
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(
            Modifier.width(width)
                .height(44.dp)
                .liquidGlass(
                    resolveLiquidGlassBackdrop(backdropState),
                    Capsule(),
                    role = LiquidGlassRole.Navigation,
                    containerColor = colors.neutralContainer,
                )
                .semantics {
                    progressBarRangeInfo =
                        ProgressBarRangeInfo(
                            page.toFloat(),
                            0f..(pageCount - 1).toFloat(),
                            (pageCount - 2).coerceAtLeast(0),
                        )
                    stateDescription = "${page + 1} / $pageCount"
                    if (onPageSelected != null)
                        setProgress { value ->
                            if (!value.isFinite()) false
                            else {
                                callback.value?.invoke(
                                    value.roundToInt().coerceIn(0, pageCount - 1)
                                )
                                true
                            }
                        }
                }
                .pointerInput(pageCount, start, diameter, pitch, rtl, onPageSelected != null) {
                    if (callback.value == null) return@pointerInput
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        fun select(x: Float): Int {
                            val physicalX = if (rtl) size.width - x else x
                            return start +
                                ((physicalX - inset - diameter / 2f) / pitch)
                                    .roundToInt()
                                    .coerceIn(0, visibleCount - 1)
                        }
                        heldWindowStart = start
                        motion.press()
                        var last = select(down.position.x)
                        try {
                            callback.value?.invoke(last)
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (event.changes.count { it.pressed } > 1) break
                                if (change.pressed) {
                                    val next = select(change.position.x)
                                    if (next != last) {
                                        last = next
                                        callback.value?.invoke(next)
                                    }
                                    change.consume()
                                }
                            } while (change.pressed)
                        } finally {
                            motion.release()
                            heldWindowStart = null
                        }
                    }
                }
        ) {
            fun x(logical: Float) = if (rtl) size.width - logical else logical
            val centerY = size.height / 2f
            repeat(visibleCount) { index ->
                val isEdge =
                    (index == 0 && start > 0) ||
                        (index == visibleCount - 1 && start + visibleCount < pageCount)
                drawCircle(
                    colors.inactiveTrack,
                    diameter / 2f * if (isEdge) 0.7f else 1f,
                    Offset(x(inset + diameter / 2f + index * pitch), centerY),
                )
            }
            val position = (motion.value - start).coerceIn(0f, (visibleCount - 1).toFloat())
            val activeWidth = diameter * liquidNavigationSelectionScaleX(motion)
            val activeHeight = diameter * liquidNavigationSelectionScaleY(motion)
            val centerX = x(inset + diameter / 2f + position * pitch)
            drawRoundRect(
                primary,
                topLeft = Offset(centerX - activeWidth / 2f, centerY - activeHeight / 2f),
                size = Size(activeWidth, activeHeight),
                cornerRadius = CornerRadius(activeHeight / 2f),
            )
        }
    }
}

/**
 * Numbered page selector with one shared surface and a restrained moving selection. Pages are
 * one-based.
 */
@Composable
fun LiquidPagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    LiquidInputNormalization.positive(totalPages, "LiquidPagination totalPages")
    val page = currentPage.coerceIn(1, totalPages)
    val colors = LiquidGlassTheme.colors
    val count = min(totalPages, 5)
    val start = (page - count / 2).coerceIn(1, totalPages - count + 1)
    Row(
        modifier
            .heightIn(min = 52.dp)
            .liquidGlass(
                resolveLiquidGlassBackdrop(backdropState),
                Capsule(),
                role = LiquidGlassRole.Navigation,
                containerColor = colors.neutralContainer,
            )
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiquidIconButton(
            LiquidIcons.ChevronLeft,
            { onPageChange(page - 1) },
            enabled = page > 1,
            variant = LiquidIconButtonVariant.Ghost,
            size = 44.dp,
            contentDescription = "Pagina precedente",
        )
        BoxWithConstraints(Modifier.weight(1f)) {
            val slot = maxWidth / count
            val pillSize = minOf(36.dp, slot)
            val selectionMotion =
                LiquidPaginationMotion.rememberIndicator(
                    page - start,
                    count,
                    LocalLiquidGlassPerformance.current,
                )
            Box(
                Modifier.align(Alignment.CenterStart)
                    .offset {
                        IntOffset(
                            (slot.toPx() * selectionMotion.value + (slot - pillSize).toPx() / 2f)
                                .roundToInt(),
                            0,
                        )
                    }
                    .graphicsLayer { liquidNavigationSelectionLayer(selectionMotion) }
                    .size(pillSize)
                    .background(colors.selectedContainer, Capsule())
            )
            Row(
                Modifier.fillMaxWidth().then(selectionMotion.modifier),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(count) { index ->
                    val number = start + index
                    val selected = number == page
                    Box(
                        Modifier.weight(1f)
                            .height(44.dp)
                            .selectable(
                                selected,
                                interactionSource = remember(number) { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab,
                                onClick = { if (!selected) onPageChange(number) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            number.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            color =
                                if (selected) MaterialTheme.colorScheme.primary
                                else colors.secondaryContent,
                        )
                    }
                }
            }
        }
        LiquidIconButton(
            LiquidIcons.ChevronRight,
            { onPageChange(page + 1) },
            enabled = page < totalPages,
            variant = LiquidIconButtonVariant.Ghost,
            size = 44.dp,
            contentDescription = "Pagina successiva",
        )
    }
}
