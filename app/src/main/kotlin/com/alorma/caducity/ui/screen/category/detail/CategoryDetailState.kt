package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.ui.components.calendar.AppCalendarConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

sealed interface CategoryDetailState {
  data object Loading : CategoryDetailState

  @Stable
  data class Empty(
    val today: LocalDate,
    val category: CategoryDetailUiModel,
    val appCalendarConfig: AppCalendarConfig,
  ) : CategoryDetailState

  @Stable
  data class Success(
    val today: LocalDate,
    val category: CategoryDetailUiModel,
    val appCalendarConfig: AppCalendarConfig,
    val productTabs: ImmutableList<CategoryProductTabUiModel>,
  ) : CategoryDetailState

  @Stable
  data class Error(val message: String) : CategoryDetailState
}
