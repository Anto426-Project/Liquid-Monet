package com.anto426.liquidmonet.components.selection

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import com.anto426.liquidmonet.motion.LiquidMotion
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.liquidControlLayer
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/**
 * LiquidRatingBar - Minimal, sleek Material 3 Star Rating Selector.
 * Features clean organic spring bounce on selection and Material Theme colors.
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
    backdropState: Backdrop = emptyBackdrop()
) {
    val performance = LocalLiquidGlassPerformance.current
    val safeMaxStars = LiquidInputNormalization.positive(maxStars, "LiquidRatingBar maxStars")
    LiquidInputNormalization.positive(starSize, "LiquidRatingBar starSize")
    val safeRating = rating.coerceIn(0, safeMaxStars)
    val colorScheme = MaterialTheme.colorScheme
    val resolvedActiveColor = if (activeColor.isSpecified) activeColor else colorScheme.primary
    val resolvedInactiveColor = if (inactiveColor.isSpecified) {
        inactiveColor
    } else {
        LiquidGlassTheme.colors.inactiveTrack
    }
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Row(
        modifier = modifier
            .liquidGlass(
                backdrop = effectiveBackdrop,
                shape = Capsule(),
                role = LiquidGlassRole.Control,
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..safeMaxStars) {
            val isFilled = i <= safeRating
            val starHighlight = rememberLiquidControlHighlight()

            val animatedColor by animateColorAsState(
                targetValue = if (isFilled) resolvedActiveColor else resolvedInactiveColor,
                animationSpec = LiquidMotion.tween(performance, 200),
                label = "starColor_$i"
            )

            val animatedScale by animateFloatAsState(
                targetValue = if (isFilled) 1.05f else 0.95f,
                animationSpec = LiquidMotion.snappySpring(performance),
                label = "starScale_$i"
            )

            Box(
                modifier = Modifier
                    .size(starSize + 8.dp)
                    .liquidControlLayer(enabled, starHighlight)
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                    }
                    .liquidControlPressFeedback(
                        enabled = enabled,
                        interactiveHighlight = starHighlight,
                        shape = Capsule(),
                        drawHighlightOverlay = true
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        enabled = enabled,
                        onClick = { onRatingChanged(i) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = LiquidIcons.Star,
                    contentDescription = "Valutazione $i su $maxStars",
                    tint = animatedColor,
                    modifier = Modifier.size(starSize)
                )
            }
        }
    }
}
