package com.alorma.caducity.ui.screen.ai

import androidx.lifecycle.viewModelScope
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.feature.ai.AiGroceryParser
import com.alorma.caducity.feature.ai.ModelDownloadState
import com.alorma.caducity.feature.ai.ModelManager
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

class AiAssistantViewModel(
  private val modelManager: ModelManager,
  private val groceryParser: AiGroceryParser,
  private val appClock: AppClock,
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

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  fun send(input: String) {
    if (input.isBlank()) return

    _messages.update { it + ChatMessage.Outgoing(input) + ChatMessage.Thinking }

    viewModelScope.launch {
      val todayIso = todayIsoString()
      val proposals = groceryParser.parse(input, todayIso)

      _messages.update { messages ->
        val withoutThinking = messages.filterNot { it is ChatMessage.Thinking }
        if (proposals.isEmpty()) {
          withoutThinking + ChatMessage.Error
        } else {
          withoutThinking + ChatMessage.Proposals(proposals)
        }
      }
    }
  }

  @OptIn(FormatStringsInDatetimeFormats::class)
  private fun todayIsoString(): String {
    val today = appClock.nowDate()
    return LocalDate.Format { byUnicodePattern("yyyy-MM-dd") }.format(today)
  }

  override fun navigate(navigation: AiAssistantNavigation) {
    when (navigation) {
      AiAssistantNavigation.Cancel -> emitNavigationSideEffect(AiAssistantNavigationSideEffect.NavigateBack)
    }
  }
}
