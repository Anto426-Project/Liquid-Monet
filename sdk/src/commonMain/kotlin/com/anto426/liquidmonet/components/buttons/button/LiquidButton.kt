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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.buttons.button.motion.LiquidButtonMotion
import com.anto426.liquidmonet.components.feedback.progressbar.LiquidCircularProgressIndicator
import com.anto426.liquidmonet.components.internal.LiquidControlDefaults
import com.anto426.liquidmonet.components.internal.LiquidGlassButton
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

/**
 * LiquidButton / LiquidButton - Single Unified Liquid Glass Button Component.
 *
 * Fully configurable through its styling parameters:
 *
 * @param variant Visual style ([LiquidButtonVariant.Primary], [LiquidButtonVariant.Secondary],
 *   [LiquidButtonVariant.Tonal], [LiquidButtonVariant.Glass], [LiquidButtonVariant.Outlined],
 *   [LiquidButtonVariant.Text]).
 * @param size Button dimension scale ([LiquidButtonSize.Small], [LiquidButtonSize.Medium],
 *   [LiquidButtonSize.Large]).
 * @param enabled Whether the button is enabled and interactive.
 * @param isLoading When true, replaces content with a smooth progress indicator.
 * @param leadingIcon Optional leading icon composable.
 * @param trailingIcon Optional trailing icon composable.
 * @param shape Corner shape (defaults to Capsule).
 * @param tint Custom color tint override (harmonized with Monet by default).
 * @param backdropState Optical glass backdrop reference.
 */
@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String? = null,
    backdropState: Backdrop = emptyBackdrop(),
    enabled: Boolean = true,
    isLoading: Boolean = false,
    variant: LiquidButtonVariant = LiquidButtonVariant.Primary,
    size: LiquidButtonSize = LiquidButtonSize.Medium,
    shape: Shape = LiquidControlDefaults.shape,
    tint: Color = Color.Unspecified,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit = {
        if (text != null) Text(text)
    },
) {
    val isInteractive = enabled && !isLoading
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val targetTint =
        when {
            tint.isSpecified ->
                tint.copy(alpha = tint.alpha.coerceAtMost(glassColors.selectedContainer.alpha))
            variant == LiquidButtonVariant.Primary -> glassColors.accentContainer
            variant == LiquidButtonVariant.Secondary ->
                colorScheme.secondary.copy(alpha = glassColors.accentContainer.alpha * 0.72f)
            variant == LiquidButtonVariant.Tonal ->
                colorScheme.tertiary.copy(alpha = glassColors.accentContainer.alpha * 0.80f)
            else -> Color.Transparent
        }
    val targetContentColor =
        when (variant) {
            LiquidButtonVariant.Outlined,
            LiquidButtonVariant.Text -> colorScheme.primary
            LiquidButtonVariant.Primary,
            LiquidButtonVariant.Secondary,
            LiquidButtonVariant.Tonal,
            LiquidButtonVariant.Glass -> glassColors.content
        }.let { contentColor ->
            if (enabled) contentColor else glassColors.disabledContent
        }

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

    val height: Dp =
        when (size) {
            LiquidButtonSize.Small -> 40.dp
            LiquidButtonSize.Medium -> 48.dp
            LiquidButtonSize.Large -> 56.dp
        }

    val contentPadding: PaddingValues =
        when (size) {
            LiquidButtonSize.Small -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            LiquidButtonSize.Medium -> PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            LiquidButtonSize.Large -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        }

    val textStyle =
        when (size) {
            LiquidButtonSize.Small -> MaterialTheme.typography.labelMedium
            LiquidButtonSize.Medium -> MaterialTheme.typography.labelLarge
            LiquidButtonSize.Large -> MaterialTheme.typography.titleSmall
        }

    val border: BorderStroke? =
        if (variant == LiquidButtonVariant.Outlined) {
            BorderStroke(
                1.dp,
                if (enabled) glassColors.focusIndicator
                else glassColors.outline.copy(alpha = 0.45f),
            )
        } else null

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    LiquidGlassButton(
        onClick = onClick,
        backdrop = effectiveBackdrop,
        modifier = modifier.defaultMinSize(minWidth = 64.dp),
        isInteractive = isInteractive,
        enabled = enabled,
        shape = shape,
        height = height,
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
                            if (leadingIcon != null) {
                                leadingIcon()
                            }
                            content()
                            if (trailingIcon != null) {
                                trailingIcon()
                            }
                        }
                    }
                }
            }
        }
    }
}
