package com.alorma.caducity.domain.usecase.backup

import com.alorma.caducity.data.backup.BackupData
import com.alorma.caducity.domain.backup.BackupDataSource

class ImportBackupUseCase(
  private val backupDataSource: BackupDataSource
) {
  suspend operator fun invoke(backup: BackupData): Result<Unit> {
    return try {
      // Validate backup first
      backupDataSource.validateBackup(backup).getOrElse {
        return Result.failure(it)
      }

      // Import the backup
      backupDataSource.importBackup(backup)
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
