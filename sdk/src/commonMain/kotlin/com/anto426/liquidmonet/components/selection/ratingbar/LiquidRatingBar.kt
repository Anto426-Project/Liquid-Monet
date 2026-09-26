package com.anto426.liquidmonet.components.selection.ratingbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/**
 * LiquidRatingBar - Minimal, sleek Material 3 Star Rating Selector. Features clean organic spring
 * bounce on selection and Material Theme colors.
 */
@Composable
fun LiquidRatingBar(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 28.dp,
    activeColor: Color = Color.Unspecified,
    inactiveColor: Color = Color.Unspecified,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val safeMaxStars = LiquidInputNormalization.positive(maxStars, "LiquidRatingBar maxStars")
    LiquidInputNormalization.positive(starSize, "LiquidRatingBar starSize")
    val safeRating = rating.coerceIn(0, safeMaxStars)
    val colorScheme = MaterialTheme.colorScheme
    val resolvedActiveColor = if (activeColor.isSpecified) activeColor else colorScheme.primary
    val resolvedInactiveColor =
        if (inactiveColor.isSpecified) {
            inactiveColor
        } else {
            LiquidGlassTheme.colors.inactiveTrack
        }
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Row(
        modifier =
            modifier
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = Capsule(),
                    role = LiquidGlassRole.Control,
                )
                .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..safeMaxStars) {
            val isFilled = i <= safeRating

            LiquidIconButton(
                icon = LiquidIcons.Star,
                contentDescription = "Valutazione $i su $safeMaxStars",
                onClick = { onRatingChanged(i) },
                enabled = enabled,
                size = starSize + 8.dp,
                iconSize = starSize,
                contentColor = if (isFilled) resolvedActiveColor else resolvedInactiveColor,
                variant = LiquidIconButtonVariant.Ghost,
            )
        }
    }
}
