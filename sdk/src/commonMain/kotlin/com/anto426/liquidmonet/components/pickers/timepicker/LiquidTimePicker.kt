package com.anto426.liquidmonet.components.pickers.timepicker

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.liquidControlInteractive
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.pickers.timepicker.motion.LiquidTimePickerMotion
import com.anto426.liquidmonet.components.pickers.timepicker.state.LiquidTimePickerState
import com.anto426.liquidmonet.components.pickers.timepicker.state.twoDigits
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

private val QuickMinutePresets = listOf(0, 15, 30, 45)

@Composable
fun rememberLiquidTimePickerState(
    initialHour: Int = 12,
    initialMinute: Int = 0,
): LiquidTimePickerState {
    return remember(initialHour, initialMinute) { LiquidTimePickerState(initialHour, initialMinute) }
}

/** LiquidTimePicker - Inline Liquid Glass Time Picker with interactive drums and quick presets. */
@Composable
fun LiquidTimePicker(
    state: LiquidTimePickerState,
    modifier: Modifier = Modifier,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val scheme = MaterialTheme.colorScheme
    val colors = LiquidGlassTheme.colors
    val performance = LocalLiquidGlassPerformance.current
    val haptic = LocalHapticFeedback.current
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val isInsideSharedContainer =
        LocalLiquidGlassContainerMode.current == LiquidGlassContainerMode.Shared

    val containerShape = RoundedRectangle(24.dp)
    val drumWidth = 96.dp
    val drumHeight = 96.dp
    val colonWidth = 34.dp

    var hourDirection by remember { mutableIntStateOf(1) }
    var minuteDirection by remember { mutableIntStateOf(1) }

    val stepHour: (Int) -> Unit = { delta ->
        hourDirection = if (delta >= 0) 1 else -1
        state.adjustHour(delta)
        haptic.performLiquidHaptic(LiquidHapticCue.Selection)
    }

    val stepMinute: (Int) -> Unit = { delta ->
        minuteDirection = if (delta >= 0) 1 else -1
        state.adjustMinute(delta)
        haptic.performLiquidHaptic(LiquidHapticCue.Selection)
    }

    val containerModifier =
        if (isInsideSharedContainer) {
            modifier.fillMaxWidth().padding(vertical = 4.dp)
        } else {
            modifier
                .fillMaxWidth()
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = containerShape,
                    role = LiquidGlassRole.Surface,
                    containerColor = colors.neutralContainer,
                )
                .border(width = 1.dp, color = colors.outline, shape = containerShape)
                .padding(16.dp)
        }

    Column(
        modifier = containerModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Strictly aligned horizontal rows ensure the ":" separator stays centered with the digits
        // across every font scale and screen density.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Row 1: Column labels
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "ORE",
                    modifier = Modifier.width(drumWidth),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                        ),
                    color = colors.secondaryContent,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.width(colonWidth))
                Text(
                    text = "MINUTI",
                    modifier = Modifier.width(drumWidth),
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp,
                        ),
                    color = colors.secondaryContent,
                    textAlign = TextAlign.Center,
                )
            }

            // Row 2: Increment buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(modifier = Modifier.width(drumWidth), contentAlignment = Alignment.Center) {
                    LiquidIconButton(
                        icon = LiquidIcons.KeyboardArrowUp,
                        onClick = { stepHour(1) },
                        contentDescription = "Aumenta ore",
                        variant = LiquidIconButtonVariant.Ghost,
                        size = 40.dp,
                        backdropState = effectiveBackdrop,
                    )
                }
                Spacer(modifier = Modifier.width(colonWidth))
                Box(modifier = Modifier.width(drumWidth), contentAlignment = Alignment.Center) {
                    LiquidIconButton(
                        icon = LiquidIcons.KeyboardArrowUp,
                        onClick = { stepMinute(1) },
                        contentDescription = "Aumenta minuti",
                        variant = LiquidIconButtonVariant.Ghost,
                        size = 40.dp,
                        backdropState = effectiveBackdrop,
                    )
                }
            }

            // Row 3: Interactive glass digit drums + centered colon separator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                LiquidTimeDrum(
                    value = state.hour,
                    modulus = 24,
                    direction = hourDirection,
                    width = drumWidth,
                    height = drumHeight,
                    backdropState = effectiveBackdrop,
                    onStep = stepHour,
                )

                Box(
                    modifier = Modifier.width(colonWidth).height(drumHeight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ":",
                        style =
                            MaterialTheme.typography.displaySmall.copy(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        color = colors.content,
                    )
                }

                LiquidTimeDrum(
                    value = state.minute,
                    modulus = 60,
                    direction = minuteDirection,
                    width = drumWidth,
                    height = drumHeight,
                    backdropState = effectiveBackdrop,
                    onStep = stepMinute,
                )
            }

            // Row 4: Decrement buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(modifier = Modifier.width(drumWidth), contentAlignment = Alignment.Center) {
                    LiquidIconButton(
                        icon = LiquidIcons.KeyboardArrowDown,
                        onClick = { stepHour(-1) },
                        contentDescription = "Diminuisci ore",
                        variant = LiquidIconButtonVariant.Ghost,
                        size = 40.dp,
                        backdropState = effectiveBackdrop,
                    )
                }
                Spacer(modifier = Modifier.width(colonWidth))
                Box(modifier = Modifier.width(drumWidth), contentAlignment = Alignment.Center) {
                    LiquidIconButton(
                        icon = LiquidIcons.KeyboardArrowDown,
                        onClick = { stepMinute(-1) },
                        contentDescription = "Diminuisci minuti",
                        variant = LiquidIconButtonVariant.Ghost,
                        size = 40.dp,
                        backdropState = effectiveBackdrop,
                    )
                }
            }
        }

        // Quick minute presets (:00, :15, :30, :45)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            QuickMinutePresets.forEach { preset ->
                val isSelected = state.minute == preset
                val chipShape = Capsule()
                val chipBg by
                    LiquidTimePickerMotion.animatePresetColor(
                        targetValue = if (isSelected) scheme.primary else colors.neutralContainer,
                        performance = performance,
                        label = "timePresetBg",
                    )
                Box(
                    modifier =
                        Modifier.weight(1f)
                            .height(36.dp)
                            .semantics { this.selected = isSelected }
                            .clip(chipShape)
                            .background(chipBg, chipShape)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else colors.outline,
                                shape = chipShape,
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.RadioButton,
                                onClick = {
                                    minuteDirection = if (preset >= state.minute) 1 else -1
                                    state.minute = preset
                                    haptic.performLiquidHaptic(LiquidHapticCue.Selection)
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = ":${preset.twoDigits()}",
                        style =
                            MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                fontFeatureSettings = "tnum",
                            ),
                        color = if (isSelected) scheme.onPrimary else colors.content,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidTimeDrum(
    value: Int,
    modulus: Int,
    direction: Int,
    width: Dp,
    height: Dp,
    backdropState: Backdrop,
    onStep: (Int) -> Unit,
) {
    val colors = LiquidGlassTheme.colors
    val performance = LocalLiquidGlassPerformance.current
    val density = LocalDensity.current
    val stepThresholdPx = with(density) { 22.dp.toPx() }
    val latestOnStep by rememberUpdatedState(onStep)

    var isDragging by remember { mutableStateOf(false) }
    val drumShape = RoundedRectangle(22.dp)

    val containerColor by
        LiquidTimePickerMotion.animateDrumColor(
            targetValue = if (isDragging) colors.selectedContainer else colors.accentContainer,
            performance = performance,
            label = "timeDrumBg",
        )
    val borderColor by
        LiquidTimePickerMotion.animateDrumColor(
            targetValue = if (isDragging) colors.focusIndicator else colors.outline,
            performance = performance,
            label = "timeDrumBorder",
        )

    val prevValue = (value + modulus - 1) % modulus
    val nextValue = (value + 1) % modulus

    Box(
        modifier =
            Modifier.size(width = width, height = height)
                .liquidGlass(
                    backdrop = backdropState,
                    shape = drumShape,
                    role = LiquidGlassRole.Control,
                    containerColor = containerColor,
                )
                .border(
                    width = if (isDragging) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = drumShape,
                )
                .pointerInput(stepThresholdPx) {
                    var dragAccumulator = 0f
                    detectVerticalDragGestures(
                        onDragStart = {
                            isDragging = true
                            dragAccumulator = 0f
                        },
                        onDragEnd = {
                            isDragging = false
                            dragAccumulator = 0f
                        },
                        onDragCancel = {
                            isDragging = false
                            dragAccumulator = 0f
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            dragAccumulator += dragAmount
                            while (dragAccumulator <= -stepThresholdPx) {
                                dragAccumulator += stepThresholdPx
                                latestOnStep(1)
                            }
                            while (dragAccumulator >= stepThresholdPx) {
                                dragAccumulator -= stepThresholdPx
                                latestOnStep(-1)
                            }
                        },
                    )
                }
                .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Subtle ghost neighbor above
        Text(
            text = prevValue.twoDigits(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontFeatureSettings = "tnum",
                ),
            color = colors.secondaryContent.copy(alpha = 0.42f),
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Active value with directional rolling animation
        AnimatedContent(
            targetState = value,
            transitionSpec = LiquidTimePickerMotion.drumDigitTransition(performance, direction),
            label = "timeDrumDigit",
        ) { digit ->
            Text(
                text = digit.twoDigits(),
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum",
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Subtle ghost neighbor below
        Text(
            text = nextValue.twoDigits(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontFeatureSettings = "tnum",
                ),
            color = colors.secondaryContent.copy(alpha = 0.42f),
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

/** LiquidTimePickerField - Liquid Glass Time Input Trigger Field. */
@Composable
fun LiquidTimePickerField(
    selectedHour: Int?,
    selectedMinute: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Seleziona Ora",
    placeholder: String = "HH:MM",
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val formattedTime =
        remember(selectedHour, selectedMinute) {
            if (selectedHour != null && selectedMinute != null) {
                "${selectedHour.twoDigits()}:${selectedMinute.twoDigits()}"
            } else null
        }

    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val shape = RoundedRectangle(20.dp)
    val borderColor =
        if (enabled) glassColors.outline
        else glassColors.outline.copy(alpha = glassColors.outline.alpha * 0.55f)

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .liquidControlInteractive(
                    enabled = enabled,
                    interactiveHighlight = interactiveHighlight,
                    shape = shape,
                    backdrop = effectiveBackdrop,
                    onClick = onClick,
                )
                .border(width = 1.dp, color = borderColor, shape = shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = LiquidIcons.Time,
            contentDescription = label,
            tint = if (enabled) colorScheme.primary else glassColors.disabledContent,
            modifier = Modifier.size(20.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            if (formattedTime != null) {
                Text(
                    text = label,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                        ),
                    color =
                        if (enabled) glassColors.secondaryContent
                        else glassColors.disabledContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = formattedTime,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFeatureSettings = "tnum",
                        ),
                    color = if (enabled) colorScheme.onSurface else glassColors.disabledContent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    text = placeholder,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                        ),
                    color = glassColors.secondaryContent.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Icon(
            imageVector = LiquidIcons.ChevronRight,
            contentDescription = null,
            tint = if (enabled) glassColors.secondaryContent else glassColors.disabledContent,
            modifier = Modifier.size(18.dp),
        )
    }
}
