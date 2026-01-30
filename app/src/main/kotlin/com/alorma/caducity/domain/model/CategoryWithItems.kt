package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class CategoryWithItems(
  val category: Category,
  val products: ImmutableList<CategoryProduct>,
  val standaloneItems: ImmutableList<Item>,
) {
  val allItems: ImmutableList<Item> = buildList {
    addAll(products.flatMap { it.items })
    addAll(standaloneItems)
  }.toImmutableList()
}

data class CategoryProduct(
  val product: Product,
  val items: ImmutableList<Item>,
)
