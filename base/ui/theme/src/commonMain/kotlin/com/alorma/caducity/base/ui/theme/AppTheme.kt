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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.theme.di.themeModule
import org.koin.compose.KoinApplicationPreview

@Suppress("ModifierRequired")
@Composable
fun AppTheme(
  themePreferences: ThemePreferences = ThemePreferencesNoOp,
  content: @Composable () -> Unit,
) {
  val systemInDarkTheme = isSystemInDarkTheme()

  val darkTheme = when (themePreferences.themeMode.value) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemInDarkTheme
  }

  val defaultColorScheme = if (darkTheme) {
    platformDarkColorScheme()
  } else {
    platformLightColorScheme()
  }

  val colorScheme: ColorScheme = if (themePreferences.useDynamicColors.value) {
    dynamicColorScheme(darkTheme) ?: defaultColorScheme
  } else {
    defaultColorScheme
  }

  val dims = CaducityDims(
    noDim = 1f,
    dim1 = 0.72f,
    dim2 = 0.68f,
    dim3 = 0.40f,
    dim4 = 0.16f,
    dim5 = 0.08f,
  )

  val expirationColorScheme = rememberExpirationColorScheme(
    schemeType = themePreferences.expirationColorSchemeType.value,
    darkTheme = darkTheme,
    colorScheme = colorScheme,
    dims = dims,
  )

  val colors = CaducityColors.fromColorScheme(
    colorScheme = colorScheme,
    expirationColorScheme = expirationColorScheme,
  )

  CompositionLocalProvider(
    LocalCaducityColors provides colors,
    LocalCaducityDims provides dims
  ) {
    MaterialExpressiveTheme(
      colorScheme = LocalCaducityColors.current.colorScheme,
      motionScheme = MotionScheme.expressive(),
      content = content,
    )
  }
}

@Suppress("ContentEmission")
@Composable
private fun rememberExpirationColorScheme(
  schemeType: ExpirationColorSchemeType,
  darkTheme: Boolean,
  colorScheme: ColorScheme,
  dims: CaducityDims,
): ExpirationColorScheme {
  return when (schemeType) {
    ExpirationColorSchemeType.VIBRANT -> {
      if (darkTheme) {
        darkExpirationColorScheme
      } else {
        lightExpirationColorScheme
      }
    }

    ExpirationColorSchemeType.HARMONIZE -> {
      val baseColor = colorScheme.primary
      val baseAlpha = dims.dim3

      // Hue values: Green ~120°, Orange ~30°, Red ~0°
      val freshColor = baseColor
        .shiftHueTowards(targetHue = 120f, amount = 1.0f)
        .copy(alpha = baseAlpha)

      val expiringSoonColor = baseColor
        .shiftHueTowards(targetHue = 30f, amount = 1.0f)
        .copy(alpha = baseAlpha)

      val expiredColor = baseColor
        .shiftHueTowards(targetHue = 0f, amount = 1.0f)
        .copy(alpha = baseAlpha)

      ExpirationColorScheme(
        fresh = freshColor,
        onFresh = colorScheme.onPrimary,
        expiringSoon = expiringSoonColor,
        onExpiringSoon = colorScheme.onPrimary,
        expired = expiredColor,
        onExpired = colorScheme.onPrimary,
      )
    }

    ExpirationColorSchemeType.GREY -> {
      val baseColor = colorScheme.onSurface

      val freshColor = baseColor.copy(alpha = dims.dim5)
      val expiringSoonColor = baseColor.copy(alpha = dims.dim4)
      val expiredColor = baseColor.copy(alpha = dims.dim3)

      ExpirationColorScheme(
        fresh = freshColor,
        onFresh = colorScheme.onSurface,
        expiringSoon = expiringSoonColor,
        onExpiringSoon = colorScheme.onSurface,
        expired = expiredColor,
        onExpired = colorScheme.onSurface,
      )
    }

    ExpirationColorSchemeType.PLAIN -> {
      ExpirationColorScheme(
        fresh = colorScheme.surfaceContainerLow,
        onFresh = colorScheme.onSurface,
        expiringSoon = colorScheme.surfaceContainer,
        onExpiringSoon = colorScheme.onSurface,
        expired = colorScheme.surfaceContainerHighest,
        onExpired = colorScheme.onSurface,
      )
    }
  }
}

@Preview
@Composable
private fun ExpirationColorsPreview() {
  KoinApplicationPreview(application = { modules(themeModule) }) {
    AppTheme {
      ExpirationColorSchemeType
        .entries
        .forEach { expirationColorSchemeType ->
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Text(text = "Type: ${expirationColorSchemeType.name}")

            ExpirationColorLegend(
              expirationColors = rememberExpirationColorScheme(
                schemeType = expirationColorSchemeType,
                darkTheme = false,
                colorScheme = CaducityTheme.colorScheme,
                dims = CaducityTheme.dims,
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