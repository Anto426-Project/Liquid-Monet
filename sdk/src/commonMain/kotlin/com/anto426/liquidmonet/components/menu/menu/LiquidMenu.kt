package com.anto426.liquidmonet.components.menu.menu

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.anto426.liquidmonet.components.menu.dropdownmenu.LiquidDropdownMenu
import com.anto426.liquidmonet.components.menu.menuitem.LiquidMenuItem
import com.anto426.liquidmonet.components.menu.morphingaction.LiquidMorphingAction
import com.anto426.liquidmonet.components.navigation.topbar.LiquidTopBarAction
import com.anto426.liquidmonet.glass.overlay.LiquidGlassDropdownPlacement
import com.anto426.liquidmonet.glass.overlay.LiquidGlassOverlayAnchorState
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop

/** Types of presentation for [LiquidMenu]. */
enum class LiquidMenuType {
    /** An anchored dropdown menu that pops from a trigger element. */
    Dropdown,

    /** A standalone floating popup menu. */
    Popup,
}

/** Types of items for [LiquidMenuItem]. */
enum class LiquidMenuItemType {
    /** Standard interactive action item. */
    Action,

    /** Selectable item featuring an active indicator check. */
    Selectable,
}

/**
 * LiquidMenu - Unified facade object for all Liquid Monet menu components.
 *
 * Exposes both the container ([Dropdown], [Popup], [LiquidDropdownMenu]) and the item ([Item],
 * [LiquidMenuItem]) as cohesive types of the same component, both sharing optical glass refraction
 * and bouncy spring physics.
 */
object LiquidMenu {
    /** Dropdown Menu container with refractive liquid glass and bounce entrance physics. */
    @Composable
    fun Dropdown(
        expanded: Boolean,
        onDismissRequest: () -> Unit,
        modifier: Modifier = Modifier,
        anchorState: LiquidGlassOverlayAnchorState? = null,
        placement: LiquidGlassDropdownPlacement = LiquidGlassDropdownPlacement.BelowEnd,
        offset: DpOffset = DpOffset(0.dp, 8.dp),
        backdropState: Backdrop = emptyBackdrop(),
        content: @Composable ColumnScope.() -> Unit,
    ) {
        LiquidDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            anchorState = anchorState,
            placement = placement,
            offset = offset,
            backdropState = backdropState,
            content = content,
        )
    }

    /** Interactive Menu Item with tactile bounce feedback and liquid control highlight. */
    @Composable
    fun Item(
        text: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
        type: LiquidMenuItemType = LiquidMenuItemType.Action,
        icon: ImageVector? = null,
        supportingText: String? = null,
        trailingText: String? = null,
        trailingIcon: ImageVector? = null,
        enabled: Boolean = true,
        contentColor: Color = Color.Unspecified,
        selected: Boolean = false,
        destructive: Boolean = false,
    ) {
        LiquidMenuItem(
            text = text,
            onClick = onClick,
            modifier = modifier,
            type = type,
            icon = icon,
            supportingText = supportingText,
            trailingText = trailingText,
            trailingIcon = trailingIcon,
            enabled = enabled,
            contentColor = contentColor,
            selected = selected,
            destructive = destructive,
        )
    }

    /** Morphing Action Button with deforming glass physics and bounce menu. */
    @Composable
    fun Morphing(
        action: LiquidTopBarAction,
        isLastItem: Boolean,
        backdropState: Backdrop,
        modifier: Modifier = Modifier,
    ) {
        LiquidMorphingAction(
            action = action,
            isLastItem = isLastItem,
            backdropState = backdropState,
            modifier = modifier,
        )
    }
}

/**
 * Top-level LiquidMenu composable offering dynamic type selection ([LiquidMenuType.Dropdown] or
 * [LiquidMenuType.Popup]).
 */
@Composable
fun LiquidMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    type: LiquidMenuType = LiquidMenuType.Dropdown,
    anchorState: LiquidGlassOverlayAnchorState? = null,
    placement: LiquidGlassDropdownPlacement = LiquidGlassDropdownPlacement.BelowEnd,
    offset: DpOffset = DpOffset(0.dp, 8.dp),
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable ColumnScope.() -> Unit,
) {
    when (type) {
        LiquidMenuType.Dropdown,
        LiquidMenuType.Popup -> {
            LiquidDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                modifier = modifier,
                anchorState = if (type == LiquidMenuType.Dropdown) anchorState else null,
                placement = placement,
                offset = offset,
                backdropState = backdropState,
                content = content,
            )
        }
    }
}
