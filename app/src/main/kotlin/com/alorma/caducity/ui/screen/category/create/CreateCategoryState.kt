package com.alorma.caducity.ui.screen.category.create

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate

data class CategoryItemInput(
  val id: String,
  val identifier: String = "",
  val expirationDateText: String? = null,
  val expirationDate: LocalDate? = null,
)

data class CategoryItemGroupInput(
  val identifier: String,
  val items: ImmutableList<CategoryItemInput>,
  val expirationDates: ImmutableList<String>, // Sorted list of unique expiration date texts
)

data class CreateCategoryState(
  val name: String = "",
  val description: String = "",
  val isLoading: Boolean = false,
  val error: String? = null,
)
