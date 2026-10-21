package com.alorma.caducity.domain.usecase

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomItemDataSource
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.domain.model.HandfulLevel
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.UnitType
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotNull
import strikt.assertions.isNull

/** Handful products (Grapats): one batch with a level that goes down, is set, frozen and reset. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class HandfulLevelTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var addProduct: AddProductUseCase
  private lateinit var consume: ConsumeItemUseCase
  private lateinit var setLevel: SetHandfulLevelUseCase
  private lateinit var restore: RestoreItemsUseCase
  private lateinit var setUnitType: SetProductUnitTypeUseCase

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
    addProduct = AddProductUseCase(pantry, clock)
    consume =
      ConsumeItemUseCase(
        RoomItemDataSource(db.itemDao(), clock, itemMapper),
        pantry,
        clock,
      )
    setLevel = SetHandfulLevelUseCase(pantry)
    restore = RestoreItemsUseCase(pantry)
    setUnitType = SetProductUnitTypeUseCase(pantry)
  }

  @After
  fun tearDown() = db.close()

  private suspend fun category(
    id: String,
    canFreeze: Boolean = true,
  ) = db.categoryDao().insertCategory(CategoryRoomEntity(id, id, "", canFreeze = canFreeze))

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun items() = db.itemDao().getAllItemsSync()

  private suspend fun peanuts(level: HandfulLevel = HandfulLevel.LOTS) =
    addProduct
      .add("Peanuts", "c1", inDays(30), 4, frozen = false, unitType = UnitType.HANDFULS, level = level)
      .getOrThrow()

  @Test
  fun `a handful product is one batch with its level and type`() =
    runTest {
      category("c1")
      val added = peanuts(HandfulLevel.HALF)

      expectThat(added.itemIds).hasSize(1)
      expectThat(items().single().level).isEqualTo(HandfulLevel.HALF)
      expectThat(
        db
          .productDao()
          .getAllProductsSync()
          .single()
          .unitType,
      ).isEqualTo(UnitType.HANDFULS)
      expectThat(
        pantry
          .observeActiveItems()
          .first()
          .single()
          .level,
      ).isEqualTo(HandfulLevel.HALF)
    }

  @Test
  fun `lowering goes lots then half then little then consumed, and undo gives each back`() =
    runTest {
      category("c1")
      val id = peanuts().itemIds.single()

      val first = consume.lowerLevel(id).getOrThrow()
      expectThat(items().single().level).isEqualTo(HandfulLevel.HALF)
      val second = consume.lowerLevel(id).getOrThrow()
      expectThat(items().single().level).isEqualTo(HandfulLevel.LITTLE)
      val third = consume.lowerLevel(id).getOrThrow()
      expectThat(items().single().consumedDate).isNotNull()

      restore.restoreSnapshots(listOf(third)).getOrThrow()
      expectThat(items().single().consumedDate).isNull()
      expectThat(items().single().level).isEqualTo(HandfulLevel.LITTLE)
      restore.restoreSnapshots(listOf(second)).getOrThrow()
      expectThat(items().single().level).isEqualTo(HandfulLevel.HALF)
      restore.restoreSnapshots(listOf(first)).getOrThrow()
      expectThat(items().single().level).isEqualTo(HandfulLevel.LOTS)
    }

  @Test
  fun `setting a level takes it at once and undo restores the previous one`() =
    runTest {
      category("c1")
      val id = peanuts().itemIds.single()

      val before = setLevel.set(id, HandfulLevel.LITTLE).getOrThrow()
      expectThat(items().single().level).isEqualTo(HandfulLevel.LITTLE)

      restore.restoreSnapshots(listOf(before)).getOrThrow()
      expectThat(items().single().level).isEqualTo(HandfulLevel.LOTS)
    }

  @Test
  fun `a batch that is not a handful has no level to lower or set`() =
    runTest {
      category("c1")
      val unit =
        addProduct
          .add("Milk", "c1", inDays(3), 1, false)
          .getOrThrow()
          .itemIds
          .single()

      expectThat(consume.lowerLevel(unit)).isFailure().isA<PantryError.NotAHandful>()
      expectThat(setLevel.set(unit, HandfulLevel.HALF)).isFailure().isA<PantryError.NotAHandful>()
      expectThat(consume.lowerLevel("missing")).isFailure().isA<PantryError.ItemNotFound>()
    }

  @Test
  fun `freezing the whole product keeps its level and undo thaws it`() =
    runTest {
      category("c1")
      val id = peanuts(HandfulLevel.HALF).itemIds.single()
      val before = restore.snapshots(listOf(id))

      FreezeItemUseCase(
        com.alorma.caducity.data.datasource.RoomItemDataSource(
          db.itemDao(),
          clock,
          ItemRoomMapper(clock, AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings()))),
        ),
        pantry,
        clock,
      ).freeze(listOf(id))
        .getOrThrow()

      expectThat(items().single().pausedDate).isNotNull()
      expectThat(items().single().level).isEqualTo(HandfulLevel.HALF)
      restore.restoreSnapshots(before).getOrThrow()
      expectThat(items().single().pausedDate).isNull()
    }

  @Test
  fun `lowering a frozen handful keeps it frozen until the last level consumes it`() =
    runTest {
      category("c1")
      val id =
        addProduct
          .add("Peanuts", "c1", inDays(30), 1, frozen = true, unitType = UnitType.HANDFULS, level = HandfulLevel.HALF)
          .getOrThrow()
          .itemIds
          .single()

      consume.lowerLevel(id).getOrThrow()
      expectThat(items().single().level).isEqualTo(HandfulLevel.LITTLE)
      expectThat(items().single().pausedDate).isNotNull()

      consume.lowerLevel(id).getOrThrow()
      expectThat(items().single().consumedDate).isNotNull()
      expectThat(items().single().pausedDate).isNull()
    }

  @Test
  fun `thawing a frozen handful gives it today plus three days and keeps its level`() =
    runTest {
      category("c1")
      val id =
        addProduct
          .add("Peanuts", "c1", inDays(30), 1, frozen = true, unitType = UnitType.HANDFULS, level = HandfulLevel.HALF)
          .getOrThrow()
          .itemIds
          .single()

      pantry.unfreezeAll(listOf(id), today.plus(3, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.currentSystemDefault()))

      expectThat(items().single().pausedDate).isNull()
      expectThat(items().single().level).isEqualTo(HandfulLevel.HALF)
    }

  @Test
  fun `from units to handfuls keeps the batch that expires first at a lot`() =
    runTest {
      category("c1")
      val far = addProduct.add("Peanuts", "c1", inDays(40), 2, false).getOrThrow()
      val near = addProduct.add("Peanuts", "c1", inDays(5), 1, false).getOrThrow()

      setUnitType.set(far.productId, UnitType.HANDFULS).getOrThrow()

      expectThat(items()).hasSize(1)
      expectThat(items().single().id).isEqualTo(near.itemIds.single())
      expectThat(items().single().level).isEqualTo(HandfulLevel.LOTS)
      expectThat(items().single().amount).isNull()
    }

  @Test
  fun `from handfuls to units or measured clears the level`() =
    runTest {
      category("c1")
      val added = peanuts(HandfulLevel.LITTLE)

      setUnitType.set(added.productId, UnitType.GRAMS).getOrThrow()
      expectThat(items().single().level).isNull()
      expectThat(items().single().amount).isEqualTo(500)

      setUnitType.set(added.productId, UnitType.HANDFULS).getOrThrow()
      expectThat(items().single().amount).isNull()
      expectThat(items().single().level).isEqualTo(HandfulLevel.LOTS)

      setUnitType.set(added.productId, UnitType.TUPPERS).getOrThrow()
      expectThat(items().single().level).isNull()
    }

  @Test
  fun `adding as handfuls to an existing product of units resets it first`() =
    runTest {
      category("c1")
      addProduct.add("Peanuts", "c1", inDays(8), 3, false).getOrThrow()

      addProduct.add("Peanuts", "c1", inDays(30), 1, false, UnitType.HANDFULS, level = HandfulLevel.HALF).getOrThrow()

      expectThat(items().map { it.level }.toSet()).isEqualTo(setOf(HandfulLevel.LOTS, HandfulLevel.HALF))
    }
}
