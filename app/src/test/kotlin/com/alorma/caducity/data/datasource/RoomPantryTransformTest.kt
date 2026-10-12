package com.alorma.caducity.data.datasource

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.domain.model.TransformIngredient
import com.alorma.caducity.domain.model.TransformResult
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
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
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class RoomPantryTransformTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource

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
  }

  @After
  fun tearDown() = db.close()

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun ingredient(
    id: String,
    days: Int,
    frozen: Boolean = false,
  ) {
    if (db.categoryDao().getCategoriesOrderedSync().none { it.id == "c1" }) {
      db.categoryDao().insertCategory(CategoryRoomEntity("c1", "c1", "", sortOrder = 0))
    }
    db.itemDao().insertItem(
      ItemRoomEntity(
        id = id,
        categoryId = "c1",
        identifier = id,
        expirationDate = inDays(days).toEpochDays() * 86_400_000L,
        pausedDate = clock.now().toEpochMilliseconds().takeIf { frozen },
        remainingDays = days.takeIf { frozen },
      ),
    )
  }

  private fun result(
    name: String = "Broth",
    servings: Int = 4,
    frozen: Int = 0,
    days: Int = 3,
  ) = TransformResult(name, servings, "c1", inDays(days), frozen)

  private suspend fun activeIds() = pantry.observeActiveItems().first().map { it.itemId }

  @Test
  fun `ingredients leave the live queries and results enter`() =
    runTest {
      ingredient("a", 2)
      ingredient("b", 5)
      ingredient("keep", 9)

      val batch = pantry.transform(listOf(TransformIngredient(listOf("a", "b"))), listOf(result()), clock.now())

      val active = pantry.observeActiveItems().first()
      expectThat(active.map { it.itemId }).containsExactlyInAnyOrder(listOf("keep") + batch.resultItemIds)
      expectThat(db.itemDao().getActiveItems().first()).hasSize(5)
      expectThat(db.itemDao().getItem("a")!!.transformationId).isEqualTo(batch.transformationId)
      expectThat(db.itemDao().getItem("a")!!.transformedDate).isEqualTo(clock.now().toEpochMilliseconds())
    }

  @Test
  fun `each serving is an item of the same product with the origin transformation`() =
    runTest {
      ingredient("a", 2)

      val batch = pantry.transform(listOf(TransformIngredient(listOf("a"))), listOf(result(servings = 4)), clock.now())

      val created = batch.resultItemIds.map { db.itemDao().getItem(it)!! }
      expectThat(created).hasSize(4)
      expectThat(created.map { it.productId }.distinct()).hasSize(1)
      expectThat(created.map { it.originTransformationId }.distinct()).containsExactlyInAnyOrder(batch.transformationId)
      expectThat(created.map { it.createdAt }.distinct()).containsExactlyInAnyOrder(clock.now().toEpochMilliseconds())
      expectThat(batch.createdProductIds).hasSize(1)
    }

  @Test
  fun `frozen servings come out frozen and the rest fresh`() =
    runTest {
      ingredient("a", 2)

      val batch =
        pantry.transform(
          listOf(TransformIngredient(listOf("a"))),
          listOf(result(servings = 4, frozen = 2)),
          clock.now(),
        )

      val created = batch.resultItemIds.map { db.itemDao().getItem(it)!! }
      expectThat(created.count { it.pausedDate != null }).isEqualTo(2)
      expectThat(
        created.filter { it.pausedDate != null }.map { it.remainingDays }.distinct(),
      ).containsExactlyInAnyOrder(3)
      expectThat(
        created.filter { it.pausedDate == null }.map { it.remainingDays }.distinct(),
      ).containsExactlyInAnyOrder(null)
    }

  @Test
  fun `a result reuses the product that already exists in the category`() =
    runTest {
      ingredient("a", 2)
      val first = pantry.addBatch("c1", "Broth", inDays(1).toInstant(), 1, null)

      val batch =
        pantry.transform(
          listOf(TransformIngredient(listOf("a"))),
          listOf(result(name = "broth")),
          clock.now(),
        )

      expectThat(batch.createdProductIds).isEmpty()
      expectThat(db.itemDao().getItem(batch.resultItemIds.first())!!.productId).isEqualTo(first.productId)
    }

  @Test
  fun `a frozen ingredient leaves the freezer when transformed`() =
    runTest {
      ingredient("a", 4, frozen = true)

      pantry.transform(listOf(TransformIngredient(listOf("a"))), listOf(result()), clock.now())

      expectThat(db.itemDao().getItem("a")!!.pausedDate).isNull()
      expectThat(activeIds()).hasSize(4)
    }

  @Test
  fun `only the chosen units of an ingredient are spent`() =
    runTest {
      ingredient("u1", 2)
      ingredient("u2", 6)

      pantry.transform(listOf(TransformIngredient(listOf("u1"))), listOf(result(servings = 1)), clock.now())

      expectThat(db.itemDao().getItem("u1")!!.transformedDate).isNotNull()
      expectThat(db.itemDao().getItem("u2")!!.transformedDate).isNull()
      expectThat(activeIds()).hasSize(2)
    }

  @Test
  fun `undo leaves the pantry identical`() =
    runTest {
      ingredient("a", 2)
      ingredient("b", 5, frozen = true)
      val itemsBefore = db.itemDao().getAllItemsSync().sortedBy { it.id }
      val productsBefore = db.productDao().getAllProductsSync()

      val batch =
        pantry.transform(
          listOf(TransformIngredient(listOf("a", "b"))),
          listOf(result(servings = 3, frozen = 1), result(name = "Bones", servings = 1)),
          clock.now(),
        )
      pantry.undoTransform(batch)

      expectThat(db.itemDao().getAllItemsSync().sortedBy { it.id }).isEqualTo(itemsBefore)
      expectThat(db.productDao().getAllProductsSync()).isEqualTo(productsBefore)
      expectThat(db.transformationDao().getAllTransformationsSync()).isEmpty()
    }

  private fun LocalDate.toInstant() = Instant.fromEpochMilliseconds(toEpochDays() * 86_400_000L)
}
