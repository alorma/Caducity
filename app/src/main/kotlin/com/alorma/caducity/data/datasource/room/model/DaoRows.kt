package com.alorma.caducity.data.datasource.room.model

/** Number of active (not consumed, not discarded) items of a category. */
data class CategoryActiveCountRow(
  val categoryId: String,
  val activeCount: Int,
)

/** A product with how many times it was used and when it was last used, for name suggestions. */
data class ProductUsageRow(
  val productId: String,
  val name: String,
  val categoryId: String,
  val uses: Int,
  val lastUsed: Long,
)
