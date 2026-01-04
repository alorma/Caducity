package com.alorma.caducity.feature.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.alorma.caducity.config.navigation.ComposeNavigator
import com.alorma.caducity.data.backup.BackupData

interface BackupFileHandler: ComposeNavigator<BackupFileHandler.BackupResult> {
  suspend fun writeBackupToUri(uri: Uri, data: BackupData): Result<Unit>
  suspend fun readBackupFromUri(uri: Uri): Result<BackupData>
  fun generateBackupFileName(): String

  fun createBackup()
  fun selectBackup()

  sealed interface BackupResult {
    data class ExportUriObtained(val uri: Uri): BackupResult
    data class RestoreUriObtained(val uri: Uri): BackupResult
  }
}