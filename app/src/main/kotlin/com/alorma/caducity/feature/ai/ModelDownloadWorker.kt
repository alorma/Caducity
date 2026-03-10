package com.alorma.caducity.feature.ai

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.alorma.caducity.R
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

class ModelDownloadWorker(
  appContext: Context,
  params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): Result {
    val url = inputData.getString(KEY_URL) ?: return Result.failure()
    val modelId = inputData.getString(KEY_MODEL_ID) ?: return Result.failure()

    ensureChannel(applicationContext)
    setForeground(foreground(modelId, 0))

    val modelsDir = File(applicationContext.filesDir, "models").apply { mkdirs() }
    val partFile = File(modelsDir, "$modelId.gguf.part")
    val finalFile = File(modelsDir, "$modelId.gguf")

    return try {
      downloadResumable(url, modelId, partFile) { pct ->
        setForeground(foreground(modelId, pct))
        setProgress(workDataOf(KEY_PROGRESS to pct))
      }

      if (finalFile.exists()) finalFile.delete()
      partFile.renameTo(finalFile)

      Result.success(workDataOf(KEY_PATH to finalFile.absolutePath))
    } catch (_: kotlinx.coroutines.CancellationException) {
      // Worker was cancelled — keep the part file for resumption next time
      Result.failure()
    } catch (_: Throwable) {
      // Any other error — retry (WorkManager will back off automatically)
      Result.retry()
    }
  }

  private suspend fun downloadResumable(
    url: String,
    modelId: String,
    partFile: File,
    onProgress: suspend (Int) -> Unit,
  ): Unit = withContext(Dispatchers.IO) {
    val already = if (partFile.exists()) partFile.length() else 0L

    val connection =
      (URL(url).openConnection() as HttpURLConnection).apply {
        setRequestProperty("Accept-Encoding", "identity")
        connectTimeout = 30_000
        readTimeout = 60_000
        if (already > 0) setRequestProperty("Range", "bytes=$already-")
      }

    try {
      connection.connect()
      val code = connection.responseCode

      // Server ignored our Range request — restart clean
      if (already > 0 && code == HttpURLConnection.HTTP_OK) {
        partFile.delete()
        return@withContext downloadResumable(
          url = url,
          modelId = modelId,
          partFile = partFile,
          onProgress = onProgress,
        )
      }

      val contentLength = connection.contentLengthLong
      val total = if (contentLength > 0) already + contentLength else -1L

      RandomAccessFile(partFile, "rw").use { raf ->
        raf.seek(already)

        val buffer = ByteArray(256 * 1024)
        var written = already
        var lastPct = -1

        connection.inputStream.use { input ->
          while (true) {
            ensureActive()
            val read = input.read(buffer)
            if (read <= 0) break

            raf.write(buffer, 0, read)
            written += read

            if (total > 0) {
              val pct = ((written * 100) / total).toInt().coerceIn(0, 100)
              if (pct != lastPct) {
                lastPct = pct
                onProgress(pct)
              }
            }
          }
        }
      }
    } finally {
      connection.disconnect()
    }
  }

  private fun foreground(
    modelId: String,
    progress: Int,
  ): ForegroundInfo {
    val notification =
      NotificationCompat
        .Builder(applicationContext, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle(applicationContext.getString(R.string.ai_model_downloading_title))
        .setContentText("$modelId • $progress%")
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setProgress(100, progress, progress == 0)
        .build()

    val id = modelId.hashCode()
    return ForegroundInfo(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
  }

  private fun ensureChannel(context: Context) {
    val manager = context.getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(
      NotificationChannel(
        CHANNEL_ID,
        context.getString(R.string.ai_model_download_channel_name),
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = context.getString(R.string.ai_model_download_channel_description)
        setShowBadge(false)
      },
    )
  }

  companion object {
    const val CHANNEL_ID = "model_downloads"
    const val KEY_MODEL_ID = "modelId"
    const val KEY_URL = "url"
    const val KEY_PROGRESS = "progress"
    const val KEY_PATH = "path"
  }
}
