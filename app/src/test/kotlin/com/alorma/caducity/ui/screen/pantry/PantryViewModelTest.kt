package com.alorma.caducity.ui.screen.pantry

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.ObservePantryUseCase
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
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PantryViewModelTest {
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
  private lateinit var addBatch: AddItemBatchUseCase
  private val created = mutableListOf<PantryViewModel>()

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
    addBatch = AddItemBatchUseCase(pantry, clock)
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
  ) = addBatch.add(name, categoryId, inDays(days), quantity, false).getOrThrow()

  private fun viewModel(): PantryViewModel {
    val items =
      ObserveItemsUseCase(pantry, AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings())), todayProvider)
    return PantryViewModel(
      observePantry = ObservePantryUseCase(items, pantry, todayProvider),
      eventTracker = EventTracker(emptyList()),
    ).also { created += it }
  }

  private suspend fun PantryViewModel.ready(predicate: (PantryState.Ready) -> Boolean = { true }): PantryState.Ready =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { state.first { it is PantryState.Ready && predicate(it) } as PantryState.Ready }
    }

  @Test
  fun `categories follow sortOrder and carry unit and expired counts`() =
    runTest {
      category("pantry", sortOrder = 1)
      category("dairy", sortOrder = 0)
      add("Milk", "dairy", days = 5, quantity = 3)
      add("Old milk", "dairy", days = -2)

      val data = viewModel().ready().data

      expectThat(data.categories.map { it.category.id }).containsExactly("dairy", "pantry")
      val dairy = data.categories.first()
      expectThat(dairy.unitCount).isEqualTo(4)
      expectThat(dairy.expiredCount).isEqualTo(1)
      expectThat(data.totalUnits).isEqualTo(4)
    }

  @Test
  fun `search ignores case and accents and matches product or category name`() =
    runTest {
      category("dairy", name = "Làctics")
      category("meat", name = "Carn")
      add("Iogurt", "dairy", days = 3)
      add("Pernil", "meat", days = 3)
      val vm = viewModel()

      vm.onQueryChange("LACTICS")
      val byCategory = vm.ready { it.query == "LACTICS" && it.results.isNotEmpty() }
      expectThat(byCategory.results.map { it.group.name }).containsExactly("Iogurt")
      expectThat(byCategory.isSearching).isTrue()

      vm.onQueryChange("pérnil")
      val byProduct = vm.ready { it.query == "pérnil" && it.results.isNotEmpty() }
      expectThat(byProduct.results.map { it.group.name }).containsExactly("Pernil")
    }

  @Test
  fun `a blank query is not a search and returns no results`() =
    runTest {
      category("dairy")
      add("Milk", "dairy", days = 3)
      val vm = viewModel()

      vm.onQueryChange("   ")
      val state = vm.ready { it.query == "   " }

      expectThat(state.isSearching).isFalse()
      expectThat(state.results).isEmpty()
    }

  @Test
  fun `search only finds active units`() =
    runTest {
      category("dairy")
      add("Milk", "dairy", days = 3)
      val vm = viewModel()

      vm.onQueryChange("nothing like this")
      expectThat(vm.ready { it.query == "nothing like this" }.results).isEmpty()
    }
}
