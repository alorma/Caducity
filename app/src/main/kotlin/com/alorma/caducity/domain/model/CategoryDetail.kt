package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

data class CategoryDetail(
  val category: Category,
  val products: List<DetailProduct>,
  val standaloneItems: List<ProductItem>,
  val standaloneFrozenItems: List<ProductItem>,
)

data class DetailProduct(
  val id: String,
  val name: String,
  val datedItemsGroups: List<ProductDatedItems>,
  val frozenItems: List<ProductItem>,
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