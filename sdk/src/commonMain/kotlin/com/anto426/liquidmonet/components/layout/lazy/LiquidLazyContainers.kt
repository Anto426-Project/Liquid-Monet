package com.anto426.liquidmonet.components.layout.lazy

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.motion.rememberLiquidOverscrollEffect

/** Shared geometry for lazy containers hosting elastic Liquid controls. */
object LiquidLazyDefaults {
    /**
     * Space reserved inside every viewport so scale/spring overshoot is not clipped at its edge.
     */
    val OverflowPadding: Dp = 12.dp
}

/**
 * LazyColumn with a built-in drawing gutter for elastic Liquid controls.
 *
 * Compose lazy layouts clip at the viewport. Adding arbitrary z-index cannot cross that boundary,
 * therefore this container reserves the required space as real content padding on all four edges.
 */
@Composable
fun LiquidLazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical? = null,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    userScrollEnabled: Boolean = true,
    overflowPadding: Dp = LiquidLazyDefaults.OverflowPadding,
    liquidOverscroll: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    require(overflowPadding >= 0.dp) { "overflowPadding must not be negative" }
    val safePadding = contentPadding.withLiquidOverflow(overflowPadding)
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = safePadding,
        reverseLayout = reverseLayout,
        verticalArrangement =
            verticalArrangement ?: if (reverseLayout) Arrangement.Bottom else Arrangement.Top,
        horizontalAlignment = horizontalAlignment,
        userScrollEnabled = userScrollEnabled,
        overscrollEffect =
            rememberLiquidOverscrollEffect(
                Orientation.Vertical,
                userScrollEnabled && liquidOverscroll,
            ),
        content = content,
    )
}

/** LazyRow counterpart of [LiquidLazyColumn], with the same unclipped elastic gutter. */
@Composable
fun LiquidLazyRow(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    userScrollEnabled: Boolean = true,
    overflowPadding: Dp = LiquidLazyDefaults.OverflowPadding,
    liquidOverscroll: Boolean = true,
    content: LazyListScope.() -> Unit,
) {
    require(overflowPadding >= 0.dp) { "overflowPadding must not be negative" }
    val safePadding = contentPadding.withLiquidOverflow(overflowPadding)
    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = safePadding,
        reverseLayout = reverseLayout,
        horizontalArrangement =
            horizontalArrangement ?: if (reverseLayout) Arrangement.End else Arrangement.Start,
        verticalAlignment = verticalAlignment,
        userScrollEnabled = userScrollEnabled,
        overscrollEffect =
            rememberLiquidOverscrollEffect(
                Orientation.Horizontal,
                userScrollEnabled && liquidOverscroll,
            ),
        content = content,
    )
}

@Composable
private fun PaddingValues.withLiquidOverflow(overflow: Dp): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + overflow,
        top = calculateTopPadding() + overflow,
        end = calculateEndPadding(layoutDirection) + overflow,
        bottom = calculateBottomPadding() + overflow,
    )
}
