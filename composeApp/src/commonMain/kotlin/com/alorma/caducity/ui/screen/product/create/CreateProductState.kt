package com.alorma.caducity.ui.screen.product.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
data class CreateProductState(
  val name: String = "",
  val description: String = "",
  val expirationDateText: String? = null,
  val expirationDate: LocalDate? = null,
  val showDatePicker: Boolean = false,
  val selectableDates: SelectableDates,
  val isLoading: Boolean = false,
  val error: String? = null,
)