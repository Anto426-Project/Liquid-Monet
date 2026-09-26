package com.anto426.liquidmonet.components.inputs.otpinput

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.inputs.otpinput.motion.LiquidOtpInputMotion
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/**
 * LiquidOtpInput - Radiant Optical Liquid Glass OTP / PIN Verification Cells Component.
 *
 * Features:
 * - Direct seamless touch focus across the entire cell strip via decorationBox.
 * - Solid text state synchronization with cursor pinning for smooth typing and backspace.
 * - Auto-completion callback ([onComplete]) triggered upon filling all slots.
 * - Saturated Monet accent glow and Snell refraction per cell.
 */
@Composable
fun LiquidOtpInput(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 4,
    obscureText: Boolean = false,
    enabled: Boolean = true,
    cellWidth: Dp = 48.dp,
    cellHeight: Dp = 56.dp,
    spacing: Dp = 10.dp,
    onComplete: ((String) -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    require(length > 0) { "LiquidOtpInput length must be greater than zero." }
    require(cellWidth > 0.dp && cellHeight > 0.dp && spacing >= 0.dp) {
        "LiquidOtpInput cell dimensions must be positive and spacing cannot be negative."
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hapticFeedback = LocalHapticFeedback.current
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val cellShape = remember { RoundedRectangle(16.dp) }
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    var isFocused by remember { mutableStateOf(false) }
    val normalizedOtp =
        remember(otpValue, length) {
            otpValue.filter { it.isDigit() }.take(length)
        }

    // Keep selection and IME composition state local. Recreating TextFieldValue for every
    // character would move the selection to the end and make native text gestures restart.
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = normalizedOtp,
                selection = TextRange(normalizedOtp.length),
            )
        )
    }
    var lastCompletedOtp by remember { mutableStateOf<String?>(null) }

    val textMatchesExternalValue = textFieldValueState.text == normalizedOtp
    val textFieldValue =
        textFieldValueState.copy(
            text = normalizedOtp,
            selection =
                TextRange(
                    start = textFieldValueState.selection.start.coerceIn(0, normalizedOtp.length),
                    end = textFieldValueState.selection.end.coerceIn(0, normalizedOtp.length),
                ),
            composition = textFieldValueState.composition.takeIf { textMatchesExternalValue },
        )
    SideEffect {
        if (textFieldValueState != textFieldValue) {
            textFieldValueState = textFieldValue
        }
        if (normalizedOtp.length < length && lastCompletedOtp != null) {
            lastCompletedOtp = null
        }
    }

    BasicTextField(
        value = textFieldValue,
        onValueChange = { newValue ->
            val filteredText = newValue.text.filter { it.isDigit() }.take(length)
            val filteredValue =
                if (filteredText == newValue.text) {
                    newValue
                } else {
                    fun mappedSelectionOffset(offset: Int): Int =
                        newValue.text
                            .take(offset.coerceIn(0, newValue.text.length))
                            .count { it.isDigit() }
                            .coerceAtMost(filteredText.length)

                    newValue.copy(
                        text = filteredText,
                        selection =
                            TextRange(
                                start = mappedSelectionOffset(newValue.selection.start),
                                end = mappedSelectionOffset(newValue.selection.end),
                            ),
                        composition = null,
                    )
                }
            val previousText = textFieldValueState.text
            val textChanged = filteredText != previousText
            textFieldValueState = filteredValue

            if (textChanged && filteredText != normalizedOtp) {
                if (filteredText.length > previousText.length) {
                    hapticFeedback.performLiquidHaptic(LiquidHapticCue.Input)
                }
                onOtpChange(filteredText)
                if (filteredText.length == length && lastCompletedOtp != filteredText) {
                    lastCompletedOtp = filteredText
                    onComplete?.invoke(filteredText)
                } else if (filteredText.length < length) {
                    lastCompletedOtp = null
                }
            }
        },
        modifier =
            modifier.focusRequester(focusRequester).onFocusChanged { isFocused = it.isFocused },
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done,
            ),
        keyboardActions =
            KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    val currentOtp = textFieldValueState.text
                    if (currentOtp.length == length && lastCompletedOtp != currentOtp) {
                        lastCompletedOtp = currentOtp
                        onComplete?.invoke(currentOtp)
                    }
                }
            ),
        cursorBrush = SolidColor(Color.Transparent),
        enabled = enabled,
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (i in 0 until length) {
                    val currentOtp = textFieldValue.text
                    val rawChar = currentOtp.getOrNull(i)?.toString() ?: ""
                    val displayChar = if (obscureText && rawChar.isNotEmpty()) "•" else rawChar
                    val isCurrent =
                        isFocused &&
                            (currentOtp.length == i ||
                                (i == length - 1 && currentOtp.length == length))
                    val isFilled = i < currentOtp.length

                    val borderColor by
                        LiquidOtpInputMotion.animateColorPrimary(
                            targetValue =
                                when {
                                    isCurrent -> colorScheme.primary
                                    isFilled ->
                                        colorScheme.primary.copy(
                                            alpha = glassColors.focusIndicator.alpha * 0.72f
                                        )
                                    else -> glassColors.outline
                                },
                            performance = performance,
                            label = "otpBorder_$i",
                        )

                    val targetContainerColor =
                        when {
                            isCurrent -> glassColors.accentContainer
                            isFilled ->
                                colorScheme.primary.copy(alpha = glassColors.neutralContainer.alpha)
                            else -> Color.Transparent
                        }
                    val containerColor by
                        LiquidOtpInputMotion.animateColorSecondary(
                            targetValue = targetContainerColor,
                            performance = performance,
                            label = "otpContainer_$i",
                        )
                    val cellScale by
                        LiquidOtpInputMotion.animateFloat(
                            targetValue =
                                when {
                                    isCurrent -> 1.06f
                                    isFilled -> 1f
                                    else -> 0.94f
                                },
                            performance = performance,
                            label = "otpCellScale_$i",
                        )
                    val cellLift by
                        LiquidOtpInputMotion.animateDpPrimary(
                            targetValue = if (isCurrent) (-2).dp else 0.dp,
                            performance = performance,
                            label = "otpCellLift_$i",
                        )
                    val borderWidth by
                        LiquidOtpInputMotion.animateDpSecondary(
                            targetValue = if (isCurrent) 2.dp else 1.dp,
                            performance = performance,
                            label = "otpBorderWidth_$i",
                        )

                    Box(
                        modifier =
                            Modifier.size(width = cellWidth, height = cellHeight)
                                .graphicsLayer {
                                    scaleX = cellScale
                                    scaleY = cellScale
                                    translationY = cellLift.toPx()
                                }
                                .liquidGlass(
                                    backdrop = effectiveBackdrop,
                                    shape = cellShape,
                                    role = LiquidGlassRole.Control,
                                    containerColor = containerColor,
                                )
                                .border(
                                    width = borderWidth,
                                    color = borderColor,
                                    shape = cellShape,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        AnimatedContent(
                            targetState = displayChar,
                            transitionSpec = LiquidOtpInputMotion.contentTransition(performance),
                            contentAlignment = Alignment.Center,
                            label = "otpDigit_$i",
                        ) { digit ->
                            Text(
                                text = digit,
                                style =
                                    TextStyle(
                                        color =
                                            if (isFilled) {
                                                colorScheme.primary
                                            } else {
                                                colorScheme.onSurface
                                            },
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                    ),
                            )
                        }
                    }
                }
            }
        },
    )
}
