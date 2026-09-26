package com.anto426.liquidmonet.motion

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** Shared finite animation bindings. Component physics remains in its owning motion package. */
@Composable
fun animateLiquidFloatAsState(
    targetValue: Float,
    animationSpec: FiniteAnimationSpec<Float>,
    label: String,
): State<Float> = animateFloatAsState(targetValue, animationSpec, label = label)

@Composable
fun animateLiquidColorAsState(
    targetValue: Color,
    animationSpec: FiniteAnimationSpec<Color>,
    label: String,
): State<Color> = animateColorAsState(targetValue, animationSpec, label = label)

@Composable
fun animateLiquidDpAsState(
    targetValue: Dp,
    animationSpec: FiniteAnimationSpec<Dp>,
    label: String,
): State<Dp> = animateDpAsState(targetValue, animationSpec, label = label)

/** One lifecycle-bound continuous animation owner; callers gate ornamental/functional activity. */
@Composable
fun rememberLiquidInfiniteTransition(label: String): InfiniteTransition =
    rememberInfiniteTransition(label = label)
