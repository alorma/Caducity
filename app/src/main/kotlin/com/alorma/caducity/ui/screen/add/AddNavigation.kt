package com.alorma.caducity.ui.screen.add

sealed interface AddNavigation {
  data object Close : AddNavigation

  data object QuickCategory : AddNavigation
}

sealed interface AddNavigationSideEffect {
  data object Close : AddNavigationSideEffect

  data object OpenQuickCategory : AddNavigationSideEffect
}
