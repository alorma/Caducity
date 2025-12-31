package com.alorma.caducity.ui.screen.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import caducity.composeapp.generated.resources.Res
import caducity.composeapp.generated.resources.dashboard_filter_expired
import caducity.composeapp.generated.resources.dashboard_filter_expiring_soon
import caducity.composeapp.generated.resources.dashboard_filter_fresh
import com.alorma.caducity.base.ui.theme.CaducityTheme
import com.alorma.caducity.domain.model.InstanceStatus
import org.jetbrains.compose.resources.stringResource

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
    }
  }

  @Composable
  fun getText(status: InstanceStatus) = when (status) {
    InstanceStatus.Expired -> stringResource(Res.string.dashboard_filter_expired)
    InstanceStatus.ExpiringSoon -> stringResource(Res.string.dashboard_filter_expiring_soon)
    InstanceStatus.Fresh -> stringResource(Res.string.dashboard_filter_fresh)
  }
}

data class StatusColors(
  val container: Color,
  val onContainer: Color,
)
