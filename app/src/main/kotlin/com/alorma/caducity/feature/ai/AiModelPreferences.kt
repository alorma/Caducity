package com.alorma.caducity.feature.ai

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AiModelPreferences(
  private val settings: Settings,
) {
  private val _selectedModel = MutableStateFlow(getPersistedModel())
  val selectedModel: StateFlow<AiModelOption> = _selectedModel.asStateFlow()

  fun getSelectedModel(): AiModelOption = _selectedModel.value

  fun setSelectedModel(model: AiModelOption) {
    settings[KEY_SELECTED_MODEL] = model.modelId
    _selectedModel.value = model
  }

  private fun getPersistedModel(): AiModelOption {
    val id = settings.getStringOrNull(KEY_SELECTED_MODEL) ?: return AiModelOption.DEFAULT
    return AiModelOption.fromModelId(id)
  }

  companion object {
    private const val KEY_SELECTED_MODEL = "ai_selected_model"
  }
}
