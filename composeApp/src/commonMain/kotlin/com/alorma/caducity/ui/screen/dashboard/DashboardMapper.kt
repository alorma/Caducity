package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.base.ui.components.shape.ShapePosition
import com.alorma.caducity.base.main.InstanceStatus
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.time.clock.AppClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class DashboardMapper(
  private val appClock: AppClock,
  private val dateFormat: DateTimeFormat<LocalDate>,
) {
  data class DashboardData(
    val items: ImmutableList<ProductUiModel>,
    val summary: DashboardSummary,
    val calendarData: CalendarData,
  )

  fun mapToDashboardSections(
    products: ImmutableList<ProductWithInstances>,
    searchQuery: String = "",
    statusFilters: Set<InstanceStatus> = emptySet(),
  ): DashboardData {
    val allProducts = products.map { it.toUiModel() }

    // Calculate summary from all products (before filtering)
    val summary = calculateSummary(allProducts)

    // Calculate calendar data from all products (before filtering)
    val calendarData = calculateCalendarData(allProducts)

    // Apply filters for the list
    val filteredItems = allProducts
      .filter { product ->
        matchesSearchQuery(product, searchQuery) && matchesStatusFilters(product, statusFilters)
      }
      .toImmutableList()

    return DashboardData(
      items = filteredItems,
      summary = summary,
      calendarData = calendarData,
    )
  }

  private fun calculateSummary(products: List<ProductUiModel>): DashboardSummary {
    var expiredCount = 0
    var expiringSoonCount = 0
    var freshCount = 0
    var frozenCount = 0

    products.forEach { product ->
      when (product) {
        is ProductUiModel.Empty -> {
          // Empty products don't contribute to summary
        }
        is ProductUiModel.WithInstances -> {
          product.instances.forEach { instance ->
            when (instance.status) {
              is InstanceStatus.Expired -> expiredCount++
              is InstanceStatus.ExpiringSoon -> expiringSoonCount++
              is InstanceStatus.Fresh -> freshCount++
              is InstanceStatus.Frozen -> frozenCount++
              is InstanceStatus.Consumed -> {} // Consumed items are already filtered out
            }
          }
        }
      }
    }

    return DashboardSummary(
      expired = expiredCount,
      expiringSoon = expiringSoonCount,
      fresh = freshCount,
      frozen = frozenCount,
    )
  }

  private fun matchesSearchQuery(product: ProductUiModel, query: String): Boolean {
    if (query.isBlank()) return true
    val lowerQuery = query.lowercase()
    return product.name.lowercase().contains(lowerQuery) ||
        product.description.lowercase().contains(lowerQuery)
  }

  private fun matchesStatusFilters(product: ProductUiModel, filters: Set<InstanceStatus>): Boolean {
    if (filters.isEmpty()) return true
    return when (product) {
      is ProductUiModel.Empty -> false  // Empty products don't match any status filter
      is ProductUiModel.WithInstances -> {
        product.instances.any { instance ->
          filters.contains(instance.status)
        }
      }
    }
  }

  private fun calculateCalendarData(products: List<ProductUiModel>): CalendarData {
    // Group products by date with most critical status
    val productsByDate = buildMap {
      products.forEach { product ->
        if (product is ProductUiModel.WithInstances) {
          product.instances.forEach { instance ->
            val date = instance.expirationDate
            val currentStatus = get(date)

            // Keep the most critical status (Expired > ExpiringSoon > Fresh)
            val newStatus = when {
              currentStatus == InstanceStatus.Expired -> InstanceStatus.Expired
              instance.status == InstanceStatus.Expired -> InstanceStatus.Expired
              currentStatus == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
              instance.status == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
              else -> InstanceStatus.Fresh
            }

            put(date, newStatus)
          }
        }
      }
    }

    // Calculate shape position for each date
    val dateWithShapes = productsByDate.mapValues { (date, status) ->
      val hasPrevDay = productsByDate.containsKey(date.plus(-1, DateTimeUnit.DAY))
      val hasNextDay = productsByDate.containsKey(date.plus(1, DateTimeUnit.DAY))

      val shapePosition = when {
        !hasPrevDay && !hasNextDay -> ShapePosition.Single
        !hasPrevDay && hasNextDay -> ShapePosition.Start
        hasPrevDay && !hasNextDay -> ShapePosition.End
        else -> ShapePosition.Middle
      }

      CalendarDateInfo(status, shapePosition)
    }.toImmutableMap()

    return CalendarData(dateWithShapes)
  }

  private fun ProductWithInstances.toUiModel(): ProductUiModel {
    if (instances.isEmpty()) {
      return ProductUiModel.Empty(
        id = product.id,
        name = product.name,
        description = product.description,
      )
    }

    val now = appClock.now()

    val today = now
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    return ProductUiModel.WithInstances(
      id = product.id,
      name = product.name,
      description = product.description,
      today = dateFormat.format(today),
      instances = instances.map { instance ->
        val expirationLocalDate = instance
          .expirationDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date

        ProductInstanceUiModel(
          id = instance.id,
          identifier = instance.identifier,
          status = instance.status,
          expirationDate = instance.expirationDate
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date,
          expirationDateText = dateFormat.format(expirationLocalDate),
        )
      }.toImmutableList()
    )
  }
}
