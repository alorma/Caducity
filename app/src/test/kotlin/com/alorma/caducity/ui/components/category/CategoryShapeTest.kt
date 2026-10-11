package com.alorma.caducity.ui.components.category

import androidx.compose.material3.MaterialShapes
import com.alorma.caducity.domain.model.CategoryShape
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

class CategoryShapeTest {
  @Test
  fun `each category shape maps to its MaterialShapes polygon`() {
    expectThat(CategoryShape.COOKIE.polygon).isSameInstanceAs(MaterialShapes.Cookie9Sided)
    expectThat(CategoryShape.FLOWER.polygon).isSameInstanceAs(MaterialShapes.Flower)
    expectThat(CategoryShape.CLOVER.polygon).isSameInstanceAs(MaterialShapes.Clover4Leaf)
    expectThat(CategoryShape.SUNNY.polygon).isSameInstanceAs(MaterialShapes.Sunny)
    expectThat(CategoryShape.PUFFY.polygon).isSameInstanceAs(MaterialShapes.Puffy)
    expectThat(CategoryShape.BURST.polygon).isSameInstanceAs(MaterialShapes.SoftBurst)
  }

  @Test
  fun `all six shapes are distinct`() {
    expectThat(
      CategoryShape.entries
        .map { it.polygon }
        .toSet()
        .size,
    ).isEqualTo(6)
  }
}
