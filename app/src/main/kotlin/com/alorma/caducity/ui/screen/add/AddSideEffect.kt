package com.alorma.caducity.ui.screen.add

import com.alorma.caducity.domain.model.UnitType

sealed interface AddSideEffect {
  data class ShowUnitTypePicker(
    val current: UnitType,
  ) : AddSideEffect

  data class ShowDatePicker(
    val currentMillis: Long,
  ) : AddSideEffect

  data object ShowSaveFailed : AddSideEffect

  data object ShowFrozenPastDate : AddSideEffect

  /** Haptic `Confirm`. */
  data object Confirm : AddSideEffect

  /** Haptic `Reject` (a disabled button was tapped). */
  data object Reject : AddSideEffect
}
