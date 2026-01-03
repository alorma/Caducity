package com.alorma.caducity.ui.screen.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alorma.caducity.R
import com.alorma.caducity.base.ui.icons.AppIcons
import com.alorma.caducity.base.ui.icons.Backup
import com.alorma.caducity.base.ui.icons.Restore
import com.alorma.caducity.feature.backup.BackupFileHandler
import com.alorma.caducity.ui.components.StyledCenterAlignedTopAppBar
import com.alorma.caducity.ui.components.scaffold.AppScaffold
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.snackbar.AppSnackbarType
import com.alorma.caducity.ui.components.snackbar.LocalAppSnackbarHostState
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupScreen(
  modifier: Modifier = Modifier,
  viewModel: BackupViewModel = koinViewModel(),
  backupFileHandler: BackupFileHandler = koinInject()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val showRestoreDialog by viewModel.showRestoreDialog.collectAsStateWithLifecycle()
  val snackbarHostState = LocalAppSnackbarHostState.current

  val exportBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument("application/json")
  ) { uri ->
    uri?.let { viewModel.onExportBackup(it) }
  }

  val restoreBackupLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri ->
    uri?.let { viewModel.onRestoreBackupRequest(it) }
  }

  // Handle side effects (success/error messages)
  LaunchedEffect(viewModel.sideEffect) {
    viewModel.sideEffect.collect { sideEffect ->
      when (sideEffect) {
        is BackupSideEffect.ExportSuccess -> {
          launch {
            snackbarHostState.showSnackbar(
              message = R.string.backup_export_success,
              type = AppSnackbarType.Success,
            )
          }
        }

        is BackupSideEffect.RestoreSuccess -> {
          launch {
            snackbarHostState.showSnackbar(
              message = R.string.backup_restore_success,
              type = AppSnackbarType.Success,
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
              type = AppSnackbarType.Error,
            )
          }
        }
      }
    }
  }

  AppScaffold(
    modifier = modifier,
    topBar = {
      StyledCenterAlignedTopAppBar(
        title = {
          Text(text = stringResource(R.string.backup_screen_title))
        },
      )
    },
    snackbarState = snackbarHostState,
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
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
            onClick = {
              exportBackupLauncher.launch(backupFileHandler.generateBackupFileName())
            },
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
            onClick = {
              restoreBackupLauncher.launch(arrayOf("application/json"))
            },
            position = ShapePosition.End,
          )
        }
      }

      // Loading indicator
      if (uiState is BackupUiState.Loading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }

  // Restore confirmation dialog
  if (showRestoreDialog) {
    AlertDialog(
      onDismissRequest = { viewModel.onRestoreCancelled() },
      title = {
        Text(text = stringResource(R.string.backup_restore_warning_title))
      },
      text = {
        Text(text = stringResource(R.string.backup_restore_warning_message))
      },
      confirmButton = {
        TextButton(onClick = { viewModel.onRestoreConfirmed() }) {
          Text(text = stringResource(R.string.backup_restore_confirm))
        }
      },
      dismissButton = {
        TextButton(onClick = { viewModel.onRestoreCancelled() }) {
          Text(text = stringResource(R.string.backup_cancel))
        }
      }
    )
  }
}
