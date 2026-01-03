package com.alorma.caducity.ui.components.scaffold

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHost
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.LocalAppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarHostState
import com.infojobs.base.compose.components.dialog.AppDialogState
import com.infojobs.base.compose.components.dialog.LocalAppDialogState
import com.infojobs.base.compose.components.dialog.rememberAppDialogState

@Suppress("ModifierTopMost")
@Composable
fun AppScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  snackbarState: AppSnackbarHostState = rememberAppSnackbarHostState(),
  dialogState: AppDialogState = rememberAppDialogState(),
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = contentColorFor(containerColor),
  contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
  content: @Composable (PaddingValues) -> Unit,
) {
  CompositionLocalProvider(
    LocalAppSnackbarHostState provides snackbarState,
    LocalAppDialogState provides dialogState,
  ) {
    Scaffold(
      modifier = modifier,
      topBar = topBar,
      bottomBar = bottomBar,
      snackbarHost = { AppSnackbarHost(LocalAppSnackbarHostState.current) },
      floatingActionButton = floatingActionButton,
      floatingActionButtonPosition = floatingActionButtonPosition,
      containerColor = containerColor,
      contentColor = contentColor,
      contentWindowInsets = contentWindowInsets,
      content = content,
    )
  }
}