package com.infojobs.base.compose.components.dialog

import androidx.annotation.StringRes
import androidx.compose.ui.text.AnnotatedString

sealed interface AppDialogResource {
  data class AsAnnotatedString(val annotatedString: AnnotatedString) : AppDialogResource
  data class AsString(val string: String) : AppDialogResource
  class AsResource(
    @get:StringRes val stringRes: Int,
    vararg val formatArgs: Any,
  ) : AppDialogResource
}
