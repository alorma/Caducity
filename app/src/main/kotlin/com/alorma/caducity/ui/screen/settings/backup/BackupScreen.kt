package com.alorma.caducity.ui.screen.settings.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Backup
import com.alorma.caducity.base.ui.icons.Restore
import com.alorma.caducity.feature.backup.BackupFileHandler
import com.alorma.caducity.ui.components.NavigationIcon
import com.alorma.caducity.ui.components.StyledTopAppBar
import com.alorma.caducity.ui.components.feedback.AppFeedbackResource
import com.alorma.caducity.ui.components.feedback.AppFeedbackType
import com.alorma.caducity.ui.components.feedback.dialog.AppDialogState
import com.alorma.caducity.ui.components.feedback.dialog.DialogResult
import com.alorma.caducity.ui.components.feedback.dialog.rememberAppDialogState
import com.alorma.caducity.ui.components.feedback.snackbar.AppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.LocalAppSnackbarHostState
import com.alorma.caducity.ui.components.feedback.snackbar.rememberAppSnackbarHostState
import com.alorma.caducity.ui.components.loading.FullscreenLoading
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupScreen(
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: BackupViewModel = koinViewModel(),
  backupFileHandler: BackupFileHandler = koinInject()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  backupFileHandler.registerContracts()

  LaunchedEffect(backupFileHandler.result) {
    backupFileHandler.result.collect { result ->
      viewModel.onBackupResult(result)
    }
  }

  val dialogState = rememberAppDialogState()
  val snackbarHostState = LocalAppSnackbarHostState.current

  // Handle side effects (success/error messages)
  LaunchedEffect(viewModel.sideEffect) {
    viewModel.sideEffect.collect { sideEffect ->
      when (sideEffect) {
        is BackupSideEffect.ExportSuccess -> {
          launch {
            snackbarHostState.showSnackbar(
              message = R.string.backup_export_success,
              type = AppFeedbackType.Success,
            )
          }
        }

        is BackupSideEffect.RestoreSuccess -> {
          launch {
            snackbarHostState.showSnackbar(
              message = R.string.backup_restore_success,
              type = AppFeedbackType.Success,
            )
          }
        }

        is BackupSideEffect.Error -> {
          launch {
            val errorMessage = when (sideEffect.error) {
              is BackupError.ExportFailed -> R.string.backup_error_export_failed
              is BackupError.RestoreFailed -> R.string.backup_error_restore_failed
              is BackupError.InvalidFile -> R.string.backup_error_invalid_file
              is BackupError.VersionMismatch -> R.string.backup_error_version_mismatch
            }
            snackbarHostState.showSnackbar(
              message = errorMessage,
              type = AppFeedbackType.Error,
            )
          }
        }

        is BackupSideEffect.ConfirmRestore -> {
          val result = dialogState.showAlertDialog(
            type = AppFeedbackType.Success,
            title = AppFeedbackResource.AsResource(R.string.backup_restore_warning_title),
            text = AppFeedbackResource.AsResource(R.string.backup_restore_warning_message),
            positiveButton = AppFeedbackResource.AsResource(R.string.backup_restore_confirm),
            negativeButton = AppFeedbackResource.AsResource(R.string.backup_cancel),
          )

          if (result is DialogResult.Positive) {
            viewModel.onRestoreConfirmed(sideEffect.uri)
          }
        }
      }
    }
  }


  Box(modifier) {
    when (uiState) {
      BackupUiState.Loading -> FullscreenLoading()
      BackupUiState.Idle -> BackupScreenContent(
        dialogState = dialogState,
        snackbarHostState = snackbarHostState,
        onExport = { backupFileHandler.createBackup() },
        onRestore = { backupFileHandler.selectBackup() },
        onClose = onClose,
      )
    }
  }
}

@Composable
private fun BackupScreenContent(
  dialogState: AppDialogState,
  snackbarHostState: AppSnackbarHostState,
  onExport: () -> Unit,
  onRestore: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AppScaffold(
    modifier = Modifier.then(modifier),
    dialogState = dialogState,
    snackbarState = snackbarHostState,
    topBar = {
      StyledTopAppBar(
        navigationIcon = { NavigationIcon() },
        title = {
          Text(
            text = stringResource(R.string.settings_backup_title),
          )
        },
      )
    },
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
      // Export & Restore Group
      StyledSettingsGroup {
        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Backup,
              contentDescription = null,
            )
          },
          title = stringResource(R.string.backup_export_title),
          subtitle = stringResource(R.string.backup_export_description),
          onClick = onExport,
          position = ShapePosition.Start,
        )

        StyledSettingsCard(
          icon = {
            Icon(
              imageVector = AppIcons.Restore,
              contentDescription = null,
            )
          },
          title = stringResource(R.string.backup_restore_title),
          subtitle = stringResource(R.string.backup_restore_description),
          onClick = onRestore,
          position = ShapePosition.End,
        )
      }
    }
  }
}

@PreviewDynamicLightDark
@Composable
fun BackupScreenContentPreview() {
  PreviewTheme {
    Surface {
      BackupScreenContent(
        dialogState = rememberAppDialogState(),
        snackbarHostState = rememberAppSnackbarHostState(),
        onExport = {},
        onRestore = {},
        onClose = {},
      )
    }
  }
}