package com.alorma.caducity.ui.screen.add

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
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
import com.alorma.caducity.domain.usecase.AddItemBatchUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.UndoAddItemsUseCase
import com.alorma.caducity.domain.usecase.UpdateItemUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.screen.category.quick.CategoryCreatedEvents
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
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
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AddViewModelTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()
  private val todayProvider =
    object : TodayProvider {
      override fun today(): LocalDate = today

      override fun observe(): Flow<LocalDate> = emptyFlow()
    }

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var itemDataSource: RoomItemDataSource
  private val createdEvents = CategoryCreatedEvents()
  private lateinit var addBatch: AddItemBatchUseCase

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val itemMapper = ItemRoomMapper(clock, AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings())))
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
    db.close()
    Dispatchers.resetMain()
  }

  private suspend fun category(
    id: String,
    sortOrder: Int = 0,
  ) = db.categoryDao().insertCategory(CategoryRoomEntity(id, id, "", sortOrder = sortOrder))

  private fun viewModel(
    initialCategoryId: String? = null,
    itemId: String? = null,
  ) = AddViewModel(
    args = AddArgs(categoryId = initialCategoryId, itemId = itemId),
    todayProvider = todayProvider,
    addItemBatch = addBatch,
    undoAddItems = UndoAddItemsUseCase(pantry),
    getNameSuggestions = GetNameSuggestionsUseCase(pantry),
    getActiveItem = GetActiveItemUseCase(pantry),
    updateItem = UpdateItemUseCase(itemDataSource, pantry, clock),
    observeCategories = ObserveCategoriesUseCase(pantry),
    categoryCreated = createdEvents,
    eventTracker = EventTracker(emptyList()),
  )

  private suspend fun AddViewModel.await(predicate: (AddState) -> Boolean): AddState =
    withContext(Dispatchers.Default) { withTimeout(5_000) { state.first(predicate) } }

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun items() = db.itemDao().getAllItemsSync()

  private suspend fun products() = db.productDao().getAllProductsSync()

  private suspend fun AddViewModel.fill(
    name: String = "Milk",
    categoryId: String = "dairy",
    days: Int = 5,
  ) {
    onNameChange(name)
    onCategorySelect(categoryId)
    onDateTextChange(inDays(days).let { "%02d%02d%04d".format(it.dayOfMonth, it.monthNumber, it.year) })
  }

  @Test
  fun `name category and date are all required`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }

      expectThat(vm.state.value.missing).containsExactly(AddMissing.Name, AddMissing.Category, AddMissing.Date)
      expectThat(vm.state.value.canSave).isFalse()

      vm.onNameChange("Milk")
      vm.onCategorySelect("dairy")
      expectThat(vm.state.value.missing).containsExactly(AddMissing.Date)

      vm.onDateTextChange("01/12/26")
      expectThat(vm.state.value.missing).isEmpty()
      expectThat(vm.state.value.canSave).isTrue()
    }

  @Test
  fun `incomplete date stays missing`() =
    runTest {
      val vm = viewModel()
      vm.onDateTextChange("01/12")
      expectThat(vm.state.value.date).isNull()
      expectThat(vm.state.value.dateDigits).isEqualTo("0112")
    }

  @Test
  fun `quick date and picker fill the date`() =
    runTest {
      val vm = viewModel()
      vm.onQuickDate(QuickDate.OneWeek)
      expectThat(vm.state.value.date).isEqualTo(inDays(7))
      vm.onDatePicked(LocalDate(2027, 1, 5))
      expectThat(vm.state.value.date).isEqualTo(LocalDate(2027, 1, 5))
      expectThat(vm.state.value.dateDigits).isEqualTo("05012027")
    }

  @Test
  fun `suggestion fills name and category but not the date`() =
    runTest {
      category("dairy")
      category("fruit", sortOrder = 1)
      addBatch.add("Cheddar cheese", "dairy", inDays(10), 1, false)
      val vm = viewModel(initialCategoryId = "fruit")
      vm.await { it.categories.size == 2 }

      vm.onNameChange("ched")
      val typed = vm.await { it.suggestion != null }
      expectThat(typed.isNewProduct).isFalse()

      vm.onSuggestionClick()

      expectThat(vm.state.value.name).isEqualTo("Cheddar cheese")
      expectThat(vm.state.value.categoryId).isEqualTo("dairy")
      expectThat(vm.state.value.date).isNull()
      expectThat(vm.state.value.suggestion).isNull()
    }

  @Test
  fun `unknown name is a new product and known exact name fills the category`() =
    runTest {
      category("dairy")
      addBatch.add("Milk", "dairy", inDays(3), 1, false)
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }

      vm.onNameChange("Kefir")
      vm.await { it.isNewProduct }

      vm.onNameChange("milk")
      val exact = vm.await { it.categoryId == "dairy" }
      expectThat(exact.isNewProduct).isFalse()
      expectThat(exact.suggestion).isNull()
    }

  @Test
  fun `exact known name does not overwrite a category picked by the user`() =
    runTest {
      category("dairy")
      category("fruit", sortOrder = 1)
      addBatch.add("Milk", "dairy", inDays(3), 1, false)
      val vm = viewModel()
      vm.await { it.categories.size == 2 }

      vm.onCategorySelect("fruit")
      vm.onNameChange("Milk")
      vm.await { !it.isNewProduct }

      expectThat(vm.state.value.categoryId).isEqualTo("fruit")
    }

  @Test
  fun `save with quantity 3 creates 3 independent items and closes`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill()
      vm.onQuantityChange(3)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(items()).hasSize(3)
      expectThat(products()).hasSize(1)
    }

  @Test
  fun `quantity is clamped`() =
    runTest {
      val vm = viewModel()
      vm.onQuantityChange(0)
      expectThat(vm.state.value.quantity).isEqualTo(1)
      vm.onQuantityChange(500)
      expectThat(vm.state.value.quantity).isEqualTo(AddItemBatchUseCase.MAX_QUANTITY)
    }

  @Test
  fun `a past date can be saved`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(days = -2)

      vm.navigationSideEffects.test {
        vm.onSave()
        awaitItem()
      }

      expectThat(items()).hasSize(1)
    }

  @Test
  fun `frozen with a past date is rejected and nothing is saved`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(days = -2)
      vm.onFrozenChange(true)

      vm.sideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.ShowFrozenPastDate)
        expectThat(awaitItem()).isEqualTo(AddSideEffect.Reject)
      }

      expectThat(items()).isEmpty()
    }

  @Test
  fun `tapping save with missing fields rejects`() =
    runTest {
      val vm = viewModel()
      vm.sideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.Reject)
        vm.onSaveAndAnother()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.Reject)
      }
    }

  @Test
  fun `save and another keeps category and date, clears the name and enters batch mode`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill()
      vm.onQuantityChange(2)

      vm.sideEffects.test {
        vm.onSaveAndAnother()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.Confirm)
        expectThat(awaitItem()).isA<AddSideEffect.ShowSaved>()
      }

      val state = vm.state.value
      expectThat(state.name).isEqualTo("")
      expectThat(state.categoryId).isEqualTo("dairy")
      expectThat(state.date).isEqualTo(inDays(5))
      expectThat(state.quantity).isEqualTo(1)
      expectThat(state.isBatchMode).isTrue()
      expectThat(state.added).hasSize(1)
      expectThat(state.isDone).isTrue()
      expectThat(items()).hasSize(2)
    }

  @Test
  fun `done in batch mode closes without saving anything else`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill()
      vm.sideEffects.test {
        vm.onSaveAndAnother()
        awaitItem()
        awaitItem()
      }

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }
      expectThat(items()).hasSize(1)
    }

  @Test
  fun `typing a known name in batch mode takes its category and keeps the date`() =
    runTest {
      category("dairy")
      category("fruit", sortOrder = 1)
      addBatch.add("Apple", "fruit", inDays(20), 1, false)
      val vm = viewModel()
      vm.await { it.categories.size == 2 }
      vm.fill()
      vm.sideEffects.test {
        vm.onSaveAndAnother()
        awaitItem()
        awaitItem()
      }

      vm.onNameChange("Apple")
      vm.await { it.categoryId == "fruit" }

      expectThat(vm.state.value.date).isEqualTo(inDays(5))
    }

  @Test
  fun `undo removes the batch items and the entry`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill()
      vm.onQuantityChange(3)
      vm.sideEffects.test {
        vm.onSaveAndAnother()
        awaitItem()
        val saved = awaitItem()
        expectThat(saved).isA<AddSideEffect.ShowSaved>()
        vm.onUndo((saved as AddSideEffect.ShowSaved).entry)
      }

      vm.await { it.added.isEmpty() }
      expectThat(items()).isEmpty()
      expectThat(products()).isEmpty()
      expectThat(vm.state.value.isBatchMode).isFalse()
    }

  @Test
  fun `deleted category is deselected`() =
    runTest {
      category("dairy")
      val vm = viewModel(initialCategoryId = "ghost")
      vm.await { it.categories.isNotEmpty() }
      expectThat(vm.state.value.categoryId).isNull()
      expectThat(
        vm.state.value.categories
          .firstOrNull(),
      ).isNotNull()
    }

  @Test
  fun `editing from a route loads the unit and saving updates only that unit`() =
    runTest {
      category("dairy")
      category("fruit", sortOrder = 1)
      val batch = addBatch.add("Milk", "dairy", inDays(5), 2, false).getOrThrow()
      val itemId = batch.itemIds.first()
      val vm = viewModel(itemId = itemId)
      vm.await { it.editing != null && it.date != null }

      expectThat(vm.state.value.name).isEqualTo("Milk")
      expectThat(vm.state.value.categoryId).isEqualTo("dairy")
      expectThat(vm.state.value.date).isEqualTo(inDays(5))

      vm.onNameChange("Oat milk")
      vm.onCategorySelect("fruit")
      vm.onQuickDate(QuickDate.Tomorrow)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      val edited = items().first { it.id == itemId }
      expectThat(edited.categoryId).isEqualTo("fruit")
      expectThat(items()).hasSize(2)
      expectThat(items().count { it.categoryId == "dairy" }).isEqualTo(1)
    }

  @Test
  fun `editing a batch chip updates the entry and restores the form`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Milk")
      vm.sideEffects.test {
        vm.onSaveAndAnother()
        awaitItem()
        awaitItem()
      }
      val entry =
        vm.state.value.added
          .single()

      vm.onEditEntry(entry)
      vm.await { it.editing != null && it.name == "Milk" }
      vm.onNameChange("Whole milk")
      vm.onSave()

      val state = vm.await { it.editing == null && it.added.single().name == "Whole milk" }
      expectThat(state.name).isEqualTo("")
      expectThat(state.categoryId).isEqualTo("dairy")
      expectThat(state.isBatchMode).isTrue()
    }

  @Test
  fun `closing while editing a batch chip cancels the edit instead of leaving`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Milk")
      vm.sideEffects.test {
        vm.onSaveAndAnother()
        awaitItem()
        awaitItem()
      }
      vm.onEditEntry(
        vm.state.value.added
          .single(),
      )
      vm.await { it.editing != null }

      vm.navigate(AddNavigation.Close)

      expectThat(vm.state.value.editing).isNull()
      expectThat(vm.state.value.name).isEqualTo("")
    }

  @Test
  fun `a category created in the sheet is left selected even if it arrives late`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }

      createdEvents.emit("fresh")
      category("fresh", sortOrder = 1)

      val state = vm.await { it.categoryId == "fresh" }
      expectThat(state.categories.map { it.id }).containsExactly("dairy", "fresh")
    }
}
