package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedEco: ImageVector? = null

val Symbols.Outlined.Eco: ImageVector
  get() =
    _OutlinedEco ?: ImageVector
      .Builder(
        name = "Outlined.Eco",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M216-176q-45-45-70.5-104T120-402q0-63 24-124.5T222-642q60-60 169.5-91T675-759q26 1 48 11t39 27q17 17 27 39.5t11 48.5q2 82-4.5 151.5t-21 125.5q-14.5 56-37 99.5T684-182q-53 53-112.5 77.5T450-80q-65 0-127-25.5T216-176Zm112-16q29 17 59.5 24.5T450-160q46 0 91-18.5t86-59.5q18-18 36.5-50.5t32-85Q709-426 716-500.5t2-177.5q-49-2-110.5-1.5T485-670q-61 9-116 29t-90 55q-45 45-62 89t-17 85q0 59 22.5 103.5T262-246q42-80 111-153.5T534-520q-72 63-125.5 142.5T328-192Zm0 0Zm0 0Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedEco = it }

private var _FilledEco: ImageVector? = null

val Symbols.Filled.Eco: ImageVector
  get() =
    _FilledEco ?: ImageVector
      .Builder(
        name = "Filled.Eco",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M450-80q-33 0-66.5-7.5T315-109q12-121 70-226t149-185q-110 56-190.5 148T231-162q-4-3-7.5-6.5L216-176q-47-47-71.5-105T120-402q0-68 27-130t75-110q70-70 173.5-98T675-760q26 1 48 11.5t39 27.5q17 17 27 39.5t12 48.5q6 180-21 281t-96 170q-49 49-109.5 75.5T450-80Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledEco = it }
