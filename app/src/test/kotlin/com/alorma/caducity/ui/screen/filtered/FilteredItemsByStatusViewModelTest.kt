package com.alorma.caducity.ui.screen.filtered

import app.cash.turbine.test
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryProduct
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.usecase.GetItemsByStatusUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import java.util.UUID
import kotlin.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class FilteredItemsByStatusViewModelTest {
  private val testDispatcher = StandardTestDispatcher()

  // Mocks
  private val eventTracker: EventTracker = mock()

  // Fake data flow
  private val categoriesFlow = MutableStateFlow(persistentListOf<CategoryWithItems>())

  // Mock use case that returns our fake flow
  private val getItemsByStatusUseCase: GetItemsByStatusUseCase =
    mock {
      on { load(any()) } doReturn categoriesFlow
    }

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    categoriesFlow.value = persistentListOf()
  }

  private fun createViewModel(status: ItemStatus = ItemStatus.Expired): FilteredItemsByStatusViewModel =
    FilteredItemsByStatusViewModel(
      status = status,
      getItemsByStatusUseCase = getItemsByStatusUseCase,
      eventTracker = eventTracker,
    )

  private fun createTestCategory(
    id: String = UUID.randomUUID().toString(),
    name: String = "Test Category",
    description: String = "Test Description",
    products: List<CategoryProduct> = emptyList(),
    standaloneItems: List<Item> = emptyList(),
  ): CategoryWithItems =
    CategoryWithItems(
      category =
        Category(
          id = id,
          name = name,
          description = description,
        ),
      products = products.toImmutableList(),
      standaloneItems = standaloneItems.toImmutableList(),
    )

  private fun createTestItem(
    id: String = UUID.randomUUID().toString(),
    categoryId: String = UUID.randomUUID().toString(),
    productId: String? = null,
    identifier: String = "Test Item",
    expirationDate: Instant = Instant.parse("2024-02-15T00:00:00Z"),
    status: ItemStatus = ItemStatus.Expired,
  ): Item =
    Item(
      id = id,
      categoryId = categoryId,
      identifier = identifier,
      productId = productId,
      expirationDate = expirationDate,
      status = status,
      pausedDate = null,
    )

  private fun createTestProduct(
    id: String = UUID.randomUUID().toString(),
    categoryId: String,
    name: String = "Test Product",
  ): Product =
    Product(
      id = id,
      categoryId = categoryId,
      name = name,
      createdAt = now,
    )

  @Test
  fun `initial state is Loading`() =
    runTest {
      // When
      val viewModel = createViewModel()

      // Then
      expectThat(viewModel.state.value)
        .isA<FilteredItemsByStatusState.Loading>()
    }

  @Test
  fun `state updates to Empty when no items match status`() =
    runTest {
      // Given
      categoriesFlow.value = persistentListOf()

      // When
      val viewModel = createViewModel(ItemStatus.Expired)

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<FilteredItemsByStatusState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Empty state
        expectThat(awaitItem()).isA<FilteredItemsByStatusState.Empty>()
      }
    }

  @Test
  fun `state updates to Success with filtered expired items`() =
    runTest {
      // Given
      val categoryId = UUID.randomUUID().toString()
      val productId = UUID.randomUUID().toString()
      val product = createTestProduct(id = productId, categoryId = categoryId)

      val expiredItem1 =
        createTestItem(
          productId = productId,
          identifier = "Expired Item 1",
          expirationDate = Instant.parse("2024-02-05T00:00:00Z"),
          status = ItemStatus.Expired,
        )

      val expiredItem2 =
        createTestItem(
          productId = productId,
          identifier = "Expired Item 2",
          expirationDate = Instant.parse("2024-02-01T00:00:00Z"),
          status = ItemStatus.Expired,
        )

      val categoryWithItems =
        createTestCategory(
          id = categoryId,
          name = "Dairy",
          products =
            listOf(
              CategoryProduct(
                product = product,
                items = persistentListOf(expiredItem1, expiredItem2),
              ),
            ),
        )

      categoriesFlow.value = persistentListOf(categoryWithItems)

      // When
      val viewModel = createViewModel(ItemStatus.Expired)

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<FilteredItemsByStatusState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Success state
        val state = awaitItem()
        expectThat(state).isA<FilteredItemsByStatusState.Success>()
        val successState = state as FilteredItemsByStatusState.Success
        expectThat(successState.categories.size).isEqualTo(1)
        expectThat(successState.categories[0].category.name).isEqualTo("Dairy")
        expectThat(successState.categories[0].products.size).isEqualTo(1)
        expectThat(
          successState.categories[0]
            .products[0]
            .items.size,
        ).isEqualTo(2)
      }
    }

  @Test
  fun `state updates to Success with frozen items`() =
    runTest {
      // Given
      val categoryId = UUID.randomUUID().toString()
      val frozenItem =
        createTestItem(
          identifier = "Frozen Item",
          status = ItemStatus.Frozen,
        )

      val categoryWithItems =
        createTestCategory(
          id = categoryId,
          name = "Meats",
          standaloneItems = listOf(frozenItem),
        )

      categoriesFlow.value = persistentListOf(categoryWithItems)

      // When
      val viewModel = createViewModel(ItemStatus.Frozen)

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<FilteredItemsByStatusState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Success state
        val state = awaitItem()
        expectThat(state).isA<FilteredItemsByStatusState.Success>()
        val successState = state as FilteredItemsByStatusState.Success
        expectThat(successState.categories.size).isEqualTo(1)
        expectThat(successState.categories[0].standaloneItems.size).isEqualTo(1)
        expectThat(successState.categories[0].standaloneItems[0].status).isEqualTo(ItemStatus.Frozen)
      }
    }

  @Test
  fun `state updates to Success with multiple categories`() =
    runTest {
      // Given
      val category1Id = UUID.randomUUID().toString()
      val category2Id = UUID.randomUUID().toString()

      val expiredItem1 =
        createTestItem(
          identifier = "Expired Item 1",
          status = ItemStatus.Expired,
        )

      val expiredItem2 =
        createTestItem(
          identifier = "Expired Item 2",
          status = ItemStatus.Expired,
        )

      val category1 =
        createTestCategory(
          id = category1Id,
          name = "Dairy",
          standaloneItems = listOf(expiredItem1),
        )

      val category2 =
        createTestCategory(
          id = category2Id,
          name = "Produce",
          standaloneItems = listOf(expiredItem2),
        )

      categoriesFlow.value = persistentListOf(category1, category2)

      // When
      val viewModel = createViewModel(ItemStatus.Expired)

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<FilteredItemsByStatusState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Success state
        val state = awaitItem()
        expectThat(state).isA<FilteredItemsByStatusState.Success>()
        val successState = state as FilteredItemsByStatusState.Success
        expectThat(successState.categories.size).isEqualTo(2)
        expectThat(successState.categories[0].category.name).isEqualTo("Dairy")
        expectThat(successState.categories[1].category.name).isEqualTo("Produce")
      }
    }

  @Test
  fun `onProductClick emits bottom sheet side effect`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      val items =
        listOf(
          createTestItem(identifier = "Item 1"),
          createTestItem(identifier = "Item 2"),
        )

      // When
      viewModel.sideEffects.test {
        viewModel.onProductClick("Test Product", items)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val sideEffect = awaitItem()
        expectThat(sideEffect).isA<FilteredItemsByStatusSideEffect.ShowProductItemsBottomSheet>()
        val bottomSheetEffect = sideEffect as FilteredItemsByStatusSideEffect.ShowProductItemsBottomSheet
        expectThat(bottomSheetEffect.productName).isEqualTo("Test Product")
        expectThat(bottomSheetEffect.items.size).isEqualTo(2)
      }
    }

  @Test
  fun `onItemClick emits bottom sheet side effect`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      val item = createTestItem(identifier = "Test Item")

      // When
      viewModel.sideEffects.test {
        viewModel.onItemClick(item)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val sideEffect = awaitItem()
        expectThat(sideEffect).isA<FilteredItemsByStatusSideEffect.ShowItemActionsBottomSheet>()
        val bottomSheetEffect = sideEffect as FilteredItemsByStatusSideEffect.ShowItemActionsBottomSheet
        expectThat(bottomSheetEffect.item.identifier).isEqualTo("Test Item")
      }
    }

  @Test
  fun `navigate to Category emits navigation side effect with category ID`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      val categoryId = UUID.randomUUID().toString()
      testDispatcher.scheduler.advanceUntilIdle()

      // When
      viewModel.navigationSideEffects.test {
        viewModel.navigate(FilteredItemsNavigation.Category(categoryId, "list"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val sideEffect = awaitItem()
        expectThat(sideEffect).isA<FilteredItemsByStatusNavigationSideEffect.NavigateToCategory>()
        val navEffect = sideEffect as FilteredItemsByStatusNavigationSideEffect.NavigateToCategory
        expectThat(navEffect.categoryId).isEqualTo(categoryId)
      }
    }

  @Test
  fun `navigate to Category tracks action with source`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      val categoryId = UUID.randomUUID().toString()
      testDispatcher.scheduler.advanceUntilIdle()

      // When
      viewModel.navigate(FilteredItemsNavigation.Category(categoryId, "card"))
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      verify(eventTracker).trackAction(any())
    }

  companion object {
    private val now = Instant.parse("2024-02-10T12:00:00Z")
  }
}
