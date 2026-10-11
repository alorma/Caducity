package com.alorma.caducity.data.datasource.room

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.data.datasource.room.model.CategoryRoomEntity
import com.alorma.caducity.data.datasource.room.model.ItemRoomEntity
import com.alorma.caducity.data.datasource.room.model.ProductRoomEntity
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class DaoTest {
  private lateinit var db: AppDatabase

  @Before
  fun setUp() {
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
  }

  @After
  fun tearDown() = db.close()

  private suspend fun seed() {
    db.categoryDao().insertCategory(
      CategoryRoomEntity("c1", "Dairy", "", CategoryShape.CLOVER8, CategoryIcon("water_full"), 0),
    )
    db.categoryDao().insertCategory(CategoryRoomEntity("c2", "Pantry", "", sortOrder = 1))
    db.productDao().insertProduct(ProductRoomEntity("p1", "c1", "Milk", 100))
    db.productDao().insertProduct(ProductRoomEntity("p2", "c1", "Yogurt", 200))
    db.itemDao().insertItem(ItemRoomEntity("i1", "c1", "", "p1", 1000, createdAt = 10))
    db.itemDao().insertItem(ItemRoomEntity("i2", "c1", "", "p1", 2000, createdAt = 30))
    db.itemDao().insertItem(ItemRoomEntity("i3", "c1", "", "p1", 3000, consumedDate = 5))
    db.itemDao().insertItem(ItemRoomEntity("i4", "c2", "", null, 4000, pausedDate = 1, remainingDays = 3))
  }

  @Test
  fun `avatars are stored through converters and round-trip`() =
    runTest {
      seed()
      val category = db.categoryDao().getCategory("c1")
      expectThat(category?.shape).isEqualTo(CategoryShape.CLOVER8)
      expectThat(category?.icon).isEqualTo(CategoryIcon("water_full"))
      expectThat(db.categoryDao().getCategory("c2")?.shape).isEqualTo(CategoryShape.COOKIE)
    }

  @Test
  fun `active items exclude consumed and discarded`() =
    runTest {
      seed()
      db.itemDao().discardItem("i2", discardedDate = 99)
      expectThat(
        db
          .itemDao()
          .getActiveItems()
          .first()
          .map { it.id },
      ).containsExactlyInAnyOrder("i1", "i4")
    }

  @Test
  fun `discard unfreezes the item and restore makes it active again`() =
    runTest {
      seed()
      db.itemDao().discardItem("i4", discardedDate = 99)
      val discarded = db.itemDao().getItem("i4")
      expectThat(discarded?.discardedDate).isEqualTo(99L)
      expectThat(discarded?.pausedDate).isNull()
      expectThat(discarded?.remainingDays).isNull()

      db.itemDao().restoreItem("i4")
      expectThat(db.itemDao().getItem("i4")?.discardedDate).isNull()
      db.itemDao().restoreItem("i3")
      expectThat(db.itemDao().getItem("i3")?.consumedDate).isNull()
    }

  @Test
  fun `moveItem changes category and product`() =
    runTest {
      seed()
      db.itemDao().moveItem("i1", categoryId = "c2", productId = null)
      val moved = db.itemDao().getItem("i1")
      expectThat(moved?.categoryId).isEqualTo("c2")
      expectThat(moved?.productId).isNull()
    }

  @Test
  fun `active counts per category ignore consumed and discarded`() =
    runTest {
      seed()
      db.itemDao().discardItem("i1", 99)
      val counts =
        db
          .itemDao()
          .getActiveCountByCategory()
          .first()
          .associate { it.categoryId to it.activeCount }
      expectThat(counts).isEqualTo(mapOf("c1" to 1, "c2" to 1))
    }

  @Test
  fun `product usage counts every item and takes the latest createdAt with expiration fallback`() =
    runTest {
      seed()
      val usage = db.productDao().getProductUsage().associateBy { it.productId }
      expectThat(usage.getValue("p1").uses).isEqualTo(3)
      // i1 createdAt=10, i2 createdAt=30, i3 createdAt null -> expirationDate 3000
      expectThat(usage.getValue("p1").lastUsed).isEqualTo(3000L)
      expectThat(usage.getValue("p2").uses).isEqualTo(0)
      expectThat(usage.getValue("p2").lastUsed).isEqualTo(200L)
    }

  @Test
  fun `deleteProductIfUnused only deletes empty products`() =
    runTest {
      seed()
      db.productDao().deleteProductIfUnused("p1")
      db.productDao().deleteProductIfUnused("p2")
      expectThat(db.productDao().getProduct("p1")).isNotNull()
      expectThat(db.productDao().getProduct("p2")).isNull()
    }

  @Test
  fun `deleteItems removes only the given ids`() =
    runTest {
      seed()
      db.itemDao().deleteItems(listOf("i1", "i2"))
      expectThat(db.itemDao().getAllItemsSync().map { it.id }).containsExactlyInAnyOrder("i3", "i4")
      db.itemDao().deleteItems(emptyList())
    }

  @Test
  fun `categories come ordered by sortOrder`() =
    runTest {
      db.categoryDao().insertCategory(CategoryRoomEntity("b", "B", "", sortOrder = 2))
      db.categoryDao().insertCategory(CategoryRoomEntity("a", "A", "", sortOrder = 0))
      expectThat(
        db
          .categoryDao()
          .getCategoriesOrdered()
          .first()
          .map { it.id },
      ).isEqualTo(listOf("a", "b"))
      expectThat(db.categoryDao().getMaxSortOrder()).isEqualTo(2)
      db.categoryDao().clearAllCategories()
      expectThat(db.categoryDao().getMaxSortOrder()).isEqualTo(-1)
      expectThat(db.itemDao().getAllItemsSync()).isEmpty()
    }
}
