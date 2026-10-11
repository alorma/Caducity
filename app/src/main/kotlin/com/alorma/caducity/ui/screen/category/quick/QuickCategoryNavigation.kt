package com.alorma.caducity.ui.screen.category.quick

sealed interface QuickCategoryNavigation {
  data object Close : QuickCategoryNavigation
}

sealed interface QuickCategoryNavigationSideEffect {
  data object Close : QuickCategoryNavigationSideEffect
}
