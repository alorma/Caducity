package com.alorma.caducity.ui.screen.product.detail.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alorma.caducity.ui.theme.CaducityTheme

@Composable
fun TimelineBulletAndLine(
  itemHeight: Dp,
  bulletOffset: Offset,
  onBulletPositionObtained: (Offset) -> Unit,
) {
  val localDensity = LocalDensity.current

  Box(
    modifier = Modifier
      .width(24.dp)
      .height(itemHeight),
    contentAlignment = Alignment.TopCenter,
  ) {
    val lineColor = CaducityTheme.colorScheme.outline

    val startOffset = Offset.Zero

    val endOffset = with(localDensity) {
      Offset(x = 0f, y = itemHeight.toPx())
    }

    Canvas(
      modifier = Modifier
        .height(itemHeight)
        .width(1.dp),
    ) {
      drawLine(
        strokeWidth = 1.dp.toPx(),
        color = lineColor,
        start = startOffset,
        end = endOffset,
      )
    }

    TimelineBullet(
      modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        with(localDensity) {
          val positionInParent = layoutCoordinates.positionInParent()
          onBulletPositionObtained(
            Offset(
              x = positionInParent.x.toDp().toPx(),
              y = positionInParent.y.toDp().toPx() + layoutCoordinates.size.height.toDp()
                .toPx() / 2,
            )
          )
        }
      },
    )
  }
}

@Composable
internal fun TimelineBullet(
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = Modifier
      .padding(top = 24.dp, start = 4.dp, end = 4.dp)
      .size(16.dp)
      .clip(CircleShape)
      .border(width = 1.dp, color = CaducityTheme.colorScheme.outline, shape = CircleShape)
      .background(color = CaducityTheme.colorScheme.surface, shape = CircleShape)
      .then(modifier),
  )
}