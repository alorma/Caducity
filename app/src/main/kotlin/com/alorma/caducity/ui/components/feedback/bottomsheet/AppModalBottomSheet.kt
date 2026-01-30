package com.alorma.caducity.ui.components.feedback.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.softColors
import com.alorma.caducity.ui.theme.CaducityTheme




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
  bottomSheetState: AppBottomSheetState,
  appFeedbackType: AppFeedbackType,
  containerColor: Color = appFeedbackType.softColors().container,
  contentColor: Color = appFeedbackType.softColors().onContainer,
  content: @Composable () -> Unit,
) = with(bottomSheetState) {
  ModalBottomSheet(
    sheetState = bottomSheetState.sheetState,
    containerColor = containerColor,
    contentColor = contentColor,
    onDismissRequest = onDismissRequest,
    content = { content() },
  )
}
