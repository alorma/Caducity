package com.alorma.caducity.domain.model

import kotlin.math.abs

/** The 8 stops offered for a category's usual duration; the value is always stored in days. */
object ThresholdStops {
  val days: List<Int> = listOf(1, 2, 3, 7, 14, 30, 60, 90)

  val lastIndex: Int get() = days.lastIndex

  fun daysAt(index: Int): Int = days[index.coerceIn(0, lastIndex)]

  /** Index of the stop closest to [value]; ties go to the shorter one. */
  fun nearestIndex(value: Int): Int = days.indices.minBy { abs(days[it] - value) }
}

/** Usual durations suggested for the onboarding categories and for «Torna als valors suggerits». */
object SuggestedDurations {
  fun of(category: SuggestedCategory): Int =
    when (category) {
      SuggestedCategory.MEAT_FISH -> 3
      SuggestedCategory.BAKERY -> 3
      SuggestedCategory.READY_MEALS -> 3
      SuggestedCategory.DAIRY -> 7
      SuggestedCategory.PRODUCE -> 7
      SuggestedCategory.COLD_CUTS -> 7
      SuggestedCategory.DRINKS -> 30
      SuggestedCategory.PANTRY -> 90
    }

  private val KEYWORDS: List<Pair<List<String>, SuggestedCategory>> =
    listOf(
      listOf("dairy", "lacteo", "lactic", "llet", "leche") to SuggestedCategory.DAIRY,
      listOf("produce", "fruit", "fruta", "verdura") to SuggestedCategory.PRODUCE,
      listOf("meat", "fish", "carne", "pescado", "carn", "peix") to SuggestedCategory.MEAT_FISH,
      listOf("dry goods", "secs", "secos", "conserv", "pantry", "despensa", "rebost") to SuggestedCategory.PANTRY,
      listOf("bakery", "panader", "fleca") to SuggestedCategory.BAKERY,
      listOf("drink", "bebida", "begud") to SuggestedCategory.DRINKS,
      listOf("cold cut", "fiambre", "embutido", "embotit") to SuggestedCategory.COLD_CUTS,
      listOf("ready", "prepared", "preparado", "preparat", "cocinado") to SuggestedCategory.READY_MEALS,
    )

  private fun suggestedFor(name: String): SuggestedCategory? {
    val normalized = name.normalizeForSearch()
    return KEYWORDS.firstOrNull { (words, _) -> words.any { normalized.contains(it) } }?.second
  }

  /** Suggested duration for a category recognised by its name; null for user-made categories. */
  fun forName(name: String): Int? = suggestedFor(name)?.let(::of)

  /** Shapes the onboarding gave to the suggested categories before they changed; stored categories may still have them. */
  private val LEGACY_SHAPES: Map<SuggestedCategory, CategoryShape> =
    mapOf(
      SuggestedCategory.PRODUCE to CategoryShape.CLOVER8,
      SuggestedCategory.BAKERY to CategoryShape.PUFFY,
      SuggestedCategory.DRINKS to CategoryShape.BURST,
      SuggestedCategory.READY_MEALS to CategoryShape.CLOVER8,
    )

  /**
   * Duration for a category that still looks like a default one: its name matches a suggested category
   * and it keeps that category's shape (current or legacy) and icon. Null for user-made or edited categories
   * (v4 → v5 migration).
   */
  fun forDefaultCategory(
    name: String,
    shape: CategoryShape,
    icon: CategoryIcon,
  ): Int? =
    suggestedFor(name)
      ?.takeIf { (it.shape == shape || LEGACY_SHAPES[it] == shape) && it.icon == icon }
      ?.let(::of)
}
