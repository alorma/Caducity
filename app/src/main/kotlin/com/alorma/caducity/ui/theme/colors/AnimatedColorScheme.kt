package com.alorma.caducity.ui.theme.colors

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private const val PALETTE_FADE_MILLIS = 300

/** Fos entre colors en canviar de paleta (o de tema); no anima la primera composició. */
@Composable
fun ColorScheme.animated(): ColorScheme {
  @Composable
  fun Color.fade(): Color = animateColorAsState(this, tween(PALETTE_FADE_MILLIS), label = "scheme").value

  return copy(
    primary = primary.fade(),
    onPrimary = onPrimary.fade(),
    primaryContainer = primaryContainer.fade(),
    onPrimaryContainer = onPrimaryContainer.fade(),
    inversePrimary = inversePrimary.fade(),
    secondary = secondary.fade(),
    onSecondary = onSecondary.fade(),
    secondaryContainer = secondaryContainer.fade(),
    onSecondaryContainer = onSecondaryContainer.fade(),
    background = background.fade(),
    onBackground = onBackground.fade(),
    surface = surface.fade(),
    onSurface = onSurface.fade(),
    surfaceVariant = surfaceVariant.fade(),
    onSurfaceVariant = onSurfaceVariant.fade(),
    surfaceTint = surfaceTint.fade(),
    inverseSurface = inverseSurface.fade(),
    inverseOnSurface = inverseOnSurface.fade(),
    outline = outline.fade(),
    outlineVariant = outlineVariant.fade(),
    surfaceBright = surfaceBright.fade(),
    surfaceDim = surfaceDim.fade(),
    surfaceContainerLowest = surfaceContainerLowest.fade(),
    surfaceContainerLow = surfaceContainerLow.fade(),
    surfaceContainer = surfaceContainer.fade(),
    surfaceContainerHigh = surfaceContainerHigh.fade(),
    surfaceContainerHighest = surfaceContainerHighest.fade(),
  )
}
