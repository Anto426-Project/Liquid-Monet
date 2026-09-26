package com.anto426.liquidmonet.components.navigation.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButton
import com.anto426.liquidmonet.components.buttons.iconbutton.LiquidIconButtonVariant
import com.anto426.liquidmonet.components.inputs.searchbar.LiquidSearchBar
import com.anto426.liquidmonet.components.menu.morphingaction.LiquidMorphingAction
import com.anto426.liquidmonet.components.navigation.topbar.motion.LiquidTopBarMotion
import com.anto426.liquidmonet.glass.LiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.LocalLiquidGlassContainerMode
import com.anto426.liquidmonet.glass.LocalLiquidGlassTopBarScrollBehavior
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.liquidTopBarZIndex
import com.anto426.liquidmonet.glass.overlay.LocalLiquidGlassContentBackdrop
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

data class LiquidTopBarAction(
    val icon: ImageVector,
    val label: String,
    val onClick: () -> Unit = {},
    val subItems: List<LiquidTopBarAction> = emptyList(),
    val iconRotation: Float = 0f,
    val selected: Boolean = false,
)

enum class LiquidTopBarVariant {
    Large,
    Medium,
    Compact,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiquidTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    statusBadge: String? = null,
    compactTitle: Boolean = false,
    expandedHeight: Dp = 136.dp,
    variant: LiquidTopBarVariant = LiquidTopBarVariant.Large,
    showNavigationIcon: Boolean = false,
    onNavigationClick: (() -> Unit)? = null,
    navigationIcon: @Composable () -> Unit = {},
    backdropState: Backdrop = emptyBackdrop(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    actionItems: List<LiquidTopBarAction> = emptyList(),
    isSearchActive: Boolean = false,
    searchQuery: String = "",
    onQueryChange: (String) -> Unit = {},
    onSearchActiveChange: ((Boolean) -> Unit)? = null,
    searchPlaceholder: String = "Cerca...",
    onHeightChanged: ((Dp) -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val effectiveScrollBehavior = scrollBehavior ?: LocalLiquidGlassTopBarScrollBehavior.current
    val performance = LocalLiquidGlassPerformance.current
    val density = LocalDensity.current
    val currentOnHeightChanged by rememberUpdatedState(onHeightChanged)

    val collapsedFraction = effectiveScrollBehavior?.state?.collapsedFraction ?: 0f
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer(clip = false)
                .onSizeChanged { size ->
                    currentOnHeightChanged?.invoke(with(density) { size.height.toDp() })
                }
                .liquidTopBarZIndex()
    ) {
        // Keep the glass clipped, not the controls. Top-bar actions can now overshoot their
        // nominal bounds during elastic feedback without being eaten by the bar container.
        Box(
            modifier =
                Modifier.matchParentSize()
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = RectangleShape,
                        role = LiquidGlassRole.TopBar,
                    )
        )

        Column(modifier = Modifier.fillMaxWidth().graphicsLayer(clip = false)) {
            // Actions reuse the same scene sample; Shared mode avoids a second optical pass.
            CompositionLocalProvider(
                LocalLiquidGlassContentBackdrop provides effectiveBackdrop,
                LocalLiquidGlassContainerMode provides LiquidGlassContainerMode.Shared,
            ) {
                val titleContent: @Composable () -> Unit = {
                    Column {
                        LiquidTopBarTitle(
                            title = title,
                            statusBadge = statusBadge,
                            compactTitle = compactTitle,
                            collapsedFraction = collapsedFraction,
                            variant = variant,
                        )
                        if (!subtitle.isNullOrBlank()) {
                            val subtitleAlpha = (1f - (collapsedFraction * 2.5f)).coerceIn(0f, 1f)
                            if (subtitleAlpha > 0.01f) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = subtitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.graphicsLayer { alpha = subtitleAlpha },
                                )
                            }
                        }
                    }
                }

                val navigationIconContent: @Composable () -> Unit = {
                    if (showNavigationIcon && onNavigationClick != null) {
                        LiquidIconButton(
                            icon = LiquidIcons.ArrowBack,
                            onClick = onNavigationClick,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp),
                            iconSize = 20.dp,
                            variant = LiquidIconButtonVariant.TopBarAction,
                            backdropState = effectiveBackdrop,
                        )
                    } else {
                        navigationIcon()
                    }
                }

                val actionsContent: @Composable RowScope.() -> Unit = {
                    actionItems.forEachIndexed { index, action ->
                        LiquidMorphingAction(
                            action = action,
                            isLastItem = index == actionItems.lastIndex,
                            backdropState = effectiveBackdrop,
                        )
                    }
                    actions?.invoke(this)
                }

                val barColors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    )
                val barModifier = Modifier.fillMaxWidth().graphicsLayer(clip = false)

                when (variant) {
                    LiquidTopBarVariant.Medium -> {
                        MediumTopAppBar(
                            modifier = barModifier,
                            title = titleContent,
                            navigationIcon = navigationIconContent,
                            actions = actionsContent,
                            colors = barColors,
                            scrollBehavior = effectiveScrollBehavior,
                        )
                    }
                    LiquidTopBarVariant.Large -> {
                        LargeTopAppBar(
                            modifier = barModifier,
                            title = titleContent,
                            navigationIcon = navigationIconContent,
                            actions = actionsContent,
                            expandedHeight = expandedHeight,
                            colors = barColors,
                            scrollBehavior = effectiveScrollBehavior,
                        )
                    }
                    LiquidTopBarVariant.Compact -> {
                        TopAppBar(
                            modifier = barModifier,
                            title = titleContent,
                            navigationIcon = navigationIconContent,
                            actions = actionsContent,
                            colors = barColors,
                            scrollBehavior = effectiveScrollBehavior,
                        )
                    }
                }

                // Expandable Liquid Glass Search Bar (Appears below the TopBar smoothly without
                // hiding the TopBar!)
                AnimatedVisibility(
                    visible = isSearchActive,
                    enter = remember(performance) { LiquidTopBarMotion.enter(performance) },
                    exit = remember(performance) { LiquidTopBarMotion.exit(performance) },
                ) {
                    Box(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    ) {
                        LiquidSearchBar(
                            query = searchQuery,
                            onQueryChange = onQueryChange,
                            placeholderText = searchPlaceholder,
                            backdropState = effectiveBackdrop,
                            onClose = {
                                onSearchActiveChange?.invoke(false)
                                onQueryChange("")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiquidTopBarTitle(
    title: String,
    statusBadge: String?,
    compactTitle: Boolean,
    collapsedFraction: Float,
    variant: LiquidTopBarVariant = LiquidTopBarVariant.Large,
) {
    val expandedSize =
        when {
            compactTitle -> 20f
            variant == LiquidTopBarVariant.Medium -> 24f
            variant == LiquidTopBarVariant.Compact -> 18f
            else -> 28f
        }
    val collapsedSize = 18f
    val currentSize = lerp(expandedSize, collapsedSize, collapsedFraction)
    val badgeAlpha = (1f - (collapsedFraction * 2.5f)).coerceIn(0f, 1f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Text(
            text = title,
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontSize = currentSize.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!statusBadge.isNullOrBlank() && badgeAlpha > 0.05f) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier =
                    Modifier.graphicsLayer { alpha = badgeAlpha }
                        .clip(Capsule())
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = statusBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
