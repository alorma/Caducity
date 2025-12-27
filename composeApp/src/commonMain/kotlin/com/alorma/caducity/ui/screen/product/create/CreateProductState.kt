package com.alorma.caducity.ui.screen.product.create

import kotlinx.datetime.LocalDate

data class CreateProductState(
  val name: String = "",
  val description: String = "",
  val expirationDateText: String? = null,
  val expirationDate: LocalDate? = null,
  val showDatePicker: Boolean = false,
  val isLoading: Boolean = false,
  val error: String? = null,
)