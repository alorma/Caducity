package com.alorma.caducity.domain.model

data class Category(
  val id: String,
  val name: String,
  val description: String,
  val shape: CategoryShape = CategoryShape.COOKIE,
  val icon: CategoryIcon = CategoryIcon.DEFAULT,
  val sortOrder: Int = 0,
  /** Days before expiry when a unit becomes "soon"; null follows the global default. */
  val soonThresholdDays: Int? = null,
)
