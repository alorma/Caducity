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
  fun getColors(
    instanceStatus: InstanceStatus,
    matchSaturation: Boolean = false,
  ): StatusColors {
    val expirationColors = CaducityTheme.expirationColors
    return when (instanceStatus) {
      InstanceStatus.Fresh -> statusColors(expirationColors.fresh, matchSaturation)
      InstanceStatus.ExpiringSoon -> statusColors(expirationColors.expiringSoon, matchSaturation)
      InstanceStatus.Expired -> statusColors(expirationColors.expired, matchSaturation)
      InstanceStatus.Frozen -> statusColors(expirationColors.frozen, matchSaturation)
      InstanceStatus.Consumed -> statusColors(expirationColors.consumed, matchSaturation)
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
