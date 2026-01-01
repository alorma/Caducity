package com.alorma.caducity.base.ui.icons.outlined

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons

val AppIcons.Outlined.Dashboard: ImageVector
  get() {
    if (_dashboard != null) return _dashboard!!

    _dashboard = ImageVector.Builder(
      name = "dashboard_customize",
      defaultWidth = 24.dp,
      defaultHeight = 24.dp,
      viewportWidth = 960f,
      viewportHeight = 960f
    ).apply {
      path(
        fill = SolidColor(Color.Black)
      ) {
        moveTo(160f, 440f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(120f, 400f)
        verticalLineToRelative(-240f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(160f, 120f)
        horizontalLineToRelative(240f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(440f, 160f)
        verticalLineToRelative(240f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(400f, 440f)
        horizontalLineTo(160f)
        close()
        moveToRelative(40f, -240f)
        verticalLineToRelative(160f)
        verticalLineToRelative(-160f)
        close()
        moveToRelative(360f, 240f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(520f, 400f)
        verticalLineToRelative(-240f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(560f, 120f)
        horizontalLineToRelative(240f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(840f, 160f)
        verticalLineToRelative(240f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(800f, 440f)
        horizontalLineTo(560f)
        close()
        moveToRelative(40f, -240f)
        verticalLineToRelative(160f)
        verticalLineToRelative(-160f)
        close()
        moveTo(160f, 840f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(120f, 800f)
        verticalLineToRelative(-240f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(160f, 520f)
        horizontalLineToRelative(240f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(440f, 560f)
        verticalLineToRelative(240f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(400f, 840f)
        horizontalLineTo(160f)
        close()
        moveToRelative(40f, -240f)
        verticalLineToRelative(160f)
        verticalLineToRelative(-160f)
        close()
        moveToRelative(480f, 240f)
        quadToRelative(-17f, 0f, -28.5f, -11.5f)
        reflectiveQuadTo(640f, 800f)
        verticalLineToRelative(-80f)
        horizontalLineToRelative(-81f)
        quadToRelative(-17f, 0f, -28f, -11.5f)
        reflectiveQuadTo(520f, 680f)
        quadToRelative(0f, -17f, 11.5f, -28.5f)
        reflectiveQuadTo(560f, 640f)
        horizontalLineToRelative(80f)
        verticalLineToRelative(-81f)
        quadToRelative(0f, -17f, 11.5f, -28f)
        reflectiveQuadToRelative(28.5f, -11f)
        quadToRelative(17f, 0f, 28.5f, 11.5f)
        reflectiveQuadTo(720f, 560f)
        verticalLineToRelative(80f)
        horizontalLineToRelative(81f)
        quadToRelative(17f, 0f, 28f, 11.5f)
        reflectiveQuadToRelative(11f, 28.5f)
        quadToRelative(0f, 17f, -11.5f, 28.5f)
        reflectiveQuadTo(800f, 720f)
        horizontalLineToRelative(-80f)
        verticalLineToRelative(81f)
        quadToRelative(0f, 17f, -11.5f, 28f)
        reflectiveQuadTo(680f, 840f)
        close()
        moveToRelative(-80f, -640f)
        verticalLineToRelative(160f)
        horizontalLineToRelative(160f)
        verticalLineToRelative(-160f)
        horizontalLineTo(600f)
        close()
        moveToRelative(-400f, 0f)
        verticalLineToRelative(160f)
        horizontalLineToRelative(160f)
        verticalLineToRelative(-160f)
        horizontalLineTo(200f)
        close()
        moveToRelative(0f, 400f)
        verticalLineToRelative(160f)
        horizontalLineToRelative(160f)
        verticalLineToRelative(-160f)
        horizontalLineTo(200f)
        close()
      }
    }.build()

    return _dashboard!!
  }

private var _dashboard: ImageVector? = null
