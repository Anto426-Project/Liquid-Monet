package com.anto426.liquidmonet.components.navigation.floatingtoolbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.navigation.floatingtoolbar.motion.LiquidFloatingToolbarMotion
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
import com.anto426.liquidmonet.glass.runtime.asBalancedFrostedIfEssential
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.shapes.Capsule

/** One action button hosted inside a [LiquidFloatingToolbar]. */
@Immutable
data class LiquidToolbarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val label: String? = null,
    val selected: Boolean = false,
    val enabled: Boolean = true,
    val badge: String? = null,
    val destructive: Boolean = false,
)

/** Axis along which [LiquidFloatingToolbar] lays out its actions and optional companion FAB. */
enum class LiquidFloatingToolbarOrientation {
    Horizontal,
    Vertical,
}

/** Visual tone of a [LiquidFloatingToolbar]. */
enum class LiquidFloatingToolbarStyle {
    /** Neutral crystal liquid glass surface. */
    Standard,

    /** Monet-accented vibrant liquid glass surface. */
    Vibrant,
}

/** Pure formatting helpers for [LiquidFloatingToolbar]. */
internal object LiquidFloatingToolbarMath {
    fun compactBadge(raw: String?): String? {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.isEmpty()) return null
        val numeric = trimmed.toIntOrNull()
        if (numeric != null && numeric > 999) return "999+"
        return if (trimmed.length > 4) "${trimmed.take(3)}…" else trimmed
    }
}

/**
 * LiquidFloatingToolbar - Floating refractive liquid glass action capsule (Horizontal or Vertical)
 * with optional companion [floatingAction] slot (e.g. a detached primary action button).
 */
@Composable
fun LiquidFloatingToolbar(
    actions: List<LiquidToolbarAction>,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    orientation: LiquidFloatingToolbarOrientation = LiquidFloatingToolbarOrientation.Horizontal,
    style: LiquidFloatingToolbarStyle = LiquidFloatingToolbarStyle.Standard,
    shape: Shape = Capsule(),
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
    floatingAction: (@Composable () -> Unit)? = null,
) {
    if (actions.isEmpty() && floatingAction == null) return

    val performance = LocalLiquidGlassPerformance.current
    val toolbarPerformance = remember(performance) { performance.asBalancedFrostedIfEssential() }
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val glassColors = LiquidGlassTheme.colors
    val containerColor =
        when (style) {
            LiquidFloatingToolbarStyle.Standard -> null
            LiquidFloatingToolbarStyle.Vibrant -> glassColors.accentContainer
        }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.graphicsLayer(clip = false),
        enter = remember(performance, orientation) {
            LiquidFloatingToolbarMotion.enter(performance, orientation)
        },
        exit = remember(performance, orientation) {
            LiquidFloatingToolbarMotion.exit(performance, orientation)
        },
    ) {
        val toolbarPill: @Composable () -> Unit = {
            if (actions.isNotEmpty()) {
                val pillPadding =
                    when (orientation) {
                        LiquidFloatingToolbarOrientation.Horizontal ->
                            PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                        LiquidFloatingToolbarOrientation.Vertical ->
                            PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                    }

                Box(
                    modifier =
                        Modifier.liquidGlass(
                                backdrop = effectiveBackdrop,
                                shape = shape,
                                role = LiquidGlassRole.Navigation,
                                containerColor = containerColor,
                                performance = toolbarPerformance,
                                backdropPolicy = backdropPolicy,
                                legibility = legibility,
                            )
                            .border(width = 0.75.dp, color = glassColors.outline, shape = shape)
                            .padding(pillPadding)
                ) {
                    CompositionLocalProvider(
                        LocalContentColor provides glassColors.content,
                        LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                        LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
                        LocalLiquidGlassLegibility provides legibility,
                    ) {
                        when (orientation) {
                            LiquidFloatingToolbarOrientation.Horizontal ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    actions.forEach { action ->
                                        LiquidToolbarActionItem(action = action)
                                    }
                                }
                            LiquidFloatingToolbarOrientation.Vertical ->
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    actions.forEach { action ->
                                        LiquidToolbarActionItem(action = action)
                                    }
                                }
                        }
                    }
                }
            }
        }

        if (floatingAction != null) {
            when (orientation) {
                LiquidFloatingToolbarOrientation.Horizontal ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        toolbarPill()
                        floatingAction()
                    }
                LiquidFloatingToolbarOrientation.Vertical ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        toolbarPill()
                        floatingAction()
                    }
            }
        } else {
            toolbarPill()
        }
    }
}

/**
 * Custom-content overload of [LiquidFloatingToolbar] for toolbars that mix labels, counters,
 * dividers, or bespoke controls inside the same floating refractive capsule.
 */
@Composable
fun LiquidFloatingToolbar(
    modifier: Modifier = Modifier,
    visible: Boolean = true,
    style: LiquidFloatingToolbarStyle = LiquidFloatingToolbarStyle.Standard,
    shape: Shape = Capsule(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    backdrop: Backdrop = emptyLiquidGlassBackdrop(),
    backdropPolicy: LiquidGlassBackdropPolicy = LiquidGlassBackdropPolicy.SceneFirst,
    legibility: LiquidGlassLegibility = LocalLiquidGlassLegibility.current,
    content: @Composable RowScope.() -> Unit,
) {
    val performance = LocalLiquidGlassPerformance.current
    val toolbarPerformance = remember(performance) { performance.asBalancedFrostedIfEssential() }
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdrop, policy = backdropPolicy)
    val glassColors = LiquidGlassTheme.colors
    val containerColor =
        when (style) {
            LiquidFloatingToolbarStyle.Standard -> null
            LiquidFloatingToolbarStyle.Vibrant -> glassColors.accentContainer
        }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.graphicsLayer(clip = false),
        enter = remember(performance) {
            LiquidFloatingToolbarMotion.enter(
                performance,
                LiquidFloatingToolbarOrientation.Horizontal,
            )
        },
        exit = remember(performance) {
            LiquidFloatingToolbarMotion.exit(
                performance,
                LiquidFloatingToolbarOrientation.Horizontal,
            )
        },
    ) {
        Row(
            modifier =
                Modifier.liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = shape,
                        role = LiquidGlassRole.Navigation,
                        containerColor = containerColor,
                        performance = toolbarPerformance,
                        backdropPolicy = backdropPolicy,
                        legibility = legibility,
                    )
                    .border(width = 0.75.dp, color = glassColors.outline, shape = shape)
                    .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides glassColors.content,
                LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
                LocalLiquidGlassLegibility provides legibility,
            ) {
                content()
            }
        }
    }
}

@Composable
private fun LiquidToolbarActionItem(action: LiquidToolbarAction) {
    val performance = LocalLiquidGlassPerformance.current
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val itemShape = remember { Capsule() }
    val compactBadge = remember(action.badge) { LiquidFloatingToolbarMath.compactBadge(action.badge) }

    val targetContainer =
        when {
            !action.enabled -> Color.Transparent
            action.destructive && action.selected -> colorScheme.error.copy(alpha = 0.24f)
            action.selected -> glassColors.selectedContainer
            else -> Color.Transparent
        }
    val targetContent =
        when {
            !action.enabled -> glassColors.disabledContent
            action.destructive -> glassColors.error
            action.selected -> colorScheme.primary
            else -> glassColors.content
        }

    val containerColor by
        LiquidFloatingToolbarMotion.animateColor(
            targetValue = targetContainer,
            performance = performance,
            label = "toolbarActionContainer",
        )
    val contentColor by
        LiquidFloatingToolbarMotion.animateColor(
            targetValue = targetContent,
            performance = performance,
            label = "toolbarActionContent",
        )

    val hasLabel = !action.label.isNullOrBlank()

    Box(
        modifier =
            Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
                .semantics {
                    role = Role.Button
                    selected = action.selected
                    contentDescription = action.contentDescription
                }
                .liquidClickable(
                    shape = itemShape,
                    enabled = action.enabled,
                    onClick = action.onClick,
                )
                .clip(itemShape)
                .background(containerColor)
                .padding(horizontal = if (hasLabel) 14.dp else 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp),
                )
                if (compactBadge != null) {
                    Box(
                        modifier =
                            Modifier.align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-6).dp)
                                .clip(Capsule())
                                .background(colorScheme.primary)
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = compactBadge,
                            maxLines = 1,
                            softWrap = false,
                            style =
                                TextStyle(
                                    color = colorScheme.onPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                    }
                }
            }
            if (hasLabel) {
                BasicText(
                    text = action.label.orEmpty(),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                    style =
                        TextStyle(
                            color = contentColor,
                            fontSize = 13.sp,
                            fontWeight =
                                if (action.selected) FontWeight.SemiBold else FontWeight.Medium,
                        ),
                )
            }
        }
    }
}
