package com.alorma.caducity.ui.screen.transform.picker

sealed interface IngredientPickerNavigation {
  data object Close : IngredientPickerNavigation

  /** Hands the selected products over to Transform and closes. */
  data object Confirm : IngredientPickerNavigation
}

sealed interface IngredientPickerNavigationSideEffect {
  data object Close : IngredientPickerNavigationSideEffect
}
