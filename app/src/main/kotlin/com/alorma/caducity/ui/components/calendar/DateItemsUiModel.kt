package com.alorma.caducity.ui.components.calendar

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.ui.components.bottomsheet.ItemDetailUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

// TEMP(fase 10b): input of the old calendar components; deleted with them.
@Stable
data class DateItemsUiModel(
  val text: String,
  val status: ItemStatus,
  val date: LocalDate,
  val items: ImmutableList<ItemDetailUiModel>,
)
