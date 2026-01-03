package com.alorma.caducity.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import com.alorma.caducity.ui.theme.colors.ExpirationColors
import com.alorma.caducity.ui.theme.colors.ExpirationColorsPalette
import com.alorma.caducity.ui.theme.colors.SoftExpirationColors
import com.alorma.caducity.ui.theme.colors.VibrantExpirationColors
import com.alorma.caducity.ui.theme.colors.darkColorScheme
import com.alorma.caducity.ui.theme.colors.dynamicColorScheme
import com.alorma.caducity.ui.theme.colors.lightColorScheme
import com.alorma.compose.settings.ui.base.internal.LocalSettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
import org.koin.compose.koinInject

@Suppress("ModifierRequired")
@Composable
fun AppTheme(
  themePreferences: ThemePreferences = koinInject(),
  defaultExpirationColors: ExpirationColorsPalette = koinInject(),
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
        dims = dims,
        darkMode = darkTheme,
        defaultExpirationColors = defaultExpirationColors,
        content = content,
      )
    },
  )

  // Update system bars appearance based on theme
  val systemBarsAppearance = LocalSystemBarsAppearance.current
  SideEffect {
    systemBarsAppearance?.let {
      it.setLightStatusBars(!darkTheme)
      it.setLightNavigationBars(!darkTheme)
    }
  }
}

@Suppress("ModifierRequired")
@Composable
fun InternalTheme(
  dims: CaducityDims,
  darkMode: Boolean,
  defaultExpirationColors: ExpirationColorsPalette,
  content: @Composable () -> Unit,
) {
  val colorScheme = CaducityTheme.colorScheme

  val expirationColors = ExpirationColors(
    vibrant = VibrantExpirationColors(
      default = defaultExpirationColors,
      baseColor = colorScheme.surfaceContainerHighest,
    ),
    soft = SoftExpirationColors(
      default = defaultExpirationColors,
      baseColor = colorScheme.surfaceContainerHighest,
    ),
  )

  CompositionLocalProvider(
    LocalCaducityDims provides dims,
    LocalDarkMode provides darkMode,
    LocalExpirationColors provides expirationColors,
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
