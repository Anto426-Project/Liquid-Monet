package com.anto426.liquidmonet.components.display.badge

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/** LiquidBadge - Translucent Liquid Glass Badge Indicator. */
@Composable
fun LiquidBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    text: String? = if (count != null) (if (count > 99) "99+" else count.toString()) else null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    count?.let { LiquidInputNormalization.nonNegative(it, "LiquidBadge count") }
    val defaultFill = MaterialTheme.colorScheme.error
    val effectiveContainerColor = containerColor ?: defaultFill
    val effectiveContentColor =
        contentColor
            ?: LiquidGlassDefaults.contentColorFor(
                effectiveContainerColor,
                MaterialTheme.colorScheme,
            )
    val shape = Capsule()
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    if (text == null) {
        // Dot Badge
        Box(
            modifier =
                modifier
                    .size(8.dp)
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Control,
                        containerColor = effectiveContainerColor,
                    )
        )
    } else {
        // Pill Badge with text / count
        Box(
            modifier =
                modifier
                    .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Control,
                        containerColor = effectiveContainerColor,
                    )
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = text,
                style =
                    TextStyle(
                        color = effectiveContentColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }
    }
}

/**
 * LiquidBadgedBox - Anchor container to display an LiquidBadge attached to any icon or component.
 */
@Composable
fun LiquidBadgedBox(
    badge: @Composable BoxScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        propagateMinConstraints = false,
    ) {
        content()
        Box(modifier = Modifier.align(Alignment.TopEnd).offset(x = 6.dp, y = (-4).dp)) {
            badge()
        }
    }
}
