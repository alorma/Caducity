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
import strikt.assertions.isSuccess

/** Measured products (Grams, Millilitres): one batch with an amount that is consumed, frozen and reset. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MeasuredAmountTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var addProduct: AddProductUseCase
  private lateinit var consumeAmount: ConsumeAmountUseCase
  private lateinit var freezeAmount: FreezeAmountUseCase
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
    consumeAmount = ConsumeAmountUseCase(pantry, clock)
    freezeAmount = FreezeAmountUseCase(pantry, clock)
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

  private suspend fun pasta(
    amount: Int = 500,
    days: Int = 30,
  ) = addProduct
    .add("Pasta", "c1", inDays(days), 1, frozen = false, unitType = UnitType.GRAMS, amount = amount)
    .getOrThrow()

  // region Adding

  @Test
  fun `a measured product is one batch with its amount and type`() =
    runTest {
      category("c1")
      val added =
        addProduct
          .add(
            "Pasta",
            "c1",
            inDays(30),
            quantity = 5,
            frozen = false,
            UnitType.GRAMS,
            500,
          ).getOrThrow()

      expectThat(added.itemIds).hasSize(1)
      expectThat(items().single().amount).isEqualTo(500)
      expectThat(
        db
          .productDao()
          .getAllProductsSync()
          .single()
          .unitType,
      ).isEqualTo(UnitType.GRAMS)
      expectThat(
        pantry
          .observeActiveItems()
          .first()
          .single()
          .amount,
      ).isEqualTo(500)
    }

  @Test
  fun `an amount out of range is rejected`() =
    runTest {
      category("c1")
      listOf(0, -5, UnitType.MAX_AMOUNT + 1).forEach { amount ->
        expectThat(addProduct.add("Pasta", "c1", inDays(30), 1, false, UnitType.GRAMS, amount))
          .isFailure()
          .isA<PantryError.InvalidAmount>()
      }
    }

  // endregion

  // region Consuming

  @Test
  fun `consuming part of a batch leaves the rest and undo puts it back`() =
    runTest {
      category("c1")
      val id = pasta().itemIds.single()

      val before = consumeAmount.consume(id, 100).getOrThrow()
      expectThat(db.itemDao().getItem(id)!!.amount).isEqualTo(400)
      expectThat(db.itemDao().getItem(id)!!.consumedDate).isNull()

      expectThat(restore.restoreSnapshots(listOf(before))).isSuccess()
      expectThat(db.itemDao().getItem(id)!!.amount).isEqualTo(500)
    }

  @Test
  fun `consuming the whole batch or more makes the product disappear`() =
    runTest {
      category("c1")
      val id = pasta().itemIds.single()
      consumeAmount.consume(id, 300).getOrThrow()
      expectThat(pantry.observeActiveItems().first()).hasSize(1)

      val before = consumeAmount.consume(id, 800).getOrThrow()
      expectThat(pantry.observeActiveItems().first()).hasSize(0)
      expectThat(db.itemDao().getItem(id)!!.consumedDate).isNotNull()

      restore.restoreSnapshots(listOf(before)).getOrThrow()
      expectThat(db.itemDao().getItem(id)!!.amount).isEqualTo(200)
      expectThat(pantry.observeActiveItems().first()).hasSize(1)
    }

  @Test
  fun `consuming an invalid amount or from a unit fails`() =
    runTest {
      category("c1")
      val id = pasta().itemIds.single()
      val unit =
        addProduct
          .add("Milk", "c1", inDays(3), 1, false)
          .getOrThrow()
          .itemIds
          .single()

      expectThat(consumeAmount.consume(id, 0)).isFailure().isA<PantryError.InvalidAmount>()
      expectThat(consumeAmount.consume(unit, 100)).isFailure().isA<PantryError.InvalidAmount>()
      expectThat(consumeAmount.consume("missing", 100)).isFailure().isA<PantryError.ItemNotFound>()
    }

  // endregion

  // region Freezing

  @Test
  fun `freezing part of a batch splits a frozen one and undo joins it back`() =
    runTest {
      category("c1")
      val id = pasta(days = 12).itemIds.single()

      val outcome = freezeAmount.freeze(id, 150).getOrThrow()
      expectThat(outcome.frozen.created).isEqualTo(true)
      val all = items().sortedBy { it.pausedDate != null }
      expectThat(all).hasSize(2)
      expectThat(all[0].amount).isEqualTo(350)
      expectThat(all[0].pausedDate).isNull()
      expectThat(all[1].amount).isEqualTo(150)
      expectThat(all[1].pausedDate).isNotNull()
      expectThat(all[1].remainingDays).isEqualTo(12)
      expectThat(all[1].expirationDate).isEqualTo(all[0].expirationDate)

      expectThat(freezeAmount.undo(outcome)).isSuccess()
      expectThat(items().single().amount).isEqualTo(500)
      expectThat(items().single().pausedDate).isNull()
    }

  @Test
  fun `freezing the whole batch freezes it as it is`() =
    runTest {
      category("c1")
      val id = pasta().itemIds.single()

      val outcome = freezeAmount.freeze(id, 500).getOrThrow()

      expectThat(outcome.frozen.created).isEqualTo(false)
      expectThat(items().single().pausedDate).isNotNull()
      expectThat(items().single().amount).isEqualTo(500)
      expectThat(freezeAmount.undo(outcome)).isSuccess()
      expectThat(items().single().pausedDate).isNull()
    }

  @Test
  fun `a batch that cannot be frozen fails and changes nothing`() =
    runTest {
      category("c1", canFreeze = false)
      val notFreezable = pasta().itemIds.single()
      expectThat(freezeAmount.freeze(notFreezable, 100)).isFailure().isA<PantryError.CannotFreeze>()
      expectThat(items().single().amount).isEqualTo(500)

      category("c2")
      val expired =
        addProduct
          .add("Rice", "c2", inDays(-1), 1, false, UnitType.GRAMS, 300)
          .getOrThrow()
          .itemIds
          .single()
      expectThat(freezeAmount.freeze(expired, 100)).isFailure().isA<PantryError.CannotFreeze>()
    }

  @Test
  fun `thawing part of a frozen batch splits an unfrozen one with today plus three days`() =
    runTest {
      category("c1")
      val id = pasta(days = 12).itemIds.single()
      freezeAmount.freeze(id, 500).getOrThrow()

      val outcome = freezeAmount.unfreeze(id, 200).getOrThrow()

      expectThat(outcome.frozen.created).isEqualTo(true)
      val all = items().sortedBy { it.pausedDate != null }
      expectThat(all).hasSize(2)
      expectThat(all[0].amount).isEqualTo(200)
      expectThat(all[0].pausedDate).isNull()
      expectThat(all[0].expirationDate)
        .isEqualTo(inDays(3).atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds())
      expectThat(all[1].amount).isEqualTo(300)
      expectThat(all[1].pausedDate).isNotNull()

      expectThat(freezeAmount.undo(outcome)).isSuccess()
      expectThat(items().single().amount).isEqualTo(500)
      expectThat(items().single().pausedDate).isNotNull()
    }

  @Test
  fun `thawing the whole frozen batch thaws it as it is and a batch that is not frozen fails`() =
    runTest {
      category("c1")
      val id = pasta().itemIds.single()
      expectThat(freezeAmount.unfreeze(id, 100)).isFailure().isA<PantryError.CannotFreeze>()

      freezeAmount.freeze(id, 500).getOrThrow()
      val outcome = freezeAmount.unfreeze(id, 500).getOrThrow()

      expectThat(outcome.frozen.created).isEqualTo(false)
      expectThat(items().single().pausedDate).isNull()
      expectThat(items().single().amount).isEqualTo(500)
    }

  @Test
  fun `consuming from a frozen batch leaves it frozen`() =
    runTest {
      category("c1")
      val id = pasta().itemIds.single()
      freezeAmount.freeze(id, 500).getOrThrow()

      consumeAmount.consume(id, 100).getOrThrow()

      expectThat(items().single().amount).isEqualTo(400)
      expectThat(items().single().pausedDate).isNotNull()
    }

  // endregion

  // region Transforma

  private fun soup() = TransformResult("Soup", 2, "c1", inDays(3), 0)

  @Test
  fun `an amount spent in a transformation takes the batches soonest first and undo gives it back`() =
    runTest {
      category("c1")
      val late = pasta(amount = 500, days = 20).itemIds.single()
      val soon = pasta(amount = 250, days = 10).itemIds.single()
      val transform = TransformItemsUseCase(pantry, clock)

      val batch =
        transform
          .transform(listOf(IngredientSpend(listOf(late, soon), count = 1, amount = 300)), listOf(soup()))
          .getOrThrow()

      val all = items().associateBy { it.id }
      expectThat(all.getValue(soon).transformedDate).isNotNull()
      expectThat(all.getValue(late).transformedDate).isNull()
      expectThat(all.getValue(late).amount).isEqualTo(450)

      expectThat(transform.undo(batch)).isSuccess()
      val back = items().associateBy { it.id }
      expectThat(back.getValue(soon).transformedDate).isNull()
      expectThat(back.getValue(late).amount).isEqualTo(500)
    }

  @Test
  fun `spending more than the product has in a transformation fails`() =
    runTest {
      category("c1")
      val id = pasta(amount = 500).itemIds.single()

      val result =
        TransformItemsUseCase(pantry, clock)
          .transform(listOf(IngredientSpend(listOf(id), count = 1, amount = 501)), listOf(soup()))

      expectThat(result).isFailure().isA<PantryError.InvalidAmount>()
      expectThat(items().single().amount).isEqualTo(500)
    }

  @Test
  fun `a measured result is one batch with its amount and sets the type of its product`() =
    runTest {
      category("c1")
      val id = pasta().itemIds.single()
      val result = TransformResult("Broth", 1, "c1", inDays(3), 0, UnitType.MILLILITRES, 750)

      val batch =
        TransformItemsUseCase(pantry, clock)
          .transform(listOf(IngredientSpend(listOf(id), count = 1, amount = 100)), listOf(result))
          .getOrThrow()

      expectThat(batch.resultItemIds).hasSize(1)
      val created = items().first { it.id in batch.resultItemIds }
      expectThat(created.amount).isEqualTo(750)
      val product = db.productDao().getAllProductsSync().first { it.name == "Broth" }
      expectThat(product.unitType).isEqualTo(UnitType.MILLILITRES)
    }

  @Test
  fun `a measured result with several servings or a bad amount fails`() =
    runTest {
      category("c1")
      val transform = TransformItemsUseCase(pantry, clock)
      val severalServings = TransformResult("Broth", 2, "c1", inDays(3), 0, UnitType.GRAMS, 500)
      val badAmount = TransformResult("Broth", 1, "c1", inDays(3), 0, UnitType.GRAMS, 0)

      expectThat(transform.transform(emptyList(), listOf(severalServings))).isFailure().isA<PantryError.InvalidAmount>()
      expectThat(transform.transform(emptyList(), listOf(badAmount))).isFailure().isA<PantryError.InvalidAmount>()
      expectThat(items()).hasSize(0)
    }

  // endregion

  // region Changing the type

  @Test
  fun `from units to measured keeps the batch that expires first with the default amount`() =
    runTest {
      category("c1")
      val milk = addProduct.add("Milk", "c1", inDays(8), 2, false).getOrThrow()
      val nearer = addProduct.add("Milk", "c1", inDays(3), 1, false).getOrThrow()

      setUnitType.set(milk.productId, UnitType.GRAMS).getOrThrow()

      val left = items()
      expectThat(left).hasSize(1)
      expectThat(left.single().id).isEqualTo(nearer.itemIds.single())
      expectThat(left.single().amount).isEqualTo(500)
    }

  @Test
  fun `from measured to units every batch becomes a unit`() =
    runTest {
      category("c1")
      val added = pasta()
      freezeAmount.freeze(added.itemIds.single(), 100).getOrThrow()

      setUnitType.set(added.productId, UnitType.PACKS).getOrThrow()

      expectThat(items()).hasSize(2)
      expectThat(items().all { it.amount == null }).isEqualTo(true)
    }

  @Test
  fun `grams to millilitres keeps the amount`() =
    runTest {
      category("c1")
      val added = pasta(amount = 750)

      setUnitType.set(added.productId, UnitType.MILLILITRES).getOrThrow()

      expectThat(items().single().amount).isEqualTo(750)
    }

  @Test
  fun `adding as measured to an existing product of units resets it first`() =
    runTest {
      category("c1")
      addProduct.add("Pasta", "c1", inDays(8), 3, false).getOrThrow()

      addProduct.add("Pasta", "c1", inDays(30), 1, false, UnitType.GRAMS, 250).getOrThrow()

      val left = items()
      expectThat(left).hasSize(2)
      expectThat(left.map { it.amount }.toSet()).isEqualTo(setOf(500, 250))
    }

  // endregion
}
