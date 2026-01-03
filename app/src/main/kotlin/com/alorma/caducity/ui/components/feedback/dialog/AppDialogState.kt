package com.alorma.caducity.ui.components.feedback.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.DialogProperties
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.feedback.AppFeedbackResource
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.exposeResource
import com.alorma.caducity.ui.theme.CaducityTheme
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

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
    title: AppFeedbackResource,
    text: AppFeedbackResource,
    type: AppFeedbackType,
    positiveButton: AppFeedbackResource,
    properties: DialogProperties = DialogProperties(
      usePlatformDefaultWidth = true,
    ),
    negativeButton: AppFeedbackResource? = null,
  ): DialogResult = mutex.withLock {
    try {
      suspendCancellableCoroutine { cancellation ->
        dialogInfo = object : DialogInfo {
          override val title: @Composable () -> Unit = { Text(text = exposeResource(title)) }
          override val text: @Composable () -> Unit = { Text(text = exposeResource(text)) }
          override val positiveButton: @Composable (() -> Unit) = {
            val containerColor = containerColor(type)
            val contentColor = contentColor(type)

            TextButton(
              colors = ButtonDefaults.textButtonColors(
                containerColor = containerColor,
                contentColor = contentColor,
              ),
              onClick = { dismiss(DialogResult.Positive) },
              content = { Text(text = exposeResource(positiveButton)) },
            )
          }
          override val negativeButton: @Composable (() -> Unit)? = if (negativeButton != null) {
            {
              val containerColor = containerColor(type)
              val contentColor = contentColor(type)

              TextButton(
                colors = ButtonDefaults.textButtonColors(
                  containerColor = containerColor,
                  contentColor = contentColor,
                ),
                onClick = { dismiss(DialogResult.Negative) },
                content = { Text(text = exposeResource(negativeButton)) },
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
}

interface DialogInfo {
  val properties: DialogProperties
  val type: AppFeedbackType

  val dismiss: (DialogResult) -> Unit

  val title: @Composable () -> Unit
  val text: @Composable () -> Unit
  val positiveButton: @Composable () -> Unit
  val negativeButton: (@Composable () -> Unit)?

  fun dismiss(result: DialogResult = DialogResult.Dismissed) {
    this.dismiss.invoke(result)
  }
}

@Suppress("ModifierRequired")
@Composable
fun AppDialogHost(hostState: AppDialogState) {
  val currentDialogData = hostState.dialogInfo

  if (currentDialogData != null) {
    val containerColor = containerColor(currentDialogData.type)
    val contentColor = contentColor(currentDialogData.type)

    AlertDialog(
      properties = currentDialogData.properties,
      onDismissRequest = { currentDialogData.dismiss(DialogResult.Dismissed) },
      containerColor = containerColor,
      iconContentColor = contentColor,
      titleContentColor = contentColor,
      textContentColor = contentColor,
      title = currentDialogData.title,
      text = currentDialogData.text,
      confirmButton = currentDialogData.positiveButton,
      dismissButton = currentDialogData.negativeButton,
    )
  }
}

@Suppress("ContentEmission")
@Composable
private fun containerColor(feedbackType: AppFeedbackType): Color = when (feedbackType) {
  is AppFeedbackType.Status -> ExpirationDefaults.getSoftColors(
    instanceStatus = feedbackType.status,
  ).container

  AppFeedbackType.Error -> CaducityTheme.colorScheme.errorContainer
  AppFeedbackType.Info -> CaducityTheme.colorScheme.surfaceContainer
  AppFeedbackType.Success -> CaducityTheme.colorScheme.primaryContainer
}

@Suppress("ContentEmission")
@Composable
private fun contentColor(feedbackType: AppFeedbackType): Color = when (feedbackType) {
  is AppFeedbackType.Status -> ExpirationDefaults.getSoftColors(
    instanceStatus = feedbackType.status,
  ).onContainer

  AppFeedbackType.Error -> CaducityTheme.colorScheme.onErrorContainer
  AppFeedbackType.Info -> CaducityTheme.colorScheme.onSurface
  AppFeedbackType.Success -> CaducityTheme.colorScheme.onPrimaryContainer
}

sealed interface DialogResult {
  data object Dismissed : DialogResult
  data object Positive : DialogResult
  data object Negative : DialogResult
}
