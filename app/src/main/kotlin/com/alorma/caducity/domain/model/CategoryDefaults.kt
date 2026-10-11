package com.alorma.caducity.domain.model

/**
 * Default shape/icon assigned to a category from its name. Shared by the v2→v3 migration,
 * the v1 backup import and category seeding.
 */
data class CategoryDefaults(
  val shape: CategoryShape,
  val icon: CategoryIcon,
) {
  companion object {
    private val SHAPE_CYCLE = CategoryShape.entries

    private class Rule(
      val prefixes: List<String>,
      val exactWords: List<String>,
      val shape: CategoryShape,
      val icon: CategoryIcon,
    )

    // Order matters: the first matching rule wins. Keywords are lowercase, without accents (en/es/ca).
    private val RULES =
      listOf(
        Rule(
          listOf("dairy", "lacteo", "lactic", "llet", "leche"),
          emptyList(),
          CategoryShape.COOKIE,
          CategoryIcon("water_full"),
        ),
        Rule(
          listOf("produce", "fruit", "fruta", "verdura"),
          emptyList(),
          CategoryShape.CLOVER8,
          CategoryIcon("nutrition"),
        ),
        Rule(
          listOf("meat", "fish", "carne", "pescado", "carn", "peix"),
          emptyList(),
          CategoryShape.CLOVER,
          CategoryIcon("set_meal"),
        ),
        Rule(
          listOf("dry goods", "secs", "secos", "conserv", "pantry", "despensa", "rebost"),
          emptyList(),
          CategoryShape.SUNNY,
          CategoryIcon("grocery"),
        ),
        Rule(listOf("bakery", "panader", "fleca"), listOf("pan"), CategoryShape.PUFFY, CategoryIcon("bakery_dining")),
        Rule(listOf("drink", "bebida", "begud"), emptyList(), CategoryShape.BURST, CategoryIcon("local_drink")),
        Rule(listOf("frozen", "congelad", "congelat"), emptyList(), CategoryShape.BURST, CategoryIcon("icecream")),
      )

    /** @param index position in the sort order, used to cycle shapes when no keyword matches. */
    fun forName(
      name: String,
      index: Int,
    ): CategoryDefaults {
      val words = name.normalizeForSearch().split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
      val rule =
        RULES.firstOrNull { rule ->
          words.any { word -> rule.prefixes.any(word::startsWith) || word in rule.exactWords }
        }
      return if (rule != null) {
        CategoryDefaults(rule.shape, rule.icon)
      } else {
        CategoryDefaults(SHAPE_CYCLE[Math.floorMod(index, SHAPE_CYCLE.size)], CategoryIcon.DEFAULT)
      }
    }
  }
}
