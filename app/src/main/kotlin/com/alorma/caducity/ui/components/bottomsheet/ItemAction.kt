package com.alorma.caducity.ui.components.bottomsheet

sealed interface ItemAction {
  data object Consume : ItemAction
  data class ConsumeQuantity(val quantity: Int) : ItemAction
  data object ConsumeWithWarning : ItemAction
  data class ConsumeWithWarningQuantity(val quantity: Int) : ItemAction
  data object Freeze : ItemAction
  data class FreezeQuantity(val quantity: Int) : ItemAction
  data object Unfreeze : ItemAction
  data class UnfreezeQuantity(val quantity: Int) : ItemAction
  data object Reschedule : ItemAction
  data object Delete : ItemAction
  data class DeleteQuantity(val quantity: Int) : ItemAction
  data object Placeholder : ItemAction
}
