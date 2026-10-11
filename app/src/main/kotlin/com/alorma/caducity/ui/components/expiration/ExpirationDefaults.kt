package com.alorma.caducity.ui.components.expiration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import com.alorma.caducity.R
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.colors.ContainerColors
import com.alorma.caducity.ui.theme.CaducityTheme
import com.alorma.caducity.ui.theme.colors.StatusColors

private val LightInk = Color(0xFF29241C)
private val LightSurface = Color(0xFFFBF6EC)

object ExpirationDefaults {
  @Composable
  fun getTitle(itemStatus: ItemStatus): String =
    when (itemStatus) {
      ItemStatus.Expired -> stringResource(R.string.expiration_status_badge_expired)
      ItemStatus.ExpiringSoon -> stringResource(R.string.expiration_status_badge_expiring_soon)
      ItemStatus.Fresh -> stringResource(R.string.expiration_status_badge_fresh)
      ItemStatus.Frozen -> stringResource(R.string.expiration_status_badge_frozen)
      ItemStatus.Consumed -> stringResource(R.string.expiration_status_badge_consumed)
    }

  /** Colors forts (barres, punts, botons): `strong` amb text d'alt contrast. */
  @Composable
  fun getVibrantColors(itemStatus: ItemStatus): ContainerColors {
    val strong = strongColor(itemStatus) ?: return consumedColors()
    return ContainerColors(
      container = strong,
      onContainer = contentColorFor(strong),
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

  // TEMP(fase 7): el redisseny no té tons; les pantalles velles usen els colors suaus.
  @Composable
  fun getColors(itemStatus: ItemStatus): ContainerColors = getSoftColors(itemStatus)

  // TEMP(fase 7)
  @Composable
  fun getInverseColors(itemStatus: ItemStatus): ContainerColors = getVibrantColors(itemStatus)

  /** `null` per a Consumed: no és un estat de color. */
  @Composable
  @ReadOnlyComposable
  fun statusColors(itemStatus: ItemStatus): StatusColors? =
    when (itemStatus) {
      ItemStatus.Fresh -> CaducityTheme.colors.fresh
      ItemStatus.ExpiringSoon -> CaducityTheme.colors.soon
      ItemStatus.Expired -> CaducityTheme.colors.expired
      ItemStatus.Frozen -> CaducityTheme.colors.frozen
      ItemStatus.Consumed -> null
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

  /** Tinta o crema segons la lluminància del fons. */
  @Composable
  @ReadOnlyComposable
  private fun contentColorFor(background: Color): Color =
    if (background.luminance() > 0.4f) {
      LightInk
    } else {
      LightSurface
    }
}
