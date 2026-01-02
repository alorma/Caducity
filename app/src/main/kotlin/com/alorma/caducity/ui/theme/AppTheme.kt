package com.alorma.caducity.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.theme.colors.BaseExpirationColors
import com.alorma.caducity.ui.theme.colors.ExpirationColorScheme
import com.alorma.caducity.ui.theme.colors.ExpirationColorSchemeType
import com.alorma.caducity.ui.theme.colors.darkColorScheme
import com.alorma.caducity.ui.theme.colors.dynamicColorScheme
import com.alorma.caducity.ui.theme.colors.lightColorScheme
import com.alorma.compose.settings.ui.base.internal.LocalSettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
import com.materialkolor.ktx.harmonize
import com.materialkolor.ktx.isLight
import org.koin.compose.koinInject

@Suppress("ModifierRequired")
@Composable
fun AppTheme(
  themePreferences: ThemePreferences = koinInject(),
  content: @Composable () -> Unit,
) {
  val systemInDarkTheme = isSystemInDarkTheme()

  val dims = CaducityDims(
    noDim = 1f,
    dim1 = 0.72f,
    dim2 = 0.68f,
    dim3 = 0.40f,
    dim4 = 0.16f,
    dim5 = 0.08f,
  )

  val darkTheme = when (themePreferences.themeMode.value) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemInDarkTheme
  }

  val colorScheme = if (themePreferences.useDynamicColors.value) {
    dynamicColorScheme(darkTheme)
  } else {
    if (darkTheme) {
      darkColorScheme
    } else {
      lightColorScheme
    }
  }

  MaterialExpressiveTheme(
    colorScheme = colorScheme,
    typography = caducityTypography,
    motionScheme = MotionScheme.expressive(),
    content = {
      InternalTheme(
        themePreferences = themePreferences,
        darkMode = darkTheme,
        dims = dims,
        content = content,
      )
    },
  )

  // Update system bars appearance based on theme
  val systemBarsAppearance = LocalSystemBarsAppearance.current
  SideEffect {
    systemBarsAppearance?.let {
      // When darkTheme is false (light theme), we want light status bars (dark icons)
      // When darkTheme is true (dark theme), we want dark status bars (light icons)
      it.setLightStatusBars(!darkTheme)
      it.setLightNavigationBars(!darkTheme)
    }
  }
}

@Suppress("ModifierRequired")
@Composable
fun InternalTheme(
  themePreferences: ThemePreferences,
  darkMode: Boolean,
  dims: CaducityDims,
  content: @Composable () -> Unit,
) {
  val expirationColorScheme = generateExpirationColors(
    schemeType = themePreferences.expirationColorSchemeType.value,
    darkMode = darkMode,
  )
  val colorScheme = CaducityTheme.colorScheme

  CompositionLocalProvider(
    LocalExpirationColors provides expirationColorScheme,
    LocalCaducityDims provides dims,
  ) {
    val settingsColors = SettingsTileDefaults.colors(
      containerColor = colorScheme.surfaceContainer,
      titleColor = colorScheme.primary,
      subtitleColor = colorScheme.onSurface,
      iconColor = colorScheme.primary,
      actionColor = colorScheme.primary,
    )
    CompositionLocalProvider(LocalSettingsTileColors provides settingsColors) {
      content()
    }
  }

}

@Suppress("ContentEmission")
@Composable
private fun generateExpirationColors(
  schemeType: ExpirationColorSchemeType,
  darkMode: Boolean,
  baseExpirationColors: BaseExpirationColors = koinInject(),
): ExpirationColorScheme {

  val matchSaturation = when (schemeType) {
    ExpirationColorSchemeType.VIBRANT -> false
    ExpirationColorSchemeType.HARMONIZE -> true
  }

  val baseColor = baseExpirationColors.baseColor

  val freshColor = baseExpirationColors.freshColor
    .harmonize(
      other = baseColor,
      matchSaturation = matchSaturation,
    )
  val expiringSoonColor = baseExpirationColors.expiringSoonColor
    .harmonize(
      other = baseColor,
      matchSaturation = matchSaturation,
    )
  val expiredColor = baseExpirationColors.expiredColor
    .harmonize(
      other = baseColor,
      matchSaturation = matchSaturation,
    )
  val frozenColor = baseExpirationColors.frozenColor
    .harmonize(
      other = baseColor,
      matchSaturation = matchSaturation,
    )
  val consumedColor = baseExpirationColors.consumedColor
    .harmonize(
      other = baseColor,
      matchSaturation = matchSaturation,
    )

  return ExpirationColorScheme(
    fresh = freshColor,
    onFresh = contentColorForExpiration(
      color = freshColor,
      darkMode = darkMode,
    ),
    expiringSoon = expiringSoonColor,
    onExpiringSoon = contentColorForExpiration(
      color = expiringSoonColor,
      darkMode = darkMode,
    ),
    expired = expiredColor,
    onExpired = contentColorForExpiration(
      color = expiredColor,
      darkMode = darkMode,
    ),
    frozen = frozenColor,
    onFrozen = contentColorForExpiration(
      color = frozenColor,
      darkMode = darkMode,
    ),
    consumed = consumedColor,
    onConsumed = contentColorForExpiration(
      color = consumedColor,
      darkMode = darkMode,
    ),
  )
}

@ReadOnlyComposable
@Composable
private fun contentColorForExpiration(
  color: Color,
  darkMode: Boolean,
): Color {
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
