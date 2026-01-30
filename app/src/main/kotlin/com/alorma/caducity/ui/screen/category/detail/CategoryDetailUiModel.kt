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
)

@Stable
data class CategoryDetailUiModel(
  val id: String,
  val name: String,
  val description: String,
)

@Stable
sealed class CategoryDetailProductTabUiModel {
  abstract val id: String
  abstract val name: String

  @Stable
  data class Empty(
    override val id: String,
    override val name: String,
  ) : CategoryDetailProductTabUiModel()

  @Stable
  data class WithItems(
    override val id: String,
    override val name: String,
    val datedItemsGroups: ImmutableList<DateItemsUiModel>,
    val frozenItems: ImmutableList<ItemDetailUiModel>,
    val consumedItems: ImmutableList<ItemDetailUiModel>,
  ) : CategoryDetailProductTabUiModel()
}
