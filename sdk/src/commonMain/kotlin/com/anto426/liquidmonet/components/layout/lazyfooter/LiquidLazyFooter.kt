package com.anto426.liquidmonet.components.layout.lazyfooter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.anto426.liquidmonet.components.internal.LiquidHapticCue
import com.anto426.liquidmonet.components.internal.performLiquidHaptic
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidAnimatedSwitcher
import com.anto426.liquidmonet.components.layout.animatedswitcher.LiquidSwitcherTransition
import com.anto426.liquidmonet.components.layout.lazyfooter.motion.LiquidLazyFooterMotion
import com.anto426.liquidmonet.glass.LiquidGlassRole
import com.anto426.liquidmonet.glass.liquidGlass
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import com.anto426.liquidmonet.icons.LiquidIcons
import com.anto426.liquidmonet.motion.LiquidFloatMotion
import com.anto426.liquidmonet.theme.LiquidGlassTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.shapes.RoundedRectangle

/** Scroll axis of the lazy container hosting [LiquidLazyFooter]. */
enum class LiquidLazyFooterOrientation {
    Vertical,
    Horizontal,
}

/** Visible state rendered by [LiquidLazyFooter]. */
enum class LiquidLazyFooterState {
    Hidden,
    Loading,
    End,
    Error,
}

/**
 * One reusable endpoint for both `LazyColumn` and `LazyRow`.
 *
 * Place it inside a lazy `item { ... }` and match [orientation] to the list scroll axis. The root
 * is the only glass layer: progress, status and retry content do not create nested glass panels.
 * Accepts optional [backdrop] to sample the same refraction plane as neighbouring cards.
 */
@Composable
fun LiquidLazyFooter(
    state: LiquidLazyFooterState,
    modifier: Modifier = Modifier,
    orientation: LiquidLazyFooterOrientation = LiquidLazyFooterOrientation.Vertical,
    loadingLabel: String? = null,
    endLabel: String? = null,
    errorLabel: String? = null,
    retryLabel: String? = null,
    onRetry: (() -> Unit)? = null,
    shape: Shape = RoundedRectangle(24.dp),
    backdrop: Backdrop = emptyBackdrop(),
) {
    val performance = LocalLiquidGlassPerformance.current
    val hapticFeedback = LocalHapticFeedback.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val visible = state != LiquidLazyFooterState.Hidden
    var lastVisibleState by remember {
        mutableStateOf(
            state.takeUnless { it == LiquidLazyFooterState.Hidden } ?: LiquidLazyFooterState.Loading
        )
    }
    val renderedState = state.takeUnless { it == LiquidLazyFooterState.Hidden } ?: lastVisibleState
    val liquidImpulse = remember { LiquidFloatMotion(0f) }
    val transformOrigin = TransformOrigin.Center
    var hasAnimatedEntrance by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state == LiquidLazyFooterState.Hidden) {
            liquidImpulse.snapTo(0f)
        } else {
            val previous = lastVisibleState
            lastVisibleState = state
            if (hasAnimatedEntrance && previous != state) {
                liquidImpulse.snapTo(1f)
                liquidImpulse.animateTo(
                    targetValue = 0f,
                    animationSpec = LiquidLazyFooterMotion.impulseSpring(performance),
                )
            } else {
                hasAnimatedEntrance = true
            }
        }
    }

    val enterTransition =
        remember(performance, orientation) {
            LiquidLazyFooterMotion.enter(performance, orientation)
        }
    val exitTransition =
        remember(performance, orientation) { LiquidLazyFooterMotion.exit(performance, orientation) }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier.zIndex(2f),
        enter = enterTransition,
        exit = exitTransition,
    ) {
        val baseOrientationModifier =
            when (orientation) {
                LiquidLazyFooterOrientation.Vertical ->
                    Modifier.fillMaxWidth().heightIn(min = 56.dp)

                LiquidLazyFooterOrientation.Horizontal ->
                    Modifier.fillMaxHeight().widthIn(min = 156.dp)
            }

        Box(
            modifier =
                baseOrientationModifier
                    .graphicsLayer {
                        val impulse = liquidImpulse.value
                        this.transformOrigin = transformOrigin
                        clip = false
                        when (orientation) {
                            LiquidLazyFooterOrientation.Vertical -> {
                                scaleX = 1f + impulse * 0.02f
                                scaleY = 1f - impulse * 0.02f
                            }

                            LiquidLazyFooterOrientation.Horizontal -> {
                                scaleX = 1f - impulse * 0.02f
                                scaleY = 1f + impulse * 0.02f
                            }
                        }
                    }
                    .liquidGlass(
                        backdrop = backdrop,
                        shape = shape,
                        role = LiquidGlassRole.Surface,
                    )
                    .then(
                        if (state == LiquidLazyFooterState.Error && onRetry != null) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Button,
                            ) {
                                hapticFeedback.performLiquidHaptic(LiquidHapticCue.Action)
                                onRetry()
                            }
                        } else {
                            Modifier
                        }
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            LiquidAnimatedSwitcher(
                targetState = renderedState,
                transition = LiquidSwitcherTransition.LiquidMorph,
                label = "LiquidLazyFooterState",
            ) { currentState ->
                LiquidLazyFooterContent(
                    state = currentState,
                    orientation = orientation,
                    loadingLabel = loadingLabel,
                    endLabel = endLabel,
                    errorLabel = errorLabel,
                    retryLabel = retryLabel,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun LiquidLazyFooterContent(
    state: LiquidLazyFooterState,
    orientation: LiquidLazyFooterOrientation,
    loadingLabel: String?,
    endLabel: String?,
    errorLabel: String?,
    retryLabel: String?,
    onRetry: (() -> Unit)?,
) {
    val colorScheme = MaterialTheme.colorScheme
    val stateLabel =
        when (state) {
            LiquidLazyFooterState.Hidden -> null
            LiquidLazyFooterState.Loading -> loadingLabel
            LiquidLazyFooterState.End -> endLabel
            LiquidLazyFooterState.Error -> errorLabel
        }

    if (orientation == LiquidLazyFooterOrientation.Horizontal) {
        Column(
            modifier =
                Modifier.then(
                    if (stateLabel != null) Modifier.semantics { stateDescription = stateLabel }
                    else Modifier
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ) {
            when (state) {
                LiquidLazyFooterState.Hidden -> Unit

                LiquidLazyFooterState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorScheme.primary,
                        strokeWidth = 2.5.dp,
                    )
                }

                LiquidLazyFooterState.End -> {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }

                LiquidLazyFooterState.Error -> {
                    Icon(
                        imageVector = LiquidIcons.Warning,
                        contentDescription = null,
                        tint = colorScheme.error,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            if (stateLabel != null) {
                Text(
                    text = stateLabel,
                    color =
                        if (state == LiquidLazyFooterState.Error) {
                            colorScheme.error
                        } else {
                            LiquidGlassTheme.colors.secondaryContent
                        },
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (state == LiquidLazyFooterState.Error && retryLabel != null && onRetry != null) {
                Text(
                    text = retryLabel,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    } else {
        Row(
            modifier =
                Modifier.then(
                    if (stateLabel != null) Modifier.semantics { stateDescription = stateLabel }
                    else Modifier
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (state) {
                LiquidLazyFooterState.Hidden -> Unit

                LiquidLazyFooterState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colorScheme.primary,
                        strokeWidth = 2.5.dp,
                    )
                }

                LiquidLazyFooterState.End -> {
                    Icon(
                        imageVector = LiquidIcons.Check,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                LiquidLazyFooterState.Error -> {
                    Icon(
                        imageVector = LiquidIcons.Warning,
                        contentDescription = null,
                        tint = colorScheme.error,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            if (stateLabel != null) {
                Text(
                    text = stateLabel,
                    color =
                        if (state == LiquidLazyFooterState.Error) {
                            colorScheme.error
                        } else {
                            LiquidGlassTheme.colors.secondaryContent
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (state == LiquidLazyFooterState.Error && retryLabel != null && onRetry != null) {
                Text(
                    text = retryLabel,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
