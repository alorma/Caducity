package com.alorma.caducity.ui.screen.dashboard

import app.cash.turbine.test
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.RelativeTimeFormatter
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryProduct
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import com.alorma.caducity.domain.model.ItemStatus
import com.alorma.caducity.domain.model.Product
import com.alorma.caducity.domain.usecase.ExpirationThresholds
import com.alorma.caducity.domain.usecase.ObtainDashboardUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.components.calendar.AppCalendarConfigMapper
import com.alorma.caducity.ui.components.calendar.CalendarConfigState
import java.util.UUID
import kotlin.time.Duration.Companion.days
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
import kotlinx.datetime.DayOfWeek
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
class DashboardViewModelTest {
  private val testDispatcher = StandardTestDispatcher()

  // Mocks - interfaces only
  private val appClock: AppClock =
    mock {
      on { now() } doReturn now
      on { nowDate() } doReturn nowDate
    }
  private val eventTracker: EventTracker = mock()
  private val expirationThresholds: ExpirationThresholds =
    mock {
      on { soonExpiringThreshold } doReturn 3.days
      on { consumeExpiredThreshold } doReturn 2.days
    }
  private val relativeTimeFormatter: RelativeTimeFormatter =
    mock {
      on { format(any(), any()) } doReturn "Today"
    }
  private val mockAppCalendarConfig: com.alorma.caducity.ui.components.calendar.AppCalendarConfig = mock()
  private val appCalendarConfigMapper: AppCalendarConfigMapper =
    mock {
      on { createWithDatedContent(any(), any(), any(), any()) } doReturn mockAppCalendarConfig
    }

  // Fake dependencies
  private val categoriesFlow = MutableStateFlow(persistentListOf<CategoryWithItems>())
  private val categoryDataSource: CategoryDataSource =
    mock {
      on { getCategories() } doReturn categoriesFlow
    }

  private val calendarPreferencesState =
    MutableStateFlow(
      CalendarConfigState(firstDayOfWeek = DayOfWeek.MONDAY),
    )
  private val calendarPreferences: com.alorma.caducity.ui.components.calendar.CalendarPreferences =
    mock {
      on { state } doReturn calendarPreferencesState
    }

  private val obtainDashboardUseCase =
    ObtainDashboardUseCase(
      appClock = appClock,
      categoryDataSource = categoryDataSource,
      expirationThresholds = expirationThresholds,
    )

  private val dashboardMapper =
    DashboardMapper(
      appClock = appClock,
      relativeTimeFormatter = relativeTimeFormatter,
      appCalendarConfigMapper = appCalendarConfigMapper,
    )

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    // Clear state
    categoriesFlow.value = persistentListOf()
  }

  private fun createViewModel(): DashboardViewModel =
    DashboardViewModel(
      calendarPreferences = calendarPreferences,
      obtainDashboardUseCase = obtainDashboardUseCase,
      dashboardMapper = dashboardMapper,
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
    status: ItemStatus = ItemStatus.Fresh,
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
        .isA<DashboardState.Loading>()
    }

  @Test
  fun `state updates to Success with empty categories`() =
    runTest {
      // Given
      categoriesFlow.value = persistentListOf()

      // When
      val viewModel = createViewModel()

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<DashboardState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Success state
        val state = awaitItem()
        expectThat(state).isA<DashboardState.Success.PerCategory>()
        val successState = state as DashboardState.Success.PerCategory
        expectThat(successState.categories.size).isEqualTo(0)
        expectThat(successState.summary.expired).isEqualTo(0)
        expectThat(successState.summary.expiringSoon).isEqualTo(0)
        expectThat(successState.summary.fresh).isEqualTo(0)
        expectThat(successState.summary.frozen).isEqualTo(0)
      }
    }

  @Test
  fun `state updates to Success with categories and items`() =
    runTest {
      // Given
      val categoryId = UUID.randomUUID().toString()
      val productId = UUID.randomUUID().toString()
      val product = createTestProduct(id = productId, categoryId = categoryId)

      val freshItem =
        createTestItem(
          productId = productId,
          identifier = "Fresh Item",
          expirationDate = Instant.parse("2024-02-20T00:00:00Z"),
          status = ItemStatus.Fresh,
        )

      val expiredItem =
        createTestItem(
          productId = productId,
          identifier = "Expired Item",
          expirationDate = Instant.parse("2024-02-05T00:00:00Z"),
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
                items = persistentListOf(freshItem, expiredItem),
              ),
            ),
        )

      categoriesFlow.value = persistentListOf(categoryWithItems)

      // When
      val viewModel = createViewModel()

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<DashboardState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Success state
        val state = awaitItem()
        expectThat(state).isA<DashboardState.Success.PerCategory>()
        val successState = state as DashboardState.Success.PerCategory

        // Verify categories
        expectThat(successState.categories.size).isEqualTo(1)
        expectThat(successState.categories[0].name).isEqualTo("Dairy")

        // Verify summary (includes both fresh and expired)
        expectThat(successState.summary.fresh).isEqualTo(1)
        expectThat(successState.summary.expired).isEqualTo(1)
      }
    }

  @Test
  fun `summary counts frozen items separately`() =
    runTest {
      // Given
      val categoryId = UUID.randomUUID().toString()
      val freshItem = createTestItem(status = ItemStatus.Fresh)
      val frozenItem = createTestItem(status = ItemStatus.Frozen)
      val expiredItem = createTestItem(status = ItemStatus.Expired)

      val categoryWithItems =
        createTestCategory(
          id = categoryId,
          standaloneItems = listOf(freshItem, frozenItem, expiredItem),
        )

      categoriesFlow.value = persistentListOf(categoryWithItems)

      // When
      val viewModel = createViewModel()

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<DashboardState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Success state
        val state = awaitItem()
        expectThat(state).isA<DashboardState.Success.PerCategory>()
        val successState = state as DashboardState.Success.PerCategory

        // Verify summary counts
        expectThat(successState.summary.fresh).isEqualTo(1)
        expectThat(successState.summary.frozen).isEqualTo(1)
        expectThat(successState.summary.expired).isEqualTo(1)
      }
    }

  @Test
  fun `summary excludes consumed items`() =
    runTest {
      // Given
      val categoryId = UUID.randomUUID().toString()
      val freshItem = createTestItem(status = ItemStatus.Fresh)
      val consumedItem = createTestItem(status = ItemStatus.Consumed)

      val categoryWithItems =
        createTestCategory(
          id = categoryId,
          standaloneItems = listOf(freshItem, consumedItem),
        )

      categoriesFlow.value = persistentListOf(categoryWithItems)

      // When
      val viewModel = createViewModel()

      // Then
      viewModel.state.test {
        // Skip initial Loading state
        expectThat(awaitItem()).isA<DashboardState.Loading>()

        // Advance dispatcher
        testDispatcher.scheduler.advanceUntilIdle()

        // Check Success state
        val state = awaitItem()
        expectThat(state).isA<DashboardState.Success.PerCategory>()
        val successState = state as DashboardState.Success.PerCategory

        // Verify summary excludes consumed
        expectThat(successState.summary.fresh).isEqualTo(1)
        expectThat(successState.summary.expired).isEqualTo(0)
        expectThat(successState.summary.frozen).isEqualTo(0)
      }
    }

  @Test
  fun `navigate to CreateCategory emits navigation side effect`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      // When
      viewModel.navigationSideEffects.test {
        viewModel.navigate(DashboardNavigation.CreateCategory)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val sideEffect = awaitItem()
        expectThat(sideEffect).isA<DashboardNavigationSideEffect.NavigateToCreateCategory>()
      }
    }

  @Test
  fun `navigate to CreateCategory tracks action`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      // When
      viewModel.navigate(DashboardNavigation.CreateCategory)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      verify(eventTracker).trackAction(any())
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
        viewModel.navigate(DashboardNavigation.Category(categoryId, "calendar"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val sideEffect = awaitItem()
        expectThat(sideEffect).isA<DashboardNavigationSideEffect.NavigateToCategory>()
        val navEffect = sideEffect as DashboardNavigationSideEffect.NavigateToCategory
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
      viewModel.navigate(DashboardNavigation.Category(categoryId, "summary"))
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      verify(eventTracker).trackAction(any())
    }

  @Test
  fun `navigate to FilteredItems emits navigation side effect with status`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      // When
      viewModel.navigationSideEffects.test {
        viewModel.navigate(DashboardNavigation.FilteredItems(ItemStatus.Expired))
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val sideEffect = awaitItem()
        expectThat(sideEffect).isA<DashboardNavigationSideEffect.NavigateToFilteredItems>()
        val navEffect = sideEffect as DashboardNavigationSideEffect.NavigateToFilteredItems
        expectThat(navEffect.status).isEqualTo(ItemStatus.Expired)
      }
    }

  @Test
  fun `navigate to FilteredItems tracks action with correct status param`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      // When - Test each status
      viewModel.navigate(DashboardNavigation.FilteredItems(ItemStatus.Expired))
      viewModel.navigate(DashboardNavigation.FilteredItems(ItemStatus.ExpiringSoon))
      viewModel.navigate(DashboardNavigation.FilteredItems(ItemStatus.Fresh))
      viewModel.navigate(DashboardNavigation.FilteredItems(ItemStatus.Frozen))
      testDispatcher.scheduler.advanceUntilIdle()

      // Then - Should have tracked 4 actions
      verify(eventTracker, org.mockito.kotlin.times(4)).trackAction(any())
    }

  @Test
  fun `navigate to Settings emits navigation side effect`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      // When
      viewModel.navigationSideEffects.test {
        viewModel.navigate(DashboardNavigation.Settings)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val sideEffect = awaitItem()
        expectThat(sideEffect).isA<DashboardNavigationSideEffect.NavigateToSettings>()
      }
    }

  @Test
  fun `navigate to Settings tracks action`() =
    runTest {
      // Given
      val viewModel = createViewModel()
      testDispatcher.scheduler.advanceUntilIdle()

      // When
      viewModel.navigate(DashboardNavigation.Settings)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      verify(eventTracker).trackAction(any())
    }

  companion object {
    private val now = Instant.parse("2024-02-10T12:00:00Z")
    private val nowDate = kotlinx.datetime.LocalDate(2024, 2, 10)
  }
}
