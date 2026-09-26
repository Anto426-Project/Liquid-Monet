package com.anto426.liquidmonet.components.pickers.paletteselector

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.internal.LiquidGlassButton
import com.anto426.liquidmonet.components.pickers.paletteselector.motion.LiquidPaletteSelectorMotion
import com.anto426.liquidmonet.glass.LiquidGlassGroup
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

/** LiquidPaletteOption - Represents a color option in LiquidPaletteSelector. */
data class LiquidPaletteOption(
    val name: String,
    val color: Color,
)

/**
 * LiquidPaletteSelector - Pure Liquid Glass Palette & Color Selector Component.
 *
 * Renders an array of vibrant Monet colors encased inside real-time optical glass pills with
 * specular AGSL highlight reflections, lens refraction, and spring physics.
 */
@Composable
fun LiquidPaletteSelector(
    options: List<LiquidPaletteOption>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = "Tonalità Monet Glass",
    showBadge: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (showBadge && selectedIndex in options.indices) {
                    val activeOption = options[selectedIndex]
                    Box(
                        modifier =
                            Modifier.clip(Capsule())
                                .background(activeOption.color.copy(alpha = 0.20f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = activeOption.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = activeOption.color,
                        )
                    }
                }
            }
        }

        LiquidGlassGroup(
            modifier = Modifier.fillMaxWidth(),
            backdropState = effectiveBackdrop,
            shape = Capsule(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                options.forEachIndexed { index, option ->
                    val isSelected = selectedIndex == index
                    LiquidPaletteItem(
                        color = option.color,
                        isSelected = isSelected,
                        onClick = { onSelectIndex(index) },
                        backdropState = backdropState,
                        modifier = Modifier.weight(1f).height(48.dp),
                    )
                }
            }
        }
    }
}

/** LiquidPaletteItem - Individual Liquid Glass Pill for color selection. */
@Composable
private fun LiquidPaletteItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val performance = LocalLiquidGlassPerformance.current
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val scale by
        LiquidPaletteSelectorMotion.animateFloat(
            targetValue = if (isSelected) 1.04f else 1.0f,
            performance = performance,
            label = "paletteItemScale",
        )

    LiquidGlassButton(
        onClick = onClick,
        backdrop = effectiveBackdrop,
        tint = color,
        surfaceColor =
            if (isSelected) {
                color.copy(alpha = glassColors.selectedContainer.alpha)
            } else {
                color.copy(alpha = glassColors.neutralContainer.alpha)
            },
        modifier =
            modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                clip = false
            },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = remember(performance) { LiquidPaletteSelectorMotion.enter(performance) },
                exit = remember(performance) { LiquidPaletteSelectorMotion.exit(performance) },
            ) {
                Icon(
                    imageVector = LiquidIcons.Check,
                    contentDescription = "Selezionato",
                    tint = if (color.luminance() > 0.48f) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
