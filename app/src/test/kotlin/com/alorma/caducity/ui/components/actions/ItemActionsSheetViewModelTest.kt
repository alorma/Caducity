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
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.ConsumeAmountUseCase
import com.alorma.caducity.domain.usecase.ConsumeItemUseCase
import com.alorma.caducity.domain.usecase.DiscardItemsUseCase
import com.alorma.caducity.domain.usecase.FreezeAmountUseCase
import com.alorma.caducity.domain.usecase.FreezeItemUseCase
import com.alorma.caducity.domain.usecase.MoveItemToCategoryUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.ObserveItemsUseCase
import com.alorma.caducity.domain.usecase.RescheduleItemUseCase
import com.alorma.caducity.domain.usecase.RestoreItemsUseCase
import com.alorma.caducity.domain.usecase.UnfreezeItemUseCase
import com.alorma.caducity.domain.usecase.toExpirationInstant
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.components.actions.DatePickerPurpose
import com.alorma.caducity.ui.components.actions.ItemActionsSheetNavigationSideEffect
import com.alorma.caducity.ui.components.actions.ItemActionsSheetSideEffect
import com.alorma.caducity.ui.components.actions.ItemActionsSheetState
import com.alorma.caducity.ui.components.actions.ItemActionsSheetViewModel
import com.alorma.caducity.ui.components.actions.QuickReschedule
import com.alorma.caducity.ui.components.feedback.snackbar.ScreenResults
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
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
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
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
  private val results = ScreenResults()
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
    frozen: Boolean = false,
  ): List<String> = addProduct.add(name, categoryId, inDays(days), quantity, frozen).getOrThrow().itemIds

  private fun viewModel(itemId: String) = viewModel(listOf(itemId))

  private fun viewModel(itemIds: List<String>): ItemActionsSheetViewModel {
    val consume = ConsumeItemUseCase(itemDataSource, pantry, clock)
    return ItemActionsSheetViewModel(
      itemIds = itemIds,
      observeItems = ObserveItemsUseCase(pantry, thresholds, todayProvider),
      observeCategories = ObserveCategoriesUseCase(pantry),
      consumeItem = consume,
      freezeItem = FreezeItemUseCase(itemDataSource, pantry, clock),
      consumeAmount = ConsumeAmountUseCase(pantry, clock),
      freezeAmount = FreezeAmountUseCase(pantry, clock),
      unfreezeItem = UnfreezeItemUseCase(itemDataSource, pantry, clock),
      rescheduleItem = RescheduleItemUseCase(itemDataSource, pantry),
      discardItems = DiscardItemsUseCase(pantry, clock),
      moveItem = MoveItemToCategoryUseCase(pantry),
      restoreItems = RestoreItemsUseCase(pantry),
      appClock = clock,
      results = results,
      eventTracker = EventTracker(emptyList()),
    ).also { created += it }
  }

  private suspend fun ItemActionsSheetViewModel.ready(
    until: (ItemActionsSheetState.Ready) -> Boolean = { true },
  ): ItemActionsSheetState.Ready =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) {
        state.first { it is ItemActionsSheetState.Ready && until(it) } as ItemActionsSheetState.Ready
      }
    }

  private suspend fun ItemActionsSheetViewModel.untilClosed() =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { navigationSideEffects.first { it is ItemActionsSheetNavigationSideEffect.Close } }
    }

  private suspend fun nextSnackbar() =
    withContext(Dispatchers.Default) {
      withTimeout(5_000) { results.pending.filterNotNull().first() }
    }

  private suspend fun pausedDates() =
    db
      .itemDao()
      .getAllItemsSync()
      .map { it.pausedDate }
      .toSet()

  private suspend fun item(id: String) = db.itemDao().getAllItemsSync().first { it.id == id }

  private fun TestScope.keepAlive(vm: ItemActionsSheetViewModel) {
    backgroundScope.launch { vm.state.collect {} }
  }

  @Test
  fun `the sheet gathers every unit of the product, the one that expires first on top`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = 10, quantity = 1) + add("Milk", days = 2) + add("Milk", days = 5)
      val vm = viewModel(ids)
      keepAlive(vm)

      val state = vm.ready()

      expectThat(state.units.map { it.id }).containsExactly(ids[1], ids[2], ids[0])
      expectThat(state.unitCount).isEqualTo(3)
      expectThat(state.daysLeft).isEqualTo(2)
      expectThat(state.status).isEqualTo(ExpiryStatus.SOON)
      expectThat(state.selected).isEqualTo(1)
    }

  @Test
  fun `only the units of the row are acted on, not the rest of the product`() =
    runTest {
      category("dairy")
      val tomorrow = add("Milk", days = 1, quantity = 2)
      val expired = add("Milk", days = -4)
      val vm = viewModel(tomorrow)
      keepAlive(vm)

      val state = vm.ready()

      expectThat(state.units.map { it.id }.toSet()).isEqualTo(tomorrow.toSet())
      expectThat(state.expired).isEqualTo(false)

      vm.onSelect(2)
      vm.ready { it.selected == 2 }
      vm.onDiscardSelected()
      nextSnackbar()

      expectThat(tomorrow.all { item(it).discardedDate != null }).isTrue()
      expectThat(item(expired.single()).discardedDate).isNull()
    }

  private suspend fun addMeasured(
    name: String,
    days: Int,
    amount: Int,
    unitType: UnitType = UnitType.GRAMS,
  ): String =
    addProduct
      .add(name, "dairy", inDays(days), 1, false, unitType, amount)
      .getOrThrow()
      .itemIds
      .single()

  @Test
  fun `a measured product spends 100 by default and the selector stays inside the batch`() =
    runTest {
      category("dairy")
      val id = addMeasured("Pasta", days = 20, amount = 500)
      val vm = viewModel(id)
      keepAlive(vm)

      val state = vm.ready()
      expectThat(state.measured).isEqualTo(true)
      expectThat(state.batchAmount).isEqualTo(500)
      expectThat(state.amountSpend).isEqualTo(100)

      vm.onAmountSelect(9_999)
      expectThat(vm.ready { it.amountSpend == 500 }.amountSpend).isEqualTo(500)
      vm.onAmountSelect(0)
      expectThat(vm.ready { it.amountSpend == 1 }.amountSpend).isEqualTo(1)
    }

  @Test
  fun `the default amount is capped by what the batch has`() =
    runTest {
      category("dairy")
      val id = addMeasured("Saffron", days = 20, amount = 30)
      val vm = viewModel(id)
      keepAlive(vm)

      expectThat(vm.ready().amountSpend).isEqualTo(30)
    }

  @Test
  fun `consume spends the chosen amount, closes and undo gives it back`() =
    runTest {
      category("dairy")
      val id = addMeasured("Pasta", days = 20, amount = 500)
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onAmountSelect(150)
      vm.ready { it.amountSpend == 150 }
      vm.onConsume()

      val snackbar = nextSnackbar()
      vm.untilClosed()
      expectThat(item(id).amount).isEqualTo(350)
      expectThat(item(id).consumedDate).isNull()
      expectThat(snackbar.messageArgs.last()).isEqualTo("Pasta")

      snackbar.onAction!!.invoke()
      expectThat(item(id).amount).isEqualTo(500)
    }

  @Test
  fun `consuming the whole batch removes the product and undo brings it back`() =
    runTest {
      category("dairy")
      val id = addMeasured("Pasta", days = 20, amount = 500)
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onAmountSelect(500)
      vm.ready { it.amountSpend == 500 }
      vm.onConsume()

      val snackbar = nextSnackbar()
      expectThat(item(id).consumedDate).isNotNull()
      snackbar.onAction!!.invoke()
      expectThat(item(id).consumedDate).isNull()
      expectThat(item(id).amount).isEqualTo(500)
    }

  @Test
  fun `freeze splits the chosen amount into a frozen batch and undo joins it back`() =
    runTest {
      category("dairy")
      val id = addMeasured("Pasta", days = 20, amount = 500)
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onAmountSelect(200)
      vm.ready { it.amountSpend == 200 }
      vm.onFreezeToggle()

      val snackbar = nextSnackbar()
      val all = db.itemDao().getAllItemsSync()
      expectThat(all.map { it.amount }.toSet()).isEqualTo(setOf(300, 200))
      expectThat(all.count { it.pausedDate != null }).isEqualTo(1)

      snackbar.onAction!!.invoke()
      val back = db.itemDao().getAllItemsSync()
      expectThat(back.map { it.amount }).containsExactly(500)
      expectThat(back.single().pausedDate).isNull()
    }

  @Test
  fun `a frozen measured batch thaws the chosen amount and undo freezes it again`() =
    runTest {
      category("dairy")
      val id =
        addProduct
          .add("Pasta", "dairy", inDays(20), 1, true, UnitType.GRAMS, 500)
          .getOrThrow()
          .itemIds
          .single()
      val vm = viewModel(id)
      keepAlive(vm)
      val state = vm.ready()
      expectThat(state.frozenOnly).isEqualTo(true)

      vm.onAmountSelect(200)
      vm.ready { it.amountSpend == 200 }
      vm.onFreezeToggle()

      val snackbar = nextSnackbar()
      val all = db.itemDao().getAllItemsSync()
      expectThat(all.map { it.amount }.toSet()).isEqualTo(setOf(300, 200))
      expectThat(all.count { it.pausedDate == null }).isEqualTo(1)

      snackbar.onAction!!.invoke()
      expectThat(db.itemDao().getAllItemsSync().map { it.amount }).containsExactly(500)
      expectThat(
        db
          .itemDao()
          .getAllItemsSync()
          .single()
          .pausedDate,
      ).isNotNull()
    }

  @Test
  fun `an expired measured product only discards, every batch at once`() =
    runTest {
      category("dairy")
      val first = addMeasured("Pasta", days = -3, amount = 500)
      val second = addMeasured("Pasta", days = -1, amount = 250)
      val vm = viewModel(listOf(first, second))
      keepAlive(vm)
      val state = vm.ready()
      expectThat(state.expired).isEqualTo(true)

      vm.onDiscardAll()
      nextSnackbar()

      expectThat(item(first).discardedDate).isNotNull()
      expectThat(item(second).discardedDate).isNotNull()
    }

  @Test
  fun `consume N takes the units that expire first and undo brings them back`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = 10) + add("Milk", days = 2) + add("Milk", days = 5)
      val vm = viewModel(ids)
      keepAlive(vm)
      vm.ready()

      vm.onSelect(2)
      vm.ready { it.selected == 2 }
      vm.onConsume()
      val snackbar = nextSnackbar()

      expectThat(item(ids[1]).consumedDate).isNotNull()
      expectThat(item(ids[2]).consumedDate).isNotNull()
      expectThat(item(ids[0]).consumedDate).isNull()
      expectThat(snackbar.messageArgs).containsExactly(2, "Milk")

      snackbar.onAction!!.invoke()
      expectThat(item(ids[1]).consumedDate).isNull()
      expectThat(item(ids[2]).consumedDate).isNull()
    }

  @Test
  fun `the selector stays between one and the number of units`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = 3, quantity = 3)
      val vm = viewModel(ids)
      keepAlive(vm)
      vm.ready()

      vm.onSelect(9)
      expectThat(vm.ready { it.selected == 3 }.selected).isEqualTo(3)
      vm.onSelect(0)
      expectThat(vm.ready { it.selected == 1 }.selected).isEqualTo(1)
    }

  @Test
  fun `freeze N skips expired units and counts only the ones that can be frozen`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = -2) + add("Milk", days = 4) + add("Milk", days = 6)
      val vm = viewModel(ids)
      keepAlive(vm)
      val state = vm.ready()
      expectThat(state.expired).isTrue()
      expectThat(state.freezableCount).isEqualTo(2)

      vm.onSelect(3)
      expectThat(vm.ready { it.selected == 3 }.freezeCount).isEqualTo(2)
      vm.onFreezeToggle()
      nextSnackbar()

      expectThat(item(ids[0]).pausedDate).isNull()
      expectThat(item(ids[1]).pausedDate).isNotNull()
      expectThat(item(ids[2]).pausedDate).isNotNull()
    }

  @Test
  fun `an expired product defaults the selector to its expired units and discards only those`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = -3) + add("Milk", days = -1) + add("Milk", days = 5)
      val vm = viewModel(ids)
      keepAlive(vm)

      val state = vm.ready()
      expectThat(state.expired).isTrue()
      expectThat(state.selected).isEqualTo(2)

      vm.onDiscardSelected()
      val snackbar = nextSnackbar()

      expectThat(item(ids[0]).discardedDate).isNotNull()
      expectThat(item(ids[1]).discardedDate).isNotNull()
      expectThat(item(ids[2]).discardedDate).isNull()

      snackbar.onAction!!.invoke()
      expectThat(item(ids[0]).discardedDate).isNull()
    }

  @Test
  fun `eat anyway consumes the chosen expired units`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = -3) + add("Milk", days = -1)
      val vm = viewModel(ids)
      keepAlive(vm)
      vm.ready()

      vm.onSelect(1)
      vm.ready { it.selected == 1 }
      vm.onEatAnyway()
      nextSnackbar()

      expectThat(item(ids[0]).consumedDate).isNotNull()
      expectThat(item(ids[1]).consumedDate).isNull()
    }

  @Test
  fun `fixing the date of an expired product reschedules only the expired units`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = -3) + add("Milk", days = 5)
      val vm = viewModel(ids)
      keepAlive(vm)
      vm.ready()

      vm.onDatePicked(inDays(2), DatePickerPurpose.FixExpiredDate)
      nextSnackbar()

      expectThat(item(ids[0]).expirationDate).isEqualTo(inDays(2).toEpochMillis())
      expectThat(item(ids[1]).expirationDate).isEqualTo(inDays(5).toEpochMillis())
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
      expectThat(effect).isEqualTo(ItemActionsSheetNavigationSideEffect.Transform(listOf(id)))
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
  fun `freeze keeps the remaining days and unfreeze proposes today plus three days`() =
    runTest {
      category("dairy")
      val id = add("Milk", days = 6).single()
      val vm = viewModel(id)
      keepAlive(vm)
      vm.ready()

      vm.onFreezeToggle()
      nextSnackbar()
      expectThat(item(id).remainingDays).isEqualTo(6)
      // The state comes from Room and re-emits after the write: wait for it before toggling again.
      expectThat(vm.ready { it.frozen }.frozen).isTrue()

      vm.onFreezeToggle()
      val effect = vm.sideEffects.first { it is ItemActionsSheetSideEffect.ShowDatePicker }
      expectThat((effect as ItemActionsSheetSideEffect.ShowDatePicker).purpose).isEqualTo(DatePickerPurpose.Unfreeze)
      expectThat(effect.currentMillis).isEqualTo(inDays(3).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds())
    }

  @Test
  fun `frozen units are all frozen, the one frozen the longest on top, and Defrost N brings them to Soon`() =
    runTest {
      category("dairy")
      val ids = add("Peas", days = 30, quantity = 3, frozen = true)
      val vm = viewModel(ids)
      keepAlive(vm)

      val state = vm.ready { it.frozenOnly }
      expectThat(state.multiple).isTrue()
      expectThat(state.frozenSince).isNotNull()

      vm.onSelect(2)
      vm.ready { it.selected == 2 }
      val frozenAt = pausedDates()
      vm.onFreezeToggle()
      val snackbar = nextSnackbar()

      val unfrozen = ids.filter { item(it).pausedDate == null }
      expectThat(unfrozen).hasSize(2)
      unfrozen.forEach {
        expectThat(item(it).expirationDate)
          .isEqualTo(inDays(UnfreezeItemUseCase.UNFREEZE_DAYS).toExpirationInstant().toEpochMilliseconds())
      }

      snackbar.onAction!!.invoke()
      expectThat(pausedDates()).isEqualTo(frozenAt)
    }

  @Test
  fun `a category that cannot be frozen leaves nothing to freeze and freezing does nothing`() =
    runTest {
      db.categoryDao().insertCategory(CategoryRoomEntity("fruit", "fruit", "", canFreeze = false))
      val ids = add("Apple", days = 4, categoryId = "fruit", quantity = 2)
      val vm = viewModel(ids)
      keepAlive(vm)

      val state = vm.ready()
      expectThat(state.canFreeze).isFalse()
      expectThat(state.freezableCount).isEqualTo(0)

      vm.onFreezeToggle()

      expectThat(item(ids[0]).pausedDate).isNull()
      expectThat(item(ids[1]).pausedDate).isNull()
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
  fun `throw out discards every unit of the product and undo brings them back`() =
    runTest {
      category("dairy")
      val ids = add("Milk", days = 5, quantity = 2)
      val vm = viewModel(ids)
      keepAlive(vm)
      vm.ready()

      vm.onDiscardAll()
      val snackbar = nextSnackbar()

      expectThat(ids.map { item(it).discardedDate }.all { it != null }).isTrue()
      expectThat(db.itemDao().getAllItemsSync().map { it.id }).contains(ids)

      snackbar.onAction!!.invoke()
      expectThat(ids.map { item(it).discardedDate }.all { it == null }).isTrue()
    }

  private fun LocalDate.toEpochMillis() = atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()
}
