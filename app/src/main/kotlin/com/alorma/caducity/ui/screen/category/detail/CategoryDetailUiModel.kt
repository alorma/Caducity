package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

@Stable
data class DateItemsUiModel(
  val text: String,
  val status: ItemStatus,
  val date: LocalDate,
  val items: ImmutableList<ItemDetailUiModel>,
)

@Stable
data class ItemDetailUiModel(
  val id: String,
  val expirationDate: LocalDate,
  val status: ItemStatus,
  val text: String,
  val packSize: Int? = null,
)

@Stable
data class CategoryDetailUiModel(
  val name: String,
  val description: String,
)

@Stable
data class CategoryProductTabUiModel(
  val id: String?,
  val categoryId: String,
  val name: String,
) {
  fun asKey() = "$categoryId-$id"
}
