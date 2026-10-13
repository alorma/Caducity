package com.alorma.caducity.data.datasource.room

import android.app.Application
import android.database.Cursor
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class Migration4To5Test {
  @get:Rule
  val helper =
    MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.java,
    )

  private fun SupportSQLiteDatabase.rows(
    sql: String,
    read: (Cursor) -> List<Any?>,
  ): List<List<Any?>> =
    query(sql).use { c ->
      buildList { while (c.moveToNext()) add(read(c)) }
    }

  private fun SupportSQLiteDatabase.count(sql: String): Int =
    rows(sql) { listOf(it.getInt(0)) }.single().single() as Int

  private fun Cursor.intOrNull(index: Int): Int? = if (isNull(index)) null else getInt(index)

  private fun SupportSQLiteDatabase.insertCategory(
    id: String,
    name: String,
    shape: String,
    icon: String,
    soonThresholdDays: Int? = null,
  ) = execSQL(
    "INSERT INTO categories (id, name, description, shape, icon, sortOrder, soonThresholdDays) " +
      "VALUES ('$id', '$name', '', '$shape', '$icon', 0, ${soonThresholdDays ?: "NULL"})",
  )

  private fun migrate() = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

  @Test
  fun `category durations come from the configured margin or the suggested default`() {
    helper.createDatabase(TEST_DB, 4).apply {
      insertCategory("dairy", "Làctics", "COOKIE", "water_full")
      insertCategory("meat", "Carne y pescado", "CLOVER", "set_meal")
      insertCategory("pantry", "Dry goods", "SUNNY", "grocery")
      insertCategory("drinks", "Bebidas", "BURST", "local_drink")
      insertCategory("configured", "Fleca", "PUFFY", "bakery_dining", soonThresholdDays = 14)
      insertCategory("edited", "Làctics", "SUNNY", "eco")
      insertCategory("own", "Snacks", "COOKIE", "eco")
      close()
    }

    val db = migrate()

    val durations = db.rows("SELECT id, usualDurationDays FROM categories") { listOf(it.getString(0), it.intOrNull(1)) }
    expectThat(durations).containsExactlyInAnyOrder(
      listOf("dairy", 7),
      listOf("meat", 3),
      listOf("pantry", 90),
      listOf("drinks", 30),
      listOf("configured", 14),
      listOf("edited", null),
      listOf("own", null),
    )
    expectThat(db.rows("PRAGMA foreign_key_check") { listOf(it.getString(0)) }).isEmpty()
  }

  @Test
  fun `live packs become single units and history keeps its single item`() {
    helper.createDatabase(TEST_DB, 4).apply {
      insertCategory("c1", "Snacks", "COOKIE", "eco")
      execSQL("INSERT INTO products VALUES ('p1', 'c1', 'Yogurt', 500)")
      execSQL(
        "INSERT INTO items (id, categoryId, identifier, productId, expirationDate, pausedDate, remainingDays, " +
          "consumedDate, packSize, discardedDate, createdAt) VALUES " +
          "('pack', 'c1', '', 'p1', 1000, NULL, NULL, NULL, 4, NULL, 10), " +
          "('frozen', 'c1', '', 'p1', 2000, 1500, 6, NULL, 2, NULL, 20), " +
          "('single', 'c1', '', 'p1', 3000, NULL, NULL, NULL, NULL, NULL, 30), " +
          "('one', 'c1', '', 'p1', 3500, NULL, NULL, NULL, 1, NULL, 35), " +
          "('eaten', 'c1', '', 'p1', 4000, NULL, NULL, 3500, 3, NULL, 40), " +
          "('thrown', 'c1', '', 'p1', 5000, NULL, NULL, NULL, 5, 4500, 50)",
      )
      close()
    }

    val db = migrate()

    expectThat(db.count("SELECT COUNT(*) FROM items")).isEqualTo(4 + 2 + 1 + 1 + 1 + 1)
    val packUnits =
      db.rows("SELECT expirationDate, productId, createdAt FROM items WHERE expirationDate = 1000") {
        listOf(it.getLong(0), it.getString(1), it.getLong(2))
      }
    expectThat(packUnits).isEqualTo(List(4) { listOf(1000L, "p1", 10L) })

    val frozenUnits =
      db.rows("SELECT pausedDate, remainingDays FROM items WHERE expirationDate = 2000") {
        listOf(it.getLong(0), it.getInt(1))
      }
    expectThat(frozenUnits).isEqualTo(List(2) { listOf<Any>(1500L, 6) })

    expectThat(db.count("SELECT COUNT(*) FROM items WHERE id = 'pack'")).isEqualTo(1)
    expectThat(db.count("SELECT COUNT(*) FROM items WHERE expirationDate = 4000 AND consumedDate = 3500")).isEqualTo(1)
    expectThat(db.count("SELECT COUNT(*) FROM items WHERE expirationDate = 5000 AND discardedDate = 4500")).isEqualTo(1)
    expectThat(db.count("SELECT COUNT(DISTINCT id) FROM items")).isEqualTo(10)
    expectThat(db.count("SELECT COUNT(*) FROM products")).isEqualTo(1)
    expectThat(db.rows("PRAGMA foreign_key_check") { listOf(it.getString(0)) }).isEmpty()
  }

  @Test
  fun `removed columns are gone and the foreign keys and indices keep working`() {
    helper.createDatabase(TEST_DB, 4).apply {
      insertCategory("c1", "Snacks", "COOKIE", "eco")
      execSQL("INSERT INTO products VALUES ('p1', 'c1', 'Chips', 500)")
      execSQL("INSERT INTO transformations VALUES ('t1', 700)")
      execSQL(
        "INSERT INTO items (id, categoryId, identifier, productId, expirationDate, " +
          "transformedDate, transformationId, originTransformationId) VALUES ('i1', 'c1', '', 'p1', 1000, 700, 't1', NULL)",
      )
      close()
    }

    val db = migrate()

    val itemColumns = db.rows("PRAGMA table_info(items)") { listOf(it.getString(1)) }.flatten()
    val categoryColumns = db.rows("PRAGMA table_info(categories)") { listOf(it.getString(1)) }.flatten()
    expectThat(itemColumns.contains("packSize")).isEqualTo(false)
    expectThat(categoryColumns.contains("soonThresholdDays")).isEqualTo(false)
    expectThat(categoryColumns.contains("usualDurationDays")).isEqualTo(true)
    expectThat(
      db.rows("SELECT transformedDate, transformationId FROM items") { listOf(it.getLong(0), it.getString(1)) },
    ).isEqualTo(listOf(listOf(700L, "t1")))

    db.execSQL("PRAGMA foreign_keys = ON")
    db.execSQL("DELETE FROM products WHERE id = 'p1'")
    expectThat(db.count("SELECT COUNT(*) FROM items WHERE productId IS NULL")).isEqualTo(1)
    db.execSQL("DELETE FROM categories WHERE id = 'c1'")
    expectThat(db.count("SELECT COUNT(*) FROM items")).isEqualTo(0)
  }

  @Test
  fun `migrates an empty database`() {
    helper.createDatabase(TEST_DB, 4).close()
    val db = migrate()
    expectThat(db.count("SELECT COUNT(*) FROM items")).isEqualTo(0)
    expectThat(db.count("SELECT COUNT(*) FROM categories")).isEqualTo(0)
  }

  private companion object {
    const val TEST_DB = "migration-4-5-test"
  }
}
