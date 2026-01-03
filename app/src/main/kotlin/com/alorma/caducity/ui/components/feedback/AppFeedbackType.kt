package com.alorma.caducity.ui.components.feedback

import androidx.compose.runtime.Composable
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.colors.ContainerColors
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.theme.CaducityTheme

sealed class AppFeedbackType {
  data class Status(val status: InstanceStatus) : AppFeedbackType()
  data object Success : AppFeedbackType()
  data object Info : AppFeedbackType()
  data object Error : AppFeedbackType()
}


@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.vibrantColors(): ContainerColors = when (this) {
  is AppFeedbackType.Status -> ExpirationDefaults.getVibrantColors(status)

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

@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.softColors(): ContainerColors = when (this) {
  is AppFeedbackType.Status -> ExpirationDefaults.getSoftColors(status)

  AppFeedbackType.Success -> ContainerColors(
    container = CaducityTheme.colorScheme.primaryContainer,
    onContainer = CaducityTheme.colorScheme.onPrimaryContainer,
  )

  AppFeedbackType.Info -> ContainerColors(
    container = CaducityTheme.colorScheme.surface,
    onContainer = CaducityTheme.colorScheme.onSurface,
  )

  AppFeedbackType.Error -> ContainerColors(
    container = CaducityTheme.colorScheme.errorContainer,
    onContainer = CaducityTheme.colorScheme.onErrorContainer,
  )
}