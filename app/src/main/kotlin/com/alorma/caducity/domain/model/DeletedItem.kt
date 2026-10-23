package com.alorma.caducity.domain.model

/** Every field of an item that was deleted, so the delete can be undone by putting the row back as it was. */
data class DeletedItem(
  val id: String,
  val categoryId: String,
  val identifier: String,
  val productId: String?,
  val expirationMillis: Long,
  val pausedMillis: Long?,
  val remainingDays: Int?,
  val consumedMillis: Long?,
  val discardedMillis: Long?,
  val createdMillis: Long?,
  val transformedMillis: Long?,
  val transformationId: String?,
  val originTransformationId: String?,
  val amount: Int?,
  val level: HandfulLevel?,
)
