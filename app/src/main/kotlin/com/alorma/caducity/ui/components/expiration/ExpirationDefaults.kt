package com.alorma.caducity.ui.components.expiration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.ui.components.colors.ContainerColors
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.ExpirationColorsPalette
import com.materialkolor.ktx.harmonize
import com.materialkolor.ktx.isLight

object ExpirationDefaults {

  @Composable
  fun getVibrantColors(
    instanceStatus: InstanceStatus,
  ): ContainerColors {
    return expirationColorByStatus(
      instanceStatus = instanceStatus,
      expirationColors = CaducityTheme.expirationColors.vibrant,
    )
  }

  @Composable
  fun getSoftColors(
    instanceStatus: InstanceStatus,
  ): ContainerColors {
    return expirationColorByStatus(
      instanceStatus = instanceStatus,
      expirationColors = CaducityTheme.expirationColors.soft,
    )
  }

  @Composable
  private fun expirationColorByStatus(
    instanceStatus: InstanceStatus,
    expirationColors: ExpirationColorsPalette,
  ): ContainerColors = when (instanceStatus) {
    InstanceStatus.Fresh -> statusColors(expirationColors.fresh)
    InstanceStatus.ExpiringSoon -> statusColors(expirationColors.expiringSoon)
    InstanceStatus.Expired -> statusColors(expirationColors.expired)
    InstanceStatus.Frozen -> statusColors(expirationColors.frozen)
    InstanceStatus.Consumed -> statusColors(expirationColors.consumed)
  }

  @Composable
  private fun statusColors(
    color: Color,
  ): ContainerColors {
    return ContainerColors(
      container = color,
      onContainer = contentColorForExpiration(color),
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

