package com.alorma.caducity.feature.ai

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WorkManagerModelManager(
  private val context: Context,
) : ModelManager {
  private val modelsDir get() = File(context.filesDir, "models")

  override fun isModelReady(): Boolean = File(modelsDir, "${ModelConfig.MODEL_ID}.gguf").exists()

  override fun modelFilePath(): String = File(modelsDir, "${ModelConfig.MODEL_ID}.gguf").absolutePath

  override fun startDownload() {
    val request =
      OneTimeWorkRequestBuilder<ModelDownloadWorker>()
        .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
        .setInputData(
          workDataOf(
            ModelDownloadWorker.KEY_MODEL_ID to ModelConfig.MODEL_ID,
            ModelDownloadWorker.KEY_URL to ModelConfig.MODEL_URL,
          ),
        ).addTag(WORK_TAG)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
  }

  override fun downloadState(): Flow<ModelDownloadState> =
    WorkManager
      .getInstance(context)
      .getWorkInfosByTagFlow(WORK_TAG)
      .map { infos ->
        val info = infos.firstOrNull()
        when {
          info == null -> if (isModelReady()) ModelDownloadState.Ready else ModelDownloadState.Idle
          info.state == WorkInfo.State.SUCCEEDED -> ModelDownloadState.Ready
          info.state == WorkInfo.State.FAILED -> ModelDownloadState.Failed
          info.state == WorkInfo.State.RUNNING || info.state == WorkInfo.State.ENQUEUED -> {
            val progress = info.progress.getInt(ModelDownloadWorker.KEY_PROGRESS, 0)
            ModelDownloadState.Downloading(progress)
          }
          else -> if (isModelReady()) ModelDownloadState.Ready else ModelDownloadState.Idle
        }
      }

  companion object {
    private const val WORK_NAME = "model_download"
    private const val WORK_TAG = "model_download"
  }
}
