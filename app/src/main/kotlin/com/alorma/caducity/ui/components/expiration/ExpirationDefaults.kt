package com.alorma.caducity.ui.components.expiration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.colors.ContainerColors
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.StatusColors

object ExpirationDefaults {
  @Composable
  fun getTitle(itemStatus: ItemStatus): String =
    when (itemStatus) {
      ItemStatus.Expired -> stringResource(R.string.expiration_status_badge_expired)
      ItemStatus.ExpiringSoon -> stringResource(R.string.expiration_status_badge_expiring_soon)
      ItemStatus.Fresh -> stringResource(R.string.expiration_status_badge_fresh)
      ItemStatus.Frozen -> stringResource(R.string.expiration_status_badge_frozen)
      ItemStatus.Consumed, ItemStatus.Discarded -> stringResource(R.string.expiration_status_badge_consumed)
    }

  /** Colors forts (barres, punts, botons): `strong` amb text d'alt contrast. */
  @Composable
  fun getVibrantColors(itemStatus: ItemStatus): ContainerColors {
    val strong = strongColor(itemStatus) ?: return consumedColors()
    return ContainerColors(
      container = strong,
      onContainer = CaducityTheme.contentOn(strong),
    )
  }

  /** Colors suaus (pastilles, fons): `*-container` amb `on-*-container`. */
  @Composable
  fun getSoftColors(itemStatus: ItemStatus): ContainerColors {
    val colors = statusColors(itemStatus) ?: return consumedColors()
    return ContainerColors(
      container = colors.container,
      onContainer = colors.onContainer,
    )
  }

  /** `null` per a Consumed: no és un estat de color. */
  @Composable
  @ReadOnlyComposable
  fun statusColors(itemStatus: ItemStatus): StatusColors? =
    when (itemStatus) {
      ItemStatus.Fresh -> CaducityTheme.colors.fresh
      ItemStatus.ExpiringSoon -> CaducityTheme.colors.soon
      ItemStatus.Expired -> CaducityTheme.colors.expired
      ItemStatus.Frozen -> CaducityTheme.colors.frozen
      ItemStatus.Consumed, ItemStatus.Discarded -> null
    }

  @Composable
  @ReadOnlyComposable
  private fun strongColor(itemStatus: ItemStatus): Color? = statusColors(itemStatus)?.strong

  @Composable
  @ReadOnlyComposable
  private fun consumedColors(): ContainerColors =
    ContainerColors(
      container = CaducityTheme.colorScheme.surfaceContainerHigh,
      onContainer = CaducityTheme.colorScheme.onSurfaceVariant,
    )
}
