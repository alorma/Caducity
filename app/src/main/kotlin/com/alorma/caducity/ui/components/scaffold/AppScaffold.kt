package com.alorma.caducity.ui.components.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetHost
import com.alorma.caducity.ui.components.feedback.bottomsheet.AppBottomSheetState
import com.alorma.caducity.ui.components.feedback.bottomsheet.LocalAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.bottomsheet.rememberAppBottomSheetState
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogHost
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.LocalAppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHost
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.LocalAppSnackbarState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarState

@Suppress("ModifierTopMost")
@Composable
fun AppScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  bottomSheetState: AppBottomSheetState = rememberAppBottomSheetState(),
  dialogState: AppDialogState = rememberAppDialogState(),
  snackbarState: AppSnackbarState = rememberAppSnackbarState(),
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = contentColorFor(containerColor),
  contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
  content: @Composable (PaddingValues) -> Unit,
) {
  CompositionLocalProvider(
    LocalAppSnackbarState provides snackbarState,
    LocalAppDialogState provides dialogState,
    LocalAppBottomSheetState provides bottomSheetState,
  ) {
    Scaffold(
      modifier = modifier,
      topBar = topBar,
      bottomBar = bottomBar,
      snackbarHost = { AppSnackbarHost(LocalAppSnackbarState.current) },
      floatingActionButton = floatingActionButton,
      floatingActionButtonPosition = floatingActionButtonPosition,
      containerColor = containerColor,
      contentColor = contentColor,
      contentWindowInsets = contentWindowInsets,
    ) { paddingValues ->
      Box(
        modifier =
          Modifier
            .fillMaxSize()
            .consumeWindowInsets(paddingValues)
            .imePadding(),
      ) {
        content(paddingValues)
        AppDialogHost(LocalAppDialogState.current)
        AppBottomSheetHost(LocalAppBottomSheetState.current)
      }
    }
  }
}
