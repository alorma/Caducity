package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object DashboardSectionColors {
  @Composable
  fun getSectionColors(sectionType: SectionType): SectionColors {
    return when (sectionType) {
      SectionType.EXPIRED -> SectionColors(
        container = MaterialTheme.colorScheme.errorContainer,
        onContainer = MaterialTheme.colorScheme.onErrorContainer
      )

      SectionType.EXPIRING_SOON -> SectionColors(
        container = MaterialTheme.colorScheme.secondaryContainer,
        onContainer = MaterialTheme.colorScheme.onSecondaryContainer
      )

      SectionType.FRESH -> SectionColors(
        container = MaterialTheme.colorScheme.primaryContainer,
        onContainer = MaterialTheme.colorScheme.onPrimaryContainer
      )
    }
  }
}

data class SectionColors(
  val container: Color,
  val onContainer: Color,
)
