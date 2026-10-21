package com.alorma.caducity.ui.screen.home

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.R
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
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveCalendarUseCase
import com.alorma.caducity.domain.usecase.ObserveHomeUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.ObserveWeekStripUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HomeViewModelTest {
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
  private lateinit var itemDataSource: RoomItemDataSource
  private lateinit var addProduct: AddProductUseCase
  private lateinit var thresholdPreferences: SoonThresholdPreferences
  private val created = mutableListOf<HomeViewModel>()

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    thresholdPreferences = SoonThresholdPreferences(MapSettings())
    val itemMapper = ItemRoomMapper(clock, AppExpirationThresholdsImpl(thresholdPreferences))
    itemDataSource = RoomItemDataSource(db.itemDao(), clock, itemMapper)
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
    sortOrder: Int = 0,
  ) = db.categoryDao().insertCategory(CategoryRoomEntity(id, id, "", sortOrder = sortOrder))

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun add(
    name: String,
    days: Int,
    quantity: Int = 1,
    frozen: Boolean = false,
  ): List<String> = addProduct.add(name, "dairy", inDays(days), quantity, frozen).getOrThrow().itemIds

  private suspend fun peanuts(level: HandfulLevel): List<String> =
    addProduct
      .add("Peanuts", "dairy", inDays(1), 1, false, UnitType.HANDFULS, level = level)
      .getOrThrow()
      .itemIds

  private fun viewModel(): HomeViewModel {
    val thresholds = AppExpirationThresholdsImpl(thresholdPreferences)
    val items = ObserveItemsUseCase(pantry, thresholds, todayProvider)
    return HomeViewModel(
      observeHome = ObserveHomeUseCase(items, pantry, todayProvider),
      observeWeekStrip = ObserveWeekStripUseCase(ObserveCalendarUseCase(items), todayProvider),
      consumeItem = ConsumeItemUseCase(itemDataSource, pantry, clock),
      freezeItem = FreezeItemUseCase(itemDataSource, pantry, clock),
      restoreItems = RestoreItemsUseCase(pantry),
      eventTracker = EventTracker(emptyList()),
    ).also { created += it }
  }

  private suspend fun HomeViewModel.ready(predicate: (HomeState.Ready) -> Boolean = { true }): HomeState.Ready =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { state.first { it is HomeState.Ready && predicate(it) } as HomeState.Ready }
    }

  @Test
  fun `the check of a handful row lowers its level and says what is left, and undo gives it back`() =
    runTest {
      category("dairy")
      val id = peanuts(HandfulLevel.LOTS).single()
      val vm = viewModel()
      val row =
        vm
          .ready()
          .data.sections
          .single()
          .rows
          .single()
      expectThat(row.level).isEqualTo(HandfulLevel.LOTS)

      vm.onConsume(row)
      val snackbar =
        withContext(Dispatchers.Default) {
          withTimeout(5_000) { vm.sideEffects.first() as HomeSideEffect.ShowSnackbar }.request
        }

      expectThat(
        db
          .itemDao()
          .getAllItemsSync()
          .single()
          .level,
      ).isEqualTo(HandfulLevel.HALF)
      expectThat(
        db
          .itemDao()
          .getAllItemsSync()
          .single()
          .consumedDate,
      ).isNull()
      expectThat(snackbar.message).isEqualTo(R.string.handful_snackbar_half)
      expectThat(snackbar.messageArgs).containsExactly("Peanuts")

      snackbar.onAction!!.invoke()
      expectThat(
        db
          .itemDao()
          .getAllItemsSync()
          .single()
          .level,
      ).isEqualTo(HandfulLevel.LOTS)
      expectThat(id).isEqualTo(row.nearestItemId)
    }

  @Test
  fun `the check of a handful at the lowest level consumes it`() =
    runTest {
      category("dairy")
      peanuts(HandfulLevel.LITTLE)
      val vm = viewModel()
      val row =
        vm
          .ready()
          .data.sections
          .single()
          .rows
          .single()

      vm.onConsume(row)
      val snackbar =
        withContext(Dispatchers.Default) {
          withTimeout(5_000) { vm.sideEffects.first() as HomeSideEffect.ShowSnackbar }.request
        }

      expectThat(
        db
          .itemDao()
          .getAllItemsSync()
          .single()
          .consumedDate,
      ).isNotNull()
      expectThat(snackbar.message).isEqualTo(R.string.handful_snackbar_consumed)
    }

  @Test
  fun `units of the same product and day share one row and count as one product`() =
    runTest {
      category("dairy")
      add("Milk", days = 1, quantity = 3)
      add("Eggs", days = 1)
      val vm = viewModel()

      val data = vm.ready().data

      expectThat(data.sections).hasSize(1)
      val section = data.sections.single()
      expectThat(section.rows).hasSize(2)
      expectThat(section.productCount).isEqualTo(2)
      expectThat(section.rows.first { it.name == "Milk" }.unitCount).isEqualTo(3)
      expectThat(data.soonCount).isEqualTo(2)
    }

  @Test
  fun `expired units feed the banner and never appear in the sections`() =
    runTest {
      category("dairy")
      add("Old milk", days = -5)
      add("Older milk", days = -2)
      add("Fresh milk", days = 2)
      val vm = viewModel()

      val data = vm.ready().data

      expectThat(data.expiredCount).isEqualTo(2)
      expectThat(data.oldestExpiredDaysAgo).isEqualTo(5)
      expectThat(data.sections.flatMap { it.rows }.map { it.name }).containsExactly("Fresh milk")
    }

  @Test
  fun `the soon threshold decides which units get a section`() =
    runTest {
      category("dairy")
      add("Milk", days = 5)

      thresholdPreferences.setThresholdDays(3)
      expectThat(viewModel().ready().data.sections).isEmpty()

      thresholdPreferences.setThresholdDays(7)
      val vm = viewModel()
      expectThat(vm.ready { it.data.sections.isNotEmpty() }.data.soonCount).isEqualTo(1)
    }

  @Test
  fun `nothing expires soon when the pantry only has fresh and frozen units`() =
    runTest {
      category("dairy")
      add("Milk", days = 30)
      add("Peas", days = 10, frozen = true)

      val data = viewModel().ready().data

      expectThat(data.nothingExpiresSoon).isTrue()
      expectThat(data.isPantryEmpty).isFalse()
      expectThat(data.freshCount).isEqualTo(1)
      expectThat(data.frozenCount).isEqualTo(1)
    }

  @Test
  fun `empty pantry is reported as such`() =
    runTest {
      category("dairy")

      val data = viewModel().ready().data

      expectThat(data.isPantryEmpty).isTrue()
      expectThat(data.nothingExpiresSoon).isFalse()
    }

  @Test
  fun `check consumes only the closest unit of the row and undo brings it back`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = 1, quantity = 3)
      val vm = viewModel()
      val row =
        vm
          .ready()
          .data.sections
          .single()
          .rows
          .single()

      vm.onConsume(row)
      val snackbar =
        withContext(Dispatchers.Default) {
          withTimeout(5_000) { vm.sideEffects.first() as HomeSideEffect.ShowSnackbar }.request
        }

      val consumed = db.itemDao().getAllItemsSync().filter { it.consumedDate != null }
      expectThat(consumed).hasSize(1)
      expectThat(consumed.single().id).isEqualTo(row.nearestItemId)
      expectThat(ids).contains(row.nearestItemId)
      expectThat(snackbar.messageArgs).containsExactly("Milk")
      val remaining =
        vm
          .ready {
            it.data.sections
              .single()
              .rows
              .single()
              .unitCount == 2
          }.data
      expectThat(
        remaining.sections
          .single()
          .rows
          .single()
          .unitCount,
      ).isEqualTo(2)

      snackbar.onAction!!.invoke()
      expectThat(db.itemDao().getAllItemsSync().count { it.consumedDate != null }).isEqualTo(0)
    }

  @Test
  fun `freeze freezes only the closest unit of the row and undo brings it back unfrozen`() =
    runTest {
      category("dairy")
      add("Milk", days = 1, quantity = 3)
      val vm = viewModel()
      val row =
        vm
          .ready()
          .data.sections
          .single()
          .rows
          .single()

      vm.onFreeze(row)
      val snackbar =
        withContext(Dispatchers.Default) {
          withTimeout(5_000) { vm.sideEffects.first() as HomeSideEffect.ShowSnackbar }.request
        }

      val frozen = db.itemDao().getAllItemsSync().filter { it.pausedDate != null }
      expectThat(frozen.map { it.id }).containsExactly(row.nearestItemId)
      expectThat(snackbar.messageArgs).containsExactly("Milk")

      snackbar.onAction!!.invoke()
      expectThat(db.itemDao().getAllItemsSync().count { it.pausedDate != null }).isEqualTo(0)
    }

  @Test
  fun `navigation intents become side effects`() =
    runTest {
      val vm = viewModel()

      vm.navigate(HomeNavigation.Calendar(date = inDays(3), source = "week_strip"))
      vm.navigate(HomeNavigation.StatusList(ExpiryStatus.EXPIRED, "banner"))
      vm.navigate(HomeNavigation.ItemActions(listOf("abc")))

      val effects = vm.navigationSideEffects.take(3).toList()
      expectThat(effects).containsExactly(
        HomeNavigationSideEffect.OpenCalendar(inDays(3)),
        HomeNavigationSideEffect.OpenStatusList(ExpiryStatus.EXPIRED),
        HomeNavigationSideEffect.OpenItemActions(listOf("abc")),
      )
    }

  @Test
  fun `every summary cell opens the list of its status`() =
    runTest {
      val vm = viewModel()
      val statuses = listOf(ExpiryStatus.SOON, ExpiryStatus.FRESH, ExpiryStatus.FROZEN)

      statuses.forEach { vm.navigate(HomeNavigation.StatusList(it, "summary")) }

      expectThat(vm.navigationSideEffects.take(3).toList())
        .containsExactly(statuses.map { HomeNavigationSideEffect.OpenStatusList(it) })
    }
}
