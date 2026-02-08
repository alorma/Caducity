package com.alorma.caducity.ui.screen.category.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.alorma.caducity.config.navigation.BottomSheetSceneStrategy

@Composable
fun CategoryDetailContainer(
  categoryId: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val productDetailBackStack = retain {
    mutableStateListOf<NavKey>(CategoryDetailRoutes.Root(categoryId))
  }

  val bottomSheetStrategy = remember {
    BottomSheetSceneStrategy<NavKey>()
  }

  NavDisplay(
    modifier = modifier,
    backStack = productDetailBackStack,
    sceneStrategy = bottomSheetStrategy,
    entryDecorators = listOf(
      rememberSaveableStateHolderNavEntryDecorator(),
      rememberViewModelStoreNavEntryDecorator(),
    ),
    entryProvider = entryProvider {
      entry<CategoryDetailRoutes.Root> {
        val viewModel = org.koin.compose.viewmodel.koinViewModel<CategoryDetailViewModel> {
          org.koin.core.parameter.parametersOf(categoryId)
        }

        // Handle navigation side effects
        androidx.compose.runtime.LaunchedEffect(viewModel) {
          viewModel.sideEffect.collect { effect ->
            when (effect) {
              is CategoryDetailSideEffect.NavigateToAddItem -> {
                productDetailBackStack.add(
                  CategoryDetailRoutes.AddInstance(categoryId, effect.productId)
                )
              }
              CategoryDetailSideEffect.NavigateBack -> {
                onBack()
              }
              else -> { /* Other side effects handled in screen */ }
            }
          }
        }

        CategoryDetailScreen(
          categoryId = it.categoryId,
          viewModel = viewModel,
        )
      }
      entry<CategoryDetailRoutes.AddInstance> {
        CategoryDetailAddItemScreen(
          categoryId = it.categoryId,
          productId = it.productId,
          onNavigateBack = { productDetailBackStack.removeLastOrNull() },
        )
      }
    },
  )
}
