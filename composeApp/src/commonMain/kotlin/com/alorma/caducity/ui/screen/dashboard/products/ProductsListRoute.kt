package com.alorma.caducity.ui.screen.dashboard.products

import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class ProductsListRoute(
  val date: String,
) : NavKey {
  companion object Companion {
    operator fun invoke(date: LocalDate): ProductsListRoute {
      return ProductsListRoute(date.toString())
    }
  }

  fun getLocalDate(): LocalDate = LocalDate.parse(date)
}
