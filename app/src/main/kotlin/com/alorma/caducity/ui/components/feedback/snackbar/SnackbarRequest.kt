package com.alorma.caducity.ui.components.feedback.snackbar

import androidx.annotation.StringRes
import com.alorma.caducity.ui.components.feedback.AppFeedbackType

/**
 * A snackbar to show in a screen's own scaffold, e.g. the «Desfés» shown after an action or after
 * the actions sheet closes. [onAction] runs when the action is tapped.
 */
data class SnackbarRequest(
  @StringRes val message: Int,
  val messageArgs: List<Any> = emptyList(),
  @StringRes val actionLabel: Int? = null,
  val type: AppFeedbackType = AppFeedbackType.Info,
  val onAction: (suspend () -> Unit)? = null,
)
