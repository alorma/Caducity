package com.alorma.caducity.base.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.theme.colors.ExpirationColorScheme
import com.alorma.caducity.base.ui.theme.colors.ExpirationColorSchemeType
import com.alorma.caducity.base.ui.theme.colors.darkColorScheme
import com.alorma.caducity.base.ui.theme.colors.dynamicColorScheme
import com.alorma.caducity.base.ui.theme.colors.lightColorScheme
import com.alorma.caducity.base.ui.theme.preview.AppPreview
import com.alorma.compose.settings.ui.base.internal.LocalSettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
import com.materialkolor.ktx.harmonizeWithPrimary
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
): ExpirationColorScheme {

  val colorScheme = CaducityTheme.colorScheme

  val matchSaturation = when (schemeType) {
    ExpirationColorSchemeType.VIBRANT -> false
    ExpirationColorSchemeType.HARMONIZE -> true
  }

  val freshColor = colorScheme.harmonizeWithPrimary(
    color = Color.Green,
    matchSaturation = matchSaturation,
  )
  val expiringSoonColor = colorScheme.harmonizeWithPrimary(
    color = Color(0xFFFFDE21),
    matchSaturation = matchSaturation,
  )
  val expiredColor = colorScheme.harmonizeWithPrimary(
    color = Color.Red,
    matchSaturation = matchSaturation,
  )
  val frozenColor = colorScheme.harmonizeWithPrimary(
    color = Color.Cyan,
    matchSaturation = matchSaturation,
  )
  val consumedColor = colorScheme.harmonizeWithPrimary(
    color = Color.Gray,
    matchSaturation = matchSaturation,
  )

  return ExpirationColorScheme(
    fresh = freshColor,
    onFresh = if (freshColor.isLight() && !darkMode) {
      colorScheme.inverseSurface
    } else {
      colorScheme.surface
    },
    expiringSoon = expiringSoonColor,
    onExpiringSoon = if (expiringSoonColor.isLight() && !darkMode) {
      colorScheme.inverseSurface
    } else {
      colorScheme.surface
    },
    expired = expiredColor,
    onExpired = if (expiredColor.isLight() && !darkMode) {
      colorScheme.inverseSurface
    } else {
      colorScheme.surface
    },
    frozen = frozenColor,
    onFrozen = if (frozenColor.isLight() && !darkMode) {
      colorScheme.inverseSurface
    } else {
      colorScheme.surface
    },
    consumed = consumedColor,
    onConsumed = if (consumedColor.isLight() && !darkMode) {
      colorScheme.inverseSurface
    } else {
      colorScheme.surface
    },
  )
}

@Preview
@Composable
private fun ExpirationColorsPreview() {
  AppPreview {
    Surface {
      ExpirationColorSchemeType
        .entries
        .forEach { expirationColorSchemeType ->
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(text = "Type: ${expirationColorSchemeType.name}")

            ExpirationColorLegend(
              expirationColors = generateExpirationColors(
                schemeType = expirationColorSchemeType,
                darkMode = isSystemInDarkTheme(),
              )
            )
          }
        }
    }
  }
}


@Composable
private fun ExpirationColorLegend(expirationColors: ExpirationColorScheme) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    ColorLegendItem(
      label = "Fresh",
      color = expirationColors.fresh,
    )
    ColorLegendItem(
      label = "Expire soon",
      color = expirationColors.expiringSoon,
    )
    ColorLegendItem(
      label = "Expired",
      color = expirationColors.expired,
    )
  }
}

@Composable
private fun ColorLegendItem(
  label: String,
  color: Color,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(color),
    )
    Text(text = label)
  }
}