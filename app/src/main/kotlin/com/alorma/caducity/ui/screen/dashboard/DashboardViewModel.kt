package com.alorma.caducity.ui.screen.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.GenerateProductsFromPromptUseCase
import com.alorma.caducity.domain.usecase.ObtainDashboardProductsUseCase
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class DashboardViewModel(
  calendarPreferences: CalendarPreferences,
  private val obtainDashboardProductsUseCase: ObtainDashboardProductsUseCase,
  private val dashboardMapper: DashboardMapper,
  private val generateProductsFromPromptUseCase: GenerateProductsFromPromptUseCase,
) : ViewModel() {

  @OptIn(ExperimentalCoroutinesApi::class)
  val state: StateFlow<DashboardState> = calendarPreferences.state
    .flatMapLatest { calendarConfig ->
      obtainPerProductDashboard(calendarConfig.firstDayOfWeek)
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5.seconds),
      initialValue = DashboardState.Loading,
    )

  private val _aiGenerationState = MutableStateFlow(AIGenerationState())
  val aiGenerationState: StateFlow<AIGenerationState> = _aiGenerationState.asStateFlow()

  private fun obtainPerProductDashboard(firstDayOfWeek: kotlinx.datetime.DayOfWeek): Flow<DashboardState.Success> {
    return obtainDashboardProductsUseCase
      .obtainProducts()
      .map { products ->
        dashboardMapper.mapToPerProductState(products = products, firstDayOfWeek = firstDayOfWeek)
      }
  }

  fun onGenerateFromPrompt(prompt: String) {
    viewModelScope.launch {
      _aiGenerationState.value = AIGenerationState(isGenerating = true)

      generateProductsFromPromptUseCase.generate(prompt).collect { progress ->
        when (progress) {
          is GenerationProgress.Started,
          is GenerationProgress.CheckingExisting,
          is GenerationProgress.GeneratingWithAI,
          is GenerationProgress.InsertingToDatabase -> {
            _aiGenerationState.value = AIGenerationState(
              isGenerating = true,
              progress = progress
            )
          }
          is GenerationProgress.Completed -> {
            _aiGenerationState.value = AIGenerationState(
              isGenerating = false,
              completedResult = progress
            )
          }
          is GenerationProgress.Failed -> {
            _aiGenerationState.value = AIGenerationState(
              isGenerating = false,
              error = progress.error.toUserMessage()
            )
          }
        }
      }
    }
  }

  fun dismissAIError() {
    _aiGenerationState.value = AIGenerationState()
  }

  fun resetAIState() {
    _aiGenerationState.value = AIGenerationState()
  }

  private fun com.alorma.caducity.feature.fakedata.models.GeminiError.toUserMessage(): String = when (this) {
    is com.alorma.caducity.feature.fakedata.models.GeminiError.NetworkError -> "Network error. Check your connection."
    is com.alorma.caducity.feature.fakedata.models.GeminiError.QuotaExceeded -> "AI quota exceeded. Try again later."
    is com.alorma.caducity.feature.fakedata.models.GeminiError.InvalidResponse -> "AI returned invalid data. Try again."
    is com.alorma.caducity.feature.fakedata.models.GeminiError.ParseError -> "Failed to parse AI response."
    is com.alorma.caducity.feature.fakedata.models.GeminiError.FirebaseNotConfigured -> "Firebase not configured."
    is com.alorma.caducity.feature.fakedata.models.GeminiError.ServiceDisabled -> "Vertex AI not enabled in Firebase Console."
    is com.alorma.caducity.feature.fakedata.models.GeminiError.UnknownError -> "Unknown error occurred."
  }
}

data class AIGenerationState(
  val isGenerating: Boolean = false,
  val progress: GenerationProgress? = null,
  val error: String? = null,
  val completedResult: GenerationProgress.Completed? = null
)
