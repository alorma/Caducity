package com.alorma.caducity.ui.components.actions

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
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.MoveItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.components.actions.ItemActionsSheetNavigationSideEffect
import com.alorma.caducity.ui.components.actions.ItemActionsSheetSideEffect
import com.alorma.caducity.ui.components.actions.ItemActionsSheetState
import com.alorma.caducity.ui.components.actions.ItemActionsSheetViewModel
import com.alorma.caducity.ui.components.actions.QuickReschedule
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
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
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ItemActionsSheetViewModelTest {
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
  private lateinit var thresholds: AppExpirationThresholdsImpl
  private val snackbars = GlobalSnackbarEvents()
  private val created = mutableListOf<ItemActionsSheetViewModel>()

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    thresholds = AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings()))
    val itemMapper = ItemRoomMapper(clock, thresholds)
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
    categoryId: String = "dairy",
    quantity: Int = 1,
  ): List<String> = addProduct.add(name, categoryId, inDays(days), quantity, false).getOrThrow().itemIds

  private fun viewModel(itemId: String): ItemActionsSheetViewModel {
    val consume = ConsumeItemUseCase(itemDataSource, pantry, clock)
    return ItemActionsSheetViewModel(
      itemId = itemId,
      observeItems = ObserveItemsUseCase(pantry, thresholds, todayProvider),
      observeCategories = ObserveCategoriesUseCase(pantry),
      consumeItem = consume,
      freezeItem = FreezeItemUseCase(itemDataSource, pantry, clock),
      unfreezeItem = UnfreezeItemUseCase(itemDataSource, pantry, clock),
      rescheduleItem = RescheduleItemUseCase(itemDataSource, pantry),
      deleteItem = DeleteItemUseCase(itemDataSource),
      moveItem = MoveItemToCategoryUseCase(pantry),
      restoreItems = RestoreItemsUseCase(pantry),
      appClock = clock,
      snackbars = snackbars,
      eventTracker = EventTracker(emptyList()),
    ).also { created += it }
  }

  private suspend fun ItemActionsSheetViewModel.ready(): ItemActionsSheetState.Ready =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { state.first { it is ItemActionsSheetState.Ready } as ItemActionsSheetState.Ready }
    }

  private suspend fun ItemActionsSheetViewModel.untilClosed() =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { navigationSideEffects.first { it is ItemActionsSheetNavigationSideEffect.Close } }
    }

  private suspend fun nextSnackbar() =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { snackbars.events.first() }
    }

  private suspend fun item(id: String) = db.itemDao().getAllItemsSync().first { it.id == id }

  private fun TestScope.keepAlive(vm: ItemActionsSheetViewModel) {
    backgroundScope.launch { vm.state.collect {} }
  }

  @Test
  fun `header shows the position of the unit among its product, ordered by date`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = 10, quantity = 1) + add("Milk", days = 2) + add("Milk", days = 5)
      val vm = viewModel(ids[2])
      keepAlive(vm)

      val state = vm.ready()

      expectThat(state.unitIndex).isEqualTo(2)
      expectThat(state.unitCount).isEqualTo(3)
      expectThat(state.daysLeft).isEqualTo(5)
      expectThat(state.status).isEqualTo(ExpiryStatus.FRESH)
    }

  @Test
  fun `consume marks the unit consumed, closes and undo brings it back`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = 5).single()
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onConsume()

      val snackbar = nextSnackbar()
      vm.untilClosed()
      expectThat(item(id).consumedDate).isNotNull()
      expectThat(snackbar.messageArgs).containsExactly("Milk")

      snackbar.onAction!!.invoke()
      expectThat(item(id).consumedDate).isNull()
    }

  @Test
  fun `transform asks to open Transforma with this unit`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = 5).single()
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.navigate(ItemActionsSheetNavigation.Transform)

      val effect = withContext(Dispatchers.Default) { withTimeout(5_000) { vm.navigationSideEffects.first() } }
      expectThat(effect).isEqualTo(ItemActionsSheetNavigationSideEffect.Transform(id))
    }

  @Test
  fun `consuming an expired unit is allowed`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = -3).single()
      val vm = viewModel(id)
      keepAlive(vm)
      expectThat(vm.ready().status).isEqualTo(ExpiryStatus.EXPIRED)

      vm.onConsume()
      nextSnackbar()

      expectThat(item(id).consumedDate).isNotNull()
    }

  @Test
  fun `quick date adds days to the current date and undo restores it`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = 5).single()
      val original = item(id).expirationDate
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onQuickReschedule(QuickReschedule.ThreeDays)
      val snackbar = nextSnackbar()

      expectThat(item(id).expirationDate).isEqualTo(inDays(8).toEpochMillis())

      snackbar.onAction!!.invoke()
      expectThat(item(id).expirationDate).isEqualTo(original)
    }

  @Test
  fun `freeze keeps the remaining days and unfreeze proposes today plus those days`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = 6).single()
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onFreezeToggle()
      nextSnackbar()
      expectThat(item(id).remainingDays).isEqualTo(6)
      expectThat(vm.ready().frozen).isTrue()

      vm.onFreezeToggle()
      val effect = vm.sideEffects.first { it is ItemActionsSheetSideEffect.ShowDatePicker }
      expectThat((effect as ItemActionsSheetSideEffect.ShowDatePicker).unfreeze).isTrue()
      expectThat(effect.currentMillis).isEqualTo(inDays(6).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
    }

  @Test
  fun `an expired unit cannot be frozen`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = -1).single()
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onFreezeToggle()

      expectThat(nextSnackbar().message).isEqualTo(R.string.item_actions_error_freeze_expired)
      expectThat(item(id).pausedDate).isNull()
    }

  @Test
  fun `move changes the category and undo moves it back`() =
    runTest {
      category("dairy")
      category("fridge", sortOrder = 1)
      val id = add("Milk", days = 5).single()
      val vm = viewModel(id)
      keepAlive(vm)
      expectThat(vm.ready().otherCategories.map { it.id }).containsExactly("fridge")

      vm.onMove("fridge")
      val snackbar = nextSnackbar()
      expectThat(item(id).categoryId).isEqualTo("fridge")

      snackbar.onAction!!.invoke()
      expectThat(item(id).categoryId).isEqualTo("dairy")
    }

  @Test
  fun `delete asks for confirmation before removing the unit`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = 5).single()
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onDelete()
      val effect = vm.sideEffects.first { it is ItemActionsSheetSideEffect.ShowDeleteConfirm }
      expectThat((effect as ItemActionsSheetSideEffect.ShowDeleteConfirm).name).isEqualTo("Milk")
      expectThat(db.itemDao().getAllItemsSync().map { it.id }).contains(id)

      vm.onDeleteConfirmed()
      nextSnackbar()
      expectThat(db.itemDao().getAllItemsSync().map { it.id }).not { contains(id) }
    }

  private fun LocalDate.toEpochMillis() = atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}
