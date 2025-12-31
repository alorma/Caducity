package com.alorma.caducity.domain.usecase

import com.alorma.caducity.domain.model.InstanceStatus
import kotlinx.datetime.LocalDate

sealed class ProductsListFilter {
  data class ByDate(
    val date: LocalDate,
  ) : ProductsListFilter()

  data class ByStatus(
    val statuses: Set<InstanceStatus>,
  ) : ProductsListFilter()

  data class ByDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
  ) : ProductsListFilter()

  data object All : ProductsListFilter()
}