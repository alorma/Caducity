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
import com.alorma.caducity.ui.components.snackbar.rememberAppSnackbarHostState
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsCard
import com.alorma.caducity.ui.screen.settings.components.StyledSettingsGroup
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
  val snackbarHostState = rememberAppSnackbarHostState()

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

  // Handle success/error messages
  LaunchedEffect(uiState) {
    when (uiState) {
      is BackupUiState.ExportSuccess -> {
        snackbarHostState.showSnackbar(
          message = "Backup exported successfully",
        )
        viewModel.onSuccessDismissed()
      }

      is BackupUiState.RestoreSuccess -> {
        snackbarHostState.showSnackbar(
          message = "Backup restored successfully"
        )
        viewModel.onSuccessDismissed()
      }

      is BackupUiState.Error -> {
        val errorMessage = when (val error = (uiState as BackupUiState.Error).error) {
          is BackupError.ExportFailed -> "Failed to export backup"
          is BackupError.RestoreFailed -> "Failed to restore backup"
          is BackupError.InvalidFile -> "Invalid backup file"
          is BackupError.VersionMismatch -> "Incompatible backup version"
        }
        snackbarHostState.showSnackbar(errorMessage)
        viewModel.onErrorDismissed()
      }

      else -> {}
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
