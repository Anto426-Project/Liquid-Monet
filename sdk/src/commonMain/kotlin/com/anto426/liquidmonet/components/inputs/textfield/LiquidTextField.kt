package com.anto426.liquidmonet.components.inputs.textfield

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.inputs.splitclearbutton.LiquidSplitClearButton
import com.anto426.liquidmonet.components.inputs.textfield.motion.LiquidTextFieldMotion
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle
import kotlin.math.PI
import kotlin.math.sin

enum class LiquidTextFieldType {
    Text,
    Email,
    Phone,
    Number,
    Password,
    TextArea,
}

/**
 * Unified Optical Liquid Glass input field.
 *
 * [type] configures the field's keyboard, standard icon, validation and layout. Password fields
 * include a built-in visibility toggle, while text areas support multi-line input and an optional
 * character counter through [maxLength].
 *
 * Features Snell lens refraction, Monet dynamic chromatic luminescence, smooth focus glow, live
 * text clearing, and seamless native text interaction.
 */
@Composable
fun LiquidTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: LiquidTextFieldType = LiquidTextFieldType.Text,
    label: String? =
        when (type) {
            LiquidTextFieldType.Email -> "Email"
            LiquidTextFieldType.Phone -> "Telefono"
            LiquidTextFieldType.Number -> "Valore Numerico"
            LiquidTextFieldType.Password -> "Password"
            LiquidTextFieldType.Text,
            LiquidTextFieldType.TextArea -> null
        },
    placeholder: String =
        label
            ?: when (type) {
                LiquidTextFieldType.Email -> "nome@esempio.com"
                LiquidTextFieldType.Phone -> "+39 123 456 7890"
                LiquidTextFieldType.Number -> "0.00"
                LiquidTextFieldType.Password -> "Inserisci password..."
                LiquidTextFieldType.TextArea -> "Scrivi qui..."
                LiquidTextFieldType.Text -> "Inserisci testo..."
            },
    leadingIcon: ImageVector? =
        when (type) {
            LiquidTextFieldType.Email -> LiquidIcons.Info
            LiquidTextFieldType.Phone -> LiquidIcons.Phone
            LiquidTextFieldType.Password -> LiquidIcons.Lock
            LiquidTextFieldType.Text,
            LiquidTextFieldType.Number,
            LiquidTextFieldType.TextArea -> null
        },
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = type != LiquidTextFieldType.TextArea,
    maxLines: Int =
        when {
            type == LiquidTextFieldType.TextArea -> 8
            singleLine -> 1
            else -> Int.MAX_VALUE
        },
    minHeight: Dp = if (type == LiquidTextFieldType.TextArea) 110.dp else 54.dp,
    maxLength: Int? = null,
    shape: Shape = RoundedRectangle(20.dp),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions =
        when (type) {
            LiquidTextFieldType.Email -> KeyboardOptions(keyboardType = KeyboardType.Email)
            LiquidTextFieldType.Phone -> KeyboardOptions(keyboardType = KeyboardType.Phone)
            LiquidTextFieldType.Number -> KeyboardOptions(keyboardType = KeyboardType.Decimal)
            LiquidTextFieldType.Password -> KeyboardOptions(keyboardType = KeyboardType.Password)
            LiquidTextFieldType.Text,
            LiquidTextFieldType.TextArea -> KeyboardOptions.Default
        },
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val performance = LocalLiquidGlassPerformance.current
    LiquidInputNormalization.positive(maxLines, "LiquidTextField maxLines")
    LiquidInputNormalization.positive(minHeight, "LiquidTextField minHeight")
    maxLength?.let {
        LiquidInputNormalization.nonNegative(it, "LiquidTextField maxLength")
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isPasswordVisible by remember(type) { mutableStateOf(false) }

    val isTextArea = type == LiquidTextFieldType.TextArea
    val isPassword = type == LiquidTextFieldType.Password
    val effectiveSingleLine = !isTextArea && singleLine
    val effectiveMaxLines = if (effectiveSingleLine) 1 else maxLines
    val effectiveVisualTransformation =
        when {
            isPassword && !isPasswordVisible -> PasswordVisualTransformation()
            isPassword -> VisualTransformation.None
            else -> visualTransformation
        }
    val effectiveTrailingIcon =
        if (isPassword) {
            if (isPasswordVisible) LiquidIcons.VisibilityOff else LiquidIcons.Visibility
        } else {
            trailingIcon
        }
    val effectiveTrailingIconClick =
        if (isPassword) {
            { isPasswordVisible = !isPasswordVisible }
        } else {
            onTrailingIconClick
        }

    val colorScheme = MaterialTheme.colorScheme
    val contentColor = colorScheme.onSurface
    val primaryColor = colorScheme.primary
    val glassColors = LiquidGlassTheme.colors
    val placeholderColor = glassColors.secondaryContent

    val animatedBorderColor by
        LiquidTextFieldMotion.animateColor(
            targetValue =
                if (isFocused) {
                    glassColors.focusIndicator
                } else Color.Transparent,
            performance = performance,
            label = "inputBorderColor",
        )

    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val onValueChangeHandler: (String) -> Unit = { input ->
        val isValidNumber =
            type != LiquidTextFieldType.Number ||
                input.isEmpty() ||
                input.all { it.isDigit() || it == '.' || it == ',' }
        val isWithinMaxLength = maxLength == null || input.length <= maxLength

        if (isValidNumber && isWithinMaxLength) {
            onValueChange(input)
        }
    }

    if (isTextArea) {
        BasicTextField(
            value = value,
            onValueChange = onValueChangeHandler,
            modifier =
                modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight)
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Control,
                    )
                    .border(width = 1.dp, color = animatedBorderColor, shape = shape),
            enabled = enabled,
            readOnly = readOnly,
            textStyle =
                TextStyle(
                    color = contentColor,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Normal,
                ),
            cursorBrush = SolidColor(primaryColor),
            singleLine = false,
            maxLines = effectiveMaxLines,
            visualTransformation = effectiveVisualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                        contentAlignment = Alignment.TopStart,
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style =
                                    TextStyle(
                                        color = placeholderColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Normal,
                                    ),
                            )
                        }
                        innerTextField()
                    }

                    if (maxLength != null) {
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "${value.length}/$maxLength",
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.50f),
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                }
            },
        )
    } else {
        val dropletSeparating = value.isNotEmpty() && !readOnly && enabled
        val recoilProgress by
            LiquidTextFieldMotion.animateFloat(
                targetValue = if (dropletSeparating) 1f else 0f,
                performance = performance,
                label = "textFieldRecoil",
            )
        val recoilWave = sin(recoilProgress.coerceIn(0f, 1f) * PI).toFloat()
        val mainBodySquashX = 1f - recoilWave * 0.018f
        val mainBodyStretchY = 1f + recoilWave * 0.012f

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChangeHandler,
                modifier =
                    Modifier.weight(1f)
                        .heightIn(min = minHeight)
                        .graphicsLayer {
                            scaleX = mainBodySquashX
                            scaleY = mainBodyStretchY
                            transformOrigin = TransformOrigin(1f, 0.5f)
                        }
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = shape,
                            role = LiquidGlassRole.Control,
                        )
                        .border(width = 1.dp, color = animatedBorderColor, shape = shape),
                enabled = enabled,
                readOnly = readOnly,
                textStyle =
                    TextStyle(
                        color = contentColor,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                cursorBrush = SolidColor(primaryColor),
                singleLine = effectiveSingleLine,
                maxLines = effectiveMaxLines,
                visualTransformation = effectiveVisualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        if (leadingIcon != null) {
                            Icon(
                                imageVector = leadingIcon,
                                contentDescription = null,
                                tint =
                                    if (isFocused) primaryColor
                                    else contentColor.copy(alpha = 0.65f),
                                modifier = Modifier.size(22.dp),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style =
                                        TextStyle(
                                            color = placeholderColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                        ),
                                    maxLines = effectiveMaxLines,
                                )
                            }
                            innerTextField()
                        }

                        if (effectiveTrailingIcon != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val description =
                                when {
                                    !isPassword -> null
                                    isPasswordVisible -> "Nascondi password"
                                    else -> "Mostra password"
                                }
                            val tint =
                                if (isFocused) primaryColor else contentColor.copy(alpha = 0.60f)
                            if (effectiveTrailingIconClick != null) {
                                LiquidIconButton(
                                    icon = effectiveTrailingIcon,
                                    contentDescription = description,
                                    onClick = effectiveTrailingIconClick,
                                    enabled = enabled,
                                    iconSize = 20.dp,
                                    variant = LiquidIconButtonVariant.Ghost,
                                    contentColor = tint,
                                )
                            } else {
                                Icon(
                                    imageVector = effectiveTrailingIcon,
                                    contentDescription = description,
                                    tint = tint,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                },
            )

            LiquidSplitClearButton(
                visible = value.isNotEmpty() && !readOnly && enabled,
                onClick = { onValueChange("") },
                size = minHeight,
                shape = shape,
                backdropState = effectiveBackdrop,
                enabled = enabled,
            )
        }
    }
}
