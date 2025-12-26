package com.alorma.caducity.base.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.compositeOver

data class CaducityColors(
  val colorScheme: ColorScheme,
  val expirationColorScheme: ExpirationColorScheme,
) {
  companion object {
    fun fromColorScheme(
      colorScheme: ColorScheme,
      dims: CaducityDims,
    ): CaducityColors {
      // Hue values: Green ~120°, Orange ~30°, Red ~0°
      val freshColor = colorScheme.primary
        .shiftHueTowards(targetHue = 120f, amount = 1.0f)
        .copy(alpha = dims.dim3)
        .compositeOver(colorScheme.surface)

      val expiringSoonColor = colorScheme.primary
        .shiftHueTowards(targetHue = 30f, amount = 1.0f)
        .copy(alpha = dims.dim2)
        .compositeOver(colorScheme.surface)

      val expiredColor = colorScheme.primary
        .shiftHueTowards(targetHue = 0f, amount = 1.0f)
        .copy(alpha = dims.dim1)
        .compositeOver(colorScheme.surface)

      return CaducityColors(
        colorScheme = colorScheme,
        expirationColorScheme = ExpirationColorScheme(
          fresh = freshColor,
          onFresh = colorScheme.onPrimary,
          expiringSoon = expiringSoonColor,
          onExpiringSoon = colorScheme.onPrimary,
          expired = expiredColor,
          onExpired = colorScheme.onPrimary,
        ),
      )
    }
  }
}

data class CaducityDims(
  val noDim: Float,
  val dim1: Float,
  val dim2: Float,
  val dim3: Float,
  val dim4: Float,
  val dim5: Float,
)

internal val LocalCaducityColors = staticCompositionLocalOf<CaducityColors> {
  error("No CaducityThemeColors defined")
}

internal val LocalCaducityDims = staticCompositionLocalOf<CaducityDims> {
  error("No CaducityDims defined")
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

  val dims: CaducityDims
    @Composable
    @ReadOnlyComposable
    get() = LocalCaducityDims.current

}