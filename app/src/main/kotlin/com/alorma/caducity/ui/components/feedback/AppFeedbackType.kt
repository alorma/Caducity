package com.alorma.caducity.ui.components.feedback

import androidx.compose.runtime.Composable
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.colors.ContainerColors
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.ThemePreferences
import org.koin.compose.koinInject

sealed class AppFeedbackType {
  data class Status(val status: ItemStatus) : AppFeedbackType()
  data object Success : AppFeedbackType()
  data object Info : AppFeedbackType()
  data object Error : AppFeedbackType()
}


@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.vibrantColors(
  themePreferences: ThemePreferences = koinInject(),
): ContainerColors = tonalColors(themePreferences)

@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.softColors(
  themePreferences: ThemePreferences = koinInject(),
): ContainerColors = tonalColors(themePreferences)

@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.tonalColors(
  themePreferences: ThemePreferences = koinInject(),
): ContainerColors = when (this) {
  is AppFeedbackType.Status -> ExpirationDefaults.getTonalColors(status, themePreferences)

  AppFeedbackType.Success -> ContainerColors(
    container = CaducityTheme.colorScheme.primary,
    onContainer = CaducityTheme.colorScheme.onPrimary,
  )

  AppFeedbackType.Info -> ContainerColors(
    container = CaducityTheme.colorScheme.inverseSurface,
    onContainer = CaducityTheme.colorScheme.inverseOnSurface,
  )

  AppFeedbackType.Error -> ContainerColors(
    container = CaducityTheme.colorScheme.error,
    onContainer = CaducityTheme.colorScheme.onError,
  )
}