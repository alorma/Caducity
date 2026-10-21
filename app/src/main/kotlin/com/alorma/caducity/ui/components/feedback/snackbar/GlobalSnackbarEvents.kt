package com.alorma.caducity.ui.components.feedback.snackbar

import androidx.annotation.StringRes
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A snackbar that must outlive the screen (or sheet) that asked for it, e.g. the «Desfés» shown after
 * the actions sheet closes. [onAction] runs when the action is tapped.
 */
data class SnackbarRequest(
  @StringRes val message: Int,
  val messageArgs: List<Any> = emptyList(),
  @StringRes val actionLabel: Int? = null,
  val type: AppFeedbackType = AppFeedbackType.Info,
  val onAction: (suspend () -> Unit)? = null,
)

/** Hands [SnackbarRequest]s to the app shell, which owns the host that shows them. */
class GlobalSnackbarEvents {
  private val channel = Channel<SnackbarRequest>(Channel.BUFFERED)
  val events: Flow<SnackbarRequest> = channel.receiveAsFlow()

  fun show(snackbar: SnackbarRequest) {
    channel.trySend(snackbar)
  }
}
