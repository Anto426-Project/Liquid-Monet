package com.anto426.liquidmonet.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEvent.Companion.EDGE_RIGHT
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.anto426.liquidmonet.glass.runtime.LocalLiquidGlassPerformance
import kotlinx.coroutines.launch

@Stable
internal class LiquidPredictiveBackState(
    private val settledProgress: Animatable<Float, *>,
) {
    internal var liveProgress: Float? by mutableStateOf(null)
    internal var edgeDirectionState: Float by mutableFloatStateOf(1f)

    /** Read inside `graphicsLayer` to avoid recomposing on every back-gesture frame. */
    val progress: Float
        get() = liveProgress ?: settledProgress.value

    val edgeDirection: Float
        get() = edgeDirectionState
}

/**
 * KMP navigation-event bridge shared by Liquid navigation, dialogs and sheets.
 *
 * Live progress is exposed without a time-based filter so visuals stay attached to the finger.
 * Only a cancelled gesture is settled back to zero with Liquid motion.
 */
@Composable
internal fun rememberLiquidPredictiveBackState(
    onPredictiveBack: (() -> Unit)?,
    enabled: Boolean = onPredictiveBack != null,
): LiquidPredictiveBackState {
    val performance = LocalLiquidGlassPerformance.current
    val latestOnBack by rememberUpdatedState(onPredictiveBack)
    val coroutineScope = rememberCoroutineScope()
    val settledProgress = remember { Animatable(0f) }
    val stateHolder = remember(settledProgress) { LiquidPredictiveBackState(settledProgress) }
    val navigationState =
        rememberNavigationEventState(
            currentInfo = NavigationEventInfo.None,
            backInfo = listOf(NavigationEventInfo.None),
        )
    val inProgress =
        navigationState.transitionState as? NavigationEventTransitionState.InProgress
    val latestEvent = inProgress?.latestEvent
    val gestureProgress = latestEvent?.progress?.coerceIn(0f, 1f)

    stateHolder.liveProgress = gestureProgress
    if (latestEvent != null) {
        stateHolder.edgeDirectionState = if (latestEvent.swipeEdge == EDGE_RIGHT) -1f else 1f
    }

    LaunchedEffect(gestureProgress) {
        if (gestureProgress != null) settledProgress.snapTo(gestureProgress)
    }

    NavigationBackHandler(
        state = navigationState,
        isBackEnabled = enabled && onPredictiveBack != null,
        onBackCancelled = {
            coroutineScope.launch {
                settledProgress.animateTo(
                    targetValue = 0f,
                    animationSpec =
                        LiquidMotion.spring(
                            performance = performance,
                            dampingRatio = 0.88f,
                            stiffness = 480f,
                        ),
                )
            }
        },
        onBackCompleted = {
            latestOnBack?.invoke()
            coroutineScope.launch { settledProgress.snapTo(0f) }
        },
    )

    return stateHolder
}
