package com.alorma.caducity.ui.screen.product.detail

import androidx.compose.runtime.Stable
import com.alorma.caducity.ui.components.calendar.AppCalendarConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

sealed interface ProductDetailState {
  data object Loading : ProductDetailState

  @Stable
  data class Success(
    val today: LocalDate,
    val product: ProductDetailUiModel,
    val appCalendarConfig: AppCalendarConfig,
    val datedContent: ImmutableList<DateInstancesUiModel>,
  ) : ProductDetailState

  @Stable
  data class Error(val message: String) : ProductDetailState
}
