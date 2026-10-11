package com.alorma.caducity.ui.screen.add

import com.alorma.caducity.domain.model.BatchSnapshot

sealed interface AddSideEffect {
  /** Snackbar "«name» desat · Desfés" after "Desa i un altre". */
  data class ShowSaved(
    val entry: AddedEntry,
  ) : AddSideEffect

  /** Snackbar «{name} tret · Desfés» after «Treu de la tanda». */
  data class ShowRemoved(
    val entry: AddedEntry,
    val snapshot: BatchSnapshot,
    val index: Int,
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
