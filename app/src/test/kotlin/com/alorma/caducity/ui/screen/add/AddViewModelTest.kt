package com.alorma.caducity.ui.screen.add

import android.app.Application
import androidx.lifecycle.viewModelScope
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
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.UnitType
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.DeleteItemUseCase
import com.alorma.caducity.domain.usecase.FindProductUseCase
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.SetCategoryDurationUseCase
import com.alorma.caducity.domain.usecase.SetProductCanFreezeUseCase
import com.alorma.caducity.domain.usecase.SetProductUnitTypeUseCase
import com.alorma.caducity.domain.usecase.UpdateItemUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.components.feedback.snackbar.ScreenResults
import com.alorma.caducity.ui.screen.category.quick.CategoryCreatedEvents
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
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
  private lateinit var addProduct: AddProductUseCase

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
    usualDurationDays: Int? = null,
    canFreeze: Boolean = true,
  ) = db.categoryDao().insertCategory(
    CategoryRoomEntity(id, id, "", sortOrder = sortOrder, usualDurationDays = usualDurationDays, canFreeze = canFreeze),
  )

  private val created = mutableListOf<AddViewModel>()

  private fun viewModel(
    initialCategoryId: String? = null,
    itemId: String? = null,
    results: ScreenResults = ScreenResults(),
  ) = AddViewModel(
    args = AddArgs(categoryId = initialCategoryId, itemId = itemId),
    todayProvider = todayProvider,
    addProduct = addProduct,
    getNameSuggestions = GetNameSuggestionsUseCase(pantry),
    getActiveItem = GetActiveItemUseCase(pantry),
    updateItem = UpdateItemUseCase(itemDataSource, pantry, clock),
    findProduct = FindProductUseCase(pantry),
    setProductCanFreeze = SetProductCanFreezeUseCase(pantry),
    setProductUnitType = SetProductUnitTypeUseCase(pantry),
    setCategoryDuration = SetCategoryDurationUseCase(pantry),
    deleteItem = DeleteItemUseCase(pantry),
    results = results,
    observeCategories = ObserveCategoriesUseCase(pantry),
    categoryCreated = createdEvents,
    eventTracker = EventTracker(emptyList()),
  ).also { created += it }

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
  fun `choosing a category suggests today plus its usual duration`() =
    runTest {
      category("dairy", usualDurationDays = 7)
      category("pantry", sortOrder = 1)
      val vm = viewModel()
      vm.await { it.categories.size == 2 }

      vm.onCategorySelect("dairy")
      expectThat(vm.state.value.date).isEqualTo(inDays(7))
      expectThat(vm.state.value.suggestedDurationDays).isEqualTo(7)

      vm.onCategorySelect("pantry")
      expectThat(vm.state.value.date).isNull()
      expectThat(vm.state.value.dateDigits).isEmpty()
      expectThat(vm.state.value.suggestedDurationDays).isNull()
    }

  @Test
  fun `a preselected category suggests the date once categories load`() =
    runTest {
      category("dairy", usualDurationDays = 3)
      val vm = viewModel(initialCategoryId = "dairy")

      expectThat(vm.await { it.date != null }.date).isEqualTo(inDays(3))
    }

  @Test
  fun `a date set by the user is kept when the category changes`() =
    runTest {
      category("dairy", usualDurationDays = 7)
      category("meat", sortOrder = 1, usualDurationDays = 3)
      val vm = viewModel()
      vm.await { it.categories.size == 2 }

      vm.onCategorySelect("dairy")
      vm.onQuickDate(QuickDate.TwoWeeks)
      expectThat(vm.state.value.suggestedDurationDays).isNull()

      vm.onCategorySelect("meat")
      expectThat(vm.state.value.date).isEqualTo(inDays(14))
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
      addProduct.add("Cheddar cheese", "dairy", inDays(10), 1, false)
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
      addProduct.add("Milk", "dairy", inDays(3), 1, false)
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }

      vm.onNameChange("Kefir")
      val unknown = vm.await { it.isNewProduct }
      expectThat(unknown.fromHistory).isFalse()

      vm.onNameChange("milk")
      val exact = vm.await { it.categoryId == "dairy" }
      expectThat(exact.isNewProduct).isFalse()
      expectThat(exact.fromHistory).isTrue()
      expectThat(exact.suggestion).isNull()

      vm.onNameChange("milky")
      expectThat(vm.state.value.fromHistory).isFalse()
    }

  @Test
  fun `submitting a new name opens Categoria and then Caduca`() =
    runTest {
      category("dairy", usualDurationDays = 7)
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.onNameChange("Kefir")
      vm.await { it.isNewProduct }

      vm.sideEffects.test {
        vm.onNameSubmit()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.ShowCategorySheet)

        expectThat(vm.onCategorySelect("dairy")).isTrue()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.ShowDateSheet)
        expectThat(vm.state.value.date).isEqualTo(inDays(7))
      }
    }

  @Test
  fun `a dismissed guided flow does not open Caduca after picking a category`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.onNameChange("Kefir")
      vm.await { it.isNewProduct }

      vm.sideEffects.test {
        vm.onNameSubmit()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.ShowCategorySheet)

        vm.onGuidedFlowDismissed()
        expectThat(vm.onCategorySelect("dairy")).isFalse()
        expectNoEvents()
      }
    }

  @Test
  fun `submitting a name from the history opens nothing when it has a date`() =
    runTest {
      category("dairy", usualDurationDays = 7)
      addProduct.add("Milk", "dairy", inDays(3), 1, false)
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.onNameChange("milk")
      vm.await { it.fromHistory && it.date != null }

      vm.sideEffects.test {
        vm.onNameSubmit()
        expectNoEvents()
      }
    }

  @Test
  fun `exact known name does not overwrite a category picked by the user`() =
    runTest {
      category("dairy")
      category("fruit", sortOrder = 1)
      addProduct.add("Milk", "dairy", inDays(3), 1, false)
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
      expectThat(vm.state.value.quantity).isEqualTo(AddProductUseCase.MAX_QUANTITY)
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
  fun `saving with missing fields does nothing`() =
    runTest {
      val vm = viewModel()
      vm.sideEffects.test {
        vm.onSave()
        expectNoEvents()
      }
      expectThat(items()).isEmpty()
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
  fun `editing enables Desa only while something differs from the unit`() =
    runTest {
      category("dairy")
      val added = addProduct.add("Milk", "dairy", inDays(5), 1, false).getOrThrow()
      val vm = viewModel(itemId = added.itemIds.single())
      vm.await { it.isEditing && it.date != null && it.original != null }
      expectThat(vm.state.value.canSave).isFalse()

      vm.onNameChange("Oat milk")
      expectThat(vm.state.value.canSave).isTrue()

      vm.onNameChange("Milk")
      expectThat(vm.state.value.canSave).isFalse()

      vm.onQuickDate(QuickDate.Tomorrow)
      expectThat(vm.state.value.canSave).isTrue()
    }

  @Test
  fun `deleting a unit removes it, closes, and undo puts it back`() =
    runTest {
      category("dairy")
      val added = addProduct.add("Milk", "dairy", inDays(5), 2, false).getOrThrow()
      val itemId = added.itemIds.first()
      val results = ScreenResults()
      val vm = viewModel(itemId = itemId, results = results)
      vm.await { it.isEditing && it.date != null }

      vm.navigationSideEffects.test {
        vm.onDeleteClick()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }
      expectThat(items()).hasSize(1)

      val request = results.consume()
      expectThat(request).isNotNull()
      request?.onAction?.invoke()
      expectThat(items()).hasSize(2)
      expectThat(items().map { it.id }).contains(itemId)
    }

  @Test
  fun `editing from a route loads the unit and saving updates only that unit`() =
    runTest {
      category("dairy")
      category("fruit", sortOrder = 1)
      val added = addProduct.add("Milk", "dairy", inDays(5), 2, false).getOrThrow()
      val itemId = added.itemIds.first()
      val vm = viewModel(itemId = itemId)
      vm.await { it.isEditing && it.date != null }

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
  fun `closing while editing a unit leaves the screen without saving`() =
    runTest {
      category("dairy")
      val added = addProduct.add("Milk", "dairy", inDays(5), 1, false).getOrThrow()
      val vm = viewModel(itemId = added.itemIds.single())
      vm.await { it.isEditing && it.date != null }
      vm.onNameChange("Oat milk")

      vm.navigationSideEffects.test {
        vm.navigate(AddNavigation.Close)
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(products().map { it.name }).containsExactly("Milk")
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

  // region Es pot congelar

  @Test
  fun `can freeze is hidden until a category is selected and then inherits it`() =
    runTest {
      category("dairy")
      category("dry", sortOrder = 1, canFreeze = false)
      val vm = viewModel()
      vm.await { it.categories.size == 2 }
      expectThat(vm.state.value.canFreeze).isNull()

      vm.onCategorySelect("dairy")
      expectThat(vm.state.value.canFreeze).isEqualTo(true)
      vm.onCategorySelect("dry")
      expectThat(vm.state.value.canFreeze).isEqualTo(false)
    }

  @Test
  fun `an existing product with an override wins over its category`() =
    runTest {
      category("dry", canFreeze = false)
      val pasta = addProduct.add("Pasta", "dry", inDays(30), 1, false).getOrThrow()
      SetProductCanFreezeUseCase(pantry).set(pasta.productId, true).getOrThrow()
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }

      vm.fill(name = "pasta", categoryId = "dry")
      expectThat(vm.await { it.productCanFreeze != null }.canFreeze).isEqualTo(true)

      vm.onNameChange("Rice")
      expectThat(vm.await { it.productCanFreeze == null }.canFreeze).isEqualTo(false)
    }

  @Test
  fun `flipping the switch overrides the product on save and the override survives a category change`() =
    runTest {
      category("dry", canFreeze = false)
      category("dairy", sortOrder = 1)
      val vm = viewModel()
      vm.await { it.categories.size == 2 }
      vm.fill(name = "Pasta", categoryId = "dry")

      vm.onCanFreezeChange(true)
      vm.onCategorySelect("dairy")
      expectThat(vm.state.value.canFreeze).isEqualTo(true)
      vm.onCategorySelect("dry")

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(products().single().canFreeze).isEqualTo(true)
    }

  @Test
  fun `saving without touching the switch stores no override`() =
    runTest {
      category("dry", canFreeze = false)
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Pasta", categoryId = "dry")

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(products().single().canFreeze).isNull()
    }

  @Test
  fun `when it cannot be frozen the frozen flag is forced off`() =
    runTest {
      category("dry", canFreeze = false)
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Pasta", categoryId = "dry")
      vm.onFrozenChange(true)
      expectThat(vm.state.value.frozenEffective).isFalse()

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(items().single().pausedDate).isNull()
    }

  @Test
  fun `a frozen add with a past date is not rejected when it cannot be frozen`() =
    runTest {
      category("dry", canFreeze = false)
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Pasta", categoryId = "dry", days = -2)
      vm.onFrozenChange(true)

      expectThat(vm.state.value.frozenWithPastDate).isFalse()
    }

  @Test
  fun `editing a unit shows the product value and saving the override applies to the whole product`() =
    runTest {
      category("dairy")
      val added = addProduct.add("Milk", "dairy", inDays(5), 2, false).getOrThrow()
      val vm = viewModel(itemId = added.itemIds.first())
      vm.await { it.isEditing && it.date != null }
      expectThat(vm.state.value.canFreeze).isEqualTo(true)

      vm.onCanFreezeChange(false)
      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(products().single { it.id == added.productId }.canFreeze).isEqualTo(false)
      expectThat(items()).hasSize(2)
    }

  @Test
  fun `editing a unit into another product applies the override to that product`() =
    runTest {
      category("dairy")
      val added = addProduct.add("Milk", "dairy", inDays(5), 1, false).getOrThrow()
      val vm = viewModel(itemId = added.itemIds.single())
      vm.await { it.isEditing && it.date != null }

      vm.onNameChange("Oat milk")
      vm.onCanFreezeChange(false)
      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(products().single { it.name == "Oat milk" }.canFreeze).isEqualTo(false)
      expectThat(products().single { it.name == "Milk" }.canFreeze).isNull()
    }

  // region Com ho comptes

  @Test
  fun `a new product is counted in units`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      expectThat(vm.state.value.unitType).isEqualTo(UnitType.UNITS)
    }

  @Test
  fun `tapping the row offers the current type`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.onUnitTypeChange(UnitType.PACKS)

      vm.sideEffects.test {
        vm.onUnitTypeClick()
        expectThat(awaitItem()).isEqualTo(AddSideEffect.ShowUnitTypePicker(UnitType.PACKS))
      }
    }

  @Test
  fun `an existing product shows its own type`() =
    runTest {
      category("dairy")
      val yogurt = addProduct.add("Yogurt", "dairy", inDays(30), 1, false).getOrThrow()
      SetProductUnitTypeUseCase(pantry).set(yogurt.productId, UnitType.TUPPERS).getOrThrow()
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }

      vm.fill(name = "yogurt", categoryId = "dairy")
      expectThat(vm.await { it.productUnitType != null }.unitType).isEqualTo(UnitType.TUPPERS)

      vm.onNameChange("Cheese")
      expectThat(vm.await { it.productUnitType == null }.unitType).isEqualTo(UnitType.UNITS)
    }

  @Test
  fun `the chosen type is saved with the product`() =
    runTest {
      category("dairy")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Yogurt", categoryId = "dairy")
      vm.onUnitTypeChange(UnitType.CANS)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(products().single().unitType).isEqualTo(UnitType.CANS)
    }

  @Test
  fun `editing a unit changes the type of its product`() =
    runTest {
      category("dairy")
      val yogurt = addProduct.add("Yogurt", "dairy", inDays(30), 2, false).getOrThrow()
      val vm = viewModel(itemId = yogurt.itemIds.first())
      vm.await { it.isEditing && it.name == "Yogurt" }
      vm.onUnitTypeChange(UnitType.PACKS)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(products().single().unitType).isEqualTo(UnitType.PACKS)
    }

  // endregion

  @Test
  fun `picking a measured type starts at the default amount and keeps it between grams and millilitres`() =
    runTest {
      category("dry")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      expectThat(vm.state.value.isMeasured).isEqualTo(false)

      vm.onUnitTypeChange(UnitType.GRAMS)
      expectThat(vm.state.value.isMeasured).isEqualTo(true)
      expectThat(vm.state.value.amount).isEqualTo(500)

      vm.onAmountChange(750)
      vm.onUnitTypeChange(UnitType.MILLILITRES)
      expectThat(vm.state.value.amount).isEqualTo(750)

      vm.onAmountChange(0)
      expectThat(vm.state.value.amount).isEqualTo(1)
      vm.onAmountChange(UnitType.MAX_AMOUNT * 2)
      expectThat(vm.state.value.amount).isEqualTo(UnitType.MAX_AMOUNT)
    }

  @Test
  fun `saving a measured product creates one batch with the amount`() =
    runTest {
      category("dry")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Pasta", categoryId = "dry")
      vm.onQuantityChange(4)
      vm.onUnitTypeChange(UnitType.GRAMS)
      vm.onAmountChange(750)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(items().single().amount).isEqualTo(750)
      expectThat(products().single().unitType).isEqualTo(UnitType.GRAMS)
    }

  @Test
  fun `a type of another parent than the existing product warns that the quantity resets`() =
    runTest {
      category("dry")
      val pasta = addProduct.add("Pasta", "dry", inDays(30), 3, false).getOrThrow()
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "pasta", categoryId = "dry")
      vm.await { it.productUnitType != null }
      expectThat(vm.state.value.resetsQuantity).isEqualTo(false)

      vm.onUnitTypeChange(UnitType.PACKS)
      expectThat(vm.state.value.resetsQuantity).isEqualTo(false)
      vm.onUnitTypeChange(UnitType.GRAMS)
      expectThat(vm.state.value.resetsQuantity).isEqualTo(true)
    }

  @Test
  fun `editing a unit to a measured type leaves one batch with the default amount`() =
    runTest {
      category("dry")
      val pasta = addProduct.add("Pasta", "dry", inDays(30), 3, false).getOrThrow()
      val vm = viewModel(itemId = pasta.itemIds.first())
      vm.await { it.isEditing && it.name == "Pasta" }
      vm.onUnitTypeChange(UnitType.GRAMS)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(items().single().amount).isEqualTo(500)
      expectThat(products().single().unitType).isEqualTo(UnitType.GRAMS)
    }

  @Test
  fun `saving a handful product creates one batch at the chosen level`() =
    runTest {
      category("dry")
      val vm = viewModel()
      vm.await { it.categories.isNotEmpty() }
      vm.fill(name = "Peanuts", categoryId = "dry")
      vm.onQuantityChange(4)
      vm.onUnitTypeChange(UnitType.HANDFULS)
      expectThat(vm.state.value.isHandful).isEqualTo(true)
      expectThat(vm.state.value.level).isEqualTo(HandfulLevel.LOTS)
      vm.onLevelChange(HandfulLevel.HALF)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(items().single().level).isEqualTo(HandfulLevel.HALF)
      expectThat(items().single().amount).isNull()
      expectThat(products().single().unitType).isEqualTo(UnitType.HANDFULS)
    }

  @Test
  fun `editing a unit to handfuls leaves one batch at a lot`() =
    runTest {
      category("dry")
      val peanuts = addProduct.add("Peanuts", "dry", inDays(30), 3, false).getOrThrow()
      val vm = viewModel(itemId = peanuts.itemIds.first())
      vm.await { it.isEditing && it.name == "Peanuts" }
      vm.onUnitTypeChange(UnitType.HANDFULS)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isEqualTo(AddNavigationSideEffect.Close)
      }

      expectThat(items().single().level).isEqualTo(HandfulLevel.LOTS)
    }

  // endregion
}
