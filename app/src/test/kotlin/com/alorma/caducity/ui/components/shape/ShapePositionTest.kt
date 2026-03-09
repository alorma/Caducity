package com.alorma.caducity.ui.components.shape

import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ShapePositionTest {
  @Test
  fun `calculateShapeWithGaps returns None for out of bounds index`() {
    val list = listOf("A", "B", "C")

    expectThat(list.calculateShapeWithGaps(-1)).isEqualTo(ShapePosition.None)
    expectThat(list.calculateShapeWithGaps(3)).isEqualTo(ShapePosition.None)
  }

  @Test
  fun `calculateShapeWithGaps returns None when current item has no content`() {
    val list = listOf("A", "B", "C")
    val hasContent: (String) -> Boolean = { it != "B" }

    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.None)
  }

  @Test
  fun `calculateShapeWithGaps returns Single for single item list`() {
    val list = listOf("A")

    expectThat(list.calculateShapeWithGaps(0)).isEqualTo(ShapePosition.Single)
  }

  @Test
  fun `calculateShapeWithGaps returns Single when item is isolated with content`() {
    val list = listOf("", "A", "")
    val hasContent: (String) -> Boolean = { it.isNotEmpty() }

    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.Single)
  }

  @Test
  fun `calculateShapeWithGaps returns Start for first item in continuous list`() {
    val list = listOf("A", "B", "C")

    expectThat(list.calculateShapeWithGaps(0)).isEqualTo(ShapePosition.Start)
  }

  @Test
  fun `calculateShapeWithGaps returns Start when next has content but previous does not`() {
    val list = listOf("", "A", "B")
    val hasContent: (String) -> Boolean = { it.isNotEmpty() }

    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.Start)
  }

  @Test
  fun `calculateShapeWithGaps returns Middle for item with content on both sides`() {
    val list = listOf("A", "B", "C")

    expectThat(list.calculateShapeWithGaps(1)).isEqualTo(ShapePosition.Middle)
  }

  @Test
  fun `calculateShapeWithGaps returns Middle when surrounded by items with content`() {
    val list = listOf("", "A", "B", "C", "")
    val hasContent: (String) -> Boolean = { it.isNotEmpty() }

    expectThat(list.calculateShapeWithGaps(2, hasContent)).isEqualTo(ShapePosition.Middle)
  }

  @Test
  fun `calculateShapeWithGaps returns End for last item in continuous list`() {
    val list = listOf("A", "B", "C")

    expectThat(list.calculateShapeWithGaps(2)).isEqualTo(ShapePosition.End)
  }

  @Test
  fun `calculateShapeWithGaps returns End when previous has content but next does not`() {
    val list = listOf("A", "B", "")
    val hasContent: (String) -> Boolean = { it.isNotEmpty() }

    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.End)
  }

  @Test
  fun `calculateShapeWithGaps handles complex patterns with gaps`() {
    val list = listOf("A", "", "B", "C", "", "D")
    val hasContent: (String) -> Boolean = { it.isNotEmpty() }

    // "A" is isolated (gap after)
    expectThat(list.calculateShapeWithGaps(0, hasContent)).isEqualTo(ShapePosition.Single)

    // "" has no content
    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.None)

    // "B" is start of group (gap before)
    expectThat(list.calculateShapeWithGaps(2, hasContent)).isEqualTo(ShapePosition.Start)

    // "C" is end of group (gap after)
    expectThat(list.calculateShapeWithGaps(3, hasContent)).isEqualTo(ShapePosition.End)

    // "" has no content
    expectThat(list.calculateShapeWithGaps(4, hasContent)).isEqualTo(ShapePosition.None)

    // "D" is isolated (gap before)
    expectThat(list.calculateShapeWithGaps(5, hasContent)).isEqualTo(ShapePosition.Single)
  }

  @Test
  fun `calculateShapeWithGaps with custom data class`() {
    data class Item(
      val id: Int,
      val visible: Boolean,
    )

    val list =
      listOf(
        Item(1, true),
        Item(2, false),
        Item(3, true),
        Item(4, true),
        Item(5, false),
      )
    val hasContent: (Item) -> Boolean = { it.visible }

    // Item 1 is isolated
    expectThat(list.calculateShapeWithGaps(0, hasContent)).isEqualTo(ShapePosition.Single)

    // Item 2 is not visible
    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.None)

    // Item 3 is start of group
    expectThat(list.calculateShapeWithGaps(2, hasContent)).isEqualTo(ShapePosition.Start)

    // Item 4 is end of group
    expectThat(list.calculateShapeWithGaps(3, hasContent)).isEqualTo(ShapePosition.End)

    // Item 5 is not visible
    expectThat(list.calculateShapeWithGaps(4, hasContent)).isEqualTo(ShapePosition.None)
  }

  @Test
  fun `calculateShapeWithGaps with all items having content`() {
    val list = listOf(1, 2, 3, 4, 5)

    expectThat(list.calculateShapeWithGaps(0)).isEqualTo(ShapePosition.Start)
    expectThat(list.calculateShapeWithGaps(1)).isEqualTo(ShapePosition.Middle)
    expectThat(list.calculateShapeWithGaps(2)).isEqualTo(ShapePosition.Middle)
    expectThat(list.calculateShapeWithGaps(3)).isEqualTo(ShapePosition.Middle)
    expectThat(list.calculateShapeWithGaps(4)).isEqualTo(ShapePosition.End)
  }

  @Test
  fun `calculateShapeWithGaps with no items having content`() {
    val list = listOf("", "", "")
    val hasContent: (String) -> Boolean = { it.isNotEmpty() }

    expectThat(list.calculateShapeWithGaps(0, hasContent)).isEqualTo(ShapePosition.None)
    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.None)
    expectThat(list.calculateShapeWithGaps(2, hasContent)).isEqualTo(ShapePosition.None)
  }

  @Test
  fun `calculateShapeWithGaps with two adjacent items having content`() {
    val list = listOf("", "A", "B", "")
    val hasContent: (String) -> Boolean = { it.isNotEmpty() }

    expectThat(list.calculateShapeWithGaps(1, hasContent)).isEqualTo(ShapePosition.Start)
    expectThat(list.calculateShapeWithGaps(2, hasContent)).isEqualTo(ShapePosition.End)
  }

  @Test
  fun `calculateShapeWithGaps uses default hasContent predicate`() {
    val list = listOf("A", "B", "C")

    // Default hasContent always returns true
    expectThat(list.calculateShapeWithGaps(0)).isEqualTo(ShapePosition.Start)
    expectThat(list.calculateShapeWithGaps(1)).isEqualTo(ShapePosition.Middle)
    expectThat(list.calculateShapeWithGaps(2)).isEqualTo(ShapePosition.End)
  }
}
