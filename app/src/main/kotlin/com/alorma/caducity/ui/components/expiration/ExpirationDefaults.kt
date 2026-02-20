package com.alorma.caducity.ui.components.expiration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.colors.ContainerColors
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.ExpirationColorsPalette
import com.materialkolor.ktx.isLight

object ExpirationDefaults {

  @Composable
  fun getTitle(itemStatus: ItemStatus): String {
    return when (itemStatus) {
      ItemStatus.Expired -> stringResource(R.string.expiration_status_badge_expired)
      ItemStatus.ExpiringSoon -> stringResource(R.string.expiration_status_badge_expiring_soon)
      ItemStatus.Fresh -> stringResource(R.string.expiration_status_badge_fresh)
      ItemStatus.Frozen -> stringResource(R.string.expiration_status_badge_frozen)
      ItemStatus.Consumed -> stringResource(R.string.expiration_status_badge_consumed)
    }
  }

  @Composable
  fun getVibrantColors(
    itemStatus: ItemStatus,
  ): ContainerColors {
    return expirationColorByStatus(
      itemStatus = itemStatus,
      expirationColors = CaducityTheme.expirationColors.vibrant,
    )
  }

  @Composable
  fun getSoftColors(
    itemStatus: ItemStatus,
  ): ContainerColors {
    return expirationColorByStatus(
      itemStatus = itemStatus,
      expirationColors = CaducityTheme.expirationColors.soft,
    )
  }

  @Composable
  fun getColors(
    itemStatus: ItemStatus,
  ): ContainerColors {
    val tone = CaducityTheme.themeTone
    return when (tone) {
      com.alorma.caducity.ui.theme.ThemeTone.VIBRANT -> getVibrantColors(itemStatus)
      com.alorma.caducity.ui.theme.ThemeTone.SOFT -> getSoftColors(itemStatus)
    }
  }

  @Composable
  private fun expirationColorByStatus(
    itemStatus: ItemStatus,
    expirationColors: ExpirationColorsPalette,
  ): ContainerColors = when (itemStatus) {
    ItemStatus.Fresh -> statusColors(expirationColors.fresh)
    ItemStatus.ExpiringSoon -> statusColors(expirationColors.expiringSoon)
    ItemStatus.Expired -> statusColors(expirationColors.expired)
    ItemStatus.Frozen -> statusColors(expirationColors.frozen)
    ItemStatus.Consumed -> statusColors(expirationColors.consumed)
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
}

