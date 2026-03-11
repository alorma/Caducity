package com.alorma.caducity.feature.ai

import kotlinx.coroutines.flow.Flow

interface ModelManager {
  fun isModelReady(): Boolean

  fun startDownload()

  fun downloadState(): Flow<ModelDownloadState>

  fun modelFilePath(): String
}
