package com.alorma.caducity.domain.usecase

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomBackupDataSource
import com.alorma.caducity.data.datasource.RoomCategoryDataSource
import com.alorma.caducity.data.datasource.RoomItemDataSource
import com.alorma.caducity.data.datasource.RoomProductDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.domain.usecase.fakedata.FakePlayStoreDataStrategy
import com.alorma.caducity.domain.usecase.fakedata.FakeTestDataStrategy
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEmpty
import strikt.assertions.isSuccess

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PopulateFakeDataUseCaseTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val thresholds = AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings()))

  private lateinit var db: AppDatabase
  private lateinit var populate: PopulateFakeDataUseCase

  @Before
  fun setUp() {
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val itemMapper = ItemRoomMapper(clock, thresholds)
    val productMapper = ProductRoomMapper()
    populate =
      PopulateFakeDataUseCase(
        backupDataSource = RoomBackupDataSource(db, db.categoryDao(), db.itemDao(), db.productDao(), clock),
        categoryDataSource =
          RoomCategoryDataSource(
            db.categoryDao(),
            db.itemDao(),
            CategoryRoomMapper(productMapper, itemMapper),
            itemMapper,
          ),
        itemDataSource = RoomItemDataSource(db.itemDao(), clock, itemMapper),
        productDataSource = RoomProductDataSource(db.productDao(), db.itemDao(), clock, productMapper),
        appClock = clock,
      )
  }

  @After
  fun tearDown() = db.close()

  @Test
  fun `play store data creates ordered categories with shape and icon and only product items`() =
    runTest {
      expectThat(populate.execute(FakePlayStoreDataStrategy())).isSuccess()

      val categories = db.categoryDao().getCategoriesOrderedSync()
      expectThat(
        categories.map { it.name },
      ).containsExactly("Dairy", "Produce", "Meat & Seafood", "Pantry", "Frozen Foods")
      expectThat(categories.map { it.sortOrder }).containsExactly(0, 1, 2, 3, 4)
      expectThat(
        categories.map { it.icon.id },
      ).containsExactly("water_full", "nutrition", "set_meal", "grocery", "icecream")
      expectThat(categories.map { it.shape.name }).containsExactly("COOKIE", "CLOVER8", "CLOVER", "SUNNY", "BURST")

      val items = db.itemDao().getAllItemsSync()
      expectThat(items.size).isGreaterThan(30)
      expectThat(items.filter { it.productId == null }).isEmpty()
      expectThat(items.map { it.identifier }.distinct()).containsExactly("")
      expectThat(items.all { it.createdAt != null }).isEqualTo(true)
      expectThat(db.productDao().getAllProductsSync()).isNotEmpty()
    }

  @Test
  fun `running again replaces the previous data and test data also has no standalone items`() =
    runTest {
      populate.execute(FakePlayStoreDataStrategy())
      expectThat(populate.execute(FakeTestDataStrategy(thresholds))).isSuccess()

      val categories = db.categoryDao().getCategoriesOrderedSync()
      expectThat(categories.map { it.sortOrder }).isEqualTo(categories.indices.toList())
      expectThat(db.itemDao().getAllItemsSync().filter { it.productId == null }).isEmpty()
      db.openHelper.writableDatabase
        .query("PRAGMA foreign_key_check")
        .use { expectThat(it.count).isEqualTo(0) }
      expectThat(categories.first().shape).isA<com.alorma.caducity.domain.model.CategoryShape>()
    }
}
