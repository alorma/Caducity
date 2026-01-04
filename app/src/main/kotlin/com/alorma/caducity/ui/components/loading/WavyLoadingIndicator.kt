package com.alorma.caducity.ui.components.loading

import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun WavyLoadingIndicator(modifier: Modifier = Modifier) {
  LoadingIndicator(
    modifier = modifier,
    color = CaducityTheme.colorScheme.secondary,
    polygons = listOf(
      MaterialShapes.Cookie4Sided,
      MaterialShapes.Cookie6Sided,
    ),
  )
}