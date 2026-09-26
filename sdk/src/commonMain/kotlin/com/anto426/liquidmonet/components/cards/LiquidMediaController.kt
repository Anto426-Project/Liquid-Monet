package com.anto426.liquidmonet.components.cards

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.feedback.LiquidLinearProgressIndicator
import com.anto426.liquidmonet.components.internal.liquidControlLayer
import com.anto426.liquidmonet.components.internal.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.glass.runtime.animateBackground
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidMediaController - Radiant Optical Liquid Glass Media Player Widget with Tactile Bubble Controls.
 */
@Composable
fun LiquidMediaController(
    title: String,
    artist: String,
    isPlaying: Boolean,
    progress: Float,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentTime: String = "1:24",
    totalTime: String = "3:45",
    onContainerClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
    animatePlaybackGlow: Boolean = true,
    progressState: State<Float>? = null,
    currentTimeState: State<String>? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val primaryColor = colorScheme.primary
    val glassColors = LiquidGlassTheme.colors
    val performance = LocalLiquidGlassPerformance.current
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val shape = RoundedRectangle(28.dp)
    val albumShape = RoundedRectangle(16.dp)
    val controlShape = Capsule()

    // Optional state holders let frequently changing playback values be observed only by their
    // small visual consumers. The legacy Float/String parameters remain source-compatible.
    val internalProgressState = remember {
        mutableFloatStateOf(LiquidInputNormalization.unit(progress))
    }
    val effectiveProgressState: State<Float> = progressState ?: internalProgressState
    if (progressState == null) {
        LaunchedEffect(progress) {
            internalProgressState.floatValue = LiquidInputNormalization.unit(progress)
        }
    }
    val effectiveCurrentTimeState = currentTimeState ?: rememberUpdatedState(currentTime)

    // Vibrant infinite breathing pulse while music is playing.
    val playPulse = if (animatePlaybackGlow && isPlaying && performance.animateBackground) {
        val infiniteTransition = rememberInfiniteTransition(label = "MusicPlaybackPulse")
        val pulse by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "PlayPulse"
        )
        pulse
    } else {
        0f
    }

    // Touch feedback highlights
    val containerHighlight = rememberLiquidControlHighlight()
    val albumHighlight = rememberLiquidControlHighlight()
    val playHighlight = rememberLiquidControlHighlight()
    val prevHighlight = rememberLiquidControlHighlight()
    val nextHighlight = rememberLiquidControlHighlight()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                backdrop = effectiveBackdrop,
                shape = shape,
                role = LiquidGlassRole.Surface,
                layerBlock = if (onContainerClick != null) liquidControlLayerBlock(enabled, containerHighlight) else null
            )
            .then(
                if (onContainerClick != null) {
                    Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            enabled = enabled,
                            onClick = { onContainerClick.invoke() }
                        )
                        .liquidControlPressFeedback(enabled, containerHighlight)
                } else Modifier
            )
            .padding(18.dp)
    ) {
        CompositionLocalProvider(
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared
        ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Vibrant Glowing Album Artwork Bubble
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .liquidControlLayer(enabled, albumHighlight)
                        .liquidControlPressFeedback(
                            enabled = enabled,
                            interactiveHighlight = albumHighlight,
                            shape = albumShape,
                            drawHighlightOverlay = true
                        )
                        .drawBehind {
                            val glowRadius = size.maxDimension * 0.65f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.24f),
                                        primaryColor.copy(alpha = 0f)
                                    ),
                                    center = center,
                                    radius = glowRadius
                                ),
                                radius = glowRadius,
                                center = center
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            enabled = enabled,
                            onClick = onPlayPauseClick
                        )
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = albumShape,
                            role = LiquidGlassRole.Control,
                            containerColor = primaryColor.copy(alpha = glassColors.accentContainer.alpha)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LiquidIcons.MusicNote,
                        contentDescription = "Album",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    BasicText(
                        text = title,
                        style = TextStyle(
                            color = colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    BasicText(
                        text = artist,
                        style = TextStyle(
                            color = glassColors.secondaryContent,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Linear Progress Indicator
            LiquidMediaProgress(
                progressState = effectiveProgressState,
                backdropState = effectiveBackdrop
            )

            Spacer(modifier = Modifier.height(8.dp))

            LiquidMediaTimeLabels(
                currentTimeState = effectiveCurrentTimeState,
                totalTime = totalTime,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Playback Controls with Liquid Optical Bubble Pods
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Bubble Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .liquidControlLayer(enabled, prevHighlight)
                        .liquidControlPressFeedback(
                            enabled = enabled,
                            interactiveHighlight = prevHighlight,
                            shape = controlShape,
                            drawHighlightOverlay = true
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            enabled = enabled,
                            onClick = onPreviousClick
                        )
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = controlShape,
                            role = LiquidGlassRole.Control
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LiquidIcons.SkipPrevious,
                        contentDescription = "Precedente",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Vibrant Radiant Play / Pause Bubble with Blooming Radial Halo
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .liquidControlLayer(enabled, playHighlight)
                        .liquidControlPressFeedback(
                            enabled = enabled,
                            interactiveHighlight = playHighlight,
                            shape = controlShape,
                            drawHighlightOverlay = true
                        )
                        .drawBehind {
                            val activePulse = playPulse
                            val glowRadius = size.maxDimension * (0.65f + 0.15f * activePulse)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.28f + 0.10f * activePulse),
                                        primaryColor.copy(alpha = 0f)
                                    ),
                                    center = center,
                                    radius = glowRadius
                                ),
                                radius = glowRadius,
                                center = center
                            )
                        }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            enabled = enabled,
                            onClick = onPlayPauseClick
                        )
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = controlShape,
                            role = LiquidGlassRole.Control,
                            containerColor = primaryColor.copy(alpha = glassColors.selectedContainer.alpha)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Icon(
                            imageVector = LiquidIcons.Pause,
                            contentDescription = "Pausa",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    } else {
                        Icon(
                            imageVector = LiquidIcons.PlayArrow,
                            contentDescription = "Play",
                            tint = colorScheme.onSurface,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Next Bubble Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .liquidControlLayer(enabled, nextHighlight)
                        .liquidControlPressFeedback(
                            enabled = enabled,
                            interactiveHighlight = nextHighlight,
                            shape = controlShape,
                            drawHighlightOverlay = true
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            enabled = enabled,
                            onClick = onNextClick
                        )
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = controlShape,
                            role = LiquidGlassRole.Control
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = LiquidIcons.SkipNext,
                        contentDescription = "Successivo",
                        tint = colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        }
    }
}

@Composable
private fun LiquidMediaProgress(
    progressState: State<Float>,
    backdropState: Backdrop
) {
    val progress by progressState
    LiquidLinearProgressIndicator(
        progress = progress,
        backdropState = backdropState
    )
}

@Composable
private fun LiquidMediaTimeLabels(
    currentTimeState: State<String>,
    totalTime: String,
    color: Color
) {
    val currentTime by currentTimeState
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BasicText(
            text = currentTime,
            style = TextStyle(
                color = color.copy(alpha = 0.65f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
        BasicText(
            text = totalTime,
            style = TextStyle(
                color = color.copy(alpha = 0.65f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}
