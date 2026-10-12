package com.alorma.caducity.domain.usecase

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.domain.model.IngredientSpend
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.TransformResult
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
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
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSuccess

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class TransformItemsUseCaseTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var transform: TransformItemsUseCase
  private lateinit var addBatch: AddItemBatchUseCase

  @Before
  fun setUp() {
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
    transform = TransformItemsUseCase(pantry, clock)
    addBatch = AddItemBatchUseCase(pantry, clock)
  }

  @After
  fun tearDown() = db.close()

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun units(
    name: String,
    days: Int,
    quantity: Int = 1,
  ): List<String> {
    if (db.categoryDao().getCategoriesOrderedSync().none { it.id == "c1" }) {
      db.categoryDao().insertCategory(CategoryRoomEntity("c1", "c1", "", sortOrder = 0))
    }
    return addBatch.add(name, "c1", inDays(days), quantity, frozen = false).getOrThrow().itemIds
  }

  private fun result(
    name: String = "Broth",
    servings: Int = 4,
    frozen: Int = 0,
    days: Int = 3,
  ) = TransformResult(name, servings, "c1", inDays(days), frozen)

  @Test
  fun `transform removes the ingredients from the live items and adds the results`() =
    runTest {
      val chicken = units("Chicken", 2)
      val veg = units("Veg", 5, quantity = 2)

      val batch =
        transform
          .transform(
            listOf(IngredientSpend(chicken, 1), IngredientSpend(veg, 2)),
            listOf(result()),
          ).getOrThrow()

      val active = db.itemDao().getAllItemsSync().filter { it.transformedDate == null }
      expectThat(active.map { it.id }).containsExactlyInAnyOrder(batch.resultItemIds)
      expectThat(batch.resultItemIds).hasSize(4)
    }

  @Test
  fun `one of two units spends only the one that expires first`() =
    runTest {
      val late = units("Chicken", 9)
      val soon = units("Chicken", 2)

      transform.transform(listOf(IngredientSpend(late + soon, 1)), listOf(result(servings = 1))).getOrThrow()

      expectThat(db.itemDao().getItem(soon.single())!!.transformedDate).isNotNull()
      expectThat(db.itemDao().getItem(late.single())!!.transformedDate).isNull()
    }

  @Test
  fun `frozen servings come out frozen`() =
    runTest {
      val chicken = units("Chicken", 2)

      val batch =
        transform
          .transform(
            listOf(IngredientSpend(chicken, 1)),
            listOf(result(servings = 4, frozen = 3)),
          ).getOrThrow()

      val created = batch.resultItemIds.map { db.itemDao().getItem(it)!! }
      expectThat(created.count { it.pausedDate != null }).isEqualTo(3)
    }

  @Test
  fun `undo restores the ingredients and removes the results`() =
    runTest {
      val chicken = units("Chicken", 2, quantity = 2)
      val before = db.itemDao().getAllItemsSync().sortedBy { it.id }

      val batch = transform.transform(listOf(IngredientSpend(chicken, 2)), listOf(result())).getOrThrow()
      expectThat(transform.undo(batch)).isSuccess()

      expectThat(db.itemDao().getAllItemsSync().sortedBy { it.id }).isEqualTo(before)
      expectThat(db.productDao().getAllProductsSync()).hasSize(1)
    }

  @Test
  fun `count above the units left is rejected and nothing changes`() =
    runTest {
      val chicken = units("Chicken", 2, quantity = 2)

      expectThat(transform.transform(listOf(IngredientSpend(chicken, 3)), listOf(result())))
        .isFailure()
        .isA<PantryError.InvalidQuantity>()
      expectThat(db.itemDao().getAllItemsSync()).hasSize(2)
      expectThat(db.transformationDao().getAllTransformationsSync()).hasSize(0)
    }

  @Test
  fun `blank name, bad servings and bad frozen count are rejected`() =
    runTest {
      val chicken = units("Chicken", 2)
      val spend = listOf(IngredientSpend(chicken, 1))

      expectThat(transform.transform(spend, listOf(result(name = " ")))).isFailure().isA<PantryError.BlankName>()
      expectThat(
        transform.transform(spend, listOf(result(servings = 100))),
      ).isFailure().isA<PantryError.InvalidQuantity>()
      expectThat(
        transform.transform(spend, listOf(result(servings = 2, frozen = 3))),
      ).isFailure().isA<PantryError.InvalidQuantity>()
      expectThat(
        transform.transform(spend, listOf(result(frozen = 1, days = -1))),
      ).isFailure().isA<PantryError.PastDateWhenFrozen>()
      expectThat(db.itemDao().getItem(chicken.single())!!.transformedDate).isNull()
    }
}
