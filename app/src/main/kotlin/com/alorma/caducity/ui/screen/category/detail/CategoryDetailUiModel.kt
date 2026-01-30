package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.InstanceStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

@Stable
data class DateItemsUiModel(
  val text: String,
  val status: InstanceStatus,
  val date: LocalDate,
  val items: ImmutableList<ItemDetailUiModel>,
)

@Stable
data class ItemDetailUiModel(
  val id: String,
  val expirationDate: LocalDate,
  val status: InstanceStatus,
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
  ) : CategoryDetailProductTabUiModel()
}
