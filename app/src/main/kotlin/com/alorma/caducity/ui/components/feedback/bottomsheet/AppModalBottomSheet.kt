package com.alorma.caducity.ui.components.feedback.bottomsheet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.CaducityTheme




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalBottomSheet(
  bottomSheetState: AppBottomSheetState,
  containerColor: Color = CaducityTheme.colorScheme.surfaceContainerHigh,
  contentColor: Color = contentColorFor(containerColor),
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
