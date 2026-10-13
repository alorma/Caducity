package com.alorma.caducity.ui.screen.transform

sealed interface TransformNavigation {
  data object Close : TransformNavigation

  data object IngredientPicker : TransformNavigation
}

sealed interface TransformNavigationSideEffect {
  data object Close : TransformNavigationSideEffect

  /** [selectedItemIds] are the units already used, so the picker starts with their products selected. */
  data class OpenIngredientPicker(
    val selectedItemIds: List<String>,
  ) : TransformNavigationSideEffect
}

sealed interface TransformSideEffect {
  /** Asks whether to throw away the changes; confirming closes the screen. */
  data object ShowDiscardConfirm : TransformSideEffect

  data class ShowDatePicker(
    val resultId: Int,
    val currentMillis: Long,
  ) : TransformSideEffect

  /** «Resultat esborrat · Desfés»: the removed result is kept by the ViewModel until the snackbar is gone. */
  data object ShowResultRemoved : TransformSideEffect

  data object ShowSaveFailed : TransformSideEffect

  data object ShowFrozenPastDate : TransformSideEffect
}
