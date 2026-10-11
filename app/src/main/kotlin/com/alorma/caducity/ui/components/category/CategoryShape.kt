package com.alorma.caducity.ui.components.category

import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.graphics.shapes.RoundedPolygon
import com.alorma.caducity.domain.model.CategoryShape

val CategoryShape.polygon: RoundedPolygon
  get() =
    when (this) {
      CategoryShape.COOKIE -> MaterialShapes.Cookie9Sided
      CategoryShape.FLOWER -> MaterialShapes.Flower
      CategoryShape.CLOVER -> MaterialShapes.Clover4Leaf
      CategoryShape.SUNNY -> MaterialShapes.Sunny
      CategoryShape.PUFFY -> MaterialShapes.Puffy
      CategoryShape.BURST -> MaterialShapes.SoftBurst
    }

@Composable
fun CategoryShape.toComposeShape(): Shape = polygon.toShape()
