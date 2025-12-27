package com.alorma.caducity.ui.screen.product.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import kotlinx.datetime.LocalDate

data class ProductInstanceInput(
  val id: String,
  val identifier: String = "",
  val expirationDateText: String? = null,
  val expirationDate: LocalDate? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
data class CreateProductState(
  val name: String = "",
  val description: String = "",
  val instances: List<ProductInstanceInput> = listOf(),
  val showDatePickerForInstanceId: String? = null,
  val selectableDates: SelectableDates,
  val isLoading: Boolean = false,
  val error: String? = null,
)