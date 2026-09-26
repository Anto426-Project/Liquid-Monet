package com.anto426.liquidmonet.components.inputs.searchbar

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.inputs.searchbar.motion.LiquidSearchBarMotion
import com.anto426.liquidmonet.components.inputs.splitclearbutton.LiquidSplitClearButton
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import kotlin.math.PI
import kotlin.math.sin

/**
 * LiquidSearchBar - Optical Liquid Glass Search Bar. Features Snell lens refraction, Monet dynamic
 * chromatic luminescence, smooth focus glow, live query clearing, and 360-degree omnidirectional
 * liquid touch dynamics.
 */
@Composable
fun LiquidSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Cerca...",
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
    onClose: (() -> Unit)? = null,
    onSearch: ((String) -> Unit)? = null,
) {
    val performance = LocalLiquidGlassPerformance.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val colorScheme = MaterialTheme.colorScheme
    val contentColor = colorScheme.onSurface
    val primaryColor = colorScheme.primary
    val glassColors = LiquidGlassTheme.colors
    val placeholderColor = glassColors.secondaryContent
    val shape = Capsule()

    val animatedBorderColor by
        LiquidSearchBarMotion.animateColor(
            targetValue =
                if (isFocused) {
                    glassColors.focusIndicator
                } else Color.Transparent,
            performance = performance,
            label = "searchBorderColor",
        )

    // Fluid recoil on the main capsule as the water drop separates
    val dropletSeparating = query.isNotEmpty()
    val recoilProgress by
        LiquidSearchBarMotion.animateFloat(
            targetValue = if (dropletSeparating) 1f else 0f,
            performance = performance,
            label = "searchBarRecoil",
        )
    val recoilWave = sin(recoilProgress.coerceIn(0f, 1f) * PI).toFloat()
    val mainBodySquashX = 1f - recoilWave * 0.018f
    val mainBodyStretchY = 1f + recoilWave * 0.012f

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    Row(
        modifier = modifier.fillMaxWidth().height(54.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier =
                Modifier.weight(1f)
                    .height(54.dp)
                    .graphicsLayer {
                        scaleX = mainBodySquashX
                        scaleY = mainBodyStretchY
                        transformOrigin =
                            TransformOrigin(
                                1f,
                                0.5f,
                            ) // Pinches slightly at right contact point as the drop detaches
                    }
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Control,
                    )
                    .border(width = 1.dp, color = animatedBorderColor, shape = shape),
            enabled = enabled,
            singleLine = true,
            textStyle =
                TextStyle(
                    color = contentColor,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Medium,
                ),
            cursorBrush = SolidColor(primaryColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch?.invoke(query) }),
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Icon(
                        imageVector = LiquidIcons.Search,
                        contentDescription = "Cerca",
                        tint = if (isFocused) primaryColor else contentColor.copy(alpha = 0.70f),
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholderText,
                                style =
                                    TextStyle(
                                        color = placeholderColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )

        LiquidSplitClearButton(
            visible = query.isNotEmpty(),
            onClick = { onQueryChange("") },
            size = 54.dp,
            backdropState = effectiveBackdrop,
            enabled = enabled,
        )
    }
}
