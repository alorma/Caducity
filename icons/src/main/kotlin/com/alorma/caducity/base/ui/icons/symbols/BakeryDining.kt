package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedBakeryDining: ImageVector? = null

val Symbols.Outlined.BakeryDining: ImageVector
  get() =
    _OutlinedBakeryDining ?: ImageVector
      .Builder(
        name = "Outlined.BakeryDining",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M804-282q17 9 30-4t4-30l-58-108-42 108 66 34Zm-200-38h48l96-238q3-8-1.5-13.5T736-580l-80-32q-9-3-17.5 2T628-596l-24 276Zm-296 0h48l-24-276q-2-11-10.5-15t-17.5-1l-80 32q-8 3-11.5 8.5T212-558l96 238Zm-152 38 66-34-42-108-58 108q-9 17 4 30t30 4Zm280-38h88l30-338q2-9-4.5-15.5T534-680H426q-8 0-14.5 6.5T406-658l30 338ZM138-200q-42 0-70-31.5T40-306q0-12 3.5-23.5T52-352l88-168q-14-40 1-79t53-55l80-32q14-5 28-7t28 1q14-29 39-48.5t57-19.5h108q32 0 57 19.5t39 48.5q14-2 28-.5t28 6.5l80 32q40 16 56 55t-2 77l88 168q6 11 9 23t3 25q0 45-30.5 75.5T814-200q-11 0-22-2.5t-22-7.5l-62-30H250l-56 30q-13 7-27.5 8.5T138-200Zm342-280Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedBakeryDining = it }

private var _FilledBakeryDining: ImageVector? = null

val Symbols.Filled.BakeryDining: ImageVector
  get() =
    _FilledBakeryDining ?: ImageVector
      .Builder(
        name = "Filled.BakeryDining",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "m835-252-99-34 78-215 85 175q14 27-12 55t-52 19Zm-251-38 43-374q2-15 13.5-20.5t27.5-.5l100 37q14 6 18.5 18t-.5 26L673-290h-89Zm-296 0L175-604q-5-14-.5-26.5T193-648l100-37q14-6 26.5-.5T334-664l42 374h-88Zm-172 38q-26 8-47-19.5T62-326l86-175 77 215-109 34Zm320-38-48-430q-2-17 9.5-28.5T426-760h108q17 0 28.5 11.5T572-720l-48 430h-88Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledBakeryDining = it }
