package com.alorma.caducity.ui.components.bottomsheet

import androidx.compose.runtime.Stable
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.datetime.LocalDate

// TEMP(fase 7): only the old actions sheet and the old Expired screen use it; deleted with them.
@Stable
data class ItemDetailUiModel(
  val id: String,
  val expirationDate: LocalDate,
  val status: ItemStatus,
  val text: String,
  val packSize: Int? = null,
)
