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
import androidx.compose.material3.ListItemDefaults
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
import com.alorma.caducity.ui.theme.colors.CaducityColors
import com.alorma.caducity.ui.theme.colors.DarkStateColors
import com.alorma.caducity.ui.theme.colors.LightStateColors
import com.alorma.caducity.ui.theme.colors.animated
import com.alorma.caducity.ui.theme.colors.dynamicColorScheme
import com.alorma.caducity.ui.theme.colors.scheme
import com.alorma.caducity.ui.theme.colors.supportsDynamicColors
import com.alorma.caducity.ui.theme.preview.PreviewDynamicLightDark
import com.alorma.caducity.ui.theme.preview.PreviewTheme
import com.alorma.compose.settings.ui.core.LocalSettingsTileColors
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
  content: @Composable (() -> Unit),
) {
  val systemInDarkTheme = isSystemInDarkTheme()

  val dims =
    CaducityDims(
      noDim = 1f,
      dim1 = 0.72f,
      dim2 = 0.68f,
      dim3 = 0.40f,
      dim4 = 0.16f,
      dim5 = 0.08f,
    )

  val darkTheme =
    when (themePreferences.themeMode.value) {
      ThemeMode.LIGHT -> false
      ThemeMode.DARK -> true
      ThemeMode.SYSTEM -> systemInDarkTheme
    }

  // DYNAMIC sense suport (p. ex. un setting restaurat d'un altre dispositiu): Rebost, sense tocar el setting.
  val palette = themePreferences.colorPalette.value
  val targetScheme =
    if (palette == ColorPalette.DYNAMIC && supportsDynamicColors()) {
      dynamicColorScheme(darkTheme)
    } else {
      palette.scheme(darkTheme)
    }
  val colorScheme = targetScheme.animated()

  MaterialExpressiveTheme(
    colorScheme = colorScheme,
    shapes = CaducityMaterialShapes,
    typography = caducityTypography(),
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

  CompositionLocalProvider(
    LocalCaducityDims provides dims,
    LocalDarkMode provides darkMode,
    LocalCaducityColors provides
      CaducityColors(
        surfaceShape = colorScheme.surfaceContainerHighest,
        states = if (darkMode) DarkStateColors else LightStateColors,
      ),
  ) {
    val settingsColors =
      ListItemDefaults.colors(
        containerColor = colorScheme.surfaceContainer,
        contentColor = colorScheme.primary,
        overlineContentColor = colorScheme.onSurface,
        supportingContentColor = colorScheme.onSurface,
        leadingContentColor = colorScheme.primary,
        trailingContentColor = colorScheme.primary,
      )
    CompositionLocalProvider(LocalSettingsTileColors provides settingsColors) {
      content()
    }
  }
}

@Suppress("ModifierRequired")
@PreviewDynamicLightDark
@Composable
fun MaterialColorsPreview() {
  PreviewTheme {
    MaterialColorsPreviewContent()
  }
}

@Composable
private fun MaterialColorsPreviewContent() {
  Surface {
    Column(
      modifier =
        Modifier
          .padding(16.dp)
          .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = "Color Scheme",
        style = CaducityTheme.typography.titleLarge,
        color = CaducityTheme.colorScheme.onSurface,
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

@Suppress("ModifierRequired")
@PreviewDynamicLightDark
@Composable
fun StatusColorsPreview() {
  PreviewTheme {
    Surface {
      Column(
        modifier =
          Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Text(
          text = "Status colors",
          style = CaducityTheme.typography.titleLarge,
          color = CaducityTheme.colorScheme.onSurface,
        )

        listOf(
          "Expired" to CaducityTheme.colors.expired,
          "Soon" to CaducityTheme.colors.soon,
          "Fresh" to CaducityTheme.colors.fresh,
          "Frozen" to CaducityTheme.colors.frozen,
        ).forEach { (name, colors) ->
          ColorRow("$name strong", colors.strong)
          ColorRow("$name container", colors.container)
        }
      }
    }
  }
}

@Composable
private fun ColorRow(
  name: String,
  color: Color,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(48.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier
          .size(48.dp)
          .background(color, RoundedCornerShape(8.dp))
          .border(
            width = 1.dp,
            color = CaducityTheme.colorScheme.outline,
            shape = RoundedCornerShape(8.dp),
          ),
    )
    Text(
      text = name,
      style = CaducityTheme.typography.bodyMedium,
      color = CaducityTheme.colorScheme.onSurface,
    )
  }
}
