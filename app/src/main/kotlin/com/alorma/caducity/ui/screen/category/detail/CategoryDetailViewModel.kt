package com.alorma.caducity.ui.screen.category.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alorma.caducity.domain.usecase.CreateProductUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import com.alorma.caducity.domain.usecase.ObtainCategoryDetailUseCase
import com.alorma.caducity.feature.tracking.CategoryDeletedAction
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.feature.tracking.NavigateToAddItemFromCategoryAction
import com.alorma.caducity.ui.components.calendar.CalendarPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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
  private val eventTracker: EventTracker,
) : ViewModel() {

  private val _sideEffect = Channel<CategoryDetailSideEffect>(Channel.BUFFERED)
  val sideEffect: Flow<CategoryDetailSideEffect> = _sideEffect.receiveAsFlow()

  // Track the currently selected product ID (null means "Other" tab with standalone items)
  private val _selectedProductId = MutableStateFlow<String?>(null)
  
  // Get the category detail first to determine initial product
  private val categoryDetailFlow = obtainCategoryDetailUseCase.obtain(categoryId)

  private val customState: StateFlow<CategoryDetailState> = combine(
    categoryDetailFlow,
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
    // Set initial selected product ID based on first available product
    viewModelScope.launch {
      val result = categoryDetailFlow.first()
      result.onSuccess { categoryDetail ->
        if (_selectedProductId.value == null) {
          // Set to first product, or null if only standalone items exist
          _selectedProductId.value = categoryDetail.products.firstOrNull()?.id
        }
      }
    }
    
    job = customState.onEach { detailState -> 
      state.emit(detailState)
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
          navigate(CategoryDetailNavigation.CategoryDeleted)
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

  fun navigate(navigation: CategoryDetailNavigation) {
    when (navigation) {
      is CategoryDetailNavigation.AddItem -> {
        eventTracker.trackAction(
          NavigateToAddItemFromCategoryAction(
            source = navigation.source,
            hasProduct = navigation.productId != null
          )
        )
        emitSideEffect(CategoryDetailSideEffect.NavigateToAddItem(navigation.productId))
      }
      CategoryDetailNavigation.CategoryDeleted -> {
        eventTracker.trackAction(CategoryDeletedAction())
        emitSideEffect(CategoryDetailSideEffect.NavigateBack)
      }
    }
  }
}
