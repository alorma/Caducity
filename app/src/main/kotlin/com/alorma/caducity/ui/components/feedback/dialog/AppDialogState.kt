package com.alorma.caducity.ui.components.feedback.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.softColors
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Creates a [AppDialogState].
 */
@Composable
fun rememberAppDialogState(): AppDialogState = remember { AppDialogState() }

val LocalAppDialogState = compositionLocalOf<AppDialogState> {
  throw (IllegalStateException("Should be provided from a AppDialogState"))
}

@Stable
class AppDialogState() {
  /**
   * Only one [Dialog] can be shown at a time.
   * Since a suspending Mutex is a fair queue, this manages our message queue
   * and we don't have to maintain one.
   */
  private val mutex = Mutex()

  var dialogInfo by mutableStateOf<DialogInfo?>(null)

  suspend fun showAlertDialog(
    title: @Composable () -> Unit,
    content: @Composable () -> Unit,
    positiveButton: @Composable () -> Unit,
    negativeButton: (@Composable () -> Unit)? = null,
    type: AppFeedbackType,
    properties: DialogProperties = DialogProperties(
      usePlatformDefaultWidth = true,
    ),
  ): DialogResult = mutex.withLock {
    try {
      suspendCancellableCoroutine { cancellation ->
        dialogInfo = object : DialogInfo.AlertDialog {
          override val title: @Composable () -> Unit = title
          override val content: @Composable () -> Unit = content
          override val positiveButton: @Composable (() -> Unit) = {
            val colors = type.softColors()

            TextButton(
              colors = ButtonDefaults.textButtonColors(
                containerColor = colors.container,
                contentColor = colors.onContainer,
              ),
              onClick = { dismiss(DialogResult.Positive) },
              content = { positiveButton() },
            )
          }
          override val negativeButton: @Composable (() -> Unit)? = if (negativeButton != null) {
            {
              val colors = type.softColors()

              TextButton(
                colors = ButtonDefaults.textButtonColors(
                  containerColor = colors.container,
                  contentColor = colors.onContainer,
                ),
                onClick = { dismiss(DialogResult.Negative) },
                content = { negativeButton() },
              )
            }
          } else {
            null
          }

          override val properties: DialogProperties = properties
          override val type: AppFeedbackType = type
          override val dismiss: (DialogResult) -> Unit = { result ->
            if (!cancellation.isCompleted) {
              cancellation.resume(result)
            }
            dialogInfo = null
          }
        }
      }
    } finally {
      dialogInfo = null
    }
  }

  suspend fun showDatePickerDialog(
    datePickerState: DatePickerState,
    positiveButton: @Composable (Boolean) -> Unit,
    negativeButton: (@Composable () -> Unit)? = null,
    type: AppFeedbackType,
    properties: DialogProperties = DialogProperties(
      usePlatformDefaultWidth = true,
    ),
  ): DialogResult = mutex.withLock {
    try {
      suspendCancellableCoroutine { cancellation ->
        dialogInfo = object : DialogInfo.CustomAlertDialog {
          override val content: @Composable (() -> Unit) = {
            val confirmEnabled by remember {
              derivedStateOf { datePickerState.selectedDateMillis != null }
            }

            val colors = type.softColors()

            DatePickerDialog(
              colors = DatePickerDefaults.colors(
                containerColor = colors.container,
                titleContentColor = colors.onContainer,
                navigationContentColor = colors.onContainer,
                disabledDayContentColor = colors.onContainer,
              ),
              onDismissRequest = {
                if (!cancellation.isCompleted) {
                  cancellation.resume(DialogResult.Positive)
                }
                dialogInfo = null
              },
              confirmButton = { positiveButton(confirmEnabled) },
              dismissButton = negativeButton,
            ) {
              DatePicker(state = datePickerState)
            }
          }
          override val properties: DialogProperties = properties
          override val dismiss: (DialogResult) -> Unit = { result ->
            if (!cancellation.isCompleted) {
              cancellation.resume(result)
            }
            dialogInfo = null
          }
        }
      }
    } finally {
      dialogInfo = null
    }
  }
}


sealed interface DialogInfo {
  val properties: DialogProperties
  val dismiss: (DialogResult) -> Unit

  fun dismiss(result: DialogResult = DialogResult.Dismissed) {
    this.dismiss.invoke(result)
  }

  interface AlertDialog : DialogInfo {
    val title: @Composable (() -> Unit)?
    val content: (@Composable () -> Unit)?
    val type: AppFeedbackType
    val negativeButton: (@Composable () -> Unit)?
    val positiveButton: @Composable () -> Unit
  }

  interface CustomAlertDialog : DialogInfo {
    val content: @Composable () -> Unit
  }
}

@Suppress("ModifierRequired")
@Composable
fun AppDialogHost(hostState: AppDialogState) {
  when (val currentDialogData = hostState.dialogInfo) {
    is DialogInfo.AlertDialog -> {
      val colors = currentDialogData.type.softColors()

      AlertDialog(
        properties = currentDialogData.properties,
        onDismissRequest = { currentDialogData.dismiss(DialogResult.Dismissed) },
        containerColor = colors.container,
        iconContentColor = colors.onContainer,
        titleContentColor = colors.onContainer,
        textContentColor = colors.onContainer,
        title = currentDialogData.title,
        text = currentDialogData.content,
        confirmButton = currentDialogData.positiveButton,
        dismissButton = currentDialogData.negativeButton,
      )
    }

    is DialogInfo.CustomAlertDialog -> {
      currentDialogData.content()
    }
    null -> {}
  }
}

sealed interface DialogResult {
  data object Dismissed : DialogResult
  data object Positive : DialogResult
  data object Negative : DialogResult
}
