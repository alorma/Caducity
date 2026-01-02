package com.alorma.caducity.ui.components.expiration

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.base.ui.theme.CaducityTheme

object ExpirationDefaults {

  @Composable
  fun getColors(instanceStatus: InstanceStatus): StatusColors {
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

      InstanceStatus.Frozen -> StatusColors(
        container = CaducityTheme.expirationColorScheme.frozen,
        onContainer = CaducityTheme.expirationColorScheme.onFrozen,
      )

      InstanceStatus.Consumed -> StatusColors(
        container = CaducityTheme.expirationColorScheme.consumed,
        onContainer = CaducityTheme.expirationColorScheme.onConsumed,
      )
    }
  }
}

data class StatusColors(
  val container: Color,
  val onContainer: Color,
)
