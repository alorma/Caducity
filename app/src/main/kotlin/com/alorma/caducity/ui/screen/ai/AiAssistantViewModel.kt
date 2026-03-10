package com.alorma.caducity.ui.screen.ai

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.feature.ai.ModelDownloadState
import com.alorma.caducity.feature.ai.ModelManager
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class AiAssistantViewModel(
  private val modelManager: ModelManager,
) : BaseViewModel<AiAssistantNavigation, AiAssistantNavigationSideEffect, Unit>() {
  val modelState: StateFlow<ModelDownloadState> =
    modelManager
      .downloadState()
      .onStart {
        if (!modelManager.isModelReady()) {
          modelManager.startDownload()
        }
      }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = if (modelManager.isModelReady()) ModelDownloadState.Ready else ModelDownloadState.Idle,
      )

  override fun navigate(navigation: AiAssistantNavigation) {
    when (navigation) {
      AiAssistantNavigation.Cancel -> emitNavigationSideEffect(AiAssistantNavigationSideEffect.NavigateBack)
    }
  }
}
