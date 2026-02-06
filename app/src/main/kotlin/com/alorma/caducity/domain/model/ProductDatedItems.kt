package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

/**
 * Items grouped by date for product pages.
 * Used by GetProductItemsUseCase and ProductPageViewModel.
 */
data class ProductDatedItems(
  val date: LocalDate,
  val status: ItemStatus,
  val items: List<ProductItem>,
)

data class ProductItem(
  val id: String,
  val name: String,
)
