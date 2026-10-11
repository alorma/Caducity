package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedTouchApp: ImageVector? = null

val Symbols.Outlined.TouchApp: ImageVector
  get() =
    _OutlinedTouchApp ?: ImageVector
      .Builder(
        name = "Outlined.TouchApp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M419-80q-28 0-52.5-12T325-126L124-381q-8-9-7-21.5t9-20.5q20-21 48-25t52 11l74 45v-328q0-17 11.5-28.5T340-760q17 0 29 11.5t12 28.5v400q0 23-20.5 34.5T320-286l-36-22 104 133q6 7 14 11t17 4h221q33 0 56.5-23.5T720-240v-160q0-17-11.5-28.5T680-440H501q-17 0-28.5-11.5T461-480q0-17 11.5-28.5T501-520h179q50 0 85 35t35 85v160q0 66-47 113T640-80H419Zm83-260Zm-23-260q-17 0-28.5-11.5T439-640q0-2 5-20 8-14 12-28.5t4-31.5q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 17 4 31.5t12 28.5q3 5 4 10t1 10q0 17-11 28.5T202-600q-11 0-20.5-6T167-621q-13-22-20-47t-7-52q0-83 58.5-141.5T340-920q83 0 141.5 58.5T540-720q0 27-7 52t-20 47q-5 9-14 15t-20 6Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedTouchApp = it }

private var _FilledTouchApp: ImageVector? = null

val Symbols.Filled.TouchApp: ImageVector
  get() =
    _FilledTouchApp ?: ImageVector
      .Builder(
        name = "Filled.TouchApp",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M419-80q-28 0-52.5-12T325-126L124-381q-8-9-7-21.5t9-20.5q20-21 48-25t52 11l74 45v-328q0-17 11.5-28.5T340-760q17 0 29 11.5t12 28.5v200h299q50 0 85 35t35 85v160q0 66-47 113T640-80H419Zm60-520q-17 0-28.5-11.5T439-640q0-2 5-20 8-14 12-28.5t4-31.5q0-50-35-85t-85-35q-50 0-85 35t-35 85q0 17 4 31.5t12 28.5q3 5 4 10t1 10q0 17-11 28.5T202-600q-11 0-20.5-6T167-621q-13-22-20-47t-7-52q0-83 58.5-141.5T340-920q83 0 141.5 58.5T540-720q0 27-7 52t-20 47q-5 9-14 15t-20 6Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledTouchApp = it }
