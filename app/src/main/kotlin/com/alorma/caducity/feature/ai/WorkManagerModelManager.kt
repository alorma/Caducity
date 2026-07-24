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
  private val aiModelPreferences: AiModelPreferences,
) : ModelManager {
  private val modelsDir get() = File(context.filesDir, "models")

  private val selectedModel: AiModelOption get() = aiModelPreferences.getSelectedModel()

  override fun currentModelOption(): AiModelOption = selectedModel

  override fun isModelReady(): Boolean = File(modelsDir, "${selectedModel.modelId}.gguf").exists()

  override fun modelFilePath(): String = File(modelsDir, "${selectedModel.modelId}.gguf").absolutePath

  override fun switchModel(model: AiModelOption) {
    aiModelPreferences.setSelectedModel(model)
  }

  override fun startDownload() {
    val model = selectedModel
    val request =
      OneTimeWorkRequestBuilder<ModelDownloadWorker>()
        .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
        .setInputData(
          workDataOf(
            ModelDownloadWorker.KEY_MODEL_ID to model.modelId,
            ModelDownloadWorker.KEY_URL to model.url,
          ),
        ).addTag(workTag(model))
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      workName(model),
      ExistingWorkPolicy.KEEP,
      request,
    )
  }

  override fun downloadState(): Flow<ModelDownloadState> {
    val model = selectedModel
    return WorkManager
      .getInstance(context)
      .getWorkInfosByTagFlow(workTag(model))
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
  }

  private fun workTag(model: AiModelOption): String = "model_download_${model.modelId}"

  private fun workName(model: AiModelOption): String = "model_download_${model.modelId}"
}
