package com.alorma.caducity.ui.screen.settings.backup

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.backup.ExportBackupUseCase
import com.alorma.caducity.domain.usecase.backup.ImportBackupUseCase
import com.alorma.caducity.feature.backup.BackupFileHandler
import com.alorma.caducity.ui.base.BaseViewModel
import com.alorma.caducity.ui.base.NoNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BackupViewModel(
  private val exportBackupUseCase: ExportBackupUseCase,
  private val importBackupUseCase: ImportBackupUseCase,
  private val backupFileHandler: BackupFileHandler
) : BaseViewModel<NoNavigation, BackupSideEffect, BackupSideEffect>() {

  // Alias for backward compatibility
  val sideEffect = sideEffects

  private val _uiState = MutableStateFlow<BackupUiState>(BackupUiState.Idle)
  val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

  fun onExportBackup(uri: Uri) {
    viewModelScope.launch {
      _uiState.value = BackupUiState.Loading
      try {
        exportBackupUseCase.export().fold(
          onSuccess = { backupData ->
            backupFileHandler.writeBackupToUri(uri, backupData).fold(
              onSuccess = {
                _uiState.value = BackupUiState.Idle
                emitSideEffect(BackupSideEffect.ExportSuccess)
              },
              onFailure = { error ->
                _uiState.value = BackupUiState.Idle
                emitSideEffect(BackupSideEffect.Error(BackupError.ExportFailed(error.message)))
              }
            )
          },
          onFailure = { error ->
            _uiState.value = BackupUiState.Idle
            emitSideEffect(BackupSideEffect.Error(BackupError.ExportFailed(error.message)))
          }
        )
      } catch (e: Exception) {
        _uiState.value = BackupUiState.Idle
        emitSideEffect(BackupSideEffect.Error(BackupError.ExportFailed(e.message)))
      }
    }
  }

  fun onRestoreConfirmed(uri: Uri) {

    viewModelScope.launch {
      _uiState.value = BackupUiState.Loading
      try {
        backupFileHandler.readBackupFromUri(uri).fold(
          onSuccess = { backupData ->
            importBackupUseCase.import(backupData).fold(
              onSuccess = {
                _uiState.value = BackupUiState.Idle
                emitSideEffect(BackupSideEffect.RestoreSuccess)
              },
              onFailure = { error ->
                _uiState.value = BackupUiState.Idle
                val backupError = when {
                  error.message?.contains("version", ignoreCase = true) == true ->
                    BackupError.VersionMismatch

                  else ->
                    BackupError.RestoreFailed(error.message)
                }
                emitSideEffect(BackupSideEffect.Error(backupError))
              }
            )
          },
          onFailure = { error ->
            _uiState.value = BackupUiState.Idle
            emitSideEffect(BackupSideEffect.Error(BackupError.InvalidFile(error.message)))
          }
        )
      } catch (e: Exception) {
        _uiState.value = BackupUiState.Idle
        emitSideEffect(BackupSideEffect.Error(BackupError.InvalidFile(e.message)))
      }
    }
  }

  fun onBackupResult(result: BackupFileHandler.BackupResult) {
    when (result) {
      is BackupFileHandler.BackupResult.ExportUriObtained -> {
        onExportBackup(result.uri)
      }

      is BackupFileHandler.BackupResult.RestoreUriObtained -> {
        viewModelScope.launch {
          emitSideEffect(BackupSideEffect.ConfirmRestore(uri = result.uri))
        }
      }
    }
  }

  override fun navigate(navigation: NoNavigation) {
    // Empty - this ViewModel doesn't navigate
  }
}

sealed interface BackupUiState {
  data object Idle : BackupUiState
  data object Loading : BackupUiState
}

sealed interface BackupSideEffect {
  data object ExportSuccess : BackupSideEffect
  data object RestoreSuccess : BackupSideEffect
  data class Error(val error: BackupError) : BackupSideEffect
  data class ConfirmRestore(val uri: Uri) : BackupSideEffect
}

sealed interface BackupError {
  data class ExportFailed(val message: String?) : BackupError
  data class RestoreFailed(val message: String?) : BackupError
  data class InvalidFile(val message: String?) : BackupError
  data object VersionMismatch : BackupError
}
