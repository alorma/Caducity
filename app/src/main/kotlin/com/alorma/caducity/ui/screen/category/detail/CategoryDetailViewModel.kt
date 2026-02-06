package com.alorma.caducity.ui.screen.category.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.model.ProductDeletionStrategy
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteProductUseCase
import com.alorma.caducity.domain.usecase.ObtainCategoryDetailUseCase
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryDetailViewModel(
  private val categoryId: String,
  obtainCategoryDetailUseCase: ObtainCategoryDetailUseCase,
  categoryDetailMapper: CategoryDetailMapper,
  calendarPreferences: CalendarPreferences,
  private val createProductUseCase: CreateProductUseCase,
  private val deleteProductUseCase: DeleteProductUseCase,
  private val deleteCategoryUseCase: DeleteCategoryUseCase,
) : ViewModel() {

  private val _sideEffect = Channel<CategoryDetailSideEffect>(Channel.BUFFERED)
  val sideEffect: Flow<CategoryDetailSideEffect> = _sideEffect.receiveAsFlow()

  private val customState: StateFlow<CategoryDetailState> = combine(
    obtainCategoryDetailUseCase.obtain(categoryId),
    calendarPreferences.state,
  ) { result, calendarConfig ->
    result.fold(
      onSuccess = { category ->
        categoryDetailMapper.mapToCategoryDetail(category, calendarConfig.firstDayOfWeek)
      },
      onFailure = { _ ->
        CategoryDetailState.Error("Not found")
      },
    )
  }.stateIn(
    viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = CategoryDetailState.Loading
  )

  val state: MutableStateFlow<CategoryDetailState> = MutableStateFlow(CategoryDetailState.Loading)

  var job: Job? = null

  init {
    job = customState.onEach { detailState -> state.emit(detailState) }.launchIn(viewModelScope)
  }

  fun onShowAddProductDialog() {
    emitSideEffect(CategoryDetailSideEffect.ShowAddProductDialog)
  }

  fun onCreateProduct(productName: String) {
    viewModelScope.launch {
      val result = createProductUseCase.create(categoryId, productName)
      if (result.isSuccess) {
        emitSideEffect(CategoryDetailSideEffect.ProductCreated)
      } else {
        emitSideEffect(CategoryDetailSideEffect.CreateProductFailed)
      }
    }
  }

  fun onDeleteProductClick(productId: String) {
    viewModelScope.launch {
      val activeItemCount = deleteProductUseCase.getActiveItemCount(productId)
      if (activeItemCount > 0) {
        // Get available products from current state
        val currentState = state.value
        val availableProducts = if (currentState is CategoryDetailState.Success) {
          currentState.productTabs.filter { it.id != null && it.id != productId }
        } else {
          emptyList()
        }
        emitSideEffect(
          CategoryDetailSideEffect.ShowDeleteProductWithItemsDialog(
            productId = productId,
            activeItemCount = activeItemCount,
            availableProducts = availableProducts,
            onDeleteProduct = ::onDeleteProduct,
          )
        )
      } else {
        emitSideEffect(
          CategoryDetailSideEffect.ShowDeleteProductDialog(
            productId,
            onDeleteProduct = ::onDeleteProduct,
          )
        )
      }
    }
  }

  fun onDeleteProduct(productId: String, strategy: ProductDeletionStrategy) {
    viewModelScope.launch {
      val result = deleteProductUseCase.delete(productId, strategy)
      if (result.isSuccess) {
        emitSideEffect(CategoryDetailSideEffect.ProductDeleted)
      } else {
        emitSideEffect(CategoryDetailSideEffect.DeleteProductFailed)
      }
    }
  }

  fun onClearProductItemsClick(productId: String) {
    emitSideEffect(CategoryDetailSideEffect.ShowClearProductItemsDialog(productId))
  }

  fun onClearProductItems(productId: String, clearAll: Boolean) {
    // TODO: Implement clear items use case
  }

  fun onDeleteCategoryClick() {
    emitSideEffect(CategoryDetailSideEffect.ShowDeleteCategoryDialog)
  }

  fun onDeleteCategory() {
    viewModelScope.launch {
      job?.cancel()
      try {
        val result = deleteCategoryUseCase.deleteCategory(categoryId)
        if (result.isSuccess) {
          emitSideEffect(CategoryDetailSideEffect.CategoryDeleted)
        } else {
          emitSideEffect(CategoryDetailSideEffect.DeleteCategoryFailed)
        }
      } catch (_: Exception) {
        job?.join()
        emitSideEffect(CategoryDetailSideEffect.DeleteCategoryFailed)
      }
    }
  }

  private fun emitSideEffect(effect: CategoryDetailSideEffect) {
    viewModelScope.launch {
      _sideEffect.send(effect)
    }
  }
}
