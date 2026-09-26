package com.anto426.liquidmonet.components.display

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
import com.anto426.liquidmonet.components.internal.liquidControlLayer
import com.anto426.liquidmonet.components.internal.liquidControlPressFeedback
import com.anto426.liquidmonet.components.internal.rememberLiquidControlHighlight
import com.anto426.liquidmonet.components.internal.LiquidInputNormalization
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.resolveLiquidGlassBackdrop
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.anto426.liquidmonet.theme.LiquidGlassDefaults
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.Capsule

enum class LiquidAvatarPresence {
    None,
    Online,
    Away,
    Busy,
    Offline
}

/**
 * LiquidAvatar - Optical Liquid Glass Avatar Component with Tactile Bounce Dynamics.
 */
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
    LiquidInputNormalization.positive(size, "LiquidAvatar size")
    val effectiveBackdrop = resolveLiquidGlassBackdrop(backdropState)

    val interactiveHighlight = rememberLiquidControlHighlight()
    val colorScheme = MaterialTheme.colorScheme
    val glassColors = LiquidGlassTheme.colors
    val resolvedContainerColor = containerColor ?: glassColors.accentContainer
    val avatarContentColor = LiquidGlassDefaults.contentColorFor(
        resolvedContainerColor,
        colorScheme
    )
    val presenceColor = when (presence) {
        LiquidAvatarPresence.Online -> glassColors.success
        LiquidAvatarPresence.Away -> glassColors.warning
        LiquidAvatarPresence.Busy -> glassColors.error
        LiquidAvatarPresence.Offline -> glassColors.secondaryContent
        LiquidAvatarPresence.None -> Color.Transparent
    }

    Box(
        modifier = modifier
            .size(size)
            .liquidControlLayer(enabled, interactiveHighlight)
            .liquidControlPressFeedback(
                enabled = enabled,
                interactiveHighlight = interactiveHighlight,
                shape = CircleShape,
                drawHighlightOverlay = true
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Glass Avatar Body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, glassColors.outline, CircleShape)
                .liquidGlass(
                    backdrop = effectiveBackdrop,
                    shape = Capsule(),
                    role = LiquidGlassRole.Control,
                    containerColor = resolvedContainerColor
                ),
            contentAlignment = Alignment.Center
        ) {
            if (content != null) {
                content()
            } else if (initials != null) {
                Text(
                    text = initials.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.38f).sp
                    ),
                    color = avatarContentColor
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = avatarContentColor,
                    modifier = Modifier.size(size * 0.55f)
                )
            }
        }

        // Presence Indicator Dot floating in the bottom corner unclipped
        if (presence != LiquidAvatarPresence.None) {
            val badgeSize = (size.value * 0.30f).coerceAtLeast(10f).dp
            Box(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .background(presenceColor)
                    .border(2.dp, colorScheme.surface, CircleShape)
            )
        }
    }
}

/**
 * LiquidAvatarGroup - Overlapping Stack of Liquid Glass Avatars with Tactile Interactions.
 */
@Composable
fun LiquidAvatarGroup(
    avatars: List<String>, // List of initials
    modifier: Modifier = Modifier,
    maxDisplay: Int = 4,
    size: Dp = 40.dp,
    onAvatarClick: ((Int) -> Unit)? = null,
    onOverflowClick: (() -> Unit)? = null,
    backdropState: Backdrop = emptyBackdrop()
) {
    val safeMaxDisplay = LiquidInputNormalization.positive(
        maxDisplay,
        "LiquidAvatarGroup maxDisplay"
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
        horizontalArrangement = Arrangement.spacedBy(overlapSpacing)
    ) {
        displayList.forEachIndexed { index, initials ->
            LiquidAvatar(
                initials = initials,
                size = size,
                onClick = { onAvatarClick?.invoke(index) },
                backdropState = effectiveBackdrop
            )
        }

        if (overflowCount > 0) {
            val overflowHighlight = rememberLiquidControlHighlight()

            Box(
                modifier = Modifier
                    .size(size)
                    .liquidControlLayer(true, overflowHighlight)
                    .liquidControlPressFeedback(
                        enabled = true,
                        interactiveHighlight = overflowHighlight,
                        shape = CircleShape,
                        drawHighlightOverlay = true
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Button,
                        onClick = { onOverflowClick?.invoke() }
                    )
                    .border(
                        1.5.dp,
                        glassColors.outline,
                        CircleShape
                    )
                    .liquidGlass(
                        backdrop = effectiveBackdrop,
                        shape = Capsule(),
                        role = LiquidGlassRole.Navigation,
                        containerColor = glassColors.selectedContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+$overflowCount",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = (size.value * 0.34f).sp
                    ),
                    color = glassColors.content
                )
            }
        }
    }
}
