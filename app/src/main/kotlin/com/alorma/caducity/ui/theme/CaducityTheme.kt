package com.alorma.caducity.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.alorma.caducity.ui.theme.colors.ExpirationColorScheme

data class CaducityDims(
  val noDim: Float,
  val dim1: Float,
  val dim2: Float,
  val dim3: Float,
  val dim4: Float,
  val dim5: Float,
)

internal val LocalExpirationColors = staticCompositionLocalOf<ExpirationColorScheme> {
  error("No ExpirationColorScheme defined")
}

internal val LocalCaducityDims = staticCompositionLocalOf<CaducityDims> {
  error("No CaducityDims defined")
}

object CaducityTheme {

  val colorScheme: ColorScheme
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme

  val expirationColorScheme: ExpirationColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalExpirationColors.current

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

}