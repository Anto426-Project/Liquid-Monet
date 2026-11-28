package com.anto426.liquidmonet.components.feedback.shimmer

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
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
 * LiquidShimmerBox - Organic Liquid Glass Skeleton Placeholder with Size-Proportional Sweeping
 * Optical Caustic Wave.
 */
@Composable
fun LiquidShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedRectangle(16.dp),
    backdrop: Backdrop = emptyBackdrop(),
    tint: Color = Color.Unspecified,
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val isLight = colorScheme.surface.luminance() > 0.5f
    val accentColor = if (tint.isSpecified) tint else colorScheme.primary
    val baseFill =
        if (tint.isSpecified) {
            lerp(glassColors.neutralContainer, accentColor.copy(alpha = if (isLight) 0.14f else 0.18f), 0.55f)
        } else {
            colorScheme.onSurface.copy(alpha = if (isLight) 0.075f else 0.11f)
        }
    val causticShoulder = accentColor.copy(alpha = if (isLight) 0.16f else 0.22f)
    val causticPeak =
        if (tint.isSpecified) {
            lerp(accentColor, Color.White, if (isLight) 0.52f else 0.65f)
                .copy(alpha = if (isLight) 0.56f else 0.42f)
        } else {
            lerp(colorScheme.primary, Color.White, if (isLight) 0.78f else 0.85f)
                .copy(alpha = if (isLight) 0.58f else 0.26f)
        }
    val topSheen = Color.White.copy(alpha = if (isLight) 0.30f else 0.10f)

    val animateShimmer = LocalLiquidGlassPerformance.current.animateFunctionalContent
    val infiniteTransition =
        if (animateShimmer) rememberLiquidInfiniteTransition(label = "glassShimmer") else null
    val translateAnim by
        if (infiniteTransition != null) {
            LiquidShimmerMotion.rememberShimmerTranslation(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "shimmerTranslation",
            )
        } else {
            remember { mutableFloatStateOf(0.45f) }
        }

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)

    Box(
        modifier =
            modifier
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = shape,
                    role = LiquidGlassRole.Surface,
                )
                .clip(shape)
                .drawWithCache {
                    val minBandPx = 88.dp.toPx()
                    val bandWidth = (size.width * 0.78f).coerceAtLeast(minBandPx)
                    val diagonalDrop = (size.height * 0.45f).coerceAtLeast(12.dp.toPx())
                    val topSheenBrush =
                        Brush.verticalGradient(
                            0.0f to topSheen,
                            0.35f to Color.Transparent,
                            startY = 0f,
                            endY = size.height.coerceAtLeast(1f),
                        )
                    onDrawBehind {
                        // Observe animation only in draw phase so skeletons never recompose or
                        // rebuild their glass modifier on every frame.
                        drawRect(color = baseFill)

                        val startX = -bandWidth + (size.width + bandWidth * 2f) * translateAnim
                        val endX = startX + bandWidth
                        drawRect(
                            brush =
                                Brush.linearGradient(
                                    0.0f to Color.Transparent,
                                    0.24f to causticShoulder,
                                    0.50f to causticPeak,
                                    0.76f to causticShoulder,
                                    1.0f to Color.Transparent,
                                    start = Offset(startX, 0f),
                                    end = Offset(endX, diagonalDrop),
                                )
                        )
                        drawRect(brush = topSheenBrush)
                    }
                }
    )
}
