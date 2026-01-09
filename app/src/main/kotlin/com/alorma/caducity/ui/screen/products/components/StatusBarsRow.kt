package com.alorma.caducity.ui.screen.products.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.components.expiration.ExpirationDefaults
import com.alorma.caducity.ui.components.shape.ShapePosition
import com.alorma.caducity.ui.components.shape.toHorizontalShape
import com.alorma.caducity.ui.screen.products.ProductInstanceVariantGroup
import kotlinx.collections.immutable.ImmutableList

@Composable
fun StatusBarsRow(
  statusGroups: ImmutableList<ProductInstanceVariantGroup>,
  modifier: Modifier = Modifier,
  gap: Dp = 4.dp,
) {
  if (statusGroups.isEmpty()) return

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(24.dp),
    horizontalArrangement = Arrangement.spacedBy(gap),
  ) {
    val groupSize = statusGroups.size
    statusGroups.forEachIndexed { index, statusGroup ->
      val colors = ExpirationDefaults.getVibrantColors(statusGroup.status)
      val shape = when {
        groupSize == 1 -> ShapePosition.Single
        index == 0 -> ShapePosition.Start
        index == statusGroups.lastIndex -> ShapePosition.End
        else -> ShapePosition.Middle
      }
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .weight(statusGroup.count.toFloat())
          .clip(shape.toHorizontalShape())
          .background(colors.container),
      )
    }
  }
}
