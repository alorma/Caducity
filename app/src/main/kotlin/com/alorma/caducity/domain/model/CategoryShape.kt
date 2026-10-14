package com.alorma.caducity.domain.model

/**
 * Organic shape of a category avatar. Persisted by name; unknown stored values fall back to [COOKIE].
 * [CLOVER8] and [BURST] are kept only to draw categories that already have them (database, backups): they are
 * not offered when picking a shape, see [selectableShapes].
 */
enum class CategoryShape {
  COOKIE,
  CLOVER8,
  CLOVER,
  SUNNY,
  PUFFY,
  BURST,
  COOKIE7,
  COOKIE4,
  ;

  companion object {
    /** The six shapes a user can pick, in picker order. */
    val selectableShapes: List<CategoryShape> = listOf(COOKIE, COOKIE7, CLOVER, SUNNY, COOKIE4, PUFFY)
  }
}
