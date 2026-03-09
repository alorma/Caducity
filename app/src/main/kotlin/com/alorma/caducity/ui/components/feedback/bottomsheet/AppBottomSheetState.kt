@file:OptIn(ExperimentalMaterial3Api::class)

package com.alorma.caducity.ui.components.feedback.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.alorma.caducity.ui.components.feedback.AppFeedbackType

@Composable
fun rememberAppBottomSheetState(skipHalfExpanded: Boolean = false): AppBottomSheetState {
  val materialModalBottomSheetState =
    rememberModalBottomSheetState(skipPartiallyExpanded = skipHalfExpanded)
  return remember {
    AppBottomSheetState(
      sheetState = materialModalBottomSheetState,
    )
  }
}

val LocalAppBottomSheetState =
  compositionLocalOf<AppBottomSheetState> {
    throw (kotlin.IllegalStateException("Should be provided from a AppBottomSheetState"))
  }

class AppBottomSheetState(
  val sheetState: SheetState,
) {
  internal var showSheet: Boolean by mutableStateOf(false)
  internal var sheetContent by mutableStateOf<(@Composable () -> Unit)>({})
  internal var onDismissRequest by mutableStateOf({})

  fun show(
    onDismissRequest: () -> Unit = {},
    appFeedbackType: AppFeedbackType = AppFeedbackType.Info,
    content: @Composable () -> Unit,
  ) {
    this.onDismissRequest = {
      preDismiss()
      onDismissRequest()
    }
    sheetContent = {
      AppModalBottomSheet(
        bottomSheetState = this,
        appFeedbackType = appFeedbackType,
        content = content,
      )
    }
    showSheet = true
  }

  private fun preDismiss() {
    sheetContent = {}
    showSheet = false
  }

  suspend fun hide() {
    preDismiss()
    sheetState.hide()
  }
}
