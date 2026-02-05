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
        CategoryDetailScreen(
          categoryId = it.categoryId,
          onNavigateToAddInstance = { productId ->
            productDetailBackStack.add(CategoryDetailRoutes.AddInstance(categoryId, productId))
          },
        )
      }
      entry<CategoryDetailRoutes.AddInstance> {
        CategoryDetailAddItemScreen(
          categoryId = it.categoryId,
          productId = it.productId,
          onClose = { productDetailBackStack.removeLastOrNull() },
        )
      }
    },
  )
}
