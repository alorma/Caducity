package com.alorma.caducity.domain.model

sealed interface ProductDeletionStrategy {
  data object CascadeDelete : ProductDeletionStrategy

  data object MoveToStandalone : ProductDeletionStrategy

  data class MoveToProduct(
    val targetProductId: String,
  ) : ProductDeletionStrategy
}
