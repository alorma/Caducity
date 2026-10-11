package com.alorma.caducity.data.datasource.room

import java.text.Normalizer

/** Default shape/icon assigned to a category from its name (shared by the v2→v3 migration and the v1 backup import). */
data class CategoryDefaults(
  val shape: String,
  val icon: String,
) {
  companion object {
    private val SHAPE_CYCLE = listOf("COOKIE", "FLOWER", "CLOVER", "SUNNY", "PUFFY", "BURST")
    private const val DEFAULT_ICON = "eco"

    private class Rule(
      val prefixes: List<String>,
      val exactWords: List<String>,
      val shape: String,
      val icon: String,
    )

    // Order matters: the first matching rule wins. Keywords are lowercase, without accents (en/es/ca).
    private val RULES =
      listOf(
        Rule(listOf("dairy", "lacteo", "lactic", "llet", "leche"), emptyList(), "COOKIE", "water_full"),
        Rule(listOf("produce", "fruit", "fruta", "verdura"), emptyList(), "FLOWER", "nutrition"),
        Rule(listOf("meat", "fish", "carne", "pescado", "carn", "peix"), emptyList(), "CLOVER", "set_meal"),
        Rule(listOf("pantry", "despensa", "rebost"), emptyList(), "SUNNY", "grocery"),
        Rule(listOf("bakery", "panader", "fleca"), listOf("pan"), "PUFFY", "bakery_dining"),
        Rule(listOf("drink", "bebida", "begud"), emptyList(), "BURST", "local_drink"),
        Rule(listOf("frozen", "congelad"), emptyList(), "BURST", "icecream"),
      )

    /** @param index position in the sort order, used to cycle shapes when no keyword matches. */
    fun forName(
      name: String,
      index: Int,
    ): CategoryDefaults {
      val words = normalize(name).split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
      val rule =
        RULES.firstOrNull { rule ->
          words.any { word -> rule.prefixes.any(word::startsWith) || word in rule.exactWords }
        }
      return if (rule != null) {
        CategoryDefaults(rule.shape, rule.icon)
      } else {
        CategoryDefaults(SHAPE_CYCLE[Math.floorMod(index, SHAPE_CYCLE.size)], DEFAULT_ICON)
      }
    }

    private fun normalize(text: String): String =
      Normalizer
        .normalize(text.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
  }
}
