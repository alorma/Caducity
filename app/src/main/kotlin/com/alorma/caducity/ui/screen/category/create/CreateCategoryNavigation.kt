package com.alorma.caducity.ui.screen.category.create

sealed interface CreateCategoryNavigation {
  data object Cancel : CreateCategoryNavigation

  data class CategoryCreated(
    val categoryId: String,
  ) : CreateCategoryNavigation
}

sealed interface CreateCategoryNavigationSideEffect {
  data object NavigateBack : CreateCategoryNavigationSideEffect

  data class NavigateToCategoryDetail(
    val categoryId: String,
  ) : CreateCategoryNavigationSideEffect
}
