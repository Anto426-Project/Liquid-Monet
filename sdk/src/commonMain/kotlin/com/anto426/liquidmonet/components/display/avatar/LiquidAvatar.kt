package com.anto426.liquidmonet.components.display.avatar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.motion.liquidControlLayer
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

enum class LiquidAvatarPresence {
    None,
    Online,
    Away,
    Busy,
    Offline,
}

/** Optical avatar; the original overload retains its circular defaults and binary API. */
@Composable
fun LiquidAvatar(
    modifier: Modifier = Modifier,
    initials: String? = null,
    icon: ImageVector? = if (initials == null) LiquidIcons.AccountCircle else null,
    presence: LiquidAvatarPresence = LiquidAvatarPresence.None,
    size: Dp = 44.dp,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    LiquidAvatar(
        shape = CircleShape, modifier = modifier, initials = initials, icon = icon,
        presence = presence, size = size, containerColor = containerColor,
        onClick = onClick, enabled = enabled, backdropState = backdropState, content = content,
    )
}

/**
 * Custom avatar body, typography, image/content slot and independent presence indicator.
 * Pass [shape] to select this overload. Set either border to null to remove its outline.
 * Presence content can contain its own click action without activating the avatar action.
 */
@Composable
fun LiquidAvatar(
    shape: Shape,
    modifier: Modifier = Modifier,
    initials: String? = null,
    icon: ImageVector? = if (initials == null) LiquidIcons.AccountCircle else null,
    presence: LiquidAvatarPresence = LiquidAvatarPresence.None,
    size: Dp = 44.dp,
    containerColor: Color? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    backdropState: Backdrop = emptyBackdrop(),
    contentColor: Color = Color.Unspecified,
    border: BorderStroke? = BorderStroke(1.dp, LiquidGlassTheme.colors.outline),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initialsTextStyle: TextStyle = TextStyle.Default,
    iconSize: Dp = size * 0.55f,
    presenceColor: Color = Color.Unspecified,
    presenceSize: Dp = (size.value * 0.30f).coerceAtLeast(10f).dp,
    presenceShape: Shape = CircleShape,
    presenceBorder: BorderStroke? = BorderStroke(2.dp, MaterialTheme.colorScheme.surface),
    presenceAlignment: Alignment = Alignment.BottomEnd,
    presenceOffset: DpOffset = DpOffset(1.dp, 1.dp),
    presenceContent: (@Composable BoxScope.() -> Unit)? = null,
    content: (@Composable BoxScope.() -> Unit)? = null,
) {
    LiquidInputNormalization.positive(size, "LiquidAvatar size")
    LiquidInputNormalization.positive(iconSize, "LiquidAvatar iconSize")
    LiquidInputNormalization.positive(presenceSize, "LiquidAvatar presenceSize")
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)
    val colorScheme = MaterialTheme.colorScheme
    val colors = LiquidGlassTheme.colors
    val fill = containerColor ?: colors.accentContainer
    val foreground = if (contentColor.isSpecified) contentColor else LiquidGlassDefaults.contentColorFor(fill, colorScheme)
    val statusColor = if (presenceColor.isSpecified) presenceColor else when (presence) {
        LiquidAvatarPresence.Online -> colors.success
        LiquidAvatarPresence.Away -> colors.warning
        LiquidAvatarPresence.Busy -> colors.error
        LiquidAvatarPresence.Offline -> colors.secondaryContent
        LiquidAvatarPresence.None -> Color.Transparent
    }
    Box(
        modifier = modifier.size(size)
            .liquidClickable(shape = shape, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier.fillMaxSize()
                .liquidGlass(backdrop = effectiveBackdrop, shape = shape, role = LiquidGlassRole.Control, containerColor = fill)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .padding(contentPadding),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(LocalContentColor provides foreground) {
                if (content != null) content()
                else if (initials != null) Text(
                    initials.take(2).uppercase(), color = foreground,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold, fontSize = (size.value * 0.38f).sp,
                    ).merge(initialsTextStyle),
                )
                else if (icon != null) Icon(icon, null, Modifier.size(iconSize), tint = foreground)
            }
        }
        if (presence != LiquidAvatarPresence.None || presenceContent != null) {
            Box(
                Modifier.size(presenceSize).align(presenceAlignment)
                    .offset(presenceOffset.x, presenceOffset.y).clip(presenceShape).background(statusColor)
                    .then(if (presenceBorder != null) Modifier.border(presenceBorder, presenceShape) else Modifier),
                contentAlignment = Alignment.Center,
            ) { presenceContent?.invoke(this) }
        }
    }
}

/** LiquidAvatarGroup - Overlapping Stack of Liquid Glass Avatars with Tactile Interactions. */
@Composable
fun LiquidAvatarGroup(
    avatars: List<String>, // List of initials
    modifier: Modifier = Modifier,
    maxDisplay: Int = 4,
    size: Dp = 40.dp,
    onAvatarClick: ((Int) -> Unit)? = null,
    onOverflowClick: (() -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop(),
) {
    val safeMaxDisplay =
        LiquidInputNormalization.positive(
            maxDisplay,
            "LiquidAvatarGroup maxDisplay",
        )
    LiquidInputNormalization.positive(size, "LiquidAvatarGroup size")
    val glassColors = LiquidGlassTheme.colors
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val displayList = avatars.take(safeMaxDisplay)
    val overflowCount = (avatars.size - safeMaxDisplay).coerceAtLeast(0)
    val overlapSpacing = -(size * 0.28f)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(overlapSpacing),
    ) {
        displayList.forEachIndexed { index, initials ->
            LiquidAvatar(
                initials = initials,
                size = size,
                onClick = { onAvatarClick?.invoke(index) },
                backdropState = effectiveBackdrop,
            )
        }

        if (overflowCount > 0) {
            val overflowHighlight = rememberLiquidControlHighlight()

            Box(
                modifier =
                    Modifier.size(size)
                        .liquidControlLayer(true, overflowHighlight)
                        .liquidControlPressFeedback(
                            enabled = true,
                            interactiveHighlight = overflowHighlight,
                            shape = CircleShape,
                            drawHighlightOverlay = true,
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = { onOverflowClick?.invoke() },
                        )
                        .border(
                            1.5.dp,
                            glassColors.outline,
                            CircleShape,
                        )
                        .liquidGlass(
                            backdrop = effectiveBackdrop,
                            shape = Capsule(),
                            role = LiquidGlassRole.Navigation,
                            containerColor = glassColors.selectedContainer,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "+$overflowCount",
                    style =
                        MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = (size.value * 0.34f).sp,
                        ),
                    color = glassColors.content,
                )
            }
        }
    }
}
