package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object ExpirationColors {

  @Composable
  fun getSectionColors(instanceStatus: InstanceStatus): StatusColors {
    return when (instanceStatus) {
      InstanceStatus.Expired -> StatusColors(
        container = MaterialTheme.colorScheme.errorContainer,
        onContainer = MaterialTheme.colorScheme.onErrorContainer
      )

      InstanceStatus.ExpiringSoon -> StatusColors(
        container = MaterialTheme.colorScheme.onSecondaryContainer,
        onContainer = MaterialTheme.colorScheme.secondaryContainer
      )

      InstanceStatus.Fresh -> StatusColors(
        container = MaterialTheme.colorScheme.onPrimaryContainer,
        onContainer = MaterialTheme.colorScheme.primaryContainer
      )
    }
  }
}

data class StatusColors(
  val container: Color,
  val onContainer: Color,
)
