package com.alorma.caducity.domain.model

data class ProductItems(
  val datedItemsGroups: List<ProductDatedItems>,
  val frozenItems: List<ProductItem>,
  val consumedItems: List<ProductItem>,
)
