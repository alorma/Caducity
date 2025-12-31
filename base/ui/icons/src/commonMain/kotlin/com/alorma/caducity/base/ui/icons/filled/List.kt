package com.alorma.caducity.base.ui.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons

val AppIcons.Filled.List: ImageVector
  get() {
    if (_List != null) return _List!!

    _List = ImageVector.Builder(
      name = "list-alt",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 512f,
      viewportHeight = 512f
    ).apply {
      path(
        fill = SolidColor(Color.Black)
      ) {
        moveTo(464f, 480f)
        horizontalLineTo(48f)
        curveToRelative(-26.51f, 0f, -48f, -21.49f, -48f, -48f)
        verticalLineTo(80f)
        curveToRelative(0f, -26.51f, 21.49f, -48f, 48f, -48f)
        horizontalLineToRelative(416f)
        curveToRelative(26.51f, 0f, 48f, 21.49f, 48f, 48f)
        verticalLineToRelative(352f)
        curveToRelative(0f, 26.51f, -21.49f, 48f, -48f, 48f)
        close()
        moveTo(128f, 120f)
        curveToRelative(-22.091f, 0f, -40f, 17.909f, -40f, 40f)
        reflectiveCurveToRelative(17.909f, 40f, 40f, 40f)
        reflectiveCurveToRelative(40f, -17.909f, 40f, -40f)
        reflectiveCurveToRelative(-17.909f, -40f, -40f, -40f)
        close()
        moveToRelative(0f, 96f)
        curveToRelative(-22.091f, 0f, -40f, 17.909f, -40f, 40f)
        reflectiveCurveToRelative(17.909f, 40f, 40f, 40f)
        reflectiveCurveToRelative(40f, -17.909f, 40f, -40f)
        reflectiveCurveToRelative(-17.909f, -40f, -40f, -40f)
        close()
        moveToRelative(0f, 96f)
        curveToRelative(-22.091f, 0f, -40f, 17.909f, -40f, 40f)
        reflectiveCurveToRelative(17.909f, 40f, 40f, 40f)
        reflectiveCurveToRelative(40f, -17.909f, 40f, -40f)
        reflectiveCurveToRelative(-17.909f, -40f, -40f, -40f)
        close()
        moveToRelative(288f, -136f)
        verticalLineToRelative(-32f)
        curveToRelative(0f, -6.627f, -5.373f, -12f, -12f, -12f)
        horizontalLineTo(204f)
        curveToRelative(-6.627f, 0f, -12f, 5.373f, -12f, 12f)
        verticalLineToRelative(32f)
        curveToRelative(0f, 6.627f, 5.373f, 12f, 12f, 12f)
        horizontalLineToRelative(200f)
        curveToRelative(6.627f, 0f, 12f, -5.373f, 12f, -12f)
        close()
        moveToRelative(0f, 96f)
        verticalLineToRelative(-32f)
        curveToRelative(0f, -6.627f, -5.373f, -12f, -12f, -12f)
        horizontalLineTo(204f)
        curveToRelative(-6.627f, 0f, -12f, 5.373f, -12f, 12f)
        verticalLineToRelative(32f)
        curveToRelative(0f, 6.627f, 5.373f, 12f, 12f, 12f)
        horizontalLineToRelative(200f)
        curveToRelative(6.627f, 0f, 12f, -5.373f, 12f, -12f)
        close()
        moveToRelative(0f, 96f)
        verticalLineToRelative(-32f)
        curveToRelative(0f, -6.627f, -5.373f, -12f, -12f, -12f)
        horizontalLineTo(204f)
        curveToRelative(-6.627f, 0f, -12f, 5.373f, -12f, 12f)
        verticalLineToRelative(32f)
        curveToRelative(0f, 6.627f, 5.373f, 12f, 12f, 12f)
        horizontalLineToRelative(200f)
        curveToRelative(6.627f, 0f, 12f, -5.373f, 12f, -12f)
        close()
      }
    }.build()

    return _List!!
  }

private var _List: ImageVector? = null

