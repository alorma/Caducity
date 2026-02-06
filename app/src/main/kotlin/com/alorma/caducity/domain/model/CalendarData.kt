package com.alorma.caducity.domain.model

import kotlinx.datetime.LocalDate

/**
 * Calendar information for displaying dates and their statuses.
 * Contains no item details - only dates and their corresponding status.
 */
data class CalendarData(
  val dateStatuses: List<DateStatus>,
)

data class DateStatus(
  val date: LocalDate,
  val status: ItemStatus,
  val itemCount: Int,
)
