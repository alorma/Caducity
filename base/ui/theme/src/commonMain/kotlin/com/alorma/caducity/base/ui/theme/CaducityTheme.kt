package com.alorma.caducity.base.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

data class CaducityColors(
  val colorScheme: ColorScheme,
  val expirationColorScheme: ExpirationColorScheme,
) {
  companion object {
    fun fromColorScheme(
      colorScheme: ColorScheme,
      expirationColorScheme: ExpirationColorScheme,
    ): CaducityColors {
      return CaducityColors(
        colorScheme = colorScheme,
        expirationColorScheme = expirationColorScheme,
      )
    }
  }
}

internal val LocalCaducityColors = staticCompositionLocalOf<CaducityColors> {
  error("No CaducityThemeColors defined")
}

object CaducityTheme {

  val colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalCaducityColors.current.colorScheme

  val expirationColorScheme: ExpirationColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalCaducityColors.current.expirationColorScheme

  val shapes: Shapes
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes

  val typography: Typography
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.typography

}