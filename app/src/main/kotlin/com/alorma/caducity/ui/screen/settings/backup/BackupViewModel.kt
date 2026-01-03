package com.alorma.caducity.ui.screen.settings.backup

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.backup.ExportBackupUseCase
import com.alorma.caducity.domain.usecase.backup.ImportBackupUseCase
import com.alorma.caducity.feature.backup.BackupFileHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupViewModel(
  private val exportBackupUseCase: ExportBackupUseCase,
  private val importBackupUseCase: ImportBackupUseCase,
  private val backupFileHandler: BackupFileHandler
) : ViewModel() {

  private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
  val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

  private val _showRestoreDialog = MutableStateFlow(false)
  val showRestoreDialog: StateFlow<Boolean> = _showRestoreDialog.asStateFlow()

  private var pendingRestoreUri: Uri? = null

  fun onExportBackup(uri: Uri) {
    viewModelScope.launch {
      _uiState.value = BackupUiState.Loading
      try {
        exportBackupUseCase.export().fold(
          onSuccess = { backupData ->
            backupFileHandler.writeBackupToUri(uri, backupData).fold(
              onSuccess = {
                _uiState.value = BackupUiState.ExportSuccess
              },
              onFailure = { error ->
                _uiState.value = BackupUiState.Error(BackupError.ExportFailed(error.message))
              }
            )
          },
          onFailure = { error ->
            _uiState.value = BackupUiState.Error(BackupError.ExportFailed(error.message))
          }
        )
      } catch (e: Exception) {
        _uiState.value = BackupUiState.Error(BackupError.ExportFailed(e.message))
      }
    }
  }

  fun onRestoreBackupRequest(uri: Uri) {
    pendingRestoreUri = uri
    _showRestoreDialog.value = true
  }

  fun onRestoreConfirmed() {
    _showRestoreDialog.value = false
    val uri = pendingRestoreUri ?: return

    viewModelScope.launch {
      _uiState.value = BackupUiState.Loading
      try {
        backupFileHandler.readBackupFromUri(uri).fold(
          onSuccess = { backupData ->
            importBackupUseCase.import(backupData).fold(
              onSuccess = {
                _uiState.value = BackupUiState.RestoreSuccess
                pendingRestoreUri = null
              },
              onFailure = { error ->
                _uiState.value = when {
                  error.message?.contains("version", ignoreCase = true) == true ->
                    BackupUiState.Error(BackupError.VersionMismatch)
                  else ->
                    BackupUiState.Error(BackupError.RestoreFailed(error.message))
                }
                pendingRestoreUri = null
              }
            )
          },
          onFailure = { error ->
            _uiState.value = BackupUiState.Error(BackupError.InvalidFile(error.message))
            pendingRestoreUri = null
          }
        )
      } catch (e: Exception) {
        _uiState.value = BackupUiState.Error(BackupError.InvalidFile(e.message))
        pendingRestoreUri = null
      }
    }
  }

  fun onRestoreCancelled() {
    _showRestoreDialog.value = false
    pendingRestoreUri = null
  }

  fun onErrorDismissed() {
    _uiState.value = BackupUiState.Idle
  }

  fun onSuccessDismissed() {
    _uiState.value = BackupUiState.Idle
  }
}

sealed interface BackupUiState {
  data object Idle : BackupUiState
  data object Loading : BackupUiState
  data object ExportSuccess : BackupUiState
  data object RestoreSuccess : BackupUiState
  data class Error(val error: BackupError) : BackupUiState
}

sealed interface BackupError {
  data class ExportFailed(val message: String?) : BackupError
  data class RestoreFailed(val message: String?) : BackupError
  data class InvalidFile(val message: String?) : BackupError
  data object VersionMismatch : BackupError
}
