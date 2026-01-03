package com.alorma.caducity.domain.usecase.backup

import com.alorma.caducity.data.backup.BackupData
import com.alorma.caducity.domain.backup.BackupDataSource

class ExportBackupUseCase(
  private val backupDataSource: BackupDataSource
) {
  suspend fun export(): Result<BackupData> {
    return try {
      val backup = backupDataSource.exportBackup()
      Result.success(backup)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
