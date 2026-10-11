package com.alorma.caducity.domain.model

import kotlin.math.abs

/** The 8 stops offered for a category's "soon" margin; the value is always stored in days. */
object ThresholdStops {
  val days: List<Int> = listOf(1, 2, 3, 7, 14, 30, 60, 90)

  val lastIndex: Int get() = days.lastIndex

  fun daysAt(index: Int): Int = days[index.coerceIn(0, lastIndex)]

  /** Index of the stop closest to [value]; ties go to the shorter one. */
  fun nearestIndex(value: Int): Int = days.indices.minBy { abs(days[it] - value) }
}

/** Margins suggested for the onboarding categories and for «Torna als valors suggerits». */
object SuggestedThresholds {
  fun of(category: SuggestedCategory): Int =
    when (category) {
      SuggestedCategory.MEAT_FISH -> 2
      SuggestedCategory.BAKERY -> 2
      SuggestedCategory.DAIRY -> 3
      SuggestedCategory.PRODUCE -> 3
      SuggestedCategory.COLD_CUTS -> 3
      SuggestedCategory.READY_MEALS -> 2
      SuggestedCategory.DRINKS -> 7
      SuggestedCategory.PANTRY -> 30
    }

  private val KEYWORDS: List<Pair<List<String>, SuggestedCategory>> =
    listOf(
      listOf("dairy", "lacteo", "lactic", "llet", "leche") to SuggestedCategory.DAIRY,
      listOf("produce", "fruit", "fruta", "verdura") to SuggestedCategory.PRODUCE,
      listOf("meat", "fish", "carne", "pescado", "carn", "peix") to SuggestedCategory.MEAT_FISH,
      listOf("pantry", "despensa", "rebost") to SuggestedCategory.PANTRY,
      listOf("bakery", "panader", "fleca") to SuggestedCategory.BAKERY,
      listOf("drink", "bebida", "begud") to SuggestedCategory.DRINKS,
      listOf("cold cut", "fiambre", "embutido", "embotit") to SuggestedCategory.COLD_CUTS,
      listOf("ready", "prepared", "preparado", "preparat", "cocinado") to SuggestedCategory.READY_MEALS,
    )

  /** Suggested margin for a category recognised by its name; null for user-made categories. */
  fun forName(name: String): Int? {
    val normalized = name.normalizeForSearch()
    return KEYWORDS.firstOrNull { (words, _) -> words.any { normalized.contains(it) } }?.second?.let(::of)
  }
}
