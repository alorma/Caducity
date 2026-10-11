package com.alorma.caducity.ui.screen.status

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
import com.alorma.caducity.domain.model.ExpiryStatus
import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsByStatusUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
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
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class StatusListViewModelTest {
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
  private lateinit var addBatch: AddItemBatchUseCase
  private lateinit var thresholdPreferences: SoonThresholdPreferences
  private val snackbars = GlobalSnackbarEvents()
  private val created = mutableListOf<StatusListViewModel>()

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
    sortOrder: Int = 0,
  ) = db.categoryDao().insertCategory(CategoryRoomEntity(id, id, "", sortOrder = sortOrder))

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun add(
    name: String,
    days: Int,
    quantity: Int = 1,
    frozen: Boolean = false,
  ): List<String> = addBatch.add(name, "dairy", inDays(days), quantity, frozen).getOrThrow().itemIds

  private fun viewModel(status: ExpiryStatus): StatusListViewModel {
    val thresholds = AppExpirationThresholdsImpl(thresholdPreferences)
    val items = ObserveItemsUseCase(pantry, thresholds, todayProvider)
    return StatusListViewModel(
      status = status,
      observeByStatus = ObserveItemsByStatusUseCase(items, pantry, todayProvider),
      consumeItem = ConsumeItemUseCase(itemDataSource, pantry, clock),
      freezeItem = FreezeItemUseCase(itemDataSource, pantry, clock),
      unfreezeItem = UnfreezeItemUseCase(itemDataSource, pantry, clock),
      rescheduleItem = RescheduleItemUseCase(itemDataSource, pantry),
      discardItems = DiscardItemsUseCase(pantry, clock),
      restoreItems = RestoreItemsUseCase(pantry),
      snackbars = snackbars,
      eventTracker = EventTracker(emptyList()),
    ).also { created += it }
  }

  private suspend fun StatusListViewModel.ready(
    predicate: (StatusListState.Ready) -> Boolean = { true },
  ): StatusListState.Ready =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { state.first { it is StatusListState.Ready && predicate(it) } as StatusListState.Ready }
    }

  private suspend fun nextSnackbar() =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { snackbars.events.first() }
    }

  private suspend fun StatusListViewModel.runUndo() = nextSnackbar().onAction!!.invoke()

  @Test
  fun `rows join the units of a product that share a date and count units`() =
    runTest {
      category("dairy")
      add("Milk", days = -2, quantity = 3)
      add("Eggs", days = -2)
      val ready = viewModel(ExpiryStatus.EXPIRED).ready()

      expectThat(ready.totalCount).isEqualTo(4)
      val rows = ready.sections.single().rows
      expectThat(rows).hasSize(2)
      expectThat(rows.first { it.name == "Milk" }.unitCount).isEqualTo(3)
    }

  @Test
  fun `consume acts on one unit and undo puts it back`() =
    runTest {
      category("dairy")
      add("Milk", days = 1, quantity = 3)
      val vm = viewModel(ExpiryStatus.SOON)
      val row =
        vm
          .ready()
          .sections
          .single()
          .rows
          .single()

      vm.onConsume(row)
      val snackbar = nextSnackbar()
      expectThat(db.itemDao().getAllItemsSync().count { it.consumedDate != null }).isEqualTo(1)
      expectThat(snackbar.messageArgs).containsExactly("Milk")
      vm.ready { it.totalCount == 2 }

      snackbar.onAction!!.invoke()
      expectThat(db.itemDao().getAllItemsSync().count { it.consumedDate != null }).isEqualTo(0)
      expectThat(vm.ready { it.totalCount == 3 }.totalCount).isEqualTo(3)
    }

  @Test
  fun `freeze moves the unit out of the list and undo brings it back`() =
    runTest {
      category("dairy")
      add("Milk", days = 10)
      val vm = viewModel(ExpiryStatus.FRESH)
      val row =
        vm
          .ready()
          .sections
          .single()
          .rows
          .single()

      vm.onFreeze(row)
      val snackbar = nextSnackbar()
      expectThat(vm.ready { it.isEmpty }.isEmpty).isTrue()

      snackbar.onAction!!.invoke()
      expectThat(vm.ready { !it.isEmpty }.totalCount).isEqualTo(1)
    }

  @Test
  fun `discard one expired unit and undo`() =
    runTest {
      category("dairy")
      add("Milk", days = -3)
      val vm = viewModel(ExpiryStatus.EXPIRED)
      val row =
        vm
          .ready()
          .sections
          .single()
          .rows
          .single()

      vm.onDiscard(row)
      val snackbar = nextSnackbar()
      expectThat(vm.ready { it.isEmpty }.isEmpty).isTrue()
      expectThat(
        db
          .itemDao()
          .getAllItemsSync()
          .single()
          .discardedDate != null,
      ).isTrue()

      snackbar.onAction!!.invoke()
      expectThat(vm.ready { !it.isEmpty }.totalCount).isEqualTo(1)
    }

  @Test
  fun `selection bulk consume acts on every unit of the selected rows and undo restores them`() =
    runTest {
      category("dairy")
      add("Milk", days = 2, quantity = 3)
      add("Eggs", days = 1)
      add("Cheese", days = 2)
      val vm = viewModel(ExpiryStatus.SOON)
      val rows =
        vm
          .ready()
          .sections
          .single()
          .rows

      vm.onToggleSelecting()
      vm.onToggleRow(rows.first { it.name == "Milk" }.key)
      vm.onToggleRow(rows.first { it.name == "Eggs" }.key)
      expectThat(vm.ready { it.selectedKeys.size == 2 }.selectedUnitCount).isEqualTo(4)

      vm.onBulkConsume()
      val snackbar = nextSnackbar()
      expectThat(db.itemDao().getAllItemsSync().count { it.consumedDate != null }).isEqualTo(4)
      expectThat(vm.ready { it.totalCount == 1 }.selectedKeys).isEmpty()

      snackbar.onAction!!.invoke()
      expectThat(vm.ready { it.totalCount == 5 }.totalCount).isEqualTo(5)
    }

  @Test
  fun `bulk reschedule gives the same date to every selected unit`() =
    runTest {
      category("dairy")
      add("Milk", days = -3)
      add("Eggs", days = -1)
      val vm = viewModel(ExpiryStatus.EXPIRED)
      val rows =
        vm
          .ready()
          .sections
          .single()
          .rows

      vm.onToggleSelecting()
      rows.forEach { vm.onToggleRow(it.key) }
      vm.ready { it.selectedKeys.size == 2 }
      vm.onDateChosen(
        DateTarget(rows.flatMap { it.itemIds }, rows.map { it.name }, unfreeze = false, proposed = null),
        inDays(4),
      )
      nextSnackbar()

      expectThat(vm.ready { it.isEmpty }.isEmpty).isTrue()
      expectThat(
        db
          .itemDao()
          .getAllItemsSync()
          .map { it.expirationDate }
          .distinct(),
      ).hasSize(1)
    }

  @Test
  fun `unfreeze with a date takes the unit out of Congelats and undo freezes it again`() =
    runTest {
      category("dairy")
      add("Peas", days = 20, frozen = true)
      val vm = viewModel(ExpiryStatus.FROZEN)
      val row =
        vm
          .ready()
          .sections
          .single()
          .rows
          .single()

      vm.onDateChosen(
        DateTarget(listOf(row.nearest.itemId), listOf(row.name), unfreeze = true, proposed = inDays(20)),
        inDays(6),
      )
      val snackbar = nextSnackbar()
      expectThat(vm.ready { it.isEmpty }.isEmpty).isTrue()

      snackbar.onAction!!.invoke()
      expectThat(vm.ready { !it.isEmpty }.totalCount).isEqualTo(1)
    }

  @Test
  fun `discard all asks for confirmation with every expired unit`() =
    runTest {
      category("dairy")
      add("Milk", days = -3, quantity = 2)
      add("Eggs", days = -1)
      val vm = viewModel(ExpiryStatus.EXPIRED)
      vm.ready()

      vm.onDiscardAllClick()
      val effect = withContext(Dispatchers.Default) { withTimeout(5_000) { vm.sideEffects.first() } }

      expectThat(effect).isA<StatusListSideEffect.ShowDiscardConfirm>().get { unitCount }.isEqualTo(3)
    }

  @Test
  fun `empty variant reports empty`() =
    runTest {
      category("dairy")
      add("Milk", days = 30)
      expectThat(viewModel(ExpiryStatus.EXPIRED).ready().isEmpty).isTrue()
    }
}
