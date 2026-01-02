package com.alorma.caducity.ui.screen.dashboard

import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.domain.model.InstanceStatus
import com.alorma.caducity.domain.model.ProductWithInstances
import com.alorma.caducity.ui.components.shape.ShapePosition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
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
    val calendarState: CalendarState,
  )

  fun mapToDashboardSections(
    products: ImmutableList<ProductWithInstances>,
    searchQuery: String = "",
    statusFilters: Set<InstanceStatus> = emptySet(),
  ): DashboardData {
    val allProducts = products.map { it.toUiModel() }

    // Calculate summary from all products (before filtering)
    val summary = calculateSummary(allProducts)

    // Calculate calendar state from all products (before filtering)
    val calendarState = calculateCalendarState(allProducts)

    // Apply filters for the list
    val filteredItems = allProducts
      .filter { product ->
        matchesSearchQuery(product, searchQuery) && matchesStatusFilters(product, statusFilters)
      }
      .toImmutableList()

    return DashboardData(
      items = filteredItems,
      summary = summary,
      calendarState = calendarState,
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

  private fun calculateCalendarState(
    products: List<ProductUiModel>,
  ): CalendarState {
    val today = appClock.now()
      .toLocalDateTime(TimeZone.currentSystemDefault())
      .date

    // Calculate start and end months for calendar range
    val currentMonth = YearMonth(today.year, today.month)
    val currentMonthNum = today.month.ordinal + 1 // Month ordinal is 0-based

    val startMonthNum = currentMonthNum - 1
    val startMonth = if (startMonthNum >= 1) {
      YearMonth(today.year, startMonthNum)
    } else {
      YearMonth(today.year - 1, 12)
    }

    val endMonthNum = currentMonthNum + 3
    val endMonth = if (endMonthNum <= 12) {
      YearMonth(today.year, endMonthNum)
    } else {
      YearMonth(today.year + 1, endMonthNum - 12)
    }

    // Group products by date with most critical status
    val productsByDate = buildMap {
      products.forEach { product ->
        if (product is ProductUiModel.WithInstances) {
          product.instances.forEach { instance ->
            // Use expirationDate here as UI model already converted to correct display date
            // For frozen items: expirationDate field already contains the pausedDate
            // For normal items: expirationDate contains the expiration date
            val date = instance.expirationDate
            val currentStatus = get(date)

            // Keep the most critical status (Expired > ExpiringSoon > Frozen > Fresh)
            val newStatus = when {
              currentStatus == InstanceStatus.Expired -> InstanceStatus.Expired
              instance.status == InstanceStatus.Expired -> InstanceStatus.Expired
              currentStatus == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
              instance.status == InstanceStatus.ExpiringSoon -> InstanceStatus.ExpiringSoon
              currentStatus == InstanceStatus.Frozen -> InstanceStatus.Frozen
              instance.status == InstanceStatus.Frozen -> InstanceStatus.Frozen
              else -> InstanceStatus.Fresh
            }

            put(date, newStatus)
          }
        }
      }

      // Ensure today is always in the map, even if no products
      if (!containsKey(today)) {
        put(today, null)
      }
    }

    // Calculate shape position for each date
    val dateWithShapes = productsByDate.mapValues { (date, status) ->
      val hasPrevDay = productsByDate.containsKey(date.plus(-1, DateTimeUnit.DAY))
      val hasNextDay = productsByDate.containsKey(date.plus(1, DateTimeUnit.DAY))

      val shapePosition = when {
        date == today && !hasPrevDay && !hasNextDay -> ShapePosition.Single
        date == today && hasPrevDay && !hasNextDay -> ShapePosition.End
        !hasPrevDay && !hasNextDay -> ShapePosition.Single
        !hasPrevDay && hasNextDay -> ShapePosition.Start
        hasPrevDay && !hasNextDay -> ShapePosition.End
        else -> ShapePosition.Middle
      }

      CalendarDateInfo(status, shapePosition)
    }.toImmutableMap()

    val calendarData = CalendarData(dateWithShapes)

    return CalendarState(
      startMonth = startMonth,
      endMonth = endMonth,
      today = today,
      calendarData = calendarData,
    )
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
        // Use displayDate for frozen items (pausedDate) or expirationDate for others
        val displayLocalDate = instance
          .displayDate
          .toLocalDateTime(TimeZone.currentSystemDefault())
          .date

        ProductInstanceUiModel(
          id = instance.id,
          identifier = instance.identifier,
          status = instance.status,
          expirationDate = displayLocalDate,
          expirationDateText = dateFormat.format(displayLocalDate),
        )
      }.toImmutableList()
    )
  }
}
