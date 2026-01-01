package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Search: ImageVector
  get() {
    if (_search != null) {
      return _search!!
    }
    _search = ImageVector.Builder(
      name = "Search",
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
        // Circle (magnifying glass)
        moveTo(11f, 19f)
        arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, -16f)
        arcToRelative(8f, 8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 0f, 16f)
        close()
        
        // Handle
        moveTo(21f, 21f)
        lineTo(16.65f, 16.65f)
      }
    }.build()
    return _search!!
  }

private var _search: ImageVector? = null
