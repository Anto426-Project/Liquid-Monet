package com.anto426.liquidmonet.components.feedback.toast.state

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Immutable data payload for LiquidToast. */
@Immutable
data class LiquidToastData(
    val id: Long,
    val message: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val durationMillis: Long = 2800L,
)

/**
 * Owns toast events, queueing and dismissal independently of rendering. Commands are serialized in
 * [scope]; pass a lifecycle-bound scope from the consumer.
 */
@Stable
class LiquidToastState(private val scope: CoroutineScope) {
    var currentToast by mutableStateOf<LiquidToastData?>(null)
        private set

    private var dismissJob: Job? = null
    private var nextId = 0L
    private val pendingToasts = ArrayDeque<LiquidToastData>()
    private val commands = Mutex()

    fun show(
        message: String,
        subtitle: String? = null,
        icon: ImageVector? = null,
        durationMillis: Long = 2800L,
    ) {
        if (message.isBlank()) return
        scope.launch {
            commands.withLock {
                val data =
                    LiquidToastData(
                        id = ++nextId,
                        message = message,
                        subtitle = subtitle,
                        icon = icon,
                        durationMillis = durationMillis.coerceIn(900L, 30_000L),
                    )
                if (currentToast == null) {
                    present(data)
                } else if (
                    !currentToast!!.sameContentAs(data) &&
                        pendingToasts.none { it.sameContentAs(data) }
                ) {
                    // Keep pending notifications bounded even when a producer floods the host.
                    if (pendingToasts.size == 32) pendingToasts.removeFirst()
                    pendingToasts.addLast(data)
                }
            }
        }
    }

    private fun present(data: LiquidToastData) {
        dismissJob?.cancel()
        currentToast = data
        dismissJob = scope.launch {
            delay(data.durationMillis)
            commands.withLock {
                if (currentToast?.id == data.id) showNext()
            }
        }
    }

    fun dismiss() {
        scope.launch {
            commands.withLock {
                dismissJob?.cancel()
                showNext()
            }
        }
    }

    private fun showNext() {
        val next = if (pendingToasts.isEmpty()) null else pendingToasts.removeFirst()
        currentToast = null
        if (next != null) present(next)
    }
}

private fun LiquidToastData.sameContentAs(other: LiquidToastData): Boolean =
    message == other.message && subtitle == other.subtitle && icon == other.icon
