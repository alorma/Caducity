package com.alorma.caducity.domain.usecase

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
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.domain.model.ExpiryStatus
import com.russhwolf.settings.MapSettings
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ObserveUseCasesTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val today = clock.nowDate()
  private val todayFlow = MutableStateFlow(today)
  private val thresholdFlow = MutableStateFlow(3)

  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var addBatch: AddItemBatchUseCase
  private lateinit var discard: DiscardItemsUseCase
  private lateinit var consume: ConsumeItemUseCase
  private lateinit var items: ObserveItemsUseCase
  private lateinit var home: ObserveHomeUseCase
  private lateinit var pantryUseCase: ObservePantryUseCase
  private lateinit var category: ObserveCategoryUseCase
  private lateinit var byStatus: ObserveItemsByStatusUseCase
  private lateinit var calendar: ObserveCalendarUseCase
  private lateinit var weekStrip: ObserveWeekStripUseCase

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
        db,
        db.categoryDao(),
        db.itemDao(),
        db.productDao(),
        CategoryRoomMapper(ProductRoomMapper(), itemMapper),
        clock,
      )
    val thresholds =
      object : ExpirationThresholds {
        override val soonThresholdDays: Flow<Int> = thresholdFlow
        override val soonExpiringThreshold: Duration = Duration.ZERO
        override val consumeExpiredThreshold: Duration = Duration.ZERO
      }
    val todayProvider =
      object : TodayProvider {
        override fun today(): LocalDate = todayFlow.value

        override fun observe(): Flow<LocalDate> = todayFlow
      }
    addBatch = AddItemBatchUseCase(pantry, clock)
    discard = DiscardItemsUseCase(pantry, clock)
    consume = ConsumeItemUseCase(RoomItemDataSource(db.itemDao(), clock, itemMapper))
    items = ObserveItemsUseCase(pantry, thresholds, todayProvider)
    home = ObserveHomeUseCase(items, pantry, todayProvider)
    pantryUseCase = ObservePantryUseCase(items, pantry, todayProvider)
    category = ObserveCategoryUseCase(items, pantry, todayProvider)
    byStatus = ObserveItemsByStatusUseCase(items, pantry, todayProvider)
    calendar = ObserveCalendarUseCase(items)
    weekStrip = ObserveWeekStripUseCase(calendar, todayProvider)
  }

  @After
  fun tearDown() = db.close()

  private fun inDays(days: Int) = today.plus(days, DateTimeUnit.DAY)

  private suspend fun category(
    id: String,
    name: String = id,
    sortOrder: Int = 0,
  ) = db.categoryDao().insertCategory(CategoryRoomEntity(id, name, "", sortOrder = sortOrder))

  private suspend fun add(
    name: String,
    categoryId: String,
    days: Int,
    quantity: Int = 1,
    frozen: Boolean = false,
  ) = addBatch.add(name, categoryId, inDays(days), quantity, frozen).getOrThrow()

  // region Items

  @Test
  fun `statuses follow the threshold including the boundary days`() =
    runTest {
      category("c1")
      listOf(-1, 0, 3, 4).forEach { add("P$it", "c1", it) }
      add("Frozen", "c1", 20, frozen = true)

      val statuses = items.observe().first().associate { it.name to it.status }
      expectThat(statuses).isEqualTo(
        mapOf(
          "P-1" to ExpiryStatus.EXPIRED,
          "P0" to ExpiryStatus.SOON,
          "P3" to ExpiryStatus.SOON,
          "P4" to ExpiryStatus.FRESH,
          "Frozen" to ExpiryStatus.FROZEN,
        ),
      )
    }

  @Test
  fun `statuses are recomputed when the threshold or the day changes`() =
    runTest {
      category("c1")
      add("Milk", "c1", 5)
      items.observe().test {
        expectThat(awaitItem().single().status).isEqualTo(ExpiryStatus.FRESH)
        thresholdFlow.value = 7
        expectThat(awaitItem().single().status).isEqualTo(ExpiryStatus.SOON)
        thresholdFlow.value = 1
        expectThat(awaitItem().single().status).isEqualTo(ExpiryStatus.FRESH)
        todayFlow.value = inDays(4)
        expectThat(awaitItem().single().status).isEqualTo(ExpiryStatus.SOON)
        todayFlow.value = inDays(6)
        expectThat(awaitItem().single().status).isEqualTo(ExpiryStatus.EXPIRED)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `consumed and discarded items are not observed`() =
    runTest {
      category("c1")
      val a = add("A", "c1", 1)
      val b = add("B", "c1", 1)
      add("C", "c1", 1)
      consume.consume(a.itemIds.single())
      discard.discard(b.itemIds)
      expectThat(items.observe().first().map { it.name }).containsExactly("C")
    }

  // endregion

  // region Home

  @Test
  fun `home groups soon units by day and product, counting units`() =
    runTest {
      category("c1", "Dairy")
      category("c2", "Pantry", 1)
      add("Milk", "c1", 0, quantity = 3)
      add("Yogurt", "c1", 0)
      add("Rice", "c2", 2)
      add("Cheese", "c1", 9)
      add("Peas", "c1", 30, frozen = true)
      add("Old", "c1", -4, quantity = 2)
      add("Older", "c2", -9)

      val data = home.observe().first()

      expectThat(data.today).isEqualTo(today)
      expectThat(data.sections.map { it.date }).containsExactly(inDays(0), inDays(2))
      val todaySection = data.sections.first()
      expectThat(todaySection.unitCount).isEqualTo(4)
      expectThat(todaySection.rows.map { it.name to it.unitCount }).containsExactly("Milk" to 3, "Yogurt" to 1)
      expectThat(data.soonCount).isEqualTo(5)
      expectThat(data.freshCount).isEqualTo(1)
      expectThat(data.frozenCount).isEqualTo(1)
      expectThat(data.expiredCount).isEqualTo(3)
      expectThat(data.oldestExpiredDaysAgo).isEqualTo(9)
      expectThat(data.isPantryEmpty).isFalse()
      expectThat(data.nothingExpiresSoon).isFalse()
    }

  @Test
  fun `home row names the closest unit for the check button and excludes expired from sections`() =
    runTest {
      category("c1")
      val batch = add("Milk", "c1", 1, quantity = 2)
      add("Gone", "c1", -1)

      val data = home.observe().first()
      val row =
        data.sections
          .single()
          .rows
          .single()
      expectThat(batch.itemIds.contains(row.nearestItemId)).isTrue()
      expectThat(data.sections.flatMap { it.rows }.map { it.name }).containsExactly("Milk")
    }

  @Test
  fun `home reports empty pantry and nothing expiring soon`() =
    runTest {
      category("c1")
      expectThat(home.observe().first().isPantryEmpty).isTrue()

      add("Rice", "c1", 20)
      val data = home.observe().first()
      expectThat(data.isPantryEmpty).isFalse()
      expectThat(data.nothingExpiresSoon).isTrue()
      expectThat(data.oldestExpiredDaysAgo).isNull()
    }

  @Test
  fun `home counts a legacy pack as one unit and shows its size`() =
    runTest {
      category("c1")
      db.itemDao().insertItem(
        ItemRoomEntity("pack", "c1", "Eggs", null, inDays(1).toExpirationInstant().toEpochMilliseconds(), packSize = 6),
      )
      val data = home.observe().first()
      expectThat(data.soonCount).isEqualTo(1)
      val row =
        data.sections
          .single()
          .rows
          .single()
      expectThat(row.unitCount).isEqualTo(1)
      expectThat(row.legacyPack).isEqualTo(6)
      expectThat(row.name).isEqualTo("Eggs")
    }

  // endregion

  // region Pantry

  @Test
  fun `pantry lists categories by sortOrder with counters`() =
    runTest {
      category("b", "B", 1)
      category("a", "A", 0)
      category("empty", "Empty", 2)
      add("X", "a", -2, quantity = 2)
      add("Y", "a", 1)
      add("Z", "a", 40)
      add("F", "a", 10, frozen = true)
      add("W", "b", 5)

      val data = pantryUseCase.observe().first()

      expectThat(data.totalUnits).isEqualTo(6)
      expectThat(data.categories.map { it.category.id }).containsExactly("a", "b", "empty")
      val a = data.categories.first()
      expectThat(a.unitCount).isEqualTo(5)
      expectThat(a.expiredCount).isEqualTo(2)
      expectThat(a.statusCounts.entries.map { it.key to it.value }).containsExactly(
        ExpiryStatus.EXPIRED to 2,
        ExpiryStatus.SOON to 1,
        ExpiryStatus.FRESH to 1,
        ExpiryStatus.FROZEN to 1,
      )
      expectThat(a.nearest).isEqualTo(inDays(-2))
      expectThat(data.categories.last().unitCount).isEqualTo(0)
      expectThat(data.categories.last().nearest).isNull()
    }

  @Test
  fun `pantry search ignores accents and case, matches category names and skips inactive`() =
    runTest {
      category("c1", "Lácteos")
      category("c2", "Bebidas")
      add("Yogur Natural", "c1", 5)
      add("Café", "c2", 5)
      val gone = add("Yogur griego", "c1", 5)
      discard.discard(gone.itemIds)

      expectThat(pantryUseCase.search("YOGUR").first().map { it.group.name }).containsExactly("Yogur Natural")
      expectThat(pantryUseCase.search("cafe").first().map { it.category.id }).containsExactly("c2")
      expectThat(pantryUseCase.search("lacteos").first().map { it.group.name }).containsExactly("Yogur Natural")
      expectThat(pantryUseCase.search("  ").first()).isEmpty()
    }

  // endregion

  // region Category

  @Test
  fun `category groups products by status and numbers units by date`() =
    runTest {
      category("c1", "Dairy")
      category("c2", "Other", 1)
      add("Milk", "c1", 1)
      add("Milk", "c1", 20)
      add("Milk", "c1", 8)
      add("Gone", "c1", -2)
      add("Peas", "c1", 50, frozen = true)
      add("Elsewhere", "c2", 1)

      val data = category.observe("c1").first()!!

      expectThat(data.unitCount).isEqualTo(5)
      expectThat(data.sections.map { it.status }).containsExactly(
        ExpiryStatus.EXPIRED,
        ExpiryStatus.SOON,
        ExpiryStatus.FRESH,
        ExpiryStatus.FROZEN,
      )
      expectThat(data.statusCounts[ExpiryStatus.FRESH]).isEqualTo(2)
      expectThat(data.nearest).isEqualTo(inDays(-2))
      val fresh = data.sections.first { it.status == ExpiryStatus.FRESH }
      expectThat(
        fresh.groups
          .single()
          .units
          .map { it.expiration },
      ).containsExactly(inDays(8), inDays(20))
      val soon = data.sections.first { it.status == ExpiryStatus.SOON }
      expectThat(soon.groups.single().isSingle).isTrue()
    }

  @Test
  fun `category emits null when it does not exist`() =
    runTest {
      expectThat(category.observe("missing").first()).isNull()
    }

  // endregion

  // region Expired

  @Test
  fun `expired groups by category ordered by count and empties out`() =
    runTest {
      category("a", "A", 0)
      category("b", "B", 1)
      val one = add("One", "a", -1)
      add("Two", "b", -3)
      add("Three", "b", -5)
      add("Fresh", "a", 10)

      val data = byStatus.observe(ExpiryStatus.EXPIRED).first()
      expectThat(data.totalCount).isEqualTo(3)
      expectThat(data.groups.map { it.category.id }).containsExactly("b", "a")
      expectThat(
        data.groups
          .first()
          .entries
          .map { it.name },
      ).containsExactly("Three", "Two")
      expectThat(data.isEmpty).isFalse()

      discard.discard(data.groups.flatMap { it.entries }.map { it.itemId })
      val after = byStatus.observe(ExpiryStatus.EXPIRED).first()
      expectThat(after.isEmpty).isTrue()
      expectThat(one.itemIds).hasSize(1)
    }

  @Test
  fun `each status lists only its own units`() =
    runTest {
      category("c1")
      add("Old", "c1", -2)
      add("Soon", "c1", 2)
      add("Fresh", "c1", 10)
      add("Ice", "c1", 30, frozen = true)

      fun names(status: ExpiryStatus) =
        byStatus
          .observe(status)
          .let { runBlocking { it.first() } }
          .groups
          .flatMap { it.entries }
          .map { it.name }

      expectThat(names(ExpiryStatus.EXPIRED)).containsExactly("Old")
      expectThat(names(ExpiryStatus.SOON)).containsExactly("Soon")
      expectThat(names(ExpiryStatus.FRESH)).containsExactly("Fresh")
      expectThat(names(ExpiryStatus.FROZEN)).containsExactly("Ice")
    }

  @Test
  fun `soon rows go by date and the threshold and midnight move units between statuses`() =
    runTest {
      category("c1")
      add("Later", "c1", 3)
      add("Sooner", "c1", 1)
      add("Far", "c1", 5)

      val soon = byStatus.observe(ExpiryStatus.SOON).first()
      expectThat(soon.totalCount).isEqualTo(2)
      expectThat(
        soon.groups
          .single()
          .entries
          .map { it.name },
      ).containsExactly("Sooner", "Later")

      thresholdFlow.value = 7
      expectThat(byStatus.observe(ExpiryStatus.SOON).first().totalCount).isEqualTo(3)
      thresholdFlow.value = 3

      todayFlow.value = today.plus(2, DateTimeUnit.DAY)
      expectThat(byStatus.observe(ExpiryStatus.EXPIRED).first().totalCount).isEqualTo(1)
      expectThat(byStatus.observe(ExpiryStatus.SOON).first().totalCount).isEqualTo(2)
      todayFlow.value = today.plus(2, DateTimeUnit.DAY).plus(2, DateTimeUnit.DAY)
      expectThat(byStatus.observe(ExpiryStatus.EXPIRED).first().totalCount).isEqualTo(2)
      todayFlow.value = today
    }

  @Test
  fun `frozen units carry the date they were frozen`() =
    runTest {
      category("c1")
      add("First", "c1", 20, frozen = true)
      add("Second", "c1", 20, frozen = true)
      val data = byStatus.observe(ExpiryStatus.FROZEN).first()
      expectThat(data.totalCount).isEqualTo(2)
      expectThat(
        data.groups
          .single()
          .entries
          .all { it.frozenSince != null },
      ).isTrue()
    }

  // endregion

  // region Calendar

  @Test
  fun `calendar groups by local date excluding frozen, consumed and discarded`() =
    runTest {
      category("c1")
      category("c2", "c2", 1)
      add("A", "c1", 0, quantity = 2)
      add("B", "c1", 2)
      add("Past", "c1", -3)
      add("Out", "c1", 40)
      add("Frozen", "c1", 1, frozen = true)
      val c = add("Consumed", "c1", 2)
      consume.consume(c.itemIds.single())
      add("Other", "c2", 2)

      val all = calendar.observe(inDays(-5)..inDays(10)).first()
      expectThat(all.keys.toList()).containsExactly(inDays(-3), inDays(0), inDays(2))
      expectThat(all.getValue(inDays(0))).hasSize(2)
      expectThat(all.getValue(inDays(2)).map { it.name }).containsExactly("B", "Other")
      expectThat(all.getValue(inDays(-3)).single().status).isEqualTo(ExpiryStatus.EXPIRED)

      val onlyC2 = calendar.observe(inDays(0)..inDays(6), categoryId = "c2").first()
      expectThat(onlyC2.keys.toList()).containsExactly(inDays(2))
      expectThat(onlyC2.getValue(inDays(2)).single().name).isEqualTo("Other")
    }

  @Test
  fun `calendar range is inclusive on both ends`() =
    runTest {
      category("c1")
      add("Start", "c1", 0)
      add("End", "c1", 6)
      add("After", "c1", 7)
      val map = calendar.observe(inDays(0)..inDays(6)).first()
      expectThat(map.keys.toList()).containsExactly(inDays(0), inDays(6))
      expectThat(map.getValue(inDays(0)).first().expiration).isEqualTo(inDays(0))
    }

  // endregion

  // region Week strip

  @Test
  fun `week strip starts today, leaves expired out and lists soon before fresh`() =
    runTest {
      category("c1")
      add("Old", "c1", -2)
      add("Fresh", "c1", 6)
      add("Soon", "c1", 6, quantity = 2)
      add("Frozen", "c1", 2, frozen = true)

      val data = weekStrip.observe().first()

      expectThat(data.today).isEqualTo(today)
      expectThat(data.statusesByDate.keys.toList()).containsExactly(inDays(6))
      expectThat(data.statusesByDate.getValue(inDays(6))).hasSize(3)
    }

  @Test
  fun `week strip can be filtered by category`() =
    runTest {
      category("c1")
      category("c2", "c2", 1)
      add("A", "c1", 1)
      add("B", "c2", 2)

      val data = weekStrip.observe(categoryId = "c2").first()

      expectThat(data.statusesByDate.keys.toList()).containsExactly(inDays(2))
    }

  @Test
  fun `week strip restarts when the day changes`() =
    runTest {
      category("c1")
      add("Today", "c1", 0)
      add("Later", "c1", 3)
      weekStrip.observe().test {
        expectThat(awaitItem().statusesByDate.keys.toList()).containsExactly(inDays(0), inDays(3))
        todayFlow.value = inDays(1)
        val next = awaitItem()
        expectThat(next.today).isEqualTo(inDays(1))
        expectThat(next.statusesByDate.keys.toList()).containsExactly(inDays(3))
        cancelAndIgnoreRemainingEvents()
      }
    }

  // endregion
}
