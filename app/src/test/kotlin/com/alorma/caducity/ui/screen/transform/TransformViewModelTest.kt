package com.alorma.caducity.ui.screen.transform

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.alorma.caducity.R
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
import com.alorma.caducity.domain.usecase.GetActiveItemUseCase
import com.alorma.caducity.domain.usecase.GetNameSuggestionsUseCase
import com.alorma.caducity.domain.usecase.ObserveCategoriesUseCase
import com.alorma.caducity.domain.usecase.TransformItemsUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.alorma.caducity.ui.components.feedback.snackbar.GlobalSnackbarEvents
import com.alorma.caducity.ui.screen.transform.picker.IngredientPickerResults
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
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class TransformViewModelTest {
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
  private lateinit var addBatch: AddItemBatchUseCase
  private val created = mutableListOf<TransformViewModel>()
  private val snackbars = GlobalSnackbarEvents()
  private val pickerResults = IngredientPickerResults()

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

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private var categoriesCreated = false

  private suspend fun ensureCategories() {
    if (categoriesCreated) return
    categoriesCreated = true
    listOf("meat", "soup").forEach { db.categoryDao().insertCategory(CategoryRoomEntity(it, it, "", sortOrder = 0)) }
  }

  private suspend fun add(
    name: String,
    days: Int,
    quantity: Int = 1,
    category: String = "meat",
  ): com.alorma.caducity.domain.model.AddedBatch {
    ensureCategories()
    return addBatch
      .add(
        name = name,
        categoryId = category,
        date = inDays(days),
        quantity = quantity,
        frozen = false,
      ).getOrThrow()
  }

  private fun viewModel(itemIds: List<String>) =
    TransformViewModel(
      args = TransformArgs(itemIds),
      todayProvider = todayProvider,
      getActiveItem = GetActiveItemUseCase(pantry),
      getNameSuggestions = GetNameSuggestionsUseCase(pantry),
      transformItems = TransformItemsUseCase(pantry, clock),
      observeCategories = ObserveCategoriesUseCase(pantry),
      snackbars = snackbars,
      eventTracker = EventTracker(emptyList()),
      pickerResults = pickerResults,
    ).also { created += it }

  private suspend fun TransformViewModel.await(predicate: (TransformState) -> Boolean): TransformState =
    withContext(Dispatchers.Default) { withTimeout(5_000) { state.first(predicate) } }

  private suspend fun ready(itemIds: List<String>): TransformViewModel {
    ensureCategories()
    val vm = viewModel(itemIds)
    vm.await { it.ingredients.isNotEmpty() && it.categories.isNotEmpty() }
    return vm
  }

  @Test
  fun `units of one product become one ingredient with the picked count`() =
    runTest {
      val batch = add("Chicken", days = 3, quantity = 3)
      val vm = viewModel(batch.itemIds.take(2))
      val state = vm.await { it.ingredients.isNotEmpty() }

      expectThat(state.ingredients).hasSize(1)
      expectThat(state.ingredients.single().count).isEqualTo(2)
      expectThat(state.ingredients.single().available).isEqualTo(3)
    }

  @Test
  fun `count is limited to the units left and at least one`() =
    runTest {
      val batch = add("Chicken", days = 3, quantity = 2)
      val vm = ready(batch.itemIds.take(1))
      val key =
        vm.state.value.ingredients
          .single()
          .key

      vm.onIngredientCountChange(key, 9)
      expectThat(
        vm.state.value.ingredients
          .single()
          .count,
      ).isEqualTo(2)
      vm.onIngredientCountChange(key, 0)
      expectThat(
        vm.state.value.ingredients
          .single()
          .count,
      ).isEqualTo(1)
    }

  @Test
  fun `default date is the one of the ingredient that expires first and never grows alone`() =
    runTest {
      val chicken = add("Chicken", days = 6)
      val carrots = add("Carrots", days = 2)
      val vm = ready(chicken.itemIds + carrots.itemIds)

      expectThat(vm.state.value.defaultExpiration).isEqualTo(inDays(2))
      expectThat(
        vm.state.value.firstToExpire
          ?.name,
      ).isEqualTo("Carrots")

      vm.onRemoveIngredient(
        vm.state.value.ingredients
          .first { it.name == "Carrots" }
          .key,
      )
      expectThat(vm.state.value.defaultExpiration).isEqualTo(inDays(6))
    }

  @Test
  fun `the picker selection adds new ingredients, drops the left out and keeps counts`() =
    runTest {
      val chicken = add("Chicken", days = 6, quantity = 2)
      val carrots = add("Carrots", days = 2)
      val onions = add("Onions", days = 9)
      val vm = ready(chicken.itemIds + carrots.itemIds)
      val chickenKey =
        vm.state.value.ingredients
          .first { it.name == "Chicken" }
          .key
      vm.onIngredientCountChange(chickenKey, 2)

      pickerResults.deliver(listOf(chicken.itemIds.first(), onions.itemIds.single()))
      val state = vm.await { s -> s.ingredients.any { it.name == "Onions" } }

      expectThat(state.ingredients.map { it.name }).containsExactly("Chicken", "Onions")
      expectThat(state.ingredients.first { it.name == "Chicken" }.count).isEqualTo(2)
      expectThat(state.ingredients.first { it.name == "Onions" }.count).isEqualTo(1)
    }

  @Test
  fun `opening the picker hands over the units already used`() =
    runTest {
      val batch = add("Chicken", days = 6, quantity = 2)
      val vm = ready(batch.itemIds.take(1))

      vm.navigationSideEffects.test {
        vm.navigate(TransformNavigation.IngredientPicker)
        val effect = awaitItem()
        expectThat(effect).isA<TransformNavigationSideEffect.OpenIngredientPicker>()
        expectThat((effect as TransformNavigationSideEffect.OpenIngredientPicker).selectedItemIds)
          .containsExactlyInAnyOrder(batch.itemIds)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `save needs a name and an ingredient`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      expectThat(vm.state.value.canSave).isFalse()

      vm.onNameChange(0, "Broth")
      expectThat(vm.state.value.canSave).isTrue()

      vm.onNameChange(0, "  ")
      expectThat(vm.state.value.canSave).isFalse()
    }

  @Test
  fun `results follow the ingredients until the user overrides them`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      val result =
        vm.state.value.results
          .single()
      expectThat(vm.state.value.categoryOf(result)).isEqualTo("meat")

      vm.onCategorySelect(0, "soup")
      vm.onDatePicked(0, inDays(10))
      val updated =
        vm.state.value.results
          .single()
      expectThat(vm.state.value.categoryOf(updated)).isEqualTo("soup")
      expectThat(vm.state.value.expirationOf(updated)).isEqualTo(inDays(10))
    }

  @Test
  fun `frozen count never exceeds the servings`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      vm.onServingsChange(0, 4)
      vm.onFrozenCountChange(0, 4)
      vm.onServingsChange(0, 2)

      expectThat(
        vm.state.value.results
          .single()
          .frozenCount,
      ).isEqualTo(2)
      vm.onServingsChange(0, 500)
      expectThat(
        vm.state.value.results
          .single()
          .servings,
      ).isEqualTo(TransformItemsUseCase.MAX_SERVINGS)
    }

  @Test
  fun `results can be added and removed while one is left`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      vm.onNameChange(0, "Broth")
      vm.onAddResult()
      expectThat(vm.state.value.results).hasSize(2)
      expectThat(vm.state.value.expandedResultId).isEqualTo(1)

      vm.onRemoveResult(1)
      vm.onRemoveResult(0)
      expectThat(vm.state.value.results).hasSize(1)
    }

  @Test
  fun `a result cannot be added while one is blank`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      expectThat(vm.state.value.canAddResult).isFalse()
      vm.onAddResult()
      expectThat(vm.state.value.results).hasSize(1)

      vm.onNameChange(0, "Broth")
      expectThat(vm.state.value.canAddResult).isTrue()
      vm.onAddResult()
      expectThat(vm.state.value.results).hasSize(2)
      expectThat(vm.state.value.canAddResult).isFalse()
    }

  @Test
  fun `nothing takes the focus when the screen opens`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      expectThat(vm.state.value.focusResultId).isNull()
    }

  @Test
  fun `done folds every result and selecting one expands only that one`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      vm.onNameChange(0, "Broth")
      vm.onAddResult()
      expectThat(vm.state.value.expandedResultId).isEqualTo(1)
      expectThat(vm.state.value.focusResultId).isEqualTo(1)

      vm.onDone()
      expectThat(vm.state.value.expandedResultId).isEqualTo(NONE_EXPANDED)
      expectThat(vm.state.value.focusResultId).isNull()
      expectThat(vm.state.value.results).hasSize(2)

      vm.onSelectResult(0)
      expectThat(vm.state.value.expandedResultId).isEqualTo(0)
      vm.onSelectResult(1)
      expectThat(vm.state.value.expandedResultId).isEqualTo(1)
    }

  @Test
  fun `a result is valid with a name, a category and a date`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      val empty =
        vm.state.value.results
          .single()
      expectThat(vm.state.value.isValid(empty)).isFalse()

      vm.onNameChange(0, "Broth")
      val named =
        vm.state.value.results
          .single()
      expectThat(vm.state.value.isValid(named)).isTrue()
    }

  @Test
  fun `saving with an invalid result opens the first one that lacks something`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      vm.onNameChange(0, "Broth")
      vm.onAddResult()
      vm.onDone()
      expectThat(vm.state.value.canSave).isFalse()

      vm.onSave()
      expectThat(vm.state.value.expandedResultId).isEqualTo(1)
    }

  @Test
  fun `closing asks only when there are changes`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)

      vm.navigationSideEffects.test {
        vm.onCloseRequest()
        expectThat(awaitItem()).isA<TransformNavigationSideEffect.Close>()
      }

      vm.onNameChange(0, "Broth")
      vm.sideEffects.test {
        vm.onCloseRequest()
        expectThat(awaitItem()).isA<TransformSideEffect.ShowDiscardConfirm>()
      }
    }

  @Test
  fun `removing the last ingredient asks before closing`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      vm.sideEffects.test {
        vm.onRemoveIngredient(
          vm.state.value.ingredients
            .single()
            .key,
        )
        expectThat(awaitItem()).isA<TransformSideEffect.ShowDiscardConfirm>()
      }
      expectThat(vm.state.value.ingredients).hasSize(1)
    }

  private suspend fun freeEntry(): TransformViewModel {
    ensureCategories()
    val vm = viewModel(emptyList())
    vm.await { it.categories.isNotEmpty() }
    return vm
  }

  @Test
  fun `free entry needs name, category and date`() =
    runTest {
      val vm = freeEntry()
      expectThat(vm.state.value.canSave).isFalse()

      vm.onNameChange(0, "Tupper")
      expectThat(vm.state.value.canSave).isFalse()

      vm.onCategorySelect(0, "soup")
      expectThat(vm.state.value.canSave).isFalse()

      vm.onDatePicked(0, inDays(3))
      expectThat(vm.state.value.canSave).isTrue()
    }

  @Test
  fun `free entry does not suggest a date or a category`() =
    runTest {
      val vm = freeEntry()
      val state = vm.state.value
      expectThat(state.expirationOf(state.results.single())).isNull()
      expectThat(state.categoryOf(state.results.single())).isNull()
    }

  @Test
  fun `a fully frozen result in free entry needs no date`() =
    runTest {
      val vm = freeEntry()
      vm.onNameChange(0, "Tupper")
      vm.onCategorySelect(0, "soup")
      vm.onServingsChange(0, 2)

      vm.onFrozenCountChange(0, 1)
      expectThat(vm.state.value.canSave).isFalse()

      vm.onFrozenCountChange(0, 2)
      expectThat(vm.state.value.canSave).isTrue()
    }

  @Test
  fun `adding an ingredient to a free entry brings back the default date`() =
    runTest {
      val chicken = add("Chicken", days = 3)
      val vm = freeEntry()
      vm.onNameChange(0, "Broth")
      vm.onCategorySelect(0, "soup")
      expectThat(vm.state.value.canSave).isFalse()

      pickerResults.deliver(chicken.itemIds)
      vm.await { it.ingredients.isNotEmpty() }

      expectThat(vm.state.value.canSave).isTrue()
      expectThat(
        vm.state.value.expirationOf(
          vm.state.value.results
            .single(),
        ),
      ).isEqualTo(inDays(3))
    }

  @Test
  fun `removing the last ingredient of a free entry does not close`() =
    runTest {
      val chicken = add("Chicken", days = 3)
      val vm = freeEntry()
      pickerResults.deliver(chicken.itemIds)
      vm.await { it.ingredients.isNotEmpty() }

      vm.sideEffects.test {
        vm.onRemoveIngredient(
          vm.state.value.ingredients
            .single()
            .key,
        )
        expectNoEvents()
      }
      expectThat(vm.state.value.ingredients).isEmpty()
    }

  @Test
  fun `saving a free entry adds the products and says so`() =
    runTest {
      val vm = freeEntry()
      vm.onNameChange(0, "Tupper")
      vm.onCategorySelect(0, "soup")
      vm.onDatePicked(0, inDays(3))
      vm.onServingsChange(0, 2)
      vm.onAddResult()
      val second =
        vm.state.value.results
          .last()
          .id
      vm.onNameChange(second, "Soup")
      vm.onCategorySelect(second, "soup")
      vm.onDatePicked(second, inDays(3))

      snackbars.events.test {
        vm.onSave()
        val message = awaitItem()
        expectThat(message.message).isEqualTo(R.string.transform_added)
        expectThat(message.messageArgs).isEqualTo(listOf<Any>(2))
      }

      val active = pantry.observeActiveItems().first()
      expectThat(active.filter { it.name == "Tupper" }).hasSize(2)
      expectThat(active.filter { it.name == "Soup" }).hasSize(1)
    }

  @Test
  fun `saving closes the ingredients and creates the servings`() =
    runTest {
      val chicken = add("Chicken", days = 3, quantity = 2)
      val vm = ready(chicken.itemIds.take(1))
      vm.onNameChange(0, "Broth")
      vm.onServingsChange(0, 4)
      vm.onFrozenCountChange(0, 1)

      vm.navigationSideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isA<TransformNavigationSideEffect.Close>()
      }
      expectThat(vm.state.value.closedRows).isEqualTo(1)

      val active = pantry.observeActiveItems().first()
      expectThat(active.filter { it.name == "Broth" }).hasSize(4)
      expectThat(active.filter { it.name == "Broth" && it.frozen }).hasSize(1)
      expectThat(active.filter { it.name == "Chicken" }).hasSize(1)
    }

  @Test
  fun `the saved message follows the singular and plural of both numbers`() =
    runTest {
      val chicken = add("Chicken", days = 3, quantity = 3)
      val carrots = add("Carrots", days = 4)
      val vm = ready(chicken.itemIds.take(1))
      vm.onNameChange(0, "Broth")

      snackbars.events.test {
        vm.onSave()
        expectThat(awaitItem().message).isEqualTo(R.string.transform_done_one_one)
      }

      val many = ready((chicken.itemIds.drop(1) + carrots.itemIds))
      many.onNameChange(0, "Stew")
      snackbars.events.test {
        many.onSave()
        val message = awaitItem()
        expectThat(message.message).isEqualTo(R.string.transform_done_one_product)
        expectThat(message.messageArgs).isEqualTo(listOf<Any>(3))
      }
    }

  @Test
  fun `saving a frozen result with a past date reports it`() =
    runTest {
      val vm = ready(add("Chicken", days = 3).itemIds)
      vm.onNameChange(0, "Broth")
      vm.onFrozenCountChange(0, 1)
      vm.onDatePicked(0, inDays(-1))

      vm.sideEffects.test {
        vm.onSave()
        expectThat(awaitItem()).isA<TransformSideEffect.ShowFrozenPastDate>()
      }
      expectThat(pantry.observeActiveItems().first().filter { it.name == "Chicken" }).hasSize(1)
      expectThat(vm.state.value.saving).isFalse()
    }

  @Test
  fun `a name already in the history takes its category`() =
    runTest {
      add("Broth", days = 20, category = "soup")
      val vm = ready(add("Chicken", days = 3).itemIds)

      vm.onNameChange(0, "Broth")
      val state = vm.await { it.results.single().historyCategoryId != null }
      expectThat(state.categoryOf(state.results.single())).isEqualTo("soup")

      vm.onNameChange(0, "")
      expectThat(
        vm.state.value.results
          .single()
          .suggestion,
      ).isEqualTo(null)
      expectThat(
        vm.state.value.ingredients
          .map { it.name }
          .filter { it.isEmpty() },
      ).isEmpty()
    }
}
