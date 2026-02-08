package com.alorma.caducity.ui.screen.category.detail

sealed interface AddItemNavigation {
  data object Cancel : AddItemNavigation
  data class ItemSaved(val hasProduct: Boolean, val quantity: Int) : AddItemNavigation
}

sealed interface AddItemNavigationSideEffect {
  data object NavigateBack : AddItemNavigationSideEffect
}
