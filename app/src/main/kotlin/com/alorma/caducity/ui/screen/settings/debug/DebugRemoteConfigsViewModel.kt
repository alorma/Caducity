package com.alorma.caducity.ui.screen.settings.debug

import com.alorma.caducity.config.remoteconfig.DebugRemoteConfigRunner
import com.alorma.caducity.config.remoteconfig.RemoteConfig
import com.alorma.caducity.config.remoteconfig.RemoteConfigRunner
import com.alorma.caducity.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DebugRemoteConfigsViewModel(
  private val remoteConfigRunner: RemoteConfigRunner,
  private val remoteConfigs: List<RemoteConfig>,
) : BaseViewModel<Unit, Unit, Unit>() {
  private val _uiState =
    MutableStateFlow(
      DebugRemoteConfigsUiState(
        onToggleRemoteConfig = ::onToggleRemoteConfig,
      ),
    )
  val uiState: StateFlow<DebugRemoteConfigsUiState> = _uiState.asStateFlow()

  init {
    loadRemoteConfigValues()
  }

  private fun loadRemoteConfigValues() {
    val debugRunner = remoteConfigRunner as? DebugRemoteConfigRunner

    _uiState.value =
      _uiState.value.copy(
        remoteConfigValues =
          remoteConfigs.associate { config ->
            config.key to
              RemoteConfigUiState(
                value = config.isEnabled(),
                hasDebugOverride = debugRunner?.hasDebugOverride(config.key) ?: false,
              )
          },
      )
  }

  fun onToggleRemoteConfig(
    key: String,
    enabled: Boolean,
  ) {
    val debugRunner = remoteConfigRunner as? DebugRemoteConfigRunner
    if (debugRunner != null) {
      debugRunner.setDebugValue(key, enabled)
      loadRemoteConfigValues()
    }
  }

  override fun navigate(navigation: Unit) {
    // Empty - this ViewModel doesn't navigate
  }
}

data class DebugRemoteConfigsUiState(
  val remoteConfigValues: Map<String, RemoteConfigUiState> = emptyMap(),
  val onToggleRemoteConfig: (String, Boolean) -> Unit,
)

data class RemoteConfigUiState(
  val value: Boolean,
  val hasDebugOverride: Boolean,
)
