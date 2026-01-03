package com.alorma.caducity.domain.backup

import com.alorma.caducity.data.backup.BackupData

interface BackupDataSource {
  suspend fun exportBackup(): BackupData
  suspend fun importBackup(backup: BackupData)
  suspend fun clearAllData()
  fun validateBackup(backup: BackupData): Result<Unit>
}
