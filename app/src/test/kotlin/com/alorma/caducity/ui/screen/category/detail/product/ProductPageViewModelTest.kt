package com.alorma.caducity.ui.screen.category.detail.product

import app.cash.turbine.test
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.data.datasource.RoomItemDataSource
import com.alorma.caducity.data.datasource.RoomProductDataSource
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.domain.model.ProductDeletionStrategy
import com.alorma.caducity.domain.usecase.ClearProductItemsUseCase
import com.alorma.caducity.domain.usecase.DeleteProductUseCase
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.GetCategoryProductsUseCase
import com.alorma.caducity.domain.usecase.GetProductItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.screen.category.detail.CategoryProductTabUiModel
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class ProductPageViewModelTest {

  private val testDispatcher = StandardTestDispatcher()

  // Mocks - interfaces only
  private val appClock: AppClock = mock {
    on { now() } doReturn now
  }
  private val eventTracker: EventTracker = mock()
  private val expirationThresholds: ExpirationThresholds = mock {
    on { soonExpiringThreshold } doReturn 3.days
    on { consumeExpiredThreshold } doReturn 2.days
  }

  // In-memory storage for fake DAOs
  private val itemsInMemory = MutableStateFlow<List<ItemRoomEntity>>(emptyList())
  private val productsInMemory = MutableStateFlow<List<ProductRoomEntity>>(emptyList())

  // Fake DAOs
  private val itemDao = FakeItemDao(itemsInMemory)
  private val productDao = FakeProductDao(productsInMemory, itemsInMemory)

  // Real mappers
  private val itemMapper = ItemRoomMapper(
    appClock = appClock,
    expirationThresholds = expirationThresholds,
  )

  private val productMapper = ProductRoomMapper()

  // Real data sources
  private val itemDataSource = RoomItemDataSource(
    itemDao = itemDao,
    appClock = appClock,
    itemMapper = itemMapper,
  )

  private val productDataSource = RoomProductDataSource(
    productDao = productDao,
    itemDao = itemDao,
    appClock = appClock,
    productMapper = productMapper,
  )

  // Real use cases
  private val getProductItemsUseCase = GetProductItemsUseCase(
    itemDataSource = itemDataSource,
    appClock = appClock,
    expirationThresholds = expirationThresholds,
  )

  private val getCategoryProductsUseCase = GetCategoryProductsUseCase(
    productDataSource = productDataSource,
  )

  private val deleteProductUseCase = DeleteProductUseCase(
    productDataSource = productDataSource,
    itemDao = itemDao,
  )

  private val clearProductItemsUseCase = ClearProductItemsUseCase(
    itemDataSource = itemDataSource,
  )

  private val productPageMapper = ProductPageMapper(appClock = appClock)

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    // Clear in-memory storage
    itemsInMemory.value = emptyList()
    productsInMemory.value = emptyList()
  }

  private fun createViewModel(
    productTab: CategoryProductTabUiModel = CategoryProductTabUiModel(
      id = testProductId,
      categoryId = testCategoryId,
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

  private fun insertTestProduct(): String {
    val product = ProductRoomEntity(
      id = UUID.randomUUID().toString(),
      categoryId = testCategoryId,
      name = "Test Product",
      createdAt = now.toEpochMilliseconds(),
    )
    productsInMemory.value += product
    return product.id
  }

  private fun insertTestItem(
    productId: String? = testProductId,
    identifier: String = "Test Item",
    expirationDate: Instant = Instant.parse("2024-02-15T00:00:00Z"),
  ): String {
    val id = UUID.randomUUID().toString()
    val item = ItemRoomEntity(
      id = id,
      categoryId = testCategoryId,
      identifier = identifier,
      productId = productId,
      expirationDate = expirationDate.toEpochMilliseconds(),
      consumedDate = null,
      pausedDate = null,
      remainingDays = null,
    )
    itemsInMemory.value += item
    return id
  }

  @Test
  fun `initial state is Loading`() = runTest {
    // Given
    insertTestProduct()

    // When
    val viewModel = createViewModel()

    // Then
    expectThat(viewModel.state.value)
      .isA<ProductPageState.Loading>()
  }

  @Test
  fun `state updates to Success with empty items`() = runTest {
    // Given
    insertTestProduct()

    // When
    val viewModel = createViewModel()

    // Then
    viewModel.state.test {
      // Skip the initial Loading state
      expectThat(awaitItem()).isA<ProductPageState.Loading>()

      // Advance the dispatcher to process the load
      testDispatcher.scheduler.advanceUntilIdle()

      // Now check the Success state
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
    val productId = insertTestProduct()
    insertTestItem(productId = productId, identifier = "Item 1")
    insertTestItem(productId = productId, identifier = "Item 2")

    // When
    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )

    // Then
    viewModel.state.test {
      // Skip the initial Loading state
      expectThat(awaitItem()).isA<ProductPageState.Loading>()

      // Advance the dispatcher to process the load
      testDispatcher.scheduler.advanceUntilIdle()

      // Now check the Success state with items
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
    val productId = insertTestProduct()

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.navigationSideEffects.test {
      viewModel.onAddItemClick()
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageNavigationSideEffect.NavigateToAddItem>()
      val navEffect = sideEffect as ProductPageNavigationSideEffect.NavigateToAddItem
      expectThat(navEffect.categoryId).isEqualTo(testCategoryId)
      expectThat(navEffect.productId).isEqualTo(productId)
    }
  }

  @Test
  fun `onAddItemClick tracks action`() = runTest {
    // Given
    val productId = insertTestProduct()

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
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
    val productId = insertTestProduct()

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
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
    val productId = insertTestProduct()
    insertTestItem(productId = productId)
    insertTestItem(productId = productId)

    // Create another product
    val otherProduct = ProductRoomEntity(
      id = UUID.randomUUID().toString(),
      categoryId = testCategoryId,
      name = "Other Product",
      createdAt = now.toEpochMilliseconds(),
    )
    productsInMemory.value += otherProduct

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onDeleteProductClick()
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ShowDeleteProductWithItemsDialog>()
      val dialog = sideEffect as ProductPageSideEffect.ShowDeleteProductWithItemsDialog
      expectThat(dialog.activeItemCount).isEqualTo(2)
      expectThat(dialog.availableProducts.size).isEqualTo(1)
    }
  }

  @Test
  fun `onDeleteProduct success emits success side effect`() = runTest {
    // Given
    val productId = insertTestProduct()

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
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
  fun `onClearProductItemsClick emits dialog side effect`() = runTest {
    // Given
    val productId = insertTestProduct()

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
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
    val productId = insertTestProduct()
    insertTestItem(productId = productId)
    insertTestItem(productId = productId)

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onClearProductItems(productId, clearAll = true)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ItemsCleared>()
    }

    // Verify items were cleared
    val items = itemDataSource.getItemsByProduct(testCategoryId, productId).first()
    expectThat(items.size).isEqualTo(0)
  }

  @Test
  fun `onClearProductItems with clearAll false clears only consumed items`() = runTest {
    // Given
    val productId = insertTestProduct()
    val item1Id = insertTestItem(productId = productId)
    insertTestItem(productId = productId)

    // Mark one item as consumed
    itemsInMemory.value = itemsInMemory.value.map {
      if (it.id == item1Id) it.copy(consumedDate = now.toEpochMilliseconds()) else it
    }

    val viewModel = createViewModel(
      CategoryProductTabUiModel(
        id = productId,
        categoryId = testCategoryId,
        name = "Test Product",
      )
    )
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    viewModel.sideEffects.test {
      viewModel.onClearProductItems(productId, clearAll = false)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val sideEffect = awaitItem()
      expectThat(sideEffect).isA<ProductPageSideEffect.ItemsCleared>()
    }

    // Verify only consumed items were cleared (1 active item should remain)
    val items = itemDataSource.getItemsByProduct(testCategoryId, productId).first()
    expectThat(items.size).isEqualTo(1)
  }

  companion object {
    // Test data
    private val testCategoryId = "test-category"
    private val testProductId = "test-product"
    private val now = Instant.parse("2024-02-10T12:00:00Z")
  }
}
