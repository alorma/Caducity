package com.alorma.caducity.feature.ai

sealed interface ModelDownloadState {
  data object Ready : ModelDownloadState
  data object Idle : ModelDownloadState
  data class Downloading(val progress: Int) : ModelDownloadState
  data object Failed : ModelDownloadState
}
