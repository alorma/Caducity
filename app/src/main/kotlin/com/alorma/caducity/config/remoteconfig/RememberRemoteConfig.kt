package com.alorma.caducity.config.remoteconfig

import androidx.compose.runtime.Composable
import org.koin.compose.koinInject

/**
 * Remembers and injects a RemoteConfig instance in a Composable.
 * 
 * This allows direct usage of remote configs in Composables without needing to inject
 * them in ViewModels.
 * 
 * Usage example:
 * ```
 * @Composable
 * fun MyScreen() {
 *   val featureEnabled = rememberRemoteConfig<ExampleFeatureConfig>()
 *   
 *   if (featureEnabled.isEnabled()) {
 *     // Show feature
 *   }
 * }
 * ```
 * 
 * @param T The type of RemoteConfig to inject
 * @return The injected RemoteConfig instance
 */
@Composable
inline fun <reified T : RemoteConfig> rememberRemoteConfig(): T {
  return koinInject()
}
