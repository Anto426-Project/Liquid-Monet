package com.anto426.liquidmonet.components.cards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.graphics.TransformOrigin
import kotlin.math.PI
import kotlin.math.sin
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.display.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.internal.liquidControlLayerBlock
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassPresets
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidMotion
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidAccordionItem - Expandable Optical Liquid Glass Bubble Card with Snell Lens Refraction.
 *
 * Provides full-surface tactile hit-testing (no dead margins), continuous organic squircle
 * curvature morphing, luminous prismatic glass bubble rim highlights, Monet chromatic
 * dilation, and dedicated fluid bubble pods for leading and trailing controls.
 */
@Composable
fun LiquidAccordionItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable () -> Unit
) {
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val isLightSurface = colorScheme.surface.luminance() > 0.5f
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    // Smooth fluid expansion progress driven by the navbar droplet bouncy spring model
    val bubbleSpring = remember(performance) {
        LiquidMotion.spring<Float>(
            performance = performance,
            dampingRatio = 0.58f,
            stiffness = 300f
        )
    }

    val expansionProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = bubbleSpring,
        label = "accordionBubbleProgress"
    )

    // Fluid volume conservation: dynamic squash & stretch like the navbar droplet
    val bubbleDeformation = sin(expansionProgress.coerceIn(0f, 1f) * PI.toFloat())
    val bubbleSquashX = 1f - (bubbleDeformation * 0.016f)
    val bubbleStretchY = 1f + (bubbleDeformation * 0.022f)
    val podPulseScale = 1f + (bubbleDeformation * 0.08f)

    // Tactile rotating chevron bubble pod with lively spring snap and overshoot
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = LiquidMotion.spring(
            performance = performance,
            dampingRatio = 0.54f,
            stiffness = 320f
        ),
        label = "accordionChevronRotation"
    )

    val chevronColor by animateColorAsState(
        targetValue = if (isExpanded) glassColors.content else glassColors.secondaryContent,
        animationSpec = LiquidMotion.tween(performance, 200),
        label = "accordionChevronColor"
    )

    // Standard harmonious card shape consistent with LiquidCard
    val cardShape = remember { RoundedRectangle(24.dp) }

    // Pristine glass rim reflection (crystalline specular highlight, neutral without heavy color)
    val glassRimBrush = remember(isLightSurface) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isLightSurface) 0.35f else 0.18f),
                Color.White.copy(alpha = if (isLightSurface) 0.10f else 0.05f),
                Color.White.copy(alpha = if (isLightSurface) 0.22f else 0.10f),
                Color.White.copy(alpha = if (isLightSurface) 0.06f else 0.03f)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    // Unified tactile highlight for the entire card: active across the WHOLE component in BOTH collapsed and extended states!
    val cardHighlight = rememberLiquidControlHighlight()
    val cardLayerBlock = liquidControlLayerBlock(
        enabled = true,
        interactiveHighlight = cardHighlight
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = bubbleSquashX
                scaleY = bubbleStretchY
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
            .then(
                if (cardLayerBlock != null) {
                    Modifier.graphicsLayer(cardLayerBlock)
                } else {
                    Modifier
                }
            )
            .liquidGlass(
                backdrop = effectiveBackdrop,
                shape = cardShape,
                role = LiquidGlassRole.Surface,
                containerColor = null,
                preset = LiquidGlassPresets.Navigation,
                layerBlock = cardLayerBlock
            )
            .border(
                width = 1.dp,
                brush = glassRimBrush,
                shape = cardShape
            )
            .liquidControlPressFeedback(
                enabled = true,
                interactiveHighlight = cardHighlight,
                shape = cardShape,
                drawHighlightOverlay = true
            )
            .then(
                if (!isExpanded) {
                    Modifier.clickable(
                        interactionSource = null,
                        indication = null,
                        role = Role.Button,
                        onClick = { onExpandedChange(true) }
                    )
                } else {
                    Modifier
                }
            )
    ) {
        CompositionLocalProvider(
            LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
            LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header bar: spans full width edge-to-edge without dead margin
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isExpanded) {
                                Modifier.clickable(
                                    interactionSource = null,
                                    indication = null,
                                    role = Role.Button,
                                    onClick = { onExpandedChange(false) }
                                )
                            } else {
                                Modifier
                            }
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (leadingIcon != null) {
                            // Crystalline glass pod for leading icon with bubble pulse
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .graphicsLayer {
                                        scaleX = podPulseScale
                                        scaleY = podPulseScale
                                    }
                                    .clip(RoundedRectangle(12.dp))
                                    .background(glassColors.neutralContainer)
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = if (isLightSurface) 0.25f else 0.12f),
                                        shape = RoundedRectangle(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = leadingIcon,
                                    contentDescription = null,
                                    tint = glassColors.content,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = colorScheme.onSurface
                            )
                            if (subtitle != null) {
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glassColors.secondaryContent
                                )
                            }
                        }
                    }

                    // Chevron Glass Pod with Navbar-grade bubble spring rotation and pulse
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .graphicsLayer {
                                val chevronBubbleScale = 1f + (bubbleDeformation * 0.12f)
                                scaleX = chevronBubbleScale
                                scaleY = chevronBubbleScale
                            }
                            .clip(CircleShape)
                            .background(glassColors.neutralContainer)
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = if (isLightSurface) 0.22f else 0.10f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = LiquidIcons.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Comprimi" else "Espandi",
                            tint = chevronColor,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(chevronRotation)
                        )
                    }
                }

                // Smooth Cascading Content Chamber with Dropdown Menu Pop-out Physics
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically(
                        animationSpec = LiquidMotion.spring(
                            performance = performance,
                            dampingRatio = LiquidMotion.MenuBounceDampingRatio,
                            stiffness = LiquidMotion.MenuBounceStiffness
                        ),
                        expandFrom = Alignment.Top
                    ) + scaleIn(
                        animationSpec = LiquidMotion.spring(
                            performance = performance,
                            dampingRatio = LiquidMotion.MenuBounceDampingRatio,
                            stiffness = LiquidMotion.MenuBounceStiffness
                        ),
                        initialScale = 0.90f,
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    ) + fadeIn(
                        animationSpec = LiquidMotion.tween(
                            performance = performance,
                            durationMillis = LiquidMotion.FastDurationMillis,
                            easing = LiquidMotion.EmphasizedDecelerate
                        )
                    ),
                    exit = shrinkVertically(
                        animationSpec = LiquidMotion.snappySpring(performance),
                        shrinkTowards = Alignment.Top
                    ) + scaleOut(
                        animationSpec = LiquidMotion.tween(
                            performance = performance,
                            durationMillis = LiquidMotion.FastDurationMillis,
                            easing = LiquidMotion.EmphasizedAccelerate
                        ),
                        targetScale = 0.92f,
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    ) + fadeOut(
                        animationSpec = LiquidMotion.tween(
                            performance = performance,
                            durationMillis = LiquidMotion.FastDurationMillis,
                            easing = LiquidMotion.EmphasizedAccelerate
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        LiquidHorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))
                        content()
                    }
                }
            }
        }
    }
}

/**
 * LiquidExpandableCard - Optical Liquid Glass Expandable Card with Bubble Refraction.
 * Modern semantic alias for [LiquidAccordionItem].
 */
@Composable
fun LiquidExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable () -> Unit
) = LiquidAccordionItem(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    leadingIcon = leadingIcon,
    isExpanded = isExpanded,
    onExpandedChange = onExpandedChange,
    backdropState = backdropState,
    content = content
)
