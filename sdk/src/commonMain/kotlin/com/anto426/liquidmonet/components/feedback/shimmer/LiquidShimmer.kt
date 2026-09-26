package com.anto426.liquidmonet.components.feedback.shimmer

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.feedback.shimmer.motion.LiquidShimmerMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateFunctionalContent
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidShimmerBox - Loading Skeleton Placeholder in Liquid Glass with Sweeping Refraction Wave.
 */
@Composable
fun LiquidShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedRectangle(16.dp),
    backdropState: Backdrop = emptyBackdrop(),
) {
    val highlightColor = LiquidGlassTheme.colors.neutralContainer
    val animateShimmer = LocalLiquidGlassPerformance.current.animateFunctionalContent

    val infiniteTransition =
        if (animateShimmer) rememberLiquidInfiniteTransition(label = "glassShimmer") else null
    val translateAnim by
        if (infiniteTransition != null) {
            LiquidShimmerMotion.rememberShimmerTranslation(
                transition = infiniteTransition,
                initialValue = -1f,
                targetValue = 2f,
                label = "shimmerTranslation",
            )
        } else {
            remember { mutableFloatStateOf(0.5f) }
        }

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Box(
        modifier =
            modifier
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Surface,
                )
                .drawWithCache {
                    val colors = listOf(Color.Transparent, highlightColor, Color.Transparent)
                    onDrawBehind {
                        // Observe animation in drawing, not composition: skeletons should not
                        // rebuild their glass modifier and render effects on every animation frame.
                        drawRect(
                            Brush.linearGradient(
                                colors = colors,
                                start = Offset(translateAnim * 400f, 0f),
                                end = Offset((translateAnim + 1f) * 400f, 400f),
                            )
                        )
                    }
                }
    )
}
