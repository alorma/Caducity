package com.alorma.caducity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.alorma.caducity.ui.screen.transform.contrastRatio
import com.alorma.caducity.ui.theme.colors.CaducityColors

data class CaducityDims(
  val noDim: Float,
  val dim1: Float,
  val dim2: Float,
  val dim3: Float,
  val dim4: Float,
  val dim5: Float,
)

internal val LocalDarkMode =
  staticCompositionLocalOf<Boolean> {
    error("No dark mode defined")
  }

internal val LocalCaducityColors =
  staticCompositionLocalOf<CaducityColors> {
    error("No CaducityColors defined")
  }

internal val LocalCaducityDims =
  staticCompositionLocalOf<CaducityDims> {
    error("No CaducityDims defined")
  }

object CaducityTheme {
  val isDark: Boolean
    @Composable
    @ReadOnlyComposable
    get() = LocalDarkMode.current

  val colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

  val colors: CaducityColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCaducityColors.current

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

  val motionScheme: MotionScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.motionScheme

  /** Text or icon colour for content drawn over [color]: see [ColorScheme.contentOn]. */
  @Composable
  @ReadOnlyComposable
  fun contentOn(color: Color): Color = colorScheme.contentOn(color)
}

/**
 * Of `onSurface` and `surface`, the one with more contrast against [color]. Over the `strong` status colours
 * (dark in the light theme, light in the dark theme) this gives the cream or the ink the theme already defines.
 */
internal fun ColorScheme.contentOn(color: Color): Color =
  if (contrastRatio(onSurface, color) >= contrastRatio(surface, color)) onSurface else surface
