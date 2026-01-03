package com.alorma.caducity.ui.components.expiration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.theme.CaducityTheme
import com.materialkolor.ktx.harmonize
import com.materialkolor.ktx.isLight

object ExpirationDefaults {

  @Composable
  fun getVibrantColors(
    instanceStatus: InstanceStatus,
  ): StatusColors {
    val expirationColors = CaducityTheme.expirationColors
    return when (instanceStatus) {
      InstanceStatus.Fresh -> statusColors(expirationColors.fresh, false)
      InstanceStatus.ExpiringSoon -> statusColors(expirationColors.expiringSoon, false)
      InstanceStatus.Expired -> statusColors(expirationColors.expired, false)
      InstanceStatus.Frozen -> statusColors(expirationColors.frozen, false)
      InstanceStatus.Consumed -> statusColors(expirationColors.consumed, false)
    }
  }

  @Composable
  fun getSoftColors(
    instanceStatus: InstanceStatus,
  ): StatusColors {
    val expirationColors = CaducityTheme.expirationColors
    return when (instanceStatus) {
      InstanceStatus.Fresh -> statusColors(expirationColors.fresh, true)
      InstanceStatus.ExpiringSoon -> statusColors(expirationColors.expiringSoon, true)
      InstanceStatus.Expired -> statusColors(expirationColors.expired, true)
      InstanceStatus.Frozen -> statusColors(expirationColors.frozen, true)
      InstanceStatus.Consumed -> statusColors(expirationColors.consumed, true)
    }
  }

  @Composable
  private fun statusColors(
    color: Color,
    matchSaturation: Boolean,
  ): StatusColors {
    val contentColor = harmonizedColor(color, matchSaturation)

    return StatusColors(
      container = contentColor,
      onContainer = contentColorForExpiration(contentColor),
    )
  }

  @Composable
  private fun harmonizedColor(
    color: Color,
    matchSaturation: Boolean,
  ): Color {
    return color.harmonize(
      other = CaducityTheme.colorScheme.surfaceContainerHighest,
      matchSaturation = matchSaturation,
    )
  }

}

@Suppress("ContentEmission")
@ReadOnlyComposable
@Composable
private fun contentColorForExpiration(
  color: Color,
): Color {
  val darkMode = CaducityTheme.isDark

  return if (color.isLight()) {
    if (darkMode) {
      CaducityTheme.colorScheme.surface
    } else {
      CaducityTheme.colorScheme.onSurface
    }
  } else {
    if (darkMode) {
      CaducityTheme.colorScheme.onSurface
    } else {
      CaducityTheme.colorScheme.surface
    }
  }
}

data class StatusColors(
  val container: Color,
  val onContainer: Color,
)
