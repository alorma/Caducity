package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.base.ui.theme.CaducityTheme

object ExpirationColors {

  @Composable
  fun getSectionColors(instanceStatus: InstanceStatus): StatusColors {
    return when (instanceStatus) {
      InstanceStatus.Expired -> StatusColors(
        container = CaducityTheme.expirationColorScheme.expired,
        onContainer = CaducityTheme.expirationColorScheme.onExpired,
      )

      InstanceStatus.ExpiringSoon -> StatusColors(
        container = CaducityTheme.expirationColorScheme.expiringSoon,
        onContainer = CaducityTheme.expirationColorScheme.onExpiringSoon,
      )

      InstanceStatus.Fresh -> StatusColors(
        container = CaducityTheme.expirationColorScheme.fresh,
        onContainer = CaducityTheme.expirationColorScheme.onFresh,
      )
    }
  }
}

data class StatusColors(
  val container: Color,
  val onContainer: Color,
)
