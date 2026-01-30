package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class CategoryListItem(
  val category: Category,
  val items: ImmutableList<Item>,
)
