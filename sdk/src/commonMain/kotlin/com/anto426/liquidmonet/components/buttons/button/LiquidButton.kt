package com.anto426.liquidmonet.components.buttons.button

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.motion.LiquidButtonMotion
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.internal.LiquidControlDefaults
import com.anto426.liquidmonet.components.internal.LiquidGlassButton
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LiquidGlassQualityTier
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop

/**
 * LiquidButton - Single canonical Liquid Glass button component.
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: LiquidButtonVariant = LiquidButtonVariant.Primary,
    size: LiquidButtonSize = LiquidButtonSize.Medium,
    shape: Shape = LiquidControlDefaults.shape,
    tint: Color = Color.Unspecified,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues =
        when (size) {
            LiquidButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            LiquidButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            LiquidButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        },
    minimumHeight: Dp =
        when (size) {
            LiquidButtonSize.Small -> 40.dp
            LiquidButtonSize.Medium -> 48.dp
            LiquidButtonSize.Large -> 56.dp
        },
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    textStyle: TextStyle =
        when (size) {
            LiquidButtonSize.Small -> MaterialTheme.typography.labelMedium
            LiquidButtonSize.Medium -> MaterialTheme.typography.labelLarge
            LiquidButtonSize.Large -> MaterialTheme.typography.titleSmall
        },
    border: BorderStroke? =
        if (variant == LiquidButtonVariant.Outlined) {
            BorderStroke(
                1.dp,
                if (enabled) LiquidGlassTheme.colors.focusIndicator
                else LiquidGlassTheme.colors.outline.copy(alpha = 0.45f),
            )
        } else {
            null
        },
    content: @Composable RowScope.() -> Unit = {
        if (text != null) {
            Text(
                text = text,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    },
) {
    LiquidInputNormalization.positive(minimumHeight, "LiquidButton minimumHeight")
    val isInteractive = enabled && !isLoading
    val performance = LocalLiquidGlassPerformance.current
    val isEssential =
        (performance.opticalQualityTier ?: performance.qualityTier) ==
            LiquidGlassQualityTier.MINIMAL
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val targetTint =
        when {
            containerColor.isSpecified -> containerColor
            tint.isSpecified ->
                tint.copy(alpha = tint.alpha.coerceAtMost(glassColors.selectedContainer.alpha))
            variant == LiquidButtonVariant.Primary -> glassColors.accentContainer
            variant == LiquidButtonVariant.Secondary ->
                colorScheme.secondary.copy(alpha = glassColors.accentContainer.alpha * 0.72f)
            variant == LiquidButtonVariant.Tonal ->
                colorScheme.tertiary.copy(alpha = glassColors.accentContainer.alpha * 0.80f)
            variant == LiquidButtonVariant.Glass && isEssential -> glassColors.neutralContainer
            else -> Color.Transparent
        }
    val targetContentColor =
        (if (contentColor.isSpecified) contentColor
            else
                when (variant) {
                    LiquidButtonVariant.Outlined,
                    LiquidButtonVariant.Text -> colorScheme.primary
                    LiquidButtonVariant.Primary,
                    LiquidButtonVariant.Secondary,
                    LiquidButtonVariant.Tonal,
                    LiquidButtonVariant.Glass -> glassColors.content
                })
            .let { resolved -> if (enabled) resolved else glassColors.disabledContent }

    val animatedContentColor by
        LiquidButtonMotion.animateColor(
            targetValue = targetContentColor,
            performance = performance,
            label = "buttonContentColor",
        )

    val animatedTint by
        LiquidButtonMotion.animateColor(
            targetValue = targetTint,
            performance = performance,
            label = "buttonTint",
        )

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop)
    LiquidGlassButton(
        onClick = onClick,
        backdrop = effectiveBackdrop,
        modifier = modifier.defaultMinSize(minWidth = 64.dp),
        isInteractive = isInteractive,
        enabled = enabled,
        shape = shape,
        height = minimumHeight,
        contentPadding = contentPadding,
        border = border,
        tint = animatedTint,
    ) {
        CompositionLocalProvider(LocalContentColor provides animatedContentColor) {
            ProvideTextStyle(value = textStyle) {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = LiquidButtonMotion.contentTransition(performance),
                    label = "buttonLoadingTransition",
                ) { loading ->
                    if (loading) {
                        val spinnerSize = if (size == LiquidButtonSize.Small) 16.dp else 20.dp
                        LiquidCircularProgressIndicator(
                            modifier = Modifier.size(spinnerSize),
                            indicatorSize = spinnerSize,
                            strokeWidth = if (size == LiquidButtonSize.Small) 2.2.dp else 2.8.dp,
                            progressColor = animatedContentColor,
                            progress = null,
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (leadingIcon != null) leadingIcon()
                            content()
                            if (trailingIcon != null) trailingIcon()
                        }
                    }
                }
            }
        }
    }
}
