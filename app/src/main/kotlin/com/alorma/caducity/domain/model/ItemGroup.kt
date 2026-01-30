package com.alorma.caducity.domain.model

import kotlinx.collections.immutable.ImmutableList

data class ItemGroup(
  val identifier: String,
  val categoryId: String? = null,
  val productName: String? = null,
  val isStandalone: Boolean = false,
  val items: ImmutableList<Item>,
)
