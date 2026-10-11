package com.alorma.caducity.ui.theme.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
@ReadOnlyComposable
fun ExpiryStatus.colors(): StatusColors {
  val colors = CaducityTheme.colors
  return when (this) {
    ExpiryStatus.EXPIRED -> colors.expired
    ExpiryStatus.SOON -> colors.soon
    ExpiryStatus.FRESH -> colors.fresh
    ExpiryStatus.FROZEN -> colors.frozen
  }
}
