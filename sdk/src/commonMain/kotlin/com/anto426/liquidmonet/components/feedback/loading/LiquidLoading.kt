package com.anto426.liquidmonet.components.feedback.loading

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.feedback.loading.motion.LiquidLoadingMotion
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.feedback.shimmer.LiquidShimmerBox
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateFunctionalContent
import com.anto426.liquidmonet.motion.rememberLiquidInfiniteTransition
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlin.math.PI
import kotlin.math.sin

/** Visual style variant for unified LiquidLoading. */
enum class LiquidLoadingStyle {
    /** Sleek 360° Monet sweep gradient ring spinner. */
    Circular,
    /** Sinusoidal wavy free-floating liquid progress line. */
    Linear,
    /** 3 pulsating liquid glass crystal droplets. */
    Dots,
    /** Breathing luminous crystal orb with expanding radiant ripples. */
    Pulse,
    /** Optical liquid glass skeleton shimmer placeholder. */
    Shimmer,
    /** Full modal liquid glass dialog overlay with spinner and message. */
    Overlay,
}

/** Dimension scale for unified LiquidLoading. */
enum class LiquidLoadingSize {
    Small,
    Medium,
    Large,
}

/**
 * LiquidLoading / LiquidLoading - Single Unified Loading & Progress Component.
 *
 * Configurable with a single consistent API:
 *
 * @param style Visual loading style ([LiquidLoadingStyle.Circular], [LiquidLoadingStyle.Linear],
 *   [LiquidLoadingStyle.Dots], [LiquidLoadingStyle.Pulse], [LiquidLoadingStyle.Shimmer],
 *   [LiquidLoadingStyle.Overlay]).
 * @param progress When non-null (0.0f..1.0f), renders determinate progress; when null, runs
 *   infinite indeterminate animation.
 * @param size Dimension scale ([LiquidLoadingSize.Small], [LiquidLoadingSize.Medium],
 *   [LiquidLoadingSize.Large]).
 * @param message Optional text message displayed alongside/beneath the indicator.
 * @param tint Custom color tint (harmonized with Monet theme by default).
 * @param backdropState Optical glass backdrop reference.
 */
@Composable
fun LiquidLoading(
    modifier: Modifier = Modifier,
    style: LiquidLoadingStyle = LiquidLoadingStyle.Circular,
    progress: Float? = null,
    size: LiquidLoadingSize = LiquidLoadingSize.Medium,
    message: String? = null,
    tint: Color = Color.Unspecified,
    shape: Shape = RoundedRectangle(16.dp),
    backdropState: Backdrop = emptyBackdrop(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val primaryColor = if (tint.isSpecified) tint else colorScheme.primary
    val secondaryColor = colorScheme.tertiary
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    when (style) {
        LiquidLoadingStyle.Circular -> {
            val indicatorSize: Dp =
                when (size) {
                    LiquidLoadingSize.Small -> 24.dp
                    LiquidLoadingSize.Medium -> 40.dp
                    LiquidLoadingSize.Large -> 56.dp
                }
            val strokeWidth: Dp =
                when (size) {
                    LiquidLoadingSize.Small -> 2.5.dp
                    LiquidLoadingSize.Medium -> 4.dp
                    LiquidLoadingSize.Large -> 5.dp
                }

            if (message != null) {
                Row(
                    modifier = modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    LiquidCircularProgressIndicator(
                        progress = progress,
                        modifier = Modifier.size(indicatorSize),
                        indicatorSize = indicatorSize,
                        strokeWidth = strokeWidth,
                        progressColor = primaryColor,
                        backdropState = effectiveBackdrop,
                    )
                    Text(
                        text = message,
                        style =
                            if (size == LiquidLoadingSize.Small) {
                                MaterialTheme.typography.bodySmall
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                        color = glassColors.content,
                    )
                }
            } else {
                LiquidCircularProgressIndicator(
                    progress = progress,
                    modifier = modifier.size(indicatorSize),
                    indicatorSize = indicatorSize,
                    strokeWidth = strokeWidth,
                    progressColor = primaryColor,
                    backdropState = effectiveBackdrop,
                )
            }
        }

        LiquidLoadingStyle.Linear -> {
            val heightDp =
                when (size) {
                    LiquidLoadingSize.Small -> 4.dp
                    LiquidLoadingSize.Medium -> 6.dp
                    LiquidLoadingSize.Large -> 8.dp
                }
            Column(modifier = modifier.fillMaxWidth()) {
                if (message != null) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = glassColors.content,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                LiquidLinearProgressIndicator(
                    progress = progress,
                    height = heightDp,
                    progressColor = primaryColor,
                    backdropState = effectiveBackdrop,
                )
            }
        }

        LiquidLoadingStyle.Dots -> {
            LiquidLoadingDots(
                modifier = modifier,
                loadingSize = size,
                primaryColor = primaryColor,
                secondaryColor = secondaryColor,
                message = message,
                backdropState = effectiveBackdrop,
            )
        }

        LiquidLoadingStyle.Pulse -> {
            LiquidLoadingPulse(
                modifier = modifier,
                loadingSize = size,
                primaryColor = primaryColor,
                message = message,
                backdropState = effectiveBackdrop,
            )
        }

        LiquidLoadingStyle.Shimmer -> {
            LiquidShimmerBox(
                modifier = modifier,
                shape = shape,
                backdropState = effectiveBackdrop,
            )
        }

        LiquidLoadingStyle.Overlay -> {
            LiquidLoadingOverlay(
                modifier = modifier,
                message = message ?: "Caricamento in corso...",
                primaryColor = primaryColor,
                backdropState = effectiveBackdrop,
            )
        }
    }
}

/** 3 Pulsating liquid glass crystal droplets. */
@Composable
private fun LiquidLoadingDots(
    modifier: Modifier = Modifier,
    loadingSize: LiquidLoadingSize,
    primaryColor: Color,
    secondaryColor: Color,
    message: String?,
    backdropState: Backdrop,
) {
    val animate = LocalLiquidGlassPerformance.current.animateFunctionalContent
    val dotDiameter: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 6.dp
            LiquidLoadingSize.Medium -> 8.dp
            LiquidLoadingSize.Large -> 10.dp
        }
    val spacing: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 4.dp
            LiquidLoadingSize.Medium -> 6.dp
            LiquidLoadingSize.Large -> 8.dp
        }

    val infiniteTransition =
        if (animate) {
            rememberLiquidInfiniteTransition(label = "DotsBounceAnim")
        } else {
            null
        }
    val dot1Offset by
        if (infiniteTransition != null) {
            LiquidLoadingMotion.rememberDot1(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "dot1",
            )
        } else {
            remember { mutableFloatStateOf(0.5f) }
        }
    val dot2Offset by
        if (infiniteTransition != null) {
            LiquidLoadingMotion.rememberDot2(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "dot2",
            )
        } else {
            remember { mutableFloatStateOf(0.5f) }
        }
    val dot3Offset by
        if (infiniteTransition != null) {
            LiquidLoadingMotion.rememberDot3(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "dot3",
            )
        } else {
            remember { mutableFloatStateOf(0.5f) }
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier =
                Modifier.size(
                    width = dotDiameter * 3 + spacing * 2,
                    height = dotDiameter * 2.2f,
                ),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(
                    dot1Offset to primaryColor,
                    dot2Offset to primaryColor,
                    dot3Offset to secondaryColor,
                )
                .forEach { (phase, dotColor) ->
                    Box(
                        modifier =
                            Modifier.size(dotDiameter)
                                .graphicsLayer {
                                    val scale = 0.85f + 0.25f * phase
                                    scaleX = scale
                                    scaleY = scale
                                    translationY = -dotDiameter.toPx() * 0.45f * phase
                                }
                                .liquidGlass(
                                    backdrop = backdropState,
                                    shape = Capsule(),
                                    role = LiquidGlassRole.Control,
                                    containerColor = dotColor.copy(alpha = 0.08f + 0.10f * phase),
                                )
                    )
                }
        }
        if (message != null) {
            Text(
                text = message,
                style =
                    when (loadingSize) {
                        LiquidLoadingSize.Small -> MaterialTheme.typography.bodySmall
                        LiquidLoadingSize.Medium -> MaterialTheme.typography.bodyMedium
                        LiquidLoadingSize.Large -> MaterialTheme.typography.titleMedium
                    },
                color = LiquidGlassTheme.colors.content,
            )
        }
    }
}

/** Breathing luminous crystal orb with expanding radiant ripples. */
@Composable
private fun LiquidLoadingPulse(
    modifier: Modifier = Modifier,
    loadingSize: LiquidLoadingSize,
    primaryColor: Color,
    message: String?,
    backdropState: Backdrop,
) {
    val animate = LocalLiquidGlassPerformance.current.animateFunctionalContent
    val totalSize: Dp =
        when (loadingSize) {
            LiquidLoadingSize.Small -> 32.dp
            LiquidLoadingSize.Medium -> 48.dp
            LiquidLoadingSize.Large -> 64.dp
        }

    val infiniteTransition =
        if (animate) {
            rememberLiquidInfiniteTransition(label = "PulseAnim")
        } else {
            null
        }
    val pulseProg by
        if (infiniteTransition != null) {
            LiquidLoadingMotion.rememberPulseProgress(
                transition = infiniteTransition,
                initialValue = 0f,
                targetValue = 1f,
                label = "PulseProg",
            )
        } else {
            remember { mutableFloatStateOf(0.25f) }
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(totalSize),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val maxR = this.size.minDimension / 2f
                val coreR = maxR * 0.40f

                drawCircle(
                    color = primaryColor.copy(alpha = (1f - pulseProg) * 0.35f),
                    radius = coreR + (maxR - coreR) * pulseProg,
                    center = center,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
            Box(
                modifier =
                    Modifier.size(totalSize * 0.40f)
                        .graphicsLayer {
                            val scale = 0.92f + 0.12f * sin(pulseProg * 2 * PI.toFloat())
                            scaleX = scale
                            scaleY = scale
                        }
                        .liquidGlass(
                            backdrop = backdropState,
                            shape = Capsule(),
                            role = LiquidGlassRole.Control,
                            containerColor = LiquidGlassTheme.colors.accentContainer,
                        )
            )
        }
        if (message != null) {
            Text(
                text = message,
                style =
                    when (loadingSize) {
                        LiquidLoadingSize.Small -> MaterialTheme.typography.bodySmall
                        LiquidLoadingSize.Medium -> MaterialTheme.typography.bodyMedium
                        LiquidLoadingSize.Large -> MaterialTheme.typography.titleMedium
                    },
                color = LiquidGlassTheme.colors.content,
            )
        }
    }
}

/** Full modal liquid glass dialog overlay. */
@Composable
private fun LiquidLoadingOverlay(
    modifier: Modifier,
    message: String,
    primaryColor: Color,
    backdropState: Backdrop,
) {
    Box(
        modifier =
            modifier
                .liquidGlass(
                    backdrop = backdropState,
                    shape = RoundedRectangle(28.dp),
                    role = LiquidGlassRole.Dialog,
                    containerColor = LiquidGlassTheme.colors.neutralContainer,
                )
                .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LiquidCircularProgressIndicator(
                progress = null,
                modifier = Modifier.size(38.dp),
                progressColor = primaryColor,
                backdropState = backdropState,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
