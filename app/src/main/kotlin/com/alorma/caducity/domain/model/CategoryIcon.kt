package com.alorma.caducity.domain.model

/**
 * Icon of a category avatar, identified by the id of a Material Symbol. Kept as an open id (not an enum)
 * so icons can be added or removed without touching persisted data; the UI falls back to [DEFAULT]
 * when it does not know an id.
 */
data class CategoryIcon(
  val id: String,
) {
  companion object {
    val DEFAULT = CategoryIcon("eco")
  }
}
