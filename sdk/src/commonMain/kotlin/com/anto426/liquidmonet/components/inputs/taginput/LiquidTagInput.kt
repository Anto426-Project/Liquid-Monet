package com.anto426.liquidmonet.components.inputs.taginput

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.inputs.taginput.motion.LiquidTagInputMotion
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
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

/** Pure normalization and deduplication helpers for [LiquidTagInput]. */
internal object LiquidTagInputMath {
    fun addTag(
        existing: List<String>,
        rawCandidate: String,
        maxTags: Int?,
        allowDuplicates: Boolean = false,
    ): List<String> {
        val cleaned = rawCandidate.trim()
        if (cleaned.isEmpty()) return existing
        if (maxTags != null && existing.size >= maxTags) return existing
        if (!allowDuplicates && existing.any { it.equals(cleaned, ignoreCase = true) }) {
            return existing
        }
        return existing + cleaned
    }

    fun removeTag(existing: List<String>, tag: String): List<String> =
        existing.filterNot { it == tag }

    fun splitCompletedTokens(draft: String): Pair<List<String>, String> {
        if (!draft.contains(',') && !draft.contains('\n')) return emptyList<String>() to draft
        val parts = draft.split(',', '\n')
        val completed = parts.dropLast(1).map(String::trim).filter(String::isNotEmpty)
        val remainder = parts.last()
        return completed to remainder
    }
}

/**
 * LiquidTagInput - Multi-token Liquid Glass tag input field with removable chips, comma/enter
 * submission, case-insensitive deduplication, optional max limit, and quick suggestion chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiquidTagInput(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "Aggiungi tag...",
    suggestions: List<String> = emptyList(),
    maxTags: Int? = null,
    allowDuplicates: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = RoundedRectangle(20.dp),
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
) {
    maxTags?.let { LiquidInputNormalization.positive(it, "LiquidTagInput maxTags") }

    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var draft by remember { mutableStateOf("") }

    val canAddMore = enabled && (maxTags == null || tags.size < maxTags)
    val borderColor by
        LiquidTagInputMotion.animateColor(
            targetValue = if (isFocused) glassColors.focusIndicator else glassColors.outline,
            performance = performance,
            label = "tagInputBorderColor",
        )

    fun commitDraft(text: String = draft) {
        val updated =
            LiquidTagInputMath.addTag(
                existing = tags,
                rawCandidate = text,
                maxTags = maxTags,
                allowDuplicates = allowDuplicates,
            )
        if (updated != tags) {
            onTagsChange(updated)
        }
        draft = ""
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!label.isNullOrBlank() || maxTags != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!label.isNullOrBlank()) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = glassColors.content,
                    )
                }
                if (maxTags != null) {
                    Text(
                        text = "${tags.size} / $maxTags",
                        style = MaterialTheme.typography.labelSmall,
                        color = glassColors.secondaryContent,
                    )
                }
            }
        }

        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Surface,
                        containerColor = glassColors.neutralContainer,
                        backdropPolicy = backdropPolicy,
                        legibility = legibility,
                    )
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = borderColor,
                        shape = shape,
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CompositionLocalProvider(
                LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
                LocalLiquidGlassLegibility provides legibility,
            ) {
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        tags.forEach { tag ->
                            LiquidChip(
                                label = tag,
                                selected = true,
                                enabled = enabled,
                                onClick = {},
                                onCloseClick =
                                    if (enabled) {
                                        { onTagsChange(LiquidTagInputMath.removeTag(tags, tag)) }
                                    } else {
                                        null
                                    },
                                backdrop = effectiveBackdrop,
                            )
                        }
                    }
                }

                if (canAddMore) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            if (draft.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = glassColors.secondaryContent.copy(alpha = 0.72f),
                                )
                            }
                            BasicTextField(
                                value = draft,
                                onValueChange = { incoming ->
                                    val (completed, remainder) =
                                        LiquidTagInputMath.splitCompletedTokens(incoming)
                                    if (completed.isNotEmpty()) {
                                        var next = tags
                                        completed.forEach { token ->
                                            next =
                                                LiquidTagInputMath.addTag(
                                                    existing = next,
                                                    rawCandidate = token,
                                                    maxTags = maxTags,
                                                    allowDuplicates = allowDuplicates,
                                                )
                                        }
                                        if (next != tags) onTagsChange(next)
                                        draft = remainder
                                    } else {
                                        draft = incoming
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = canAddMore,
                                singleLine = true,
                                interactionSource = interactionSource,
                                textStyle =
                                    TextStyle(
                                        color = glassColors.content,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                    ),
                                cursorBrush = SolidColor(colorScheme.primary),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { commitDraft() }),
                            )
                        }

                        if (draft.isNotBlank()) {
                            LiquidIconButton(
                                icon = LiquidIcons.Add,
                                contentDescription = "Aggiungi tag",
                                onClick = { commitDraft() },
                                size = 34.dp,
                                iconSize = 18.dp,
                                variant = LiquidIconButtonVariant.Standard,
                                backdrop = effectiveBackdrop,
                            )
                        }
                    }
                }
            }
        }

        val availableSuggestions =
            remember(suggestions, tags, allowDuplicates) {
                if (allowDuplicates) suggestions
                else suggestions.filter { candidate ->
                    tags.none { it.equals(candidate, ignoreCase = true) }
                }
            }
        if (availableSuggestions.isNotEmpty() && canAddMore) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableSuggestions.forEach { suggestion ->
                    LiquidChip(
                        label = suggestion,
                        leadingIcon = LiquidIcons.Add,
                        selected = false,
                        enabled = canAddMore,
                        onClick = { commitDraft(suggestion) },
                        backdrop = effectiveBackdrop,
                    )
                }
            }
        }
    }
}
