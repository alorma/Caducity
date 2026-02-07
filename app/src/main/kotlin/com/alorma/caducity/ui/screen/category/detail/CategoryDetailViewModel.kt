package com.alorma.caducity.ui.screen.category.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import com.alorma.caducity.domain.usecase.ObtainCategoryDetailUseCase
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
  private val deleteCategoryUseCase: DeleteCategoryUseCase,
  private val getProductItemsUseCase: GetProductItemsUseCase,
) : ViewModel() {

  private val _sideEffect = Channel<CategoryDetailSideEffect>(Channel.BUFFERED)
  val sideEffect: Flow<CategoryDetailSideEffect> = _sideEffect.receiveAsFlow()

  // Track the currently selected product ID (null means "Other" tab with standalone items)
  private val _selectedProductId = MutableStateFlow<String?>(null)

  private val customState: StateFlow<CategoryDetailState> = combine(
    obtainCategoryDetailUseCase.obtain(categoryId),
    calendarPreferences.state,
    _selectedProductId.flatMapLatest { productId ->
      getProductItemsUseCase.obtain(categoryId, productId)
    }
  ) { result, calendarConfig, productItems ->
    result.fold(
      onSuccess = { category ->
        categoryDetailMapper.mapToCategoryDetail(
          categoryDetail = category, 
          firstDayOfWeek = calendarConfig.firstDayOfWeek,
          productItems = productItems
        )
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
    job = customState.onEach { detailState -> 
      state.emit(detailState)
      // Set initial selected product ID based on first tab if not already set
      if (detailState is CategoryDetailState.Success && 
          _selectedProductId.value == null && 
          detailState.productTabs.isNotEmpty()) {
        _selectedProductId.value = detailState.productTabs.first().id
      }
    }.launchIn(viewModelScope)
  }

  fun onProductTabChanged(productId: String?) {
    _selectedProductId.value = productId
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
