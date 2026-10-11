package com.alorma.caducity.base.ui.icons.symbols

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

private var _OutlinedChildCare: ImageVector? = null

val Symbols.Outlined.ChildCare: ImageVector
  get() =
    _OutlinedChildCare ?: ImageVector
      .Builder(
        name = "Outlined.ChildCare",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M580-490q-21 0-35.5-14.5T530-540q0-21 14.5-35.5T580-590q21 0 35.5 14.5T630-540q0 21-14.5 35.5T580-490Zm-200 0q-21 0-35.5-14.5T330-540q0-21 14.5-35.5T380-590q21 0 35.5 14.5T430-540q0 21-14.5 35.5T380-490Zm100 210q-60 0-108.5-33T300-400h360q-23 54-71.5 87T480-280Zm0 160q-75 0-140.5-28.5t-114-77q-48.5-48.5-77-114T120-480q0-75 28.5-140.5t77-114q48.5-48.5 114-77T480-840q75 0 140.5 28.5t114 77q48.5 48.5 77 114T840-480q0 75-28.5 140.5t-77 114q-48.5 48.5-114 77T480-120Zm0-80q116 0 198-82t82-198q0-116-82-198t-198-82h-12q-6 0-12 2-6 6-8 13t-2 15q0 21 14.5 35.5T496-680q9 0 16.5-3t15.5-3q12 0 20 9t8 21q0 23-21.5 29.5T496-620q-45 0-77.5-32.5T386-730v-6q0-3 1-8-83 30-135 101t-52 163q0 116 82 198t198 82Zm0-280Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _OutlinedChildCare = it }

private var _FilledChildCare: ImageVector? = null

val Symbols.Filled.ChildCare: ImageVector
  get() =
    _FilledChildCare ?: ImageVector
      .Builder(
        name = "Filled.ChildCare",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 960f,
        viewportHeight = 960f,
      ).apply {
        addGroup(translationY = 960f)
        addPath(
          pathData =
            addPathNodes(
              "M580-490q21 0 35.5-14.5T630-540q0-21-14.5-35.5T580-590q-21 0-35.5 14.5T530-540q0 21 14.5 35.5T580-490Zm-200 0q21 0 35.5-14.5T430-540q0-21-14.5-35.5T380-590q-21 0-35.5 14.5T330-540q0 21 14.5 35.5T380-490Zm100 210q60 0 108.5-33t71.5-87H300q23 54 71.5 87T480-280Zm0 160q-75 0-140.5-28.5t-114-77q-48.5-48.5-77-114T120-480q0-75 28.5-140.5t77-114q48.5-48.5 114-77T480-840q75 0 140.5 28.5t114 77q48.5 48.5 77 114T840-480q0 75-28.5 140.5t-77 114q-48.5 48.5-114 77T480-120Zm16-500q17 0 38.5-6.5T556-656q0-12-8-21t-20-9q-8 0-15.5 3t-16.5 3q-21 0-35.5-14.5T446-730q0-11 3.5-18.5T460-759q-19 2-37 5t-35 10q-1 3-1.5 7t-.5 7q0 45 32.5 77.5T496-620Z",
            ),
          fill = SolidColor(Color.Black),
        )
        clearGroup()
      }.build()
      .also { _FilledChildCare = it }
