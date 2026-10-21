package com.alorma.caducity.data.backup

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomBackupDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.data.datasource.room.model.TransformationRoomEntity
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
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
import strikt.assertions.isFailure
import strikt.assertions.isSuccess

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class BackupTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private val json = Json { ignoreUnknownKeys = true }

  private lateinit var db: AppDatabase
  private lateinit var backup: RoomBackupDataSource

  @Before
  fun setUp() {
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    backup = RoomBackupDataSource(db, db.categoryDao(), db.itemDao(), db.productDao(), clock)
  }

  @After
  fun tearDown() = db.close()

  @Test
  fun `export and import round-trips every field including avatars and discarded items`() =
    runTest {
      db.categoryDao().insertCategory(
        CategoryRoomEntity(
          "c1",
          "Dairy",
          "d",
          CategoryShape.PUFFY,
          CategoryIcon("icecream"),
          3,
          usualDurationDays = 30,
          canFreeze = false,
        ),
      )
      db.categoryDao().insertCategory(
        CategoryRoomEntity("c2", "Other", "", CategoryShape.SUNNY, CategoryIcon("future_icon"), 1),
      )
      db.productDao().insertProduct(ProductRoomEntity("p1", "c1", "Milk", 100, canFreeze = true))
      db.itemDao().insertItem(ItemRoomEntity("i1", "c1", "", "p1", 1000, createdAt = 50))
      db.itemDao().insertItem(ItemRoomEntity("i2", "c1", "", "p1", 2000, consumedDate = 60, createdAt = 51))
      db.itemDao().insertItem(ItemRoomEntity("i3", "c1", "", "p1", 3000, discardedDate = 70, createdAt = 52))
      db.itemDao().insertItem(ItemRoomEntity("i4", "c1", "", "p1", 4000, pausedDate = 5, remainingDays = 9))
      db.itemDao().insertItem(ItemRoomEntity("i5", "c2", "Legacy", null, 5000))

      val exported = backup.exportBackup()
      expectThat(exported.version).isEqualTo(BackupData.CURRENT_VERSION)
      expectThat(exported.categories.map { it.id }).containsExactly("c2", "c1")
      expectThat(exported.categories.associate { it.id to it.usualDurationDays })
        .isEqualTo(mapOf("c1" to 30, "c2" to null))

      val text =
        Json {
          prettyPrint = true
          ignoreUnknownKeys = true
        }.encodeToString(BackupData.serializer(), exported)
      val decoded = json.decodeFromString<BackupData>(text)
      val categoriesBefore = db.categoryDao().getAllCategoriesSync().sortedBy { it.id }
      val productsBefore = db.productDao().getAllProductsSync().sortedBy { it.id }
      val itemsBefore = db.itemDao().getAllItemsSync().sortedBy { it.id }

      backup.clearAllData()
      expectThat(db.itemDao().getAllItemsSync()).isEmpty()
      expectThat(backup.validateBackup(decoded)).isSuccess()
      backup.importBackup(decoded)

      expectThat(db.categoryDao().getAllCategoriesSync().sortedBy { it.id }).isEqualTo(categoriesBefore)
      expectThat(db.productDao().getAllProductsSync().sortedBy { it.id }).isEqualTo(productsBefore)
      expectThat(db.itemDao().getAllItemsSync().sortedBy { it.id }).isEqualTo(itemsBefore)
    }

  @Test
  fun `import falls back to the default shape for an unknown one`() =
    runTest {
      val category =
        BackupCategory(
          id = "c1",
          name = "Dairy",
          description = "",
          shape = "REMOVED_SHAPE",
          icon = "x",
          sortOrder = 0,
          products = emptyList(),
          standaloneItems = emptyList(),
        )
      backup.importBackup(emptyBackup(category))
      expectThat(db.categoryDao().getCategory("c1")?.shape).isEqualTo(CategoryShape.COOKIE)
    }

  @Test
  fun `only a backup of a supported version validates`() {
    val current = emptyBackup()
    expectThat(backup.validateBackup(current)).isSuccess()
    expectThat(backup.validateBackup(current.copy(version = BackupData.MIN_SUPPORTED_VERSION))).isSuccess()

    listOf(BackupData.MIN_SUPPORTED_VERSION - 1, BackupData.CURRENT_VERSION + 1).forEach { version ->
      expectThat(backup.validateBackup(current.copy(version = version)))
        .isFailure()
        .isA<IllegalArgumentException>()
    }
  }

  @Test
  fun `a version 4 backup imports with everything freezable`() =
    runTest {
      val v4 =
        json.decodeFromString<BackupData>(
          """{"version":4,"exportDate":1,"categories":[{"id":"c1","name":"Dairy","description":"","shape":"COOKIE",
          "icon":"eco","sortOrder":0,"products":[{"id":"p1","categoryId":"c1","name":"Milk","createdAt":1,"items":[]}],
          "standaloneItems":[]}],"transformations":[]}""",
        )
      expectThat(backup.validateBackup(v4)).isSuccess()
      backup.importBackup(v4)
      expectThat(db.categoryDao().getCategory("c1")?.canFreeze).isEqualTo(true)
      val product = db.productDao().getAllProductsSync().single()
      expectThat(product.canFreeze).isEqualTo(null)
    }

  @Test
  fun `transformations and transformed items survive an export and import round trip`() =
    runTest {
      db.categoryDao().insertCategory(CategoryRoomEntity("c1", "Dairy", ""))
      db.productDao().insertProduct(ProductRoomEntity("p1", "c1", "Chicken", 100))
      db.productDao().insertProduct(ProductRoomEntity("p2", "c1", "Broth", 200))
      db.transformationDao().insertTransformation(TransformationRoomEntity("t1", 500))
      db.itemDao().insertItem(
        ItemRoomEntity("i1", "c1", "", "p1", 1000, transformedDate = 500, transformationId = "t1"),
      )
      db.itemDao().insertItem(
        ItemRoomEntity("i2", "c1", "", "p2", 2000, createdAt = 500, originTransformationId = "t1"),
      )

      val exported = backup.exportBackup()
      val decoded =
        json.decodeFromString<BackupData>(json.encodeToString(BackupData.serializer(), exported))
      val itemsBefore = db.itemDao().getAllItemsSync().sortedBy { it.id }

      backup.clearAllData()
      expectThat(db.transformationDao().getAllTransformationsSync()).isEmpty()
      backup.importBackup(decoded)

      val transformations = db.transformationDao().getAllTransformationsSync()
      expectThat(transformations).containsExactly(TransformationRoomEntity("t1", 500))
      expectThat(db.itemDao().getAllItemsSync().sortedBy { it.id }).isEqualTo(itemsBefore)
    }

  private fun emptyBackup(category: BackupCategory? = null) =
    BackupData(
      version = BackupData.CURRENT_VERSION,
      exportDate = 1,
      categories = listOfNotNull(category),
      transformations = emptyList(),
    )
}
