package com.alorma.caducity.ui.screen.transform

sealed interface TransformNavigation {
  data object Close : TransformNavigation
}

sealed interface TransformNavigationSideEffect {
  data object Close : TransformNavigationSideEffect
}

sealed interface TransformSideEffect {
  /** Asks whether to throw away the changes; confirming closes the screen. */
  data object ShowDiscardConfirm : TransformSideEffect

  data class ShowDatePicker(
    val resultId: Int,
    val currentMillis: Long,
  ) : TransformSideEffect

  data object ShowSaveFailed : TransformSideEffect

  data object ShowFrozenPastDate : TransformSideEffect
}
