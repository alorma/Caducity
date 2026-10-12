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
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNull
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

  private val v1Json =
    """
    {
      "version": 1,
      "exportDate": 1,
      "categories": [
        {
          "id": "c1", "name": "Dairy", "description": "",
          "products": [
            {"id": "p1", "categoryId": "c1", "name": "Whole Milk", "createdAt": 5, "items": [
              {"id": "i1", "identifier": "Whole Milk #1", "expirationDate": 1000},
              {"id": "i2", "identifier": "Whole Milk #2", "expirationDate": 2000, "consumedDate": 1500},
              {"id": "i3", "identifier": "Pack", "expirationDate": 3000, "packSize": 6},
              {"id": "i4", "identifier": "Frozen", "expirationDate": 4000, "pausedDate": 10, "remainingDays": 7}
            ]}
          ],
          "standaloneItems": [
            {"id": "s1", "identifier": "Butter", "expirationDate": 9000},
            {"id": "s2", "identifier": "butter ", "expirationDate": 7000},
            {"id": "s3", "identifier": "whole MILK", "expirationDate": 8000},
            {"id": "s4", "identifier": "", "expirationDate": 8500}
          ]
        },
        {"id": "c2", "name": "Cosas raras", "description": "d", "products": [], "standaloneItems": []},
        {"id": "c3", "name": "Fruta", "description": "", "products": [], "standaloneItems": []}
      ]
    }
    """.trimIndent()

  // region v1 import

  @Test
  fun `v1 backup decodes without the new fields`() {
    val data = json.decodeFromString<BackupData>(v1Json)
    expectThat(data.version).isEqualTo(1)
    expectThat(data.categories.first().shape).isNull()
    expectThat(data.categories.first().soonThresholdDays).isNull()
    expectThat(
      data.categories
        .first()
        .products
        .first()
        .items
        .first()
        .createdAt,
    ).isNull()
  }

  @Test
  fun `upgrade of v1 applies defaults, converts standalone items and clears identifiers`() {
    var counter = 0
    val upgraded = json.decodeFromString<BackupData>(v1Json).upgradeToV2 { "new-${counter++}" }

    expectThat(upgraded.version).isEqualTo(BackupData.CURRENT_VERSION)
    expectThat(upgraded.categories.map { Triple(it.shape, it.icon, it.sortOrder) }).containsExactly(
      Triple("COOKIE", "water_full", 0),
      Triple("CLOVER8", "eco", 1),
      Triple("CLOVER8", "nutrition", 2),
    )

    val dairy = upgraded.categories.first()
    // "whole MILK" joins p1; "Butter"/"butter " become one new product named after the first, createdAt = min date.
    expectThat(dairy.products.map { it.name }).containsExactly("Whole Milk", "Butter")
    expectThat(
      dairy.products
        .first()
        .items
        .map { it.id },
    ).containsExactly("i1", "i2", "i3", "i4", "s3")
    val butter = dairy.products.last()
    expectThat(butter.id).isEqualTo("new-0")
    expectThat(butter.createdAt).isEqualTo(7000L)
    expectThat(butter.items.map { it.id }).containsExactly("s1", "s2")
    expectThat(
      dairy.products
        .flatMap { it.items }
        .map { it.identifier }
        .distinct(),
    ).containsExactly("")
    expectThat(dairy.standaloneItems.map { it.id }).containsExactly("s4")
  }

  @Test
  fun `upgrade leaves a v2 backup untouched`() {
    val v2 =
      BackupData(
        2,
        1,
        listOf(
          BackupCategory(
            "c",
            "Dairy",
            "",
            "BURST",
            "icecream",
            4,
            products = emptyList(),
            standaloneItems = emptyList(),
          ),
        ),
      )
    expectThat(v2.upgradeToV2()).isEqualTo(v2)
  }

  @Test
  fun `importing a v1 backup preserves values and keeps database integrity`() =
    runTest {
      val data = json.decodeFromString<BackupData>(v1Json)
      expectThat(backup.validateBackup(data)).isSuccess()
      backup.importBackup(data)

      val items = db.itemDao().getAllItemsSync().associateBy { it.id }
      expectThat(items).hasSize(8)
      expectThat(items.getValue("i2").consumedDate).isEqualTo(1500L)
      expectThat(items.getValue("i3").packSize).isEqualTo(6)
      expectThat(items.getValue("i4").pausedDate).isEqualTo(10L)
      expectThat(items.getValue("i4").remainingDays).isEqualTo(7)
      expectThat(items.getValue("s4").productId).isNull()
      expectThat(items.getValue("s3").productId).isEqualTo("p1")
      expectThat(
        items.values
          .filter { it.productId != null }
          .map { it.identifier }
          .distinct(),
      ).containsExactly("")
      expectThat(db.productDao().getAllProductsSync()).hasSize(2)

      val categories = db.categoryDao().getCategoriesOrderedSync()
      expectThat(
        categories.map {
          it.shape
        },
      ).containsExactly(CategoryShape.COOKIE, CategoryShape.CLOVER8, CategoryShape.CLOVER8)
      expectThat(categories.map { it.sortOrder }).containsExactly(0, 1, 2)
      db.openHelper.writableDatabase
        .query("PRAGMA foreign_key_check")
        .use { expectThat(it.count).isEqualTo(0) }
    }

  // endregion

  // region v2 round trip

  @Test
  fun `v2 export and import round-trips every field including avatars and discarded items`() =
    runTest {
      db.categoryDao().insertCategory(
        CategoryRoomEntity(
          "c1",
          "Dairy",
          "d",
          CategoryShape.PUFFY,
          CategoryIcon("icecream"),
          3,
          soonThresholdDays = 30,
        ),
      )
      db.categoryDao().insertCategory(
        CategoryRoomEntity("c2", "Other", "", CategoryShape.SUNNY, CategoryIcon("future_icon"), 1),
      )
      db.productDao().insertProduct(ProductRoomEntity("p1", "c1", "Milk", 100))
      db.itemDao().insertItem(ItemRoomEntity("i1", "c1", "", "p1", 1000, createdAt = 50))
      db.itemDao().insertItem(ItemRoomEntity("i2", "c1", "", "p1", 2000, consumedDate = 60, createdAt = 51))
      db.itemDao().insertItem(ItemRoomEntity("i3", "c1", "", "p1", 3000, discardedDate = 70, createdAt = 52))
      db.itemDao().insertItem(
        ItemRoomEntity("i4", "c1", "", "p1", 4000, pausedDate = 5, remainingDays = 9, packSize = 4),
      )
      db.itemDao().insertItem(ItemRoomEntity("i5", "c2", "Legacy", null, 5000))

      val exported = backup.exportBackup()
      expectThat(exported.version).isEqualTo(BackupData.CURRENT_VERSION)
      expectThat(exported.categories.map { it.id }).containsExactly("c2", "c1")
      expectThat(exported.categories.associate { it.id to it.soonThresholdDays })
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
      backup.importBackup(decoded)

      expectThat(db.categoryDao().getAllCategoriesSync().sortedBy { it.id }).isEqualTo(categoriesBefore)
      expectThat(db.productDao().getAllProductsSync().sortedBy { it.id }).isEqualTo(productsBefore)
      expectThat(db.itemDao().getAllItemsSync().sortedBy { it.id }).isEqualTo(itemsBefore)
    }

  @Test
  fun `import falls back to defaults for an unknown shape and rejects newer versions`() =
    runTest {
      val data =
        BackupData(
          2,
          1,
          listOf(
            BackupCategory(
              "c1",
              "Dairy",
              "",
              "REMOVED_SHAPE",
              "x",
              0,
              products = emptyList(),
              standaloneItems = emptyList(),
            ),
          ),
        )
      backup.importBackup(data)
      expectThat(db.categoryDao().getCategory("c1")?.shape).isEqualTo(CategoryShape.COOKIE)
      expectThat(db.categoryDao().getCategory("c1")?.shape).isNotEqualTo(null)

      expectThat(backup.validateBackup(data.copy(version = BackupData.CURRENT_VERSION + 1)))
        .isFailure()
        .isA<IllegalArgumentException>()
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
      expectThat(exported.version).isEqualTo(3)
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

  @Test
  fun `a v2 backup without transformation fields still imports`() =
    runTest {
      val v2Json =
        """
        {"version": 2, "exportDate": 1, "categories": [
          {"id": "c1", "name": "Dairy", "description": "", "shape": "COOKIE", "icon": "eco", "sortOrder": 0,
           "products": [{"id": "p1", "categoryId": "c1", "name": "Milk", "createdAt": 5, "items": [
             {"id": "i1", "identifier": "", "expirationDate": 1000, "createdAt": 7}]}],
           "standaloneItems": []}]}
        """.trimIndent()
      val data = json.decodeFromString<BackupData>(v2Json)
      expectThat(backup.validateBackup(data)).isSuccess()

      backup.importBackup(data)

      expectThat(db.itemDao().getItem("i1")?.transformedDate).isNull()
      expectThat(db.transformationDao().getAllTransformationsSync()).isEmpty()
    }

  // endregion
}
