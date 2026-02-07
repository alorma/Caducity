package com.alorma.caducity.ui.components.bottomsheet

sealed interface ItemAction {
  data object Consume : ItemAction
  data object ConsumeWithWarning : ItemAction
  data object Freeze : ItemAction
  data object Unfreeze : ItemAction
  data object Delete : ItemAction
  data object Placeholder : ItemAction
}
