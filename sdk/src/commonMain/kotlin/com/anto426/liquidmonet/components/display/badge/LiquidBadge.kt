package com.anto426.liquidmonet.components.display.badge

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule

/** Canonical translucent badge supporting dot, numeric count, label, or custom content slot. */
@Composable
fun LiquidBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    text: String? = if (count != null) (if (count > 99) "99+" else count.toString()) else null,
    shape: Shape = Capsule(),
    containerColor: Color? = null,
    contentColor: Color? = null,
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 5.dp, vertical = 1.dp),
    minimumSize: Dp = 18.dp,
    dotSize: Dp = 8.dp,
    border: BorderStroke? = null,
    textStyle: TextStyle = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold),
    contentAlignment: Alignment = Alignment.Center,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    count?.let { LiquidInputNormalization.nonNegative(it, "LiquidBadge count") }
    LiquidInputNormalization.nonNegative(minimumSize, "LiquidBadge minimumSize")
    LiquidInputNormalization.positive(dotSize, "LiquidBadge dotSize")
    val fill = containerColor ?: MaterialTheme.colorScheme.error
    val foreground =
        contentColor ?: LiquidGlassDefaults.contentColorFor(fill, MaterialTheme.colorScheme)
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)
    val isDot = text == null && content == null
    Box(
        modifier =
            modifier
                .then(
                    if (isDot) Modifier.size(dotSize)
                    else Modifier.defaultMinSize(minimumSize, minimumSize)
                )
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Control,
                    containerColor = fill,
                )
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .then(if (isDot) Modifier else Modifier.padding(contentPadding)),
        contentAlignment = contentAlignment,
    ) {
        CompositionLocalProvider(LocalContentColor provides foreground) {
            if (content != null) content()
            else if (text != null) BasicText(text, style = textStyle.copy(color = foreground))
        }
    }
}

/**
 * LiquidBadgedBox - Anchor container to display a [LiquidBadge] attached to any icon or component.
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
