package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

/**
 * Simplified category detail for the category overview screen.
 * Product tabs load their own item data via ProductPageViewModel.
 */
data class CategoryDetail(
  val category: Category,
  val products: List<Product>,
  val calendarData: List<ProductDatedItems>,
)

data class ProductDatedItems(
  val date: LocalDate,
  val status: ItemStatus,
  val items: List<ProductItem>,
)

data class ProductItem(
  val id: String,
  val name: String,
)