package com.alorma.caducity.ui.screen.product.create

import androidx.navigation3.runtime.NavKey
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRoute(
  val prefilledExpirationDate: String? = null
) : NavKey {
  companion object {
    fun withDate(date: LocalDate): CreateProductRoute {
      return CreateProductRoute(prefilledExpirationDate = date.toString())
    }
  }
}
