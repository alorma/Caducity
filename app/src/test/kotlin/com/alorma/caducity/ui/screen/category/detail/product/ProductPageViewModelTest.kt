package com.alorma.caducity.ui.screen.category.detail.product

import app.cash.turbine.test
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.model.ProductDeletionStrategy
import com.alorma.caducity.domain.usecase.ClearProductItemsUseCase
import com.alorma.caducity.domain.usecase.DeleteProductUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.screen.category.detail.CategoryProductTabUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.collections.immutable.toImmutableList
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalCoroutinesApi::class)
class ProductPageViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  // Mocks
  private lateinit var itemDataSource: ItemDataSource
  private lateinit var productDataSource: ProductDataSource
  private lateinit var itemDao: ItemDao
  private lateinit var appClock: AppClock
  private lateinit var eventTracker: EventTracker
  private lateinit var expirationThresholds: ExpirationThresholds

  // Real implementations
  private lateinit var getProductItemsUseCase: GetProductItemsUseCase
  private lateinit var getCategoryProductsUseCase: GetCategoryProductsUseCase
  private lateinit var deleteProductUseCase: DeleteProductUseCase
  private lateinit var clearProductItemsUseCase: ClearProductItemsUseCase
  private lateinit var productPageMapper: ProductPageMapper

  // Test data
  private val categoryId = "test-category"
  private val productId = "test-product"
  private val now = Instant.parse("2024-02-10T12:00:00Z")

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)

    // Create mocks
    itemDataSource = mock()
    productDataSource = mock()
    itemDao = mock<ItemDao>()
    appClock = mock()
    eventTracker = mock()
    expirationThresholds = mock()

    // Configure mocks
    whenever(appClock.now()).thenReturn(now)
    whenever(expirationThresholds.soonExpiringThreshold).thenReturn(3.days)
    whenever(expirationThresholds.consumeExpiredThreshold).thenReturn(2.days)

    getProductItemsUseCase = GetProductItemsUseCase(
      itemDataSource = itemDataSource,
      appClock = appClock,
      expirationThresholds = expirationThresholds,
    )

    getCategoryProductsUseCase = GetCategoryProductsUseCase(
      productDataSource = productDataSource,
    )

    deleteProductUseCase = DeleteProductUseCase(
      productDataSource = productDataSource,
      itemDao = itemDao,
    )

    clearProductItemsUseCase = ClearProductItemsUseCase(
      itemDataSource = itemDataSource,
    )

    productPageMapper = ProductPageMapper(appClock = appClock)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel(
    productTab: CategoryProductTabUiModel = CategoryProductTabUiModel(
      id = productId,
      categoryId = categoryId,
      name = "Test Product",
    ),
  ): ProductPageViewModel {
    return ProductPageViewModel(
      productTab = productTab,
      getCategoryProductsUseCase = getCategoryProductsUseCase,
      getProductItemsUseCase = getProductItemsUseCase,
      productPageMapper = productPageMapper,
      deleteProductUseCase = deleteProductUseCase,
      clearProductItemsUseCase = clearProductItemsUseCase,
      eventTracker = eventTracker,
    )
  }

  @Test
  fun `initial state is Loading`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))

    // When
    val viewModel = createViewModel()

    // Then
    expectThat(viewModel.state.value)
      .isA<ProductPageState.Loading>()
  }

  @Test
  fun `state updates to Success with empty items`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))

    // When
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    viewModel.state.test {
      val state = awaitItem()
      expectThat(state).isA<ProductPageState.Success>()
      val successState = state as ProductPageState.Success
      expectThat(successState.datedItemsGroups.size).isEqualTo(0)
      expectThat(successState.frozenItems.size).isEqualTo(0)
      expectThat(successState.consumedItems.size).isEqualTo(0)
    }
  }

  @Test
  fun `state updates to Success with items`() = runTest {
    // Given
    val items = listOf(
      Item(
        id = "item1",
        identifier = "Item 1",
        productId = productId,
        expirationDate = Instant.parse("2024-02-15T00:00:00Z"),
        status = ItemStatus.Fresh,
        pausedDate = null,
      ),
      Item(
        id = "item2",
        identifier = "Item 2",
        productId = productId,
        expirationDate = Instant.parse("2024-02-15T00:00:00Z"),
        status = ItemStatus.Fresh,
        pausedDate = null,
      ),
    )
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(items))

    // When
    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    viewModel.state.test {
      val state = awaitItem()
      expectThat(state).isA<ProductPageState.Success>()
      val successState = state as ProductPageState.Success
      expectThat(successState.datedItemsGroups.size).isEqualTo(1)
      expectThat(successState.datedItemsGroups[0].items.size).isEqualTo(2)
    }
  }

  @Test
  fun `onAddItemClick emits navigation side effect`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.navigationSideEffects.test {
      viewModel.onAddItemClick()
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageNavigationSideEffect.NavigateToAddItem>()
      val navEffect = sideEffect as ProductPageNavigationSideEffect.NavigateToAddItem
      expectThat(navEffect.categoryId).isEqualTo(categoryId)
      expectThat(navEffect.productId).isEqualTo(productId)
    }
  }

  @Test
  fun `onAddItemClick tracks action`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.onAddItemClick()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    verify(eventTracker).trackAction(any())
  }

  @Test
  fun `onDeleteProductClick with no items shows simple delete dialog`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))
    whenever(deleteProductUseCase.getActiveItemCount(productId))
      .thenReturn(0)

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onDeleteProductClick()
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ShowDeleteProductDialog>()
    }
  }

  @Test
  fun `onDeleteProductClick with items shows dialog with options`() = runTest {
    // Given
    val products = listOf(
      Product(
        id = "other-product",
        categoryId = categoryId,
        name = "Other Product",
        createdAt = now,
      ),
    )
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))
    whenever(deleteProductUseCase.getActiveItemCount(productId))
      .thenReturn(5)
    whenever(productDataSource.getProductsByCategory(categoryId))
      .thenReturn(flowOf(products.toImmutableList()))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onDeleteProductClick()
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ShowDeleteProductWithItemsDialog>()
      val dialog = sideEffect as ProductPageSideEffect.ShowDeleteProductWithItemsDialog
      expectThat(dialog.activeItemCount).isEqualTo(5)
      expectThat(dialog.availableProducts.size).isEqualTo(1)
    }
  }

  @Test
  fun `onDeleteProduct success emits success side effect`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))
    whenever(deleteProductUseCase.delete(productId, ProductDeletionStrategy.CascadeDelete))
      .thenReturn(Result.success(Unit))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onDeleteProduct(productId, ProductDeletionStrategy.CascadeDelete)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ProductDeleted>()
    }
  }

  @Test
  fun `onDeleteProduct failure emits failure side effect`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))
    whenever(deleteProductUseCase.delete(productId, ProductDeletionStrategy.CascadeDelete))
      .thenReturn(Result.failure(Exception("Delete failed")))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onDeleteProduct(productId, ProductDeletionStrategy.CascadeDelete)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.DeleteProductFailed>()
    }
  }

  @Test
  fun `onClearProductItemsClick emits dialog side effect`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onClearProductItemsClick()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ShowClearProductItemsDialog>()
    }
  }

  @Test
  fun `onClearProductItems with clearAll true clears all items`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))
    whenever(clearProductItemsUseCase.clearAllItems(categoryId, productId))
      .thenReturn(Result.success(Unit))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onClearProductItems(productId, clearAll = true)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ItemsCleared>()
    }

    verify(clearProductItemsUseCase).clearAllItems(categoryId, productId)
  }

  @Test
  fun `onClearProductItems with clearAll false clears only consumed items`() = runTest {
    // Given
    whenever(itemDataSource.getItemsByProduct(categoryId, productId))
      .thenReturn(flowOf(emptyList()))
    whenever(clearProductItemsUseCase.clearConsumedItems(categoryId, productId))
      .thenReturn(Result.success(Unit))

    val viewModel = createViewModel()
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onClearProductItems(productId, clearAll = false)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ItemsCleared>()
    }

    verify(clearProductItemsUseCase).clearConsumedItems(categoryId, productId)
  }
}
