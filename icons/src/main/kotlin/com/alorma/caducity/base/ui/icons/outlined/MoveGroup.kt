package com.alorma.caducity.base.ui.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons

val AppIcons.Outlined.MoveGroup: ImageVector
  get() {
    if (_MoveGroup != null) return _MoveGroup!!

    _MoveGroup =
      ImageVector
        .Builder(
          name = "move_group",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 960f,
          viewportHeight = 960f,
        ).apply {
          path(
            fill = SolidColor(Color.Black),
          ) {
            moveTo(320f, 720f)
            quadToRelative(-33f, 0f, -56.5f, -23.5f)
            reflectiveQuadTo(240f, 640f)
            verticalLineToRelative(-80f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(80f)
            horizontalLineToRelative(480f)
            verticalLineToRelative(-400f)
            horizontalLineTo(320f)
            verticalLineToRelative(80f)
            horizontalLineToRelative(-80f)
            verticalLineToRelative(-160f)
            quadToRelative(0f, -33f, 23.5f, -56.5f)
            reflectiveQuadTo(320f, 80f)
            horizontalLineToRelative(480f)
            quadToRelative(33f, 0f, 56.5f, 23.5f)
            reflectiveQuadTo(880f, 160f)
            verticalLineToRelative(480f)
            quadToRelative(0f, 33f, -23.5f, 56.5f)
            reflectiveQuadTo(800f, 720f)
            horizontalLineTo(320f)
            close()
            moveTo(160f, 880f)
            quadToRelative(-33f, 0f, -56.5f, -23.5f)
            reflectiveQuadTo(80f, 800f)
            verticalLineToRelative(-560f)
            horizontalLineToRelative(80f)
            verticalLineToRelative(560f)
            horizontalLineToRelative(560f)
            verticalLineToRelative(80f)
            horizontalLineTo(160f)
            close()
            moveToRelative(360f, -280f)
            lineToRelative(-56f, -56f)
            lineToRelative(63f, -64f)
            horizontalLineTo(240f)
            verticalLineToRelative(-80f)
            horizontalLineToRelative(287f)
            lineToRelative(-63f, -64f)
            lineToRelative(56f, -56f)
            lineToRelative(160f, 160f)
            lineToRelative(-160f, 160f)
            close()
          }
        }.build()

    return _MoveGroup!!
  }

private var _MoveGroup: ImageVector? = null
