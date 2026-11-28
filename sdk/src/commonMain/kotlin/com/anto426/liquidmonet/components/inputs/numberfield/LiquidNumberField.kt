package com.anto426.liquidmonet.components.inputs.numberfield

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.inputs.numberfield.motion.LiquidNumberFieldMotion
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.glass.LiquidGlassBackdropPolicy
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassLegibility
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LocalLiquidGlassLegibility
import com.anto426.liquidmonet.glass.emptyLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.RoundedRectangle
import kotlin.math.pow
import kotlin.math.round

/** Pure numeric parsing, stepping, and formatting helpers for [LiquidNumberField]. */
internal object LiquidNumberFieldMath {
    fun clamp(value: Double, range: ClosedFloatingPointRange<Double>): Double {
        if (!value.isFinite()) return range.start
        return value.coerceIn(range.start, range.endInclusive)
    }

    fun step(
        value: Double,
        delta: Double,
        range: ClosedFloatingPointRange<Double>,
        decimals: Int,
    ): Double {
        val factor = 10.0.pow(decimals.coerceIn(0, 6))
        val stepped = round((value + delta) * factor) / factor
        return clamp(stepped, range)
    }

    fun format(value: Double, decimals: Int): String {
        val clampedDecimals = decimals.coerceIn(0, 6)
        if (clampedDecimals == 0) return round(value).toLong().toString()
        val factor = 10.0.pow(clampedDecimals)
        val scaled = round(value * factor).toLong()
        val negative = scaled < 0
        val absScaled = kotlin.math.abs(scaled)
        val whole = absScaled / factor.toLong()
        val frac = (absScaled % factor.toLong()).toString().padStart(clampedDecimals, '0')
        return buildString {
            if (negative && (whole != 0L || frac.any { it != '0' })) append('-')
            append(whole)
            append('.')
            append(frac)
        }
    }

    fun parse(raw: String, range: ClosedFloatingPointRange<Double>): Double? {
        val normalized = raw.trim().replace(',', '.')
        if (normalized.isEmpty() || normalized == "-" || normalized == ".") return null
        val parsed = normalized.toDoubleOrNull() ?: return null
        return clamp(parsed, range)
    }
}

/**
 * LiquidNumberField - Precision Liquid Glass numeric / currency / quantity input combining direct
 * keyboard entry with integrated `-` / `+` stepper controls, optional prefix/suffix, and range
 * clamping.
 */
@Composable
fun LiquidNumberField(
    value: Double,
    onValueChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    range: ClosedFloatingPointRange<Double> = 0.0..10_000.0,
    step: Double = 1.0,
    decimals: Int = 0,
    prefix: String? = null,
    suffix: String? = null,
    enabled: Boolean = true,
    shape: Shape = RoundedRectangle(20.dp),
    decrementContentDescription: String = "Diminuisci",
    incrementContentDescription: String = "Aumenta",
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
) {
    require(step.isFinite() && step > 0.0) { "LiquidNumberField step must be positive and finite." }
    LiquidInputNormalization.nonNegative(decimals, "LiquidNumberField decimals")

    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val hapticFeedback = LocalHapticFeedback.current
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val clampedValue = LiquidNumberFieldMath.clamp(value, range)
    val formattedValue = remember(clampedValue, decimals) {
        LiquidNumberFieldMath.format(clampedValue, decimals)
    }
    var textBuffer by remember { mutableStateOf(formattedValue) }

    LaunchedEffect(formattedValue, isFocused) {
        if (!isFocused) {
            textBuffer = formattedValue
        }
    }

    val borderColor by
        LiquidNumberFieldMotion.animateColor(
            targetValue = if (isFocused) glassColors.focusIndicator else glassColors.outline,
            performance = performance,
            label = "numberFieldBorderColor",
        )

    val canDecrement = enabled && clampedValue > range.start
    val canIncrement = enabled && clampedValue < range.endInclusive

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = glassColors.content,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Control,
                        containerColor = glassColors.neutralContainer,
                        backdropPolicy = backdropPolicy,
                        legibility = legibility,
                    )
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = shape,
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CompositionLocalProvider(
                LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
                LocalLiquidGlassLegibility provides legibility,
            ) {
                LiquidIconButton(
                    icon = LiquidIcons.Remove,
                    contentDescription = decrementContentDescription,
                    onClick = {
                        hapticFeedback.performLiquidHaptic(LiquidHapticCue.Input)
                        val next = LiquidNumberFieldMath.step(clampedValue, -step, range, decimals)
                        textBuffer = LiquidNumberFieldMath.format(next, decimals)
                        onValueChange(next)
                    },
                    enabled = canDecrement,
                    size = 40.dp,
                    iconSize = 20.dp,
                    variant = LiquidIconButtonVariant.Standard,
                    backdrop = effectiveBackdrop,
                )

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!prefix.isNullOrBlank()) {
                        Text(
                            text = prefix,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = glassColors.secondaryContent,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                    BasicTextField(
                        value = textBuffer,
                        onValueChange = { incoming ->
                            val filtered =
                                incoming.filter { ch ->
                                    ch.isDigit() ||
                                        (decimals > 0 && (ch == '.' || ch == ',')) ||
                                        (range.start < 0.0 && ch == '-')
                                }
                            textBuffer = filtered
                            LiquidNumberFieldMath.parse(filtered, range)?.let(onValueChange)
                        },
                        enabled = enabled,
                        singleLine = true,
                        interactionSource = interactionSource,
                        modifier = Modifier.weight(1f, fill = false),
                        textStyle =
                            TextStyle(
                                color =
                                    if (enabled) glassColors.content
                                    else glassColors.disabledContent,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            ),
                        cursorBrush = SolidColor(colorScheme.primary),
                        keyboardOptions =
                            KeyboardOptions(
                                keyboardType =
                                    if (decimals > 0) KeyboardType.Decimal else KeyboardType.Number
                            ),
                    )
                    if (!suffix.isNullOrBlank()) {
                        Text(
                            text = suffix,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = glassColors.secondaryContent,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }

                LiquidIconButton(
                    icon = LiquidIcons.Add,
                    contentDescription = incrementContentDescription,
                    onClick = {
                        hapticFeedback.performLiquidHaptic(LiquidHapticCue.Input)
                        val next = LiquidNumberFieldMath.step(clampedValue, step, range, decimals)
                        textBuffer = LiquidNumberFieldMath.format(next, decimals)
                        onValueChange(next)
                    },
                    enabled = canIncrement,
                    size = 40.dp,
                    iconSize = 20.dp,
                    variant = LiquidIconButtonVariant.Standard,
                    backdrop = effectiveBackdrop,
                )
            }
        }
    }
}
