package com.alorma.caducity.ui.screen.calendar

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.calendar.FirstDayOfWeekPreferences
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
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveCalendarUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
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
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CalendarViewModelTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()
  private val currentDay = MutableStateFlow(today)
  private val todayProvider =
    object : TodayProvider {
      override fun today(): LocalDate = currentDay.value

      override fun observe(): Flow<LocalDate> = currentDay
    }

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var addProduct: AddProductUseCase
  private lateinit var items: ObserveItemsUseCase
  private lateinit var itemSource: RoomItemDataSource
  private val firstDay = FirstDayOfWeekPreferences(MapSettings())
  private val created = mutableListOf<CalendarViewModel>()

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val thresholds = AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings()))
    val itemMapper = ItemRoomMapper(clock, thresholds)
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
    items = ObserveItemsUseCase(pantry, thresholds, todayProvider)
    itemSource = RoomItemDataSource(db.itemDao(), clock, itemMapper)
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
    categoryId: String = "dairy",
    quantity: Int = 1,
  ) = addProduct.add(name, categoryId, inDays(days), quantity, false).getOrThrow()

  private fun viewModel(args: CalendarArgs = CalendarArgs()) =
    CalendarViewModel(
      args = args,
      observeCalendar = ObserveCalendarUseCase(items),
      observeCategories = ObserveCategoriesUseCase(pantry),
      firstDayOfWeekPreferences = firstDay,
      todayProvider = todayProvider,
      eventTracker = EventTracker(emptyList()),
      consumeItem = ConsumeItemUseCase(itemSource, pantry, clock),
      freezeItem = FreezeItemUseCase(itemSource, pantry, clock),
      discardItems = DiscardItemsUseCase(pantry, clock),
      restoreItems = RestoreItemsUseCase(pantry),
    ).also { created += it }

  private suspend fun CalendarViewModel.ready(predicate: (CalendarState.Ready) -> Boolean = { true }) =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { state.first { it is CalendarState.Ready && predicate(it) } as CalendarState.Ready }
    }

  @Test
  fun `opens on today in its month with today's units`() =
    runTest {
      category("dairy")
      add("Milk", days = 0, quantity = 2)
      add("Cheese", days = 3)
      val vm = viewModel()

      val state = vm.ready()

      expectThat(state.selected).isEqualTo(today)
      expectThat(state.month.first).isEqualTo(LocalDate(2026, 10, 1))
      // Two units of the same product on the day make one row; the calendar is not filtered, so it shows the icon.
      expectThat(state.rows.map { it.name }).containsExactly("Milk")
      expectThat(state.rows.single().unitCount).isEqualTo(2)
      val category = state.rows.single().category
      expectThat(category?.id).isEqualTo("dairy")
    }

  @Test
  fun `consuming a row consumes its nearest unit only`() =
    runTest {
      category("dairy")
      add("Milk", days = 0, quantity = 2)
      val vm = viewModel()
      val before = vm.ready { it.rows.isNotEmpty() }.rows.single()

      vm.onConsume(before)

      val after = vm.ready { it.rows.single().unitCount == 1 }.rows.single()
      expectThat(after.itemIds).containsExactly(before.itemIds.last())
    }

  @Test
  fun `a date argument selects that day and its month`() =
    runTest {
      category("dairy")
      add("Later", days = 30)
      val vm = viewModel(CalendarArgs(date = inDays(30)))

      val state = vm.ready { it.rows.isNotEmpty() }

      expectThat(state.selected).isEqualTo(inDays(30))
      expectThat(state.month.first).isEqualTo(LocalDate(2026, 11, 1))
    }

  @Test
  fun `past days show expired units as dots`() =
    runTest {
      category("dairy")
      add("Old", days = -3)
      val vm = viewModel()

      val state = vm.ready { it.statusesByDate.isNotEmpty() }

      expectThat(state.statusesByDate.getValue(inDays(-3))).containsExactly(ExpiryStatus.EXPIRED)
    }

  @Test
  fun `filtering by category only keeps its units and names it`() =
    runTest {
      category("dairy")
      category("fruit", sortOrder = 1)
      add("Milk", days = 1, categoryId = "dairy")
      add("Apple", days = 1, categoryId = "fruit")
      val vm = viewModel(CalendarArgs(categoryId = "fruit", date = inDays(1)))

      val state = vm.ready { it.rows.isNotEmpty() }

      expectThat(state.rows.map { it.name }).containsExactly("Apple")
      expectThat(state.categoryName).isEqualTo("fruit")
      // Filtered by one category: the rows carry no category icon.
      expectThat(state.rows.single().category).isEqualTo(null)
    }

  @Test
  fun `the grid follows the day change at midnight`() =
    runTest {
      category("dairy")
      add("Milk", days = 0)
      val vm = viewModel()
      val before = vm.ready()
      expectThat(before.today).isEqualTo(today)
      expectThat(before.statusesByDate.getValue(today)).containsExactly(ExpiryStatus.SOON)

      currentDay.value = today.plus(1, DateTimeUnit.DAY)

      val after = vm.ready { it.today != today }
      expectThat(after.today).isEqualTo(today.plus(1, DateTimeUnit.DAY))
      expectThat(after.statusesByDate.getValue(today)).containsExactly(ExpiryStatus.EXPIRED)
    }

  @Test
  fun `changing month selects its first day, or today when it is the current month`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.ready()

      vm.onMonthShown(LocalDate(2026, 11, 1))
      val next = vm.ready { it.month.first == LocalDate(2026, 11, 1) }
      expectThat(next.selected).isEqualTo(LocalDate(2026, 11, 1))

      vm.onMonthShown(LocalDate(2026, 10, 1))
      val back = vm.ready { it.month.first == LocalDate(2026, 10, 1) }
      expectThat(back.selected).isEqualTo(today)
    }

  @Test
  fun `the first day of the week setting changes the grid`() =
    runTest {
      category("dairy")
      firstDay.setFirstDayOfWeek(DayOfWeek.SUNDAY)
      val vm = viewModel()

      expectThat(vm.ready().month.firstDayOfWeek).isEqualTo(DayOfWeek.SUNDAY)
    }

  @Test
  fun `selecting a day lists its units`() =
    runTest {
      category("dairy")
      add("Soon", days = 2)
      val vm = viewModel()
      vm.ready()

      vm.onDayClick(inDays(2))

      expectThat(
        vm
          .ready { it.selected == inDays(2) && it.rows.isNotEmpty() }
          .rows
          .single()
          .name,
      ).isEqualTo("Soon")
    }

  @Test
  fun `grid dots and the day list come from the same products for every day, today included`() =
    runTest {
      category("dairy")
      add("Past", days = -2)
      add("Today", days = 0, quantity = 2)
      add("Tomorrow", days = 1)
      add("Later", days = 9, quantity = 3)
      val vm = viewModel()
      val month = vm.ready { it.statusesByDate.isNotEmpty() }

      month.statusesByDate.forEach { (date, statuses) ->
        vm.onDayClick(date)
        val day = vm.ready { it.selected == date }
        expectThat(day.rows.size).isEqualTo(statuses.size)
      }
      expectThat(month.statusesByDate.getValue(today)).hasSize(1)
    }
}
