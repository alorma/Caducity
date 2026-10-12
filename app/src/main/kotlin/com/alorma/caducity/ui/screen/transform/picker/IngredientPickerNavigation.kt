package com.alorma.caducity.ui.screen.transform.picker

sealed interface IngredientPickerNavigation {
  data object Close : IngredientPickerNavigation

  /** Adds the selected products as ingredients. */
  data object Confirm : IngredientPickerNavigation
}

sealed interface IngredientPickerNavigationSideEffect {
  data object Close : IngredientPickerNavigationSideEffect

  data class Confirm(
    val itemIds: List<String>,
  ) : IngredientPickerNavigationSideEffect
}
