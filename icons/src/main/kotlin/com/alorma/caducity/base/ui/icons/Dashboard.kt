package com.alorma.caducity.base.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val AppIcons.Filled.Dashboard: ImageVector
  get() {
    if (_dashboard != null) {
      return _dashboard!!
    }
    _dashboard = ImageVector.Builder(
      name = "Filled.Dashboard",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 24f,
      viewportHeight = 24f
    ).apply {
      path(fill = SolidColor(Color(0xFF000000))) {
        moveTo(3f, 13f)
        horizontalLineToRelative(8f)
        verticalLineTo(3f)
        horizontalLineTo(3f)
        verticalLineTo(13f)
        close()
        moveTo(3f, 21f)
        horizontalLineToRelative(8f)
        verticalLineToRelative(-6f)
        horizontalLineTo(3f)
        verticalLineTo(21f)
        close()
        moveTo(13f, 21f)
        horizontalLineToRelative(8f)
        verticalLineTo(11f)
        horizontalLineToRelative(-8f)
        verticalLineTo(21f)
        close()
        moveTo(13f, 3f)
        verticalLineToRelative(6f)
        horizontalLineToRelative(8f)
        verticalLineTo(3f)
        horizontalLineTo(13f)
        close()
      }
    }.build()

    return _dashboard!!
  }

private var _dashboard: ImageVector? = null
