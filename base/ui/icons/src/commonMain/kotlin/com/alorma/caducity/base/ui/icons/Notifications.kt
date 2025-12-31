package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Notifications: ImageVector
  get() {
    if (_notifications != null) {
      return _notifications!!
    }
    _notifications = ImageVector.Builder(
      name = "Notifications",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Bell body
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(18f, 8f)
        curveTo(18f, 6.4087f, 17.3679f, 4.8826f, 16.2426f, 3.7574f)
        curveTo(15.1174f, 2.6321f, 13.5913f, 2f, 12f, 2f)
        curveTo(10.4087f, 2f, 8.8826f, 2.6321f, 7.7574f, 3.7574f)
        curveTo(6.6321f, 4.8826f, 6f, 6.4087f, 6f, 8f)
        curveTo(6f, 15f, 3f, 17f, 3f, 17f)
        lineTo(21f, 17f)
        curveTo(21f, 17f, 18f, 15f, 18f, 8f)
        close()
      }
      // Bell clapper
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(13.73f, 21f)
        curveTo(13.5542f, 21.3031f, 13.3019f, 21.5547f, 12.9982f, 21.7295f)
        curveTo(12.6946f, 21.9044f, 12.3504f, 21.9965f, 12f, 21.9965f)
        curveTo(11.6496f, 21.9965f, 11.3054f, 21.9044f, 11.0018f, 21.7295f)
        curveTo(10.6982f, 21.5547f, 10.4458f, 21.3031f, 10.27f, 21f)
      }
    }.build()
    return _notifications!!
  }

private var _notifications: ImageVector? = null
