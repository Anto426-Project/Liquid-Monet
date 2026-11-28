package com.anto426.liquidmonet.components.inputs.pinpad

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.inputs.pinpad.motion.LiquidPinPadMotion
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.glass.LiquidGlassLegibility
import com.anto426.liquidmonet.glass.LocalLiquidGlassLegibility
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop

/** Pure PIN entry transitions for [LiquidPinPad]. */
internal object LiquidPinPadMath {
    fun appendDigit(current: String, digit: Char, maxLength: Int): String {
        if (maxLength <= 0 || !digit.isDigit()) return current
        val sanitized = current.filter(Char::isDigit).take(maxLength)
        if (sanitized.length >= maxLength) return sanitized
        return sanitized + digit
    }

    fun deleteLast(current: String): String =
        if (current.isEmpty()) current else current.dropLast(1)
}

/**
 * LiquidPinPad - Tactile 3x4 Liquid Glass numeric keypad with optional PIN dot indicators and
 * leading accessory action (e.g. biometrics or quick clear).
 */
@Composable
fun LiquidPinPad(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 6,
    showIndicators: Boolean = true,
    isError: Boolean = false,
    enabled: Boolean = true,
    keySize: Dp = 72.dp,
    keySpacing: Dp = 16.dp,
    onComplete: ((String) -> Unit)? = null,
    leadingActionIcon: ImageVector? = null,
    leadingActionContentDescription: String? = null,
    onLeadingActionClick: (() -> Unit)? = null,
    backspaceContentDescription: String = "Cancella",
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
) {
    val safeMaxLength = LiquidInputNormalization.positive(maxLength, "LiquidPinPad maxLength")
    LiquidInputNormalization.positive(keySize, "LiquidPinPad keySize")
    LiquidInputNormalization.nonNegative(keySpacing, "LiquidPinPad keySpacing")

    val performance = LocalLiquidGlassPerformance.current
    val glassColors = LiquidGlassTheme.colors
    val colorScheme = MaterialTheme.colorScheme
    val hapticFeedback = LocalHapticFeedback.current
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val sanitizedValue = remember(value, safeMaxLength) { value.filter(Char::isDigit).take(safeMaxLength) }

    val rows = remember {
        listOf(
            listOf('1', '2', '3'),
            listOf('4', '5', '6'),
            listOf('7', '8', '9'),
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(keySpacing),
    ) {
        if (showIndicators) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(safeMaxLength) { index ->
                    val filled = index < sanitizedValue.length
                    val dotScale by
                        LiquidPinPadMotion.animateDotScale(
                            filled = filled,
                            performance = performance,
                            label = "pinDotScale_$index",
                        )
                    val targetColor =
                        when {
                            isError -> glassColors.error
                            filled -> colorScheme.primary
                            else -> glassColors.inactiveTrack
                        }
                    val dotColor by
                        LiquidPinPadMotion.animateColor(
                            targetValue = targetColor,
                            performance = performance,
                            label = "pinDotColor_$index",
                        )
                    Box(
                        modifier =
                            Modifier.size(14.dp)
                                .graphicsLayer {
                                    scaleX = dotScale
                                    scaleY = dotScale
                                }
                                .clip(CircleShape)
                                .background(dotColor)
                                .border(
                                    width = 1.dp,
                                    color =
                                        if (isError) glassColors.error.copy(alpha = 0.65f)
                                        else glassColors.outline,
                                    shape = CircleShape,
                                )
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        rows.forEach { digits ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(keySpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                digits.forEach { digit ->
                    LiquidPinDigitKey(
                        digit = digit,
                        size = keySize,
                        enabled = enabled && sanitizedValue.length < safeMaxLength,
                        backdrop = effectiveBackdrop,
                        onClick = {
                            hapticFeedback.performLiquidHaptic(LiquidHapticCue.Input)
                            val updated = LiquidPinPadMath.appendDigit(sanitizedValue, digit, safeMaxLength)
                            if (updated != sanitizedValue) {
                                onValueChange(updated)
                                if (updated.length == safeMaxLength) {
                                    onComplete?.invoke(updated)
                                }
                            }
                        },
                    )
                }
            }
        }

        // Bottom row: optional leading action | 0 | backspace
        Row(
            horizontalArrangement = Arrangement.spacedBy(keySpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingActionIcon != null && onLeadingActionClick != null) {
                LiquidPinIconKey(
                    icon = leadingActionIcon,
                    contentDescription = leadingActionContentDescription.orEmpty(),
                    size = keySize,
                    enabled = enabled,
                    backdrop = effectiveBackdrop,
                    onClick = {
                        hapticFeedback.performLiquidHaptic(LiquidHapticCue.Input)
                        onLeadingActionClick()
                    },
                )
            } else {
                Spacer(modifier = Modifier.size(keySize))
            }

            LiquidPinDigitKey(
                digit = '0',
                size = keySize,
                enabled = enabled && sanitizedValue.length < safeMaxLength,
                backdrop = effectiveBackdrop,
                onClick = {
                    hapticFeedback.performLiquidHaptic(LiquidHapticCue.Input)
                    val updated = LiquidPinPadMath.appendDigit(sanitizedValue, '0', safeMaxLength)
                    if (updated != sanitizedValue) {
                        onValueChange(updated)
                        if (updated.length == safeMaxLength) {
                            onComplete?.invoke(updated)
                        }
                    }
                },
            )

            LiquidPinIconKey(
                icon = LiquidIcons.ArrowBack,
                contentDescription = backspaceContentDescription,
                size = keySize,
                enabled = enabled && sanitizedValue.isNotEmpty(),
                backdrop = effectiveBackdrop,
                onClick = {
                    hapticFeedback.performLiquidHaptic(LiquidHapticCue.Input)
                    val updated = LiquidPinPadMath.deleteLast(sanitizedValue)
                    if (updated != sanitizedValue) {
                        onValueChange(updated)
                    }
                },
            )
        }
    }
}

@Composable
private fun LiquidPinDigitKey(
    digit: Char,
    size: Dp,
    enabled: Boolean,
    backdrop: Backdrop,
    onClick: () -> Unit,
) {
    val glassColors = LiquidGlassTheme.colors
    val interactiveHighlight = rememberLiquidControlHighlight()

    Box(
        modifier =
            Modifier.size(size)
                .semantics {
                    role = Role.Button
                    contentDescription = digit.toString()
                }
                .liquidControlInteractive(
                    enabled = enabled,
                    interactiveHighlight = interactiveHighlight,
                    shape = CircleShape,
                    backdrop = backdrop,
                    containerColor = glassColors.neutralContainer,
                    onClick = onClick,
                )
                .border(width = 0.75.dp, color = glassColors.outline, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = digit.toString(),
            style =
                TextStyle(
                    color = if (enabled) glassColors.content else glassColors.disabledContent,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )
    }
}

@Composable
private fun LiquidPinIconKey(
    icon: ImageVector,
    contentDescription: String,
    size: Dp,
    enabled: Boolean,
    backdrop: Backdrop,
    onClick: () -> Unit,
) {
    val glassColors = LiquidGlassTheme.colors
    val interactiveHighlight = rememberLiquidControlHighlight()

    Box(
        modifier =
            Modifier.size(size)
                .semantics {
                    role = Role.Button
                    this.contentDescription = contentDescription
                }
                .liquidControlInteractive(
                    enabled = enabled,
                    interactiveHighlight = interactiveHighlight,
                    shape = CircleShape,
                    backdrop = backdrop,
                    containerColor = Color.Transparent,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) glassColors.content else glassColors.disabledContent,
            modifier = Modifier.size(24.dp),
        )
    }
}
