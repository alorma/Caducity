package com.alorma.caducity.ui.components.feedback

import androidx.compose.runtime.Composable
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.colors.ContainerColors
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.theme.CaducityTheme

sealed class AppFeedbackType {
  data class Status(val status: ItemStatus) : AppFeedbackType()
  data object Success : AppFeedbackType()
  data object Info : AppFeedbackType()
  data object Error : AppFeedbackType()
}


@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.vibrantColors(): ContainerColors = tonalColors()

@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.softColors(): ContainerColors = tonalColors()

@Suppress("ContentEmission")
@Composable
fun AppFeedbackType.tonalColors(): ContainerColors = when (this) {
  is AppFeedbackType.Status -> ExpirationDefaults.getTonalColors(status)

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