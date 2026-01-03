package com.alorma.caducity.ui.components.snackbar

import androidx.annotation.StringRes

sealed interface AppSnackbarResource {
  data class AsString(val string: String) : AppSnackbarResource
  data class AsResource(@get:StringRes val stringRes: Int) : AppSnackbarResource
}
