package com.alorma.caducity.ui.screen.pantry

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.TodayProvider
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomItemDataSource
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.ObservePantryUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
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
  private lateinit var addProduct: AddProductUseCase
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

  private fun viewModel(): PantryViewModel {
    val thresholds = AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings()))
    val items = ObserveItemsUseCase(pantry, thresholds, todayProvider)
    val itemSource = RoomItemDataSource(db.itemDao(), clock, ItemRoomMapper(clock, thresholds))
    return PantryViewModel(
      observePantry = ObservePantryUseCase(items, pantry, todayProvider),
      eventTracker = EventTracker(emptyList()),
      consumeItem = ConsumeItemUseCase(itemSource, pantry, clock),
      freezeItem = FreezeItemUseCase(itemSource, pantry, clock),
      unfreezeItem = UnfreezeItemUseCase(itemSource, pantry, clock),
      discardItems = DiscardItemsUseCase(pantry, clock),
      restoreItems = RestoreItemsUseCase(pantry),
      snackbars = GlobalSnackbarEvents(),
    ).also { created += it }
  }

  private suspend fun PantryViewModel.ready(predicate: (PantryState.Ready) -> Boolean = { true }): PantryState.Ready =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { state.first { it is PantryState.Ready && predicate(it) } as PantryState.Ready }
    }

  @Test
  fun `categories follow sortOrder and carry product and expired counts`() =
    runTest {
      category("pantry", sortOrder = 1)
      category("dairy", sortOrder = 0)
      add("Milk", "dairy", days = 5, quantity = 3)
      add("Old milk", "dairy", days = -2)

      val data = viewModel().ready().data

      expectThat(data.categories.map { it.category.id }).containsExactly("dairy", "pantry")
      val dairy = data.categories.first()
      expectThat(dairy.productCount).isEqualTo(2)
      expectThat(dairy.expiredCount).isEqualTo(1)
      expectThat(data.totalProducts).isEqualTo(2)
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
      // The query lands before its results, so wait for the ones of this query, not the previous ones.
      val byProduct = vm.ready { it.query == "pérnil" && it.results.any { result -> result.group.name == "Pernil" } }
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
