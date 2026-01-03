package com.alorma.caducity.feature.backup

import android.content.Context
import android.net.Uri
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.backup.BackupData
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json

class AndroidBackupFileHandler(
  private val context: Context,
  private val appClock: AppClock,
  private val dateFilenameFormat: DateTimeFormat<LocalDateTime>,
) : BackupFileHandler {

  private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
  }

  override suspend fun writeBackupToUri(uri: Uri, data: BackupData): Result<Unit> {
    return try {
      val jsonString = json.encodeToString(data)
      context.contentResolver.openOutputStream(uri)?.use { outputStream ->
        outputStream.write(jsonString.toByteArray())
      } ?: return Result.failure(IllegalStateException("Failed to open output stream"))

      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun readBackupFromUri(uri: Uri): Result<BackupData> {
    return try {
      val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
        inputStream.bufferedReader().readText()
      } ?: return Result.failure(IllegalStateException("Failed to open input stream"))

      val backup = json.decodeFromString<BackupData>(jsonString)
      Result.success(backup)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override fun generateBackupFileName(): String {
    val now = appClock.now()
    val localDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())

    val dateFormatted = dateFilenameFormat.format(localDateTime)

    return "caducity_backup_${dateFormatted}.json"
  }
}
