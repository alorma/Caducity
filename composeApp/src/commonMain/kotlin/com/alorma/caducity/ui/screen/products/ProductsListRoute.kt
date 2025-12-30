package com.alorma.caducity.ui.screen.products

import androidx.navigation3.runtime.NavKey
import com.alorma.caducity.ui.screen.dashboard.InstanceStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ProductsListRoute(
  val filterType: String,
  val date: String? = null,
  val startDate: String? = null,
  val endDate: String? = null,
  val statuses: List<String>? = null,
) : NavKey {
  companion object {
    fun byDate(date: LocalDate): ProductsListRoute {
      return ProductsListRoute(
        filterType = "date",
        date = date.toString(),
      )
    }

    fun byStatus(statuses: Set<InstanceStatus>): ProductsListRoute {
      return ProductsListRoute(
        filterType = "status",
        statuses = statuses.map { it.toSerializable() },
      )
    }

    fun byDateRange(startDate: LocalDate, endDate: LocalDate): ProductsListRoute {
      return ProductsListRoute(
        filterType = "dateRange",
        startDate = startDate.toString(),
        endDate = endDate.toString(),
      )
    }

    fun all(): ProductsListRoute {
      return ProductsListRoute(filterType = "all")
    }

    private fun InstanceStatus.toSerializable(): String = when (this) {
      is InstanceStatus.Expired -> "expired"
      is InstanceStatus.ExpiringSoon -> "expiringSoon"
      is InstanceStatus.Fresh -> "fresh"
    }
  }

  fun toFilter(): ProductsListFilter {
    return when (filterType) {
      "date" -> {
        requireNotNull(date) { "date is required for ByDate filter" }
        ProductsListFilter.ByDate(LocalDate.parse(date))
      }
      "status" -> {
        requireNotNull(statuses) { "statuses is required for ByStatus filter" }
        ProductsListFilter.ByStatus(
          statuses.map { it.toInstanceStatus() }.toSet()
        )
      }
      "dateRange" -> {
        requireNotNull(startDate) { "startDate is required for ByDateRange filter" }
        requireNotNull(endDate) { "endDate is required for ByDateRange filter" }
        ProductsListFilter.ByDateRange(
          LocalDate.parse(startDate),
          LocalDate.parse(endDate),
        )
      }
      "all" -> ProductsListFilter.All
      else -> error("Unknown filter type: $filterType")
    }
  }

  private fun String.toInstanceStatus(): InstanceStatus = when (this) {
    "expired" -> InstanceStatus.Expired
    "expiringSoon" -> InstanceStatus.ExpiringSoon
    "fresh" -> InstanceStatus.Fresh
    else -> error("Unknown status: $this")
  }
}
