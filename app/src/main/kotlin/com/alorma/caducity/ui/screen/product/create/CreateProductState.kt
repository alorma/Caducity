package com.alorma.caducity.ui.screen.product.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

data class ProductInstanceInput(
  val id: String,
  val identifier: String = "",
  val expirationDateText: String? = null,
  val expirationDate: LocalDate? = null,
)

data class ProductInstanceGroupInput(
  val identifier: String,
  val instances: ImmutableList<ProductInstanceInput>,
  val expirationDates: ImmutableList<String>, // Sorted list of unique expiration date texts
)

@OptIn(ExperimentalMaterial3Api::class)
data class CreateProductState(
  val name: String = "",
  val description: String = "",
  val instances: List<ProductInstanceInput> = listOf(),
  val groups: ImmutableList<ProductInstanceGroupInput> = kotlinx.collections.immutable.persistentListOf(),
  val showDatePickerForInstanceId: String? = null,
  val selectableDates: SelectableDates,
  val isLoading: Boolean = false,
  val error: String? = null,
)