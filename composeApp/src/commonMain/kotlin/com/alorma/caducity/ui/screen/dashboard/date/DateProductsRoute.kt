package com.alorma.caducity.ui.screen.dashboard.date

import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DateProductsRoute(
  val date: String, // ISO-8601 format (yyyy-MM-dd)
) : NavKey {
  companion object {
    operator fun invoke(date: LocalDate): DateProductsRoute {
      return DateProductsRoute(date.toString())
    }
  }

  fun getLocalDate(): LocalDate = LocalDate.parse(date)
}
