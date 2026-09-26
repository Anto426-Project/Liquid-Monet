package com.anto426.liquidmonet.components.selection.chipselectiongroup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.selection.chip.LiquidChip
import com.anto426.liquidmonet.icons.LiquidIcons

/**
 * Hoisted, animated multi-selection group built from [LiquidChip].
 *
 * The optional all-control selects or clears every enabled item owned by this group. Values in
 * [selectedItems] that are not present in [items] are preserved, which allows several groups to
 * share one selection set safely.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> LiquidChipSelectionGroup(
    items: List<T>,
    selectedItems: Set<T>,
    onSelectionChange: (Set<T>) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    itemEnabled: (T) -> Boolean = { true },
    leadingIcon: (T) -> ImageVector? = { null },
    trailingIcon: (T) -> ImageVector? = { null },
    badge: (T) -> String? = { null },
    tint: Color = Color.Unspecified,
    allLabel: String? = null,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
) {
    require(maxItemsInEachRow > 0) { "maxItemsInEachRow must be greater than zero" }

    val distinctItems = items.distinct()
    val enabledItems = if (enabled) distinctItems.filter(itemEnabled) else emptyList()
    val allSelected = enabledItems.isNotEmpty() && enabledItems.all(selectedItems::contains)

    FlowRow(
        // animateContentSize clips to its animated bounds internally. That cuts the elastic
        // scale of first/last-row chips, so selection motion stays on each chip and this host
        // deliberately remains an unclipped layout container.
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachRow = maxItemsInEachRow,
    ) {
        if (allLabel != null) {
            LiquidChip(
                label = allLabel,
                selected = allSelected,
                enabled = enabledItems.isNotEmpty(),
                leadingIcon = LiquidIcons.Check,
                tint = tint,
                modifier = Modifier.semantics { selected = allSelected },
                onClick = {
                    val nextSelection = selectedItems.toMutableSet()
                    if (allSelected) {
                        nextSelection.removeAll(enabledItems.toSet())
                    } else {
                        nextSelection.addAll(enabledItems)
                    }
                    onSelectionChange(nextSelection.toSet())
                },
            )
        }

        distinctItems.forEach { item ->
            key(item) {
                val itemSelected = item in selectedItems
                val isItemEnabled = enabled && itemEnabled(item)

                LiquidChip(
                    label = label(item),
                    selected = itemSelected,
                    enabled = isItemEnabled,
                    leadingIcon = leadingIcon(item),
                    trailingIcon = trailingIcon(item),
                    badge = badge(item),
                    tint = tint,
                    modifier = Modifier.semantics { selected = itemSelected },
                    onClick = {
                        val nextSelection = selectedItems.toMutableSet()
                        if (itemSelected) {
                            nextSelection.remove(item)
                        } else {
                            nextSelection.add(item)
                        }
                        onSelectionChange(nextSelection.toSet())
                    },
                )
            }
        }
    }
}
