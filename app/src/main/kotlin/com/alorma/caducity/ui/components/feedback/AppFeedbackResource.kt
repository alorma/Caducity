package com.alorma.caducity.ui.components.feedback

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString

sealed interface AppFeedbackResource {
  data class AsAnnotatedString(val annotatedString: AnnotatedString) : AppFeedbackResource
  data class AsString(val string: String) : AppFeedbackResource
  class AsResource(
    @get:StringRes val stringRes: Int,
    vararg val formatArgs: Any,
  ) : AppFeedbackResource
}

@Composable
fun exposeResource(ijDialogResource: AppFeedbackResource): AnnotatedString {
  return when (ijDialogResource) {
    is AppFeedbackResource.AsResource -> {
      if (ijDialogResource.formatArgs.isEmpty()) {
        AnnotatedString(stringResource(ijDialogResource.stringRes))
      } else {
        AnnotatedString(stringResource(ijDialogResource.stringRes, ijDialogResource.formatArgs))
      }
    }

    is AppFeedbackResource.AsString -> AnnotatedString(ijDialogResource.string)
    is AppFeedbackResource.AsAnnotatedString -> ijDialogResource.annotatedString
  }
}
