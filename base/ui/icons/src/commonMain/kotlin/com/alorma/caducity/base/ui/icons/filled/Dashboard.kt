package com.alorma.caducity.base.ui.icons.filled

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.alorma.caducity.base.ui.icons.AppIcons

val AppIcons.Filled.Dashboard: ImageVector
  get() {
    if (_Dashboard != null) return _Dashboard!!

    _Dashboard = ImageVector.Builder(
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
        moveToRelative(400f, 0f)
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
        moveToRelative(520f, 0f)
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
      }
    }.build()

    return _Dashboard!!
  }

private var _Dashboard: ImageVector? = null

