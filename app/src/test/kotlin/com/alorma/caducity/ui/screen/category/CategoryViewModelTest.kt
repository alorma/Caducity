package com.alorma.caducity.ui.screen.category

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomCategoryDataSource
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveCalendarUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CategoryViewModelTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()
  private val todayProvider =
    object : TodayProvider {
      override fun today(): LocalDate = today

      override fun observe(): Flow<LocalDate> = flowOf(today)
    }

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var addProduct: AddProductUseCase
  private lateinit var categoryDataSource: RoomCategoryDataSource
  private val created = mutableListOf<CategoryViewModel>()

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val itemMapper = ItemRoomMapper(clock, AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings())))
    pantry =
      RoomPantryDataSource(
        database = db,
        categoryDao = db.categoryDao(),
        itemDao = db.itemDao(),
        productDao = db.productDao(),
        categoryMapper = CategoryRoomMapper(ProductRoomMapper(), itemMapper),
        appClock = clock,
      )
    categoryDataSource =
      RoomCategoryDataSource(
        db.categoryDao(),
        db.itemDao(),
        CategoryRoomMapper(ProductRoomMapper(), itemMapper),
        itemMapper,
      )
    addProduct = AddProductUseCase(pantry, clock)
  }

  @After
  fun tearDown() {
    created.forEach { it.viewModelScope.cancel() }
    db.close()
    Dispatchers.resetMain()
  }

  private suspend fun category(
    id: String,
    name: String = id,
    sortOrder: Int = 0,
  ) = db.categoryDao().insertCategory(CategoryRoomEntity(id, name, "", sortOrder = sortOrder))

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun add(
    name: String,
    categoryId: String,
    days: Int,
    quantity: Int = 1,
  ) = addProduct.add(name, categoryId, inDays(days), quantity, false).getOrThrow()

  private fun viewModel(
    categoryId: String = "dairy",
    filter: ExpiryStatus? = null,
  ): CategoryViewModel {
    val items =
      ObserveItemsUseCase(pantry, AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings())), todayProvider)
    return CategoryViewModel(
      args = CategoryArgs(categoryId, filter),
      observeCategory = ObserveCategoryUseCase(items, pantry, todayProvider),
      observeWeekStrip = ObserveWeekStripUseCase(ObserveCalendarUseCase(items), todayProvider),
      deleteCategory = DeleteCategoryUseCase(categoryDataSource),
      eventTracker = EventTracker(emptyList()),
    ).also { created += it }
  }

  private suspend fun CategoryViewModel.ready(
    predicate: (CategoryState.Ready) -> Boolean = {
      true
    },
  ): CategoryState.Ready =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { state.first { it is CategoryState.Ready && predicate(it) } as CategoryState.Ready }
    }

  @Test
  fun `sections follow Expired Soon Fresh Frozen and hide empty ones`() =
    runTest {
      category("dairy")
      add("Old", "dairy", days = -2)
      add("Cheese", "dairy", days = 30)
      add("Milk", "dairy", days = 2)

      val state = viewModel().ready()

      expectThat(state.data.sections.map { it.status })
        .containsExactly(ExpiryStatus.EXPIRED, ExpiryStatus.SOON, ExpiryStatus.FRESH)
      expectThat(state.visibleSections.size).isEqualTo(3)
    }

  @Test
  fun `the filter argument shows only that status and chips can change it`() =
    runTest {
      category("dairy")
      add("Old", "dairy", days = -2)
      add("Milk", "dairy", days = 2)
      val vm = viewModel(filter = ExpiryStatus.SOON)

      expectThat(vm.ready().visibleSections.map { it.status }).containsExactly(ExpiryStatus.SOON)

      vm.onFilterSelected(null)
      expectThat(vm.ready { it.filter == null }.visibleSections.size).isEqualTo(2)
    }

  @Test
  fun `one unit is a single row and several units form one expandable group`() =
    runTest {
      category("dairy")
      add("Eggs", "dairy", days = 10)
      add("Milk", "dairy", days = 10, quantity = 3)

      val groups =
        viewModel()
          .ready()
          .data.sections
          .single()
          .groups

      expectThat(groups.single { it.name == "Eggs" }.isSingle).isTrue()
      val milk = groups.single { it.name == "Milk" }
      expectThat(milk.isSingle).isFalse()
      expectThat(milk.unitCount).isEqualTo(3)
    }

  @Test
  fun `only the units of this category are shown and the week strip is filtered`() =
    runTest {
      category("dairy")
      category("meat")
      add("Milk", "dairy", days = 2)
      add("Ham", "meat", days = 2)

      val state = viewModel().ready()

      expectThat(state.data.unitCount).isEqualTo(1)
      expectThat(
        state.weekStrip.statusesByDate[inDays(2)]
          .orEmpty()
          .size,
      ).isEqualTo(1)
    }

  @Test
  fun `a selected day lists only the units that expire that day with their position`() =
    runTest {
      category("dairy")
      add("Milk", "dairy", days = 2)
      add("Milk", "dairy", days = 2)
      add("Milk", "dairy", days = 5)
      add("Eggs", "dairy", days = 5)
      val vm = viewModel()

      vm.onDayClick(inDays(2))
      val state = vm.ready { it.selectedDay == inDays(2) }

      expectThat(state.dayUnits.map { it.name }).containsExactly("Milk", "Milk")
      expectThat(state.dayUnits.map { it.position.index }).containsExactly(1, 2)
      expectThat(state.dayUnits.map { it.position.count }).containsExactly(3, 3)
    }

  @Test
  fun `a day without units is empty and tapping the day again clears it`() =
    runTest {
      category("dairy")
      add("Milk", "dairy", days = 2)
      val vm = viewModel()

      vm.onDayClick(inDays(4))
      expectThat(vm.ready { it.selectedDay == inDays(4) }.dayUnits).isEmpty()

      vm.onDayClick(inDays(4))
      expectThat(vm.ready { it.selectedDay == null }.selectedDay).isNull()
    }

  @Test
  fun `a missing category is reported as not found`() =
    runTest {
      val vm = viewModel(categoryId = "ghost")

      withContext(Dispatchers.Default) { withTimeout(5_000) { vm.state.first { it is CategoryState.NotFound } } }
    }

  @Test
  fun `confirming the delete removes the category and its units and closes the screen`() =
    runTest {
      category("dairy")
      add("Milk", "dairy", days = 2)
      val vm = viewModel()
      vm.ready()

      vm.onDeleteConfirmed()

      withContext(Dispatchers.Default) { withTimeout(5_000) { vm.state.first { it is CategoryState.NotFound } } }
      expectThat(vm.navigationSideEffects.first()).isEqualTo(CategoryNavigationSideEffect.Close)
    }
}
