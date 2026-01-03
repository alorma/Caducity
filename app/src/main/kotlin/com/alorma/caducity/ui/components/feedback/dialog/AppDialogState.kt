package com.infojobs.base.compose.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
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
    title: AppDialogResource,
    text: AppDialogResource,
    type: AppFeedbackType,
    negativeButton: AppDialogResource?,
    positiveButton: AppDialogResource,
    properties: DialogProperties = DialogProperties(),
  ): DialogResult = mutex.withLock {
    try {
      suspendCancellableCoroutine { cancellation ->
        dialogInfo = object : DialogInfo {
          override val title: @Composable () -> Unit = { Text(text = exposeResource(title)) }
          override val content: @Composable () -> Unit = { Text(text = exposeResource(text)) }
          override val positiveButton: @Composable (() -> Unit) = {
            TextButton(
              colors = ButtonDefaults.textButtonColors(),
              onClick = { dismiss(DialogResult.Positive) },
              content = { Text(text = exposeResource(positiveButton)) },
            )
          }
          override val negativeButton: @Composable (() -> Unit)? = if (negativeButton != null) {
            {
              TextButton(
                colors = ButtonDefaults.textButtonColors(),
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
  val content: @Composable () -> Unit
  val positiveButton: @Composable () -> Unit
  val negativeButton: (@Composable () -> Unit)?

  fun dismiss(result: DialogResult = DialogResult.Dismissed) {
    this.dismiss.invoke(result)
  }
}

@Suppress("ModifierRequired")
@Composable
fun IJDialogHost(hostState: AppDialogState) {
  val currentDialogData = hostState.dialogInfo

  if (currentDialogData != null) {
    val containerColor = when (val type = currentDialogData.type) {
      is AppFeedbackType.Status -> ExpirationDefaults.getColors(type.status).container
      AppFeedbackType.Error -> CaducityTheme.colorScheme.errorContainer
      AppFeedbackType.Info -> CaducityTheme.colorScheme.surfaceContainer
      AppFeedbackType.Success -> CaducityTheme.colorScheme.primaryContainer
    }
    val contentColor = when (val type = currentDialogData.type) {
      is AppFeedbackType.Status -> ExpirationDefaults.getColors(type.status).onContainer
      AppFeedbackType.Error -> CaducityTheme.colorScheme.onErrorContainer
      AppFeedbackType.Info -> CaducityTheme.colorScheme.onSurface
      AppFeedbackType.Success -> CaducityTheme.colorScheme.onPrimaryContainer
    }

    BasicAlertDialog(
      properties = currentDialogData.properties,
      onDismissRequest = { currentDialogData.dismiss(DialogResult.Dismissed) },
    ) {
      Surface(
        modifier = Modifier
          .wrapContentWidth()
          .wrapContentHeight(),
        shape = CaducityTheme.shapes.large,
        tonalElevation = AlertDialogDefaults.TonalElevation,
        color = contentColor,
        contentColor = containerColor,
      ) {
        Column {
          currentDialogData.title()
          currentDialogData.content()
          Spacer(modifier = Modifier.height(24.dp))
          FlowRow(
            modifier = Modifier
              .fillMaxWidth()
              .align(Alignment.End),
            horizontalArrangement = Arrangement.End,
          ) {
            currentDialogData.negativeButton?.invoke()
            currentDialogData.positiveButton.invoke()
          }
        }
      }
    }
  }
}

@Composable
fun exposeResource(ijDialogResource: AppDialogResource): AnnotatedString = when (ijDialogResource) {
  is AppDialogResource.AsResource -> {
    if (ijDialogResource.formatArgs.isEmpty()) {
      AnnotatedString(stringResource(ijDialogResource.stringRes))
    } else {
      AnnotatedString(stringResource(ijDialogResource.stringRes, ijDialogResource.formatArgs))
    }
  }

  is AppDialogResource.AsString -> AnnotatedString(ijDialogResource.string)
  is AppDialogResource.AsAnnotatedString -> ijDialogResource.annotatedString
}

sealed interface DialogResult {
  data object Dismissed : DialogResult
  data object Positive : DialogResult
  data object Negative : DialogResult
}
