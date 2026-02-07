package com.alorma.caducity.ui.screen.dashboard

import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.domain.model.ItemStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface FilteredItemsRoute : NavKey {
  @Serializable
  data class ByStatus(val status: ItemStatus) : FilteredItemsRoute

  @Serializable
  data class ByDate(val date: LocalDate) : FilteredItemsRoute
}
