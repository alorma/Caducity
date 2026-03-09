package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.MoreVert: ImageVector
  get() {
    if (_moreVert != null) {
      return _moreVert!!
    }
    _moreVert =
      ImageVector
        .Builder(
          name = "MoreVert",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        ).apply {
          // Top dot
          path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
          ) {
            moveTo(12f, 8f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -4f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 4f)
          }
          // Middle dot
          path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
          ) {
            moveTo(12f, 14f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -4f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 4f)
          }
          // Bottom dot
          path(
            fill = SolidColor(Color.Black),
            pathFillType = PathFillType.NonZero,
          ) {
            moveTo(12f, 20f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -4f)
            arcToRelative(2f, 2f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 4f)
          }
        }.build()
    return _moreVert!!
  }

private var _moreVert: ImageVector? = null
