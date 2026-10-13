package com.alorma.caducity.ui.screen.category.editor

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomCategoryDataSource
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.usecase.AddProductUseCase
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.alorma.caducity.domain.usecase.DeleteCategoryUseCase
import com.alorma.caducity.domain.usecase.UpdateCategoryUseCase
import com.alorma.caducity.feature.tracking.EventTracker
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CategoryEditorViewModelTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var create: CreateCategoryUseCase
  private lateinit var update: UpdateCategoryUseCase
  private lateinit var delete: DeleteCategoryUseCase
  private lateinit var thresholds: AppExpirationThresholdsImpl
  private val created = mutableListOf<CategoryEditorViewModel>()

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
    val categoryMapper = CategoryRoomMapper(ProductRoomMapper(), itemMapper)
    pantry = RoomPantryDataSource(db, db.categoryDao(), db.itemDao(), db.productDao(), categoryMapper, clock)
    val dataSource = RoomCategoryDataSource(db.categoryDao(), db.itemDao(), categoryMapper, itemMapper)
    create = CreateCategoryUseCase(dataSource, clock, thresholds, pantry)
    update = UpdateCategoryUseCase(pantry)
    delete = DeleteCategoryUseCase(dataSource)
  }

  @After
  fun tearDown() {
    created.forEach { it.viewModelScope.cancel() }
    db.close()
    Dispatchers.resetMain()
  }

  private suspend fun viewModel(categoryId: String? = null): CategoryEditorViewModel {
    val vm =
      CategoryEditorViewModel(categoryId, pantry, create, update, delete, thresholds, EventTracker(emptyList()))
        .also(created::add)
    // Room runs its queries off the test dispatcher: wait for the editor to load.
    withContext(Dispatchers.Default) { withTimeout(5_000) { vm.state.first { !it.loading } } }
    return vm
  }

  private suspend fun <T> real(block: suspend () -> T): T =
    withContext(Dispatchers.Default) { withTimeout(5_000) { block() } }

  @Test
  fun `a repeated name, ignoring case and accents, blocks saving with an error under the field`() =
    runTest {
      create.createCategory("Lactics", "").getOrThrow()
      val vm = viewModel()

      vm.onNameChange(" lÀctics")
      expectThat(vm.state.value.error).isEqualTo(CategoryEditorError.Duplicate)
      expectThat(vm.state.value.canSave).isFalse()

      vm.onNameChange("Begudes")
      expectThat(vm.state.value.error).isNull()
      expectThat(vm.state.value.canSave).isTrue()
    }

  @Test
  fun `the margin slider starts on the default and an untouched one is saved as null`() =
    runTest {
      val vm = viewModel()
      expectThat(vm.state.value.shownSoonDays).isEqualTo(3)
      expectThat(vm.state.value.soonDays).isNull()

      vm.onNameChange("Begudes")
      vm.onSave()
      real { vm.navigationSideEffects.first() }
      expectThat(pantry.getCategories().single().soonThresholdDays).isNull()
    }

  @Test
  fun `a margin picked on the slider is saved and loaded back when editing`() =
    runTest {
      val vm = viewModel()
      vm.onNameChange("Rebost")
      vm.onSoonDaysChange(30)
      vm.onSave()
      real { vm.navigationSideEffects.first() }
      val saved = pantry.getCategories().single()
      expectThat(saved.soonThresholdDays).isEqualTo(30)

      val edit = viewModel(saved.id)
      expectThat(edit.state.value.soonDays).isEqualTo(30)
      expectThat(edit.state.value.shownSoonDays).isEqualTo(30)
    }

  @Test
  fun `editing a category without its own margin keeps it following the default`() =
    runTest {
      val id = create.createCategory("Lactics", "").getOrThrow()
      val vm = viewModel(id)
      expectThat(vm.state.value.soonDays).isNull()
      vm.onNameChange("Lactics i ous")
      vm.onSave()
      real { vm.navigationSideEffects.first() }
      expectThat(pantry.getCategories().single().soonThresholdDays).isNull()
    }

  @Test
  fun `saving a new category creates it with shape, icon and description`() =
    runTest {
      val vm = viewModel()
      vm.onNameChange("Begudes")
      vm.onShapeChange(CategoryShape.SUNNY)
      vm.onDescriptionChange("  Refrescos i sucs ")
      vm.onSave()

      expectThat(real { vm.navigationSideEffects.first() }).isA<CategoryEditorNavigationSideEffect.Created>()
      val saved = pantry.getCategories().single()
      expectThat(saved.name).isEqualTo("Begudes")
      expectThat(saved.shape).isEqualTo(CategoryShape.SUNNY)
      expectThat(saved.description).isEqualTo("Refrescos i sucs")
    }

  @Test
  fun `editing loads the category, keeps its own name valid and saves the changes`() =
    runTest {
      val id = create.createCategory("Lactics", "Llet").getOrThrow()
      val vm = viewModel(id)

      expectThat(vm.state.value.name).isEqualTo("Lactics")
      expectThat(vm.state.value.description).isEqualTo("Llet")
      vm.onNameChange("Lactics")
      expectThat(vm.state.value.canSave).isTrue()

      vm.onNameChange("Làctics i ous")
      vm.onDescriptionChange("")
      vm.onSave()

      expectThat(real { vm.navigationSideEffects.first() }).isEqualTo(CategoryEditorNavigationSideEffect.Close)
      val saved = pantry.getCategories().single()
      expectThat(saved.name).isEqualTo("Làctics i ous")
      expectThat(saved.description).isEqualTo("")
    }

  @Test
  fun `deleting asks first with the number of units and then removes the category`() =
    runTest {
      val id = create.createCategory("Lactics", "").getOrThrow()
      AddProductUseCase(pantry, clock).add("Llet", id, LocalDate(2026, 10, 14), 2, false).getOrThrow()
      val vm = viewModel(id)

      vm.onDeleteClick()
      expectThat(real { vm.sideEffects.first() }).isEqualTo(CategoryEditorSideEffect.ShowDeleteConfirm("Lactics", 2))
      expectThat(pantry.getCategories()).hasSize(1)

      vm.onDeleteConfirmed()
      expectThat(real { vm.navigationSideEffects.first() }).isEqualTo(CategoryEditorNavigationSideEffect.Deleted)
      expectThat(pantry.getCategories()).hasSize(0)
    }
}
