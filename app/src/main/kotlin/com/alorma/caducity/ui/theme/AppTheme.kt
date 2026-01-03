package com.alorma.caducity.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.colors.DefaultExpirationColors
import com.alorma.caducity.ui.theme.colors.ExpirationColors
import com.alorma.caducity.ui.theme.colors.SoftExpirationColors
import com.alorma.caducity.ui.theme.colors.VibrantExpirationColors
import com.alorma.caducity.ui.theme.colors.darkColorScheme
import com.alorma.caducity.ui.theme.colors.dynamicColorScheme
import com.alorma.caducity.ui.theme.colors.lightColorScheme
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.compose.settings.ui.base.internal.LocalSettingsTileColors
import com.alorma.compose.settings.ui.base.internal.SettingsTileDefaults
import org.koin.compose.koinInject

@Suppress("ModifierRequired")
@Composable
fun AppTheme(
  themePreferences: ThemePreferences = koinInject(),
  content: @Composable () -> Unit,
) {
  AppThemeContent(themePreferences, content)
}

@Composable
fun AppThemeContent(
  themePreferences: ThemePreferences,
  content: @Composable (() -> Unit)
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
  content: @Composable () -> Unit,
) {
  val colorScheme = CaducityTheme.colorScheme

  val defaultExpirationColors = DefaultExpirationColors(
    error = colorScheme.error,
  )

  val baseColor = colorScheme.surfaceContainerHighest

  val expirationColors = ExpirationColors(
    vibrant = VibrantExpirationColors(
      default = defaultExpirationColors,
      baseColor = baseColor,
    ),
    soft = SoftExpirationColors(
      default = defaultExpirationColors,
      baseColor = baseColor,
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


@Suppress("ModifierRequired")
@PreviewDynamicLightDark
@Composable
private fun MaterialColorsPreviewTheme() {
  PreviewTheme {
    Surface {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Color Scheme",
          style = CaducityTheme.typography.titleLarge,
          color = CaducityTheme.colorScheme.onSurface
        )

        ColorRow("Primary", CaducityTheme.colorScheme.primary)
        ColorRow("Secondary", CaducityTheme.colorScheme.secondary)
        ColorRow("Tertiary", CaducityTheme.colorScheme.tertiary)
        ColorRow("Error", CaducityTheme.colorScheme.error)
        ColorRow("Surface", CaducityTheme.colorScheme.surface)
        ColorRow("Surface Variant", CaducityTheme.colorScheme.surfaceVariant)
      }
    }
  }
}

@Suppress("ModifierRequired")
@PreviewDynamicLightDark
@Composable
private fun ExpirationColorsVibrantPreviewTheme() {
  PreviewTheme {
    Surface {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Expiration Colors - Vibrant",
          style = CaducityTheme.typography.titleLarge,
          color = CaducityTheme.colorScheme.onSurface
        )

        ColorRow("Fresh", CaducityTheme.expirationColors.vibrant.fresh)
        ColorRow("Expiring Soon", CaducityTheme.expirationColors.vibrant.expiringSoon)
        ColorRow("Expired", CaducityTheme.expirationColors.vibrant.expired)
        ColorRow("Frozen", CaducityTheme.expirationColors.vibrant.frozen)
        ColorRow("Consumed", CaducityTheme.expirationColors.vibrant.consumed)
      }
    }
  }
}

@Suppress("ModifierRequired")
@PreviewDynamicLightDark
@Composable
private fun ExpirationColorsSoftPreviewTheme() {
  PreviewTheme {
    Surface {
      Column(
        modifier = Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Expiration Colors - Soft",
          style = CaducityTheme.typography.titleLarge,
          color = CaducityTheme.colorScheme.onSurface
        )

        ColorRow("Fresh", CaducityTheme.expirationColors.soft.fresh)
        ColorRow("Expiring Soon", CaducityTheme.expirationColors.soft.expiringSoon)
        ColorRow("Expired", CaducityTheme.expirationColors.soft.expired)
        ColorRow("Frozen", CaducityTheme.expirationColors.soft.frozen)
        ColorRow("Consumed", CaducityTheme.expirationColors.soft.consumed)
      }
    }
  }
}

@Composable
private fun ColorRow(name: String, color: Color) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(48.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .background(color, RoundedCornerShape(8.dp))
        .border(
          width = 1.dp,
          color = CaducityTheme.colorScheme.outline,
          shape = RoundedCornerShape(8.dp)
        )
    )
    Text(
      text = name,
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurface
    )
  }
}