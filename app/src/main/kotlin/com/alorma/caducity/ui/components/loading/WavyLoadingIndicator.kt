package com.alorma.caducity.ui.components.loading

import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WavyLoadingIndicator(
  modifier: Modifier = Modifier,
  progress: Float? = null,
) {
  if (progress != null) {
    CircularWavyProgressIndicator(
      progress = { progress },
      amplitude = { progress },
      modifier = modifier,
    )
  } else {
    CircularWavyProgressIndicator(modifier = modifier)
  }
}
