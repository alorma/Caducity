package com.alorma.caducity.ui.screen.settings.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.feature.fakedata.FakeDataDebugHelper
import com.alorma.caducity.feature.fakedata.models.GeminiError
import com.alorma.caducity.feature.fakedata.models.GenerationProgress
import com.alorma.caducity.feature.notification.NotificationDebugHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Debug Settings screen
 * Manages state for fake data generation and notifications
 */
class DebugSettingsViewModel(
  private val fakeDataDebugHelper: FakeDataDebugHelper,
  private val notificationDebugHelper: NotificationDebugHelper
) : ViewModel() {

  private val _uiState = MutableStateFlow(DebugSettingsUiState())
  val uiState: StateFlow<DebugSettingsUiState> = _uiState.asStateFlow()

  private val _sideEffect = MutableSharedFlow<DebugSettingsSideEffect>()
  val sideEffect: SharedFlow<DebugSettingsSideEffect> = _sideEffect.asSharedFlow()

  fun onGenerateFakeData(
    maxProducts: Int,
    maxVariantsPerProduct: Int,
    minInstancesPerVariant: Int,
    maxInstancesPerVariant: Int
  ) {
    viewModelScope.launch {
      fakeDataDebugHelper.generateFakeData(
        maxProducts = maxProducts,
        variantsPerProduct = maxVariantsPerProduct,
        instancesPerVariantRange = minInstancesPerVariant..maxInstancesPerVariant
      ).collect { progress ->
        when (progress) {
          is GenerationProgress.Started -> {
            _uiState.value = _uiState.value.copy(
              isGenerating = true,
              progress = progress,
              error = null
            )
          }

          is GenerationProgress.CheckingExisting,
          is GenerationProgress.GeneratingWithAI,
          is GenerationProgress.InsertingToDatabase -> {
            _uiState.value = _uiState.value.copy(progress = progress)
          }

          is GenerationProgress.Completed -> {
            _uiState.value = _uiState.value.copy(
              isGenerating = false,
              progress = progress
            )
            _sideEffect.emit(
              DebugSettingsSideEffect.GenerationSuccess(
                products = progress.productsCreated,
                variants = progress.variantsCreated,
                instances = progress.instancesCreated
              )
            )
          }

          is GenerationProgress.Failed -> {
            _uiState.value = _uiState.value.copy(
              isGenerating = false,
              progress = progress,
              error = progress.error.toUserMessage()
            )
            _sideEffect.emit(DebugSettingsSideEffect.GenerationError(progress.error))
          }
        }
      }
    }
  }

  fun onTriggerNotificationCheck() {
    notificationDebugHelper.triggerImmediateCheck()
  }

  fun dismissError() {
    _uiState.value = _uiState.value.copy(error = null)
  }

  private fun GeminiError.toUserMessage(): String = when (this) {
    is GeminiError.NetworkError -> "Network error. Check your connection."
    is GeminiError.QuotaExceeded -> "AI quota exceeded. Try again later."
    is GeminiError.InvalidResponse -> "AI returned invalid data. Try again."
    is GeminiError.ParseError -> "Failed to parse AI response."
    is GeminiError.FirebaseNotConfigured -> "Firebase not configured. Please add google-services.json."
    is GeminiError.ServiceDisabled -> {
      buildString {
        append("Vertex AI API is not enabled.\n\n")
        append("Enable it in the Firebase Console")
        if (projectUrl != null) {
          append(":\n$projectUrl")
        } else {
          append(".")
        }
      }
    }
    is GeminiError.UnknownError -> "Unknown error: ${cause.message}"
  }
}

/**
 * UI state for Debug Settings screen
 */
data class DebugSettingsUiState(
  val isGenerating: Boolean = false,
  val progress: GenerationProgress? = null,
  val error: String? = null,

  // Configuration defaults
  val defaultMaxProducts: Int = 5,
  val defaultMaxVariantsPerProduct: Int = 3,
  val defaultMinInstancesPerVariant: Int = 6,
  val defaultMaxInstancesPerVariant: Int = 10
)

/**
 * Side effects for Debug Settings screen
 */
sealed interface DebugSettingsSideEffect {
  data class GenerationSuccess(
    val products: Int,
    val variants: Int,
    val instances: Int
  ) : DebugSettingsSideEffect

  data class GenerationError(val error: GeminiError) : DebugSettingsSideEffect
}
