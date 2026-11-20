package com.anto426.liquidmonet.components.cards.accordion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anto426.liquidmonet.components.cards.accordion.motion.LiquidAccordionMotion
import com.anto426.liquidmonet.components.cards.card.LiquidCard
import com.anto426.liquidmonet.components.cards.card.LiquidCardColors
import com.anto426.liquidmonet.components.cards.card.LiquidCardDefaults
import com.anto426.liquidmonet.components.display.divider.LiquidHorizontalDivider
import com.anto426.liquidmonet.components.display.iconcontainer.liquidIconContainer
import com.anto426.liquidmonet.components.interaction.liquidClickable
import com.anto426.liquidmonet.components.layout.expandablecontent.LiquidExpandableContent
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/** Surface, spacing and child clipping for an expandable [LiquidCard]. */
@Immutable
data class LiquidExpandableCardStyle(
    val shape: Shape = RoundedRectangle(24.dp),
    val colors: LiquidCardColors? = null,
    val headerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    val contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
    val showDivider: Boolean = true,
    val clipContent: Boolean = true,
)

/** An expandable card with a text header and independently interactive body. */
@Composable
fun LiquidAccordionItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable () -> Unit,
) = LiquidExpandableCard(
    title = title,
    modifier = modifier,
    subtitle = subtitle,
    leadingIcon = leadingIcon,
    isExpanded = isExpanded,
    onExpandedChange = onExpandedChange,
    backdropState = backdropState,
    content = content,
)

/** An expandable card using the default Liquid Card surface and spacing. */
@Composable
fun LiquidExpandableCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable () -> Unit,
) = LiquidExpandableCard(
    title = title,
    style = LiquidExpandableCardStyle(),
    modifier = modifier,
    subtitle = subtitle,
    leadingIcon = leadingIcon,
    isExpanded = isExpanded,
    onExpandedChange = onExpandedChange,
    backdropState = backdropState,
    content = content,
)

/**
 * The header owns the expand action and uses the same press and drag physics as Liquid controls.
 * Controls in [content] own their interactions. [style] controls the same surface and clipping
 * options as [LiquidCard], plus the spacing of the header and body.
 */
@Composable
fun LiquidExpandableCard(
    title: String,
    style: LiquidExpandableCardStyle,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    backdropState: Backdrop = emptyBackdrop(),
    content: @Composable () -> Unit,
) {
    val performance = LocalLiquidGlassPerformance.current
    val glassColors = LiquidGlassTheme.colors
    val opening = LiquidAccordionMotion.animateOpeningProgress(isExpanded, performance)
    val rotation = LiquidAccordionMotion.animateChevronRotation(isExpanded, performance)
    val (enter, exit) = LiquidAccordionMotion.transitions(performance)

    LiquidCard(
        modifier = modifier.graphicsLayer {
            val deformation = LiquidAccordionMotion.deformation(opening.value)
            scaleX = 1f - deformation * 0.016f
            scaleY = 1f + deformation * 0.022f
            transformOrigin = TransformOrigin(0.5f, 0f)
        },
        backdropState = backdropState,
        shape = style.shape,
        colors = style.colors ?: LiquidCardDefaults.colors(),
        contentPadding = PaddingValues(0.dp),
        clipContent = style.clipContent,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .semantics {
                        if (isExpanded) {
                            collapse {
                                onExpandedChange(false)
                                true
                            }
                        } else {
                            expand {
                                onExpandedChange(true)
                                true
                            }
                        }
                    }
                    .liquidClickable(shape = style.shape, onClick = { onExpandedChange(!isExpanded) })
                    .padding(style.headerPadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = LocalContentColor.current,
                        modifier = Modifier
                            .graphicsLayer {
                                val scale = 1f + LiquidAccordionMotion.deformation(opening.value) * 0.08f
                                scaleX = scale
                                scaleY = scale
                            }
                            .liquidIconContainer(containerColor = glassColors.neutralContainer),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (style.colors != null) LocalContentColor.current
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = LiquidIcons.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer {
                            val scale = 1f + LiquidAccordionMotion.deformation(opening.value) * 0.12f
                            scaleX = scale
                            scaleY = scale
                        }
                        .liquidIconContainer(
                            containerSize = 32.dp,
                            iconSize = 20.dp,
                            containerColor = glassColors.neutralContainer,
                            shape = CircleShape,
                        )
                        .graphicsLayer { rotationZ = rotation.value },
                    tint = LocalContentColor.current,
                )
            }

            LiquidExpandableContent(
                visible = isExpanded,
                enter = enter,
                exit = exit,
                label = "AccordionBody",
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(style.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (style.showDivider) LiquidHorizontalDivider()
                    content()
                }
            }
        }
    }
}
