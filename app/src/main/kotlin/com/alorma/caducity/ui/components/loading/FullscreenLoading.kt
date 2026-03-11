package com.alorma.caducity.ui.components.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FullscreenLoading(
  modifier: Modifier = Modifier,
  progress: Float? = null,
) {
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .then(modifier),
    contentAlignment = Alignment.Center,
  ) {
    WavyLoadingIndicator(progress = progress)
  }
}
