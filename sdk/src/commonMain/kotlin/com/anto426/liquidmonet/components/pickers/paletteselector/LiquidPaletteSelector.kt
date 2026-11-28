package com.anto426.liquidmonet.components.pickers.paletteselector

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.pickers.paletteselector.motion.LiquidPaletteSelectorMotion
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.monet.blend
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/** LiquidPaletteOption - Represents a color option in LiquidPaletteSelector. */
@Immutable
data class LiquidPaletteOption(
    val name: String,
    val color: Color,
)

/**
 * LiquidPaletteSelector - Vibrant Seed & Palette Selector.
 *
 * Displays full-saturation circular jewel swatches with an animated selection ring, adaptive
 * checkmark contrast, accessible touch targets, and a legible active color badge.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidPaletteSelector(
    options: List<LiquidPaletteOption>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = "Tonalit\u00E0 Monet Glass",
    showBadge: Boolean = true,
    backdrop: Backdrop = emptyBackdrop(),
) {
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title != null || showBadge) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                    )
                }
                if (showBadge && selectedIndex in options.indices) {
                    val activeOption = options[selectedIndex]
                    Row(
                        modifier =
                            Modifier.clip(Capsule())
                                .background(activeOption.color.copy(alpha = 0.16f))
                                .border(
                                    width = 1.dp,
                                    color = activeOption.color.copy(alpha = 0.36f),
                                    shape = Capsule(),
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier =
                                Modifier.size(8.dp)
                                    .clip(CircleShape)
                                    .background(activeOption.color)
                        )
                        Text(
                            text = activeOption.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        if (options.size <= 6) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            ) {
                options.forEachIndexed { index, option ->
                    LiquidPaletteSwatch(
                        option = option,
                        isSelected = selectedIndex == index,
                        onClick = {
                            if (selectedIndex != index) {
                                hapticFeedback.performLiquidHaptic(LiquidHapticCue.Selection)
                                onSelectIndex(index)
                            }
                        },
                    )
                }
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            ) {
                options.forEachIndexed { index, option ->
                    LiquidPaletteSwatch(
                        option = option,
                        isSelected = selectedIndex == index,
                        onClick = {
                            if (selectedIndex != index) {
                                hapticFeedback.performLiquidHaptic(LiquidHapticCue.Selection)
                                onSelectIndex(index)
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidPaletteSwatch(
    option: LiquidPaletteOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val scale by
        LiquidPaletteSelectorMotion.animateFloat(
            targetValue = if (isSelected) 1.06f else 1.0f,
            performance = performance,
            label = "paletteSwatchScale",
        )
    val ringColor = if (isSelected) colorScheme.onSurface else Color.Transparent
    val jewelBrush =
        remember(option.color) {
            Brush.verticalGradient(
                listOf(
                    blend(option.color, Color.White, 0.20f),
                    option.color,
                    blend(option.color, Color.Black, 0.14f),
                )
            )
        }
    val checkTint = if (option.color.luminance() > 0.48f) Color.Black else Color.White

    Box(
        modifier =
            modifier
                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    clip = false
                }
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                )
                .semantics {
                    selected = isSelected
                    contentDescription = option.name
                },
        contentAlignment = Alignment.Center,
    ) {
        // Outer selection ring with a clean breathing gap around the color jewel
        Box(
            modifier =
                Modifier.size(44.dp)
                    .border(
                        width = if (isSelected) 2.dp else 0.dp,
                        color = ringColor,
                        shape = CircleShape,
                    )
                    .padding(if (isSelected) 4.dp else 2.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize()
                        .clip(CircleShape)
                        .background(jewelBrush)
                        .border(
                            width = 0.85.dp,
                            color = Color.White.copy(alpha = 0.34f),
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = isSelected,
                    enter = remember(performance) { LiquidPaletteSelectorMotion.enter(performance) },
                    exit = remember(performance) { LiquidPaletteSelectorMotion.exit(performance) },
                ) {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = checkTint,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
