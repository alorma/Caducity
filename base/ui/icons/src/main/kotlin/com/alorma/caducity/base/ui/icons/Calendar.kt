package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.CalendarExpand: ImageVector
  get() {
    if (_calendarExpand != null) {
      return _calendarExpand!!
    }
    _calendarExpand = ImageVector.Builder(
      name = "CalendarExpand",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Calendar outline
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(19f, 4f)
        lineTo(5f, 4f)
        curveTo(3.8954f, 4f, 3f, 4.8954f, 3f, 6f)
        lineTo(3f, 20f)
        curveTo(3f, 21.1046f, 3.8954f, 22f, 5f, 22f)
        lineTo(19f, 22f)
        curveTo(20.1046f, 22f, 21f, 21.1046f, 21f, 20f)
        lineTo(21f, 6f)
        curveTo(21f, 4.8954f, 20.1046f, 4f, 19f, 4f)
        close()
      }
      // Top divider line
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(3f, 10f)
        lineTo(21f, 10f)
      }
      // Left calendar hanger
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(8f, 2f)
        lineTo(8f, 6f)
      }
      // Right calendar hanger
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(16f, 2f)
        lineTo(16f, 6f)
      }
      // Expand arrows pointing down and up
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        // Top arrow pointing down
        moveTo(9f, 14f)
        lineTo(12f, 17f)
        lineTo(15f, 14f)
      }
    }.build()
    return _calendarExpand!!
  }

private var _calendarExpand: ImageVector? = null

val AppIcons.CalendarCollapse: ImageVector
  get() {
    if (_calendarCollapse != null) {
      return _calendarCollapse!!
    }
    _calendarCollapse = ImageVector.Builder(
      name = "CalendarCollapse",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f,
    ).apply {
      // Calendar outline
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(19f, 4f)
        lineTo(5f, 4f)
        curveTo(3.8954f, 4f, 3f, 4.8954f, 3f, 6f)
        lineTo(3f, 20f)
        curveTo(3f, 21.1046f, 3.8954f, 22f, 5f, 22f)
        lineTo(19f, 22f)
        curveTo(20.1046f, 22f, 21f, 21.1046f, 21f, 20f)
        lineTo(21f, 6f)
        curveTo(21f, 4.8954f, 20.1046f, 4f, 19f, 4f)
        close()
      }
      // Top divider line
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(3f, 10f)
        lineTo(21f, 10f)
      }
      // Left calendar hanger
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(8f, 2f)
        lineTo(8f, 6f)
      }
      // Right calendar hanger
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        moveTo(16f, 2f)
        lineTo(16f, 6f)
      }
      // Collapse arrow pointing up
      path(
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeAlpha = 1.0f,
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathFillType = PathFillType.NonZero,
      ) {
        // Arrow pointing up
        moveTo(15f, 17f)
        lineTo(12f, 14f)
        lineTo(9f, 17f)
      }
    }.build()
    return _calendarCollapse!!
  }

private var _calendarCollapse: ImageVector? = null
