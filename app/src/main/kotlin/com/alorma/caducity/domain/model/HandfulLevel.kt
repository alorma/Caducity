package com.alorma.caducity.domain.model

/** How much is left of a handful product (Grapats): a lot, half or little. Persisted through `HandfulLevelConverters`. */
enum class HandfulLevel {
  LOTS,
  HALF,
  LITTLE,
  ;

  /** The level below this one; null when it is already the lowest (what is left then is nothing). */
  fun lower(): HandfulLevel? = entries.getOrNull(ordinal + 1)

  companion object {
    val DEFAULT = LOTS
  }
}
