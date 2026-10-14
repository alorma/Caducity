package com.alorma.caducity.ui.components.category

import androidx.compose.material3.MaterialShapes
import com.alorma.caducity.domain.model.CategoryShape
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isSameInstanceAs

class CategoryShapeTest {
  @Test
  fun `each category shape maps to its MaterialShapes polygon`() {
    expectThat(CategoryShape.COOKIE.polygon).isSameInstanceAs(MaterialShapes.Cookie9Sided)
    expectThat(CategoryShape.CLOVER8.polygon).isSameInstanceAs(MaterialShapes.Clover8Leaf)
    expectThat(CategoryShape.CLOVER.polygon).isSameInstanceAs(MaterialShapes.Clover4Leaf)
    expectThat(CategoryShape.SUNNY.polygon).isSameInstanceAs(MaterialShapes.Sunny)
    expectThat(CategoryShape.PUFFY.polygon).isSameInstanceAs(MaterialShapes.Puffy)
    expectThat(CategoryShape.BURST.polygon).isSameInstanceAs(MaterialShapes.SoftBurst)
    expectThat(CategoryShape.COOKIE7.polygon).isSameInstanceAs(MaterialShapes.Cookie7Sided)
    expectThat(CategoryShape.COOKIE4.polygon).isSameInstanceAs(MaterialShapes.Cookie4Sided)
  }

  @Test
  fun `all shapes are distinct`() {
    expectThat(
      CategoryShape.entries
        .map { it.polygon }
        .toSet()
        .size,
    ).isEqualTo(CategoryShape.entries.size)
  }

  @Test
  fun `the picker offers the six shapes of the spec and not the legacy ones`() {
    expectThat(CategoryShape.selectableShapes).containsExactly(
      CategoryShape.COOKIE,
      CategoryShape.COOKIE7,
      CategoryShape.CLOVER,
      CategoryShape.SUNNY,
      CategoryShape.COOKIE4,
      CategoryShape.PUFFY,
    )
  }
}
