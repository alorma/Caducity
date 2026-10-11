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
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.model.SeedCategory
import com.alorma.caducity.domain.model.SuggestedCategory
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
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isSuccess

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PantryUseCasesTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()

  private lateinit var db: AppDatabase
  private lateinit var itemDataSource: RoomItemDataSource
  private lateinit var pantry: RoomPantryDataSource

  private lateinit var addBatch: AddItemBatchUseCase
  private lateinit var undoAdd: UndoAddItemsUseCase
  private lateinit var discard: DiscardItemsUseCase
  private lateinit var restore: RestoreItemsUseCase
  private lateinit var updateItem: UpdateItemUseCase
  private lateinit var moveItem: MoveItemToCategoryUseCase
  private lateinit var suggestions: GetNameSuggestionsUseCase
  private lateinit var updateCategory: UpdateCategoryUseCase
  private lateinit var seed: SeedCategoriesUseCase
  private lateinit var unfreeze: UnfreezeItemUseCase
  private lateinit var consume: ConsumeItemUseCase
  private lateinit var freeze: FreezeItemUseCase
  private lateinit var reschedule: RescheduleItemUseCase

  @Before
  fun setUp() {
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
    undoAdd = UndoAddItemsUseCase(pantry)
    discard = DiscardItemsUseCase(pantry, clock)
    restore = RestoreItemsUseCase(pantry)
    updateItem = UpdateItemUseCase(itemDataSource, pantry, clock)
    moveItem = MoveItemToCategoryUseCase(pantry)
    suggestions = GetNameSuggestionsUseCase(pantry)
    updateCategory = UpdateCategoryUseCase(pantry)
    seed = SeedCategoriesUseCase(pantry)
    unfreeze = UnfreezeItemUseCase(itemDataSource, pantry, clock)
    consume = ConsumeItemUseCase(itemDataSource, pantry, clock)
    freeze = FreezeItemUseCase(itemDataSource, pantry, clock)
    reschedule = RescheduleItemUseCase(itemDataSource, pantry)
  }

  @After
  fun tearDown() = db.close()

  private suspend fun category(
    id: String,
    name: String = id,
    sortOrder: Int = 0,
  ) = db.categoryDao().insertCategory(CategoryRoomEntity(id, name, "", sortOrder = sortOrder))

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun items() = db.itemDao().getAllItemsSync()

  private suspend fun products() = db.productDao().getAllProductsSync()

  // region AddItemBatchUseCase

  @Test
  fun `add creates N independent items with the same date and no pack`() =
    runTest {
      category("c1")
      val batch = addBatch.add("Milk", "c1", inDays(5), quantity = 3, frozen = false).getOrThrow()

      expectThat(batch.itemIds).hasSize(3)
      expectThat(batch.productCreated).isEqualTo(true)
      val created = items()
      expectThat(created).hasSize(3)
      expectThat(created.map { it.expirationDate }.distinct()).hasSize(1)
      expectThat(created.map { it.packSize }.distinct()).isEqualTo(listOf(null))
      expectThat(created.map { it.identifier }.distinct()).isEqualTo(listOf(""))
      expectThat(created.map { it.productId }.distinct()).isEqualTo(listOf(batch.productId))
      expectThat(created.all { it.createdAt == clock.now().toEpochMilliseconds() }).isEqualTo(true)
    }

  @Test
  fun `add finds the existing product ignoring case, accents and spaces`() =
    runTest {
      category("c1")
      val first = addBatch.add("Café  Molt", "c1", inDays(1), 1, false).getOrThrow()
      val second = addBatch.add("  cafe molt ", "c1", inDays(2), 1, false).getOrThrow()

      expectThat(second.productId).isEqualTo(first.productId)
      expectThat(second.productCreated).isEqualTo(false)
      expectThat(products()).hasSize(1)
      expectThat(products().single().name).isEqualTo("Café Molt")
    }

  @Test
  fun `same name in another category creates another product`() =
    runTest {
      category("c1")
      category("c2")
      addBatch.add("Milk", "c1", inDays(1), 1, false)
      addBatch.add("Milk", "c2", inDays(1), 1, false)
      expectThat(products()).hasSize(2)
    }

  @Test
  fun `undo removes the N items and the product it created`() =
    runTest {
      category("c1")
      val batch = addBatch.add("Milk", "c1", inDays(5), 4, false).getOrThrow()
      undoAdd.undo(batch).getOrThrow()

      expectThat(items()).isEmpty()
      expectThat(products()).isEmpty()
    }

  @Test
  fun `undo keeps a product that already existed or has other items`() =
    runTest {
      category("c1")
      val first = addBatch.add("Milk", "c1", inDays(5), 1, false).getOrThrow()
      val second = addBatch.add("Milk", "c1", inDays(6), 2, false).getOrThrow()
      undoAdd.undo(second).getOrThrow()

      expectThat(items().map { it.id }).isEqualTo(first.itemIds)
      expectThat(products()).hasSize(1)
    }

  @Test
  fun `add validates name and quantity range`() =
    runTest {
      category("c1")
      expectThat(addBatch.add(" ", "c1", inDays(1), 1, false)).isFailure().isA<PantryError.BlankName>()
      expectThat(addBatch.add("Milk", "c1", inDays(1), 0, false)).isFailure().isA<PantryError.InvalidQuantity>()
      expectThat(addBatch.add("Milk", "c1", inDays(1), AddItemBatchUseCase.MAX_QUANTITY + 1, false))
        .isFailure()
        .isA<PantryError.InvalidQuantity>()
      expectThat(addBatch.add("Milk", "c1", inDays(1), AddItemBatchUseCase.MAX_QUANTITY, false)).isSuccess()
      expectThat(items()).hasSize(AddItemBatchUseCase.MAX_QUANTITY)
    }

  @Test
  fun `frozen add stores paused state with remaining days and rejects past dates`() =
    runTest {
      category("c1")
      expectThat(
        addBatch.add("Peas", "c1", inDays(-1), 1, frozen = true),
      ).isFailure().isA<PantryError.PastDateWhenFrozen>()
      expectThat(items()).isEmpty()

      addBatch.add("Peas", "c1", inDays(30), 2, frozen = true).getOrThrow()
      expectThat(items().map { it.remainingDays }).isEqualTo(listOf(30, 30))
      expectThat(items().all { it.pausedDate != null }).isEqualTo(true)
    }

  @Test
  fun `a past date is allowed when not frozen`() =
    runTest {
      category("c1")
      expectThat(addBatch.add("Milk", "c1", inDays(-3), 1, frozen = false)).isSuccess()
    }

  // endregion

  // region Discard / restore / consume

  @Test
  fun `discard unfreezes and restore makes the item active again`() =
    runTest {
      category("c1")
      val id =
        addBatch
          .add("Peas", "c1", inDays(30), 1, frozen = true)
          .getOrThrow()
          .itemIds
          .single()

      discard.discard(listOf(id)).getOrThrow()
      val discarded = db.itemDao().getItem(id)!!
      expectThat(discarded.discardedDate).isEqualTo(clock.now().toEpochMilliseconds())
      expectThat(discarded.pausedDate).isNull()
      expectThat(itemDataSource.getItem(id)?.isActive).isEqualTo(false)

      restore.restore(listOf(id)).getOrThrow()
      expectThat(itemDataSource.getItem(id)?.isActive).isEqualTo(true)
    }

  @Test
  fun `consume is allowed on expired items and restore undoes it`() =
    runTest {
      category("c1")
      val id =
        addBatch
          .add("Milk", "c1", inDays(-5), 1, frozen = false)
          .getOrThrow()
          .itemIds
          .single()

      expectThat(consume.consume(id)).isSuccess()
      expectThat(db.itemDao().getItem(id)?.consumedDate).isNotNull()
      restore.restore(listOf(id)).getOrThrow()
      expectThat(db.itemDao().getItem(id)?.consumedDate).isNull()
    }

  @Test
  fun `consume of an unknown item fails`() =
    runTest {
      expectThat(consume.consume("missing")).isFailure()
    }

  // endregion

  // region Update / move

  @Test
  fun `update renames into a new product keeping the old one`() =
    runTest {
      category("c1")
      val ids = addBatch.add("Milk", "c1", inDays(2), 2, false).getOrThrow().itemIds

      updateItem.update(ids[0], "Oat drink", "c1", inDays(10)).getOrThrow()

      val edited = db.itemDao().getItem(ids[0])!!
      val untouched = db.itemDao().getItem(ids[1])!!
      expectThat(products().map { it.name }).containsExactly("Milk", "Oat drink")
      expectThat(edited.productId).isNotNull()
      expectThat(edited.productId == untouched.productId).isEqualTo(false)
      expectThat(edited.expirationDate).isEqualTo(inDays(10).toExpirationInstant().toEpochMilliseconds())
    }

  @Test
  fun `update joins an existing product with a matching name and keeps the old one even if empty`() =
    runTest {
      category("c1")
      val milk = addBatch.add("Milk", "c1", inDays(2), 1, false).getOrThrow()
      val yogurt = addBatch.add("Yogurt", "c1", inDays(2), 1, false).getOrThrow()

      updateItem.update(milk.itemIds.single(), "yogurt", "c1", inDays(2)).getOrThrow()

      expectThat(db.itemDao().getItem(milk.itemIds.single())?.productId).isEqualTo(yogurt.productId)
      expectThat(products()).hasSize(2)
    }

  @Test
  fun `update of a frozen unit recomputes remaining days and rejects past dates`() =
    runTest {
      category("c1")
      val id =
        addBatch
          .add("Peas", "c1", inDays(30), 1, frozen = true)
          .getOrThrow()
          .itemIds
          .single()

      expectThat(updateItem.update(id, "Peas", "c1", inDays(-1))).isFailure().isA<PantryError.PastDateWhenFrozen>()
      updateItem.update(id, "Peas", "c1", inDays(45)).getOrThrow()

      val item = db.itemDao().getItem(id)!!
      expectThat(item.remainingDays).isEqualTo(45)
      expectThat(item.pausedDate).isNotNull()
    }

  @Test
  fun `update validates blank name and unknown item`() =
    runTest {
      category("c1")
      expectThat(updateItem.update("x", " ", "c1", today)).isFailure().isA<PantryError.BlankName>()
      expectThat(updateItem.update("missing", "Milk", "c1", today)).isFailure().isA<PantryError.ItemNotFound>()
    }

  @Test
  fun `move finds or creates the homonymous product in the destination`() =
    runTest {
      category("c1")
      category("c2")
      val ids = addBatch.add("Milk", "c1", inDays(2), 2, false).getOrThrow().itemIds
      val existing = addBatch.add("MILK", "c2", inDays(9), 1, false).getOrThrow()

      moveItem.move(ids[0], "c2").getOrThrow()
      expectThat(db.itemDao().getItem(ids[0])?.categoryId).isEqualTo("c2")
      expectThat(db.itemDao().getItem(ids[0])?.productId).isEqualTo(existing.productId)

      category("c3")
      moveItem.move(ids[1], "c3").getOrThrow()
      val moved = db.itemDao().getItem(ids[1])!!
      expectThat(db.productDao().getProduct(moved.productId!!)?.categoryId).isEqualTo("c3")
      expectThat(db.productDao().getProduct(moved.productId!!)?.name).isEqualTo("Milk")
      expectThat(db.productDao().getProduct(moved.productId!!)?.id == existing.productId).isEqualTo(false)
    }

  @Test
  fun `move of an item without product keeps it standalone`() =
    runTest {
      category("c1")
      category("c2")
      db.itemDao().insertItem(ItemRoomEntity("legacy", "c1", "", null, 1000))
      moveItem.move("legacy", "c2").getOrThrow()
      val moved = db.itemDao().getItem("legacy")!!
      expectThat(moved.categoryId).isEqualTo("c2")
      expectThat(moved.productId).isNull()
    }

  // endregion

  // region Suggestions

  @Test
  fun `suggestions match without accents or case, prefix first, then usage, max 3`() =
    runTest {
      category("c1")
      addBatch.add("Iogurt natural", "c1", inDays(1), 1, false)
      repeat(3) { addBatch.add("Bio iogurt", "c1", inDays(1), 1, false) }
      addBatch.add("Iogurt grec", "c1", inDays(1), 2, false)
      addBatch.add("Llet", "c1", inDays(1), 1, false)
      addBatch.add("Iogurt de fruites", "c1", inDays(1), 1, false)

      val result = suggestions.suggest("IÒGURT").map { it.name }
      // Prefix matches first ("Iogurt grec" has 2 uses; ties break by name), then the contains match, max 3.
      expectThat(result).containsExactly("Iogurt grec", "Iogurt de fruites", "Iogurt natural")
      expectThat(suggestions.suggest("bio")).hasSize(1)
      expectThat(suggestions.suggest("  ")).isEmpty()
      expectThat(suggestions.suggest("zzz")).isEmpty()
    }

  @Test
  fun `suggestions count consumed and discarded items as usage`() =
    runTest {
      category("c1")
      val a = addBatch.add("Pasta", "c1", inDays(1), 1, false).getOrThrow()
      val b = addBatch.add("Pizza", "c1", inDays(1), 1, false).getOrThrow()
      repeat(2) { addBatch.add("Pizza", "c1", inDays(1), 1, false) }
      discard.discard(b.itemIds)
      consume.consume(a.itemIds.single())

      expectThat(suggestions.suggest("pi").map { it.name }).isEqualTo(listOf("Pizza"))
      expectThat(suggestions.suggest("p").map { it.name }).containsExactly("Pizza", "Pasta")
    }

  // endregion

  // region Categories

  @Test
  fun `updating a category keeps its items and products`() =
    runTest {
      category("c1", "Dairy")
      addBatch.add("Milk", "c1", inDays(2), 3, false)

      updateCategory.update("c1", "Lácteos", CategoryShape.PUFFY, CategoryIcon("nutrition")).getOrThrow()

      val updated = db.categoryDao().getCategory("c1")!!
      expectThat(updated.name).isEqualTo("Lácteos")
      expectThat(updated.shape).isEqualTo(CategoryShape.PUFFY)
      expectThat(updated.icon).isEqualTo(CategoryIcon("nutrition"))
      expectThat(items()).hasSize(3)
      expectThat(products()).hasSize(1)
    }

  @Test
  fun `updating a category rejects duplicates, blanks and unknown ids`() =
    runTest {
      category("c1", "Dairy", 0)
      category("c2", "Pantry", 1)
      val shape = CategoryShape.COOKIE
      val icon = CategoryIcon.DEFAULT
      expectThat(
        updateCategory.update("c2", " dairy ", shape, icon),
      ).isFailure().isA<PantryError.DuplicateCategoryName>()
      expectThat(updateCategory.update("c2", "  ", shape, icon)).isFailure().isA<PantryError.BlankName>()
      expectThat(updateCategory.update("zzz", "New", shape, icon)).isFailure().isA<PantryError.CategoryNotFound>()
      expectThat(updateCategory.update("c2", "pantry", shape, icon)).isSuccess()
    }

  @Test
  fun `seed creates the chosen suggested categories once, in order, with their shape and icon`() =
    runTest {
      val chosen =
        listOf(SuggestedCategory.COLD_CUTS, SuggestedCategory.DAIRY, SuggestedCategory.READY_MEALS)
          .map { SeedCategory(it.name.lowercase(), it.shape, it.icon) }
      expectThat(seed.seed(chosen).getOrThrow()).isEqualTo(3)
      expectThat(seed.seed(listOf(SeedCategory("again", CategoryShape.COOKIE, CategoryIcon.DEFAULT))).getOrThrow())
        .isEqualTo(0)

      val categories = pantry.getCategories()
      expectThat(categories.map { it.name }).containsExactly("cold_cuts", "dairy", "ready_meals")
      expectThat(categories.map { it.sortOrder }).containsExactly(0, 1, 2)
      expectThat(categories.map { it.shape })
        .containsExactly(CategoryShape.COOKIE, CategoryShape.COOKIE, CategoryShape.CLOVER8)
      expectThat(categories.map { it.icon.id }).containsExactly("kebab_dining", "water_full", "soup_kitchen")
      expectThat(pantry.getNextSortOrder()).isEqualTo(3)
    }

  @Test
  fun `seed with no categories is allowed and creates nothing`() =
    runTest {
      expectThat(seed.seed(emptyList()).getOrThrow()).isEqualTo(0)
      expectThat(pantry.getCategories()).isEmpty()
    }

  // endregion

  // region Unfreeze

  @Test
  fun `unfreeze proposes today plus remaining days and applies the chosen date`() =
    runTest {
      category("c1")
      val id =
        addBatch
          .add("Peas", "c1", inDays(30), 1, frozen = true)
          .getOrThrow()
          .itemIds
          .single()

      expectThat(unfreeze.proposedDate(id)).isEqualTo(inDays(30))
      unfreeze.unfreezeOn(id, inDays(12)).getOrThrow()

      val item = db.itemDao().getItem(id)!!
      expectThat(item.pausedDate).isNull()
      expectThat(item.remainingDays).isNull()
      expectThat(item.expirationDate).isEqualTo(inDays(12).toExpirationInstant().toEpochMilliseconds())
      expectThat(unfreeze.proposedDate(id)).isNull()
    }

  // endregion

  // region Bulk actions

  private suspend fun addMany(
    vararg days: Int,
    frozen: Boolean = false,
  ) = days.map {
    addBatch
      .add("P$it", "c1", inDays(it), 1, frozen)
      .getOrThrow()
      .itemIds
      .single()
  }

  @Test
  fun `bulk consume marks every unit and the snapshot undo brings frozen units back frozen`() =
    runTest {
      category("c1")
      val ids = addMany(5, 9) + addMany(20, frozen = true)
      val snapshots = restore.snapshots(ids)

      consume.consume(ids).getOrThrow()
      ids.forEach { expectThat(db.itemDao().getItem(it)!!.consumedDate).isNotNull() }

      restore.restoreSnapshots(snapshots).getOrThrow()
      ids.forEach { expectThat(db.itemDao().getItem(it)!!.consumedDate).isNull() }
      val frozen = db.itemDao().getItem(ids.last())!!
      expectThat(frozen.pausedDate).isNotNull()
      expectThat(frozen.remainingDays).isEqualTo(20)
    }

  @Test
  fun `bulk freeze freezes with remaining days, skips expired and undoes exactly`() =
    runTest {
      category("c1")
      val ids = addMany(4, 10, -1)
      val snapshots = restore.snapshots(ids)

      val frozen = freeze.freeze(ids).getOrThrow()
      expectThat(frozen).containsExactly(ids[0], ids[1])
      expectThat(db.itemDao().getItem(ids[0])!!.remainingDays).isEqualTo(4)
      expectThat(db.itemDao().getItem(ids[1])!!.remainingDays).isEqualTo(10)
      expectThat(db.itemDao().getItem(ids[2])!!.pausedDate).isNull()

      restore.restoreSnapshots(snapshots).getOrThrow()
      ids.forEach {
        expectThat(db.itemDao().getItem(it)!!.pausedDate).isNull()
        expectThat(db.itemDao().getItem(it)!!.remainingDays).isNull()
      }
    }

  @Test
  fun `bulk reschedule moves every unit to the same date and undo restores each one`() =
    runTest {
      category("c1")
      val ids = addMany(-3, -1)
      val before = restore.snapshots(ids)

      reschedule.reschedule(ids, inDays(5)).getOrThrow()
      ids.forEach {
        expectThat(
          db.itemDao().getItem(it)!!.expirationDate,
        ).isEqualTo(inDays(5).toExpirationInstant().toEpochMilliseconds())
      }

      restore.restoreSnapshots(before).getOrThrow()
      expectThat(db.itemDao().getItem(ids[0])!!.expirationDate).isEqualTo(before[0].expirationMillis)
      expectThat(db.itemDao().getItem(ids[1])!!.expirationDate).isEqualTo(before[1].expirationMillis)
    }

  @Test
  fun `bulk discard of expired units is undone by the snapshot`() =
    runTest {
      category("c1")
      val ids = addMany(-3, -1)
      val snapshots = restore.snapshots(ids)

      discard.discard(ids).getOrThrow()
      ids.forEach { expectThat(db.itemDao().getItem(it)!!.discardedDate).isNotNull() }
      restore.restoreSnapshots(snapshots).getOrThrow()
      ids.forEach { expectThat(db.itemDao().getItem(it)!!.discardedDate).isNull() }
    }

  // endregion
}
