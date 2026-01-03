package com.alorma.caducity.feature.backup

import android.net.Uri
import com.alorma.caducity.data.backup.BackupData

interface BackupFileHandler {
  suspend fun writeBackupToUri(uri: Uri, data: BackupData): Result<Unit>
  suspend fun readBackupFromUri(uri: Uri): Result<BackupData>
  fun generateBackupFileName(): String
}
