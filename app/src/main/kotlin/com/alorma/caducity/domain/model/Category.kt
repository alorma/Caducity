package com.alorma.caducity.domain.model

data class Category(
  val id: String,
  val name: String,
  val description: String,
  val shape: CategoryShape = CategoryShape.COOKIE,
  val icon: CategoryIcon = CategoryIcon.DEFAULT,
  val sortOrder: Int = 0,
)
