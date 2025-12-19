package com.alorma.caducity.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Close: ImageVector
  get() {
    if (_close != null) {
      return _close!!
    }
    _close = ImageVector.Builder(
      name = "Close",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      path(
        fill = null,
        fillAlpha = 1.0f,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineMiter = 1.0f,
        pathFillType = PathFillType.NonZero,
      ) {
        // X shape - diagonal from top-left to bottom-right
        moveTo(6f, 6f)
        lineTo(18f, 18f)
        
        // X shape - diagonal from top-right to bottom-left
        moveTo(18f, 6f)
        lineTo(6f, 18f)
      }
    }.build()
    return _close!!
  }

private var _close: ImageVector? = null
