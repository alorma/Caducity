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
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class Migration2To3Test {
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

  private fun SupportSQLiteDatabase.count(table: String): Int =
    rows("SELECT COUNT(*) FROM $table") { listOf(it.getInt(0)) }.single().single() as Int

  private fun SupportSQLiteDatabase.foreignKeyViolations() =
    rows("PRAGMA foreign_key_check") { listOf(it.getString(0)) }

  private fun Cursor.longOrNull(index: Int): Long? = if (isNull(index)) null else getLong(index)

  private fun Cursor.intOrNull(index: Int): Int? = if (isNull(index)) null else getInt(index)

  private fun seedV2(db: SupportSQLiteDatabase) {
    // Categories in en/es/ca plus two without keyword match, inserted in a known rowid order.
    db.execSQL("INSERT INTO categories VALUES ('c1', 'Dairy', '')")
    db.execSQL("INSERT INTO categories VALUES ('c2', 'Fruta y Verdura', 'desc')")
    db.execSQL("INSERT INTO categories VALUES ('c3', 'Fleca', '')")
    db.execSQL("INSERT INTO categories VALUES ('c4', 'Snacks', '')")
    db.execSQL("INSERT INTO categories VALUES ('c5', 'Misc', '')")
    db.execSQL("INSERT INTO products VALUES ('p1', 'c1', 'Whole Milk', 500)")
    // Items with a product (identifier like "Whole Milk #1" must be cleared), consumed, frozen, pack.
    db.execSQL(
      "INSERT INTO items (id, categoryId, identifier, productId, expirationDate, pausedDate, " +
        "remainingDays, consumedDate, packSize) VALUES " +
        "('i1', 'c1', 'Whole Milk #1', 'p1', 1000, NULL, NULL, NULL, NULL), " +
        "('i2', 'c1', 'Whole Milk #2', 'p1', 2000, NULL, NULL, 1500, NULL), " +
        "('i3', 'c1', 'Whole Milk #3', 'p1', 3000, 2500, 4, NULL, NULL), " +
        "('i4', 'c1', 'Pack', 'p1', 4000, NULL, NULL, NULL, 6)",
    )
    // Items without product: two share (category, identifier), one is the same name in another category,
    // one has an empty identifier (must stay standalone).
    db.execSQL(
      "INSERT INTO items (id, categoryId, identifier, productId, expirationDate) VALUES " +
        "('s1', 'c4', 'Chips', NULL, 9000), " +
        "('s2', 'c4', 'Chips', NULL, 7000), " +
        "('s3', 'c5', 'Chips', NULL, 8000), " +
        "('s4', 'c5', '', NULL, 8500)",
    )
  }

  @Test
  fun `migrates populated database preserving data and integrity`() {
    helper.createDatabase(TEST_DB, 2).apply {
      seedV2(this)
      close()
    }

    val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

    // Counts unchanged for categories and items; 3 new products (c4/Chips, c5/Chips).
    expectThat(db.count("categories")).isEqualTo(5)
    expectThat(db.count("items")).isEqualTo(8)
    expectThat(db.count("products")).isEqualTo(3)

    // Foreign keys are consistent.
    expectThat(db.foreignKeyViolations()).isEmpty()

    // Category defaults and sortOrder follow rowid order.
    val categories =
      db.rows("SELECT id, shape, icon, sortOrder FROM categories ORDER BY sortOrder") {
        listOf(it.getString(0), it.getString(1), it.getString(2), it.getInt(3))
      }
    expectThat(categories).containsExactly(
      listOf("c1", "COOKIE", "water_full", 0),
      listOf("c2", "FLOWER", "nutrition", 1),
      listOf("c3", "PUFFY", "bakery_dining", 2),
      listOf("c4", "SUNNY", "eco", 3),
      listOf("c5", "PUFFY", "eco", 4),
    )
    // Description untouched.
    expectThat(
      db.rows("SELECT description FROM categories WHERE id = 'c2'") { listOf(it.getString(0)) }.single().single(),
    ).isEqualTo("desc")

    // Existing values preserved exactly (dates, frozen, consumed, pack).
    val items =
      db.rows(
        "SELECT id, productId, expirationDate, pausedDate, remainingDays, consumedDate, packSize, " +
          "discardedDate, createdAt FROM items WHERE id IN ('i1','i2','i3','i4') ORDER BY id",
      ) {
        listOf(
          it.getString(0),
          it.getString(1),
          it.getLong(2),
          it.longOrNull(3),
          it.intOrNull(4),
          it.longOrNull(5),
          it.intOrNull(6),
          it.longOrNull(7),
          it.longOrNull(8),
        )
      }
    expectThat(items).containsExactly(
      listOf("i1", "p1", 1000L, null, null, null, null, null, null),
      listOf("i2", "p1", 2000L, null, null, 1500L, null, null, null),
      listOf("i3", "p1", 3000L, 2500L, 4, null, null, null, null),
      listOf("i4", "p1", 4000L, null, null, null, 6, null, null),
    )

    // Identifier cleared for all items that have a product (original and converted).
    expectThat(
      db.rows("SELECT id FROM items WHERE productId IS NOT NULL AND identifier <> ''") { listOf(it.getString(0)) },
    ).isEmpty()

    // Standalone items converted: same (category, name) share one product; other category gets its own.
    val s =
      db.rows("SELECT id, productId FROM items WHERE id LIKE 's%' ORDER BY id") {
        listOf(it.getString(0), it.getString(1))
      }
    expectThat(s[0][1]).isNotNull()
    expectThat(s[1][1]).isEqualTo(s[0][1])
    expectThat(s[2][1]).isNotNull()
    expectThat(s[2][1]).isNotEqualTo(s[0][1])
    expectThat(s[3][1]).isEqualTo(null)

    // Converted products take the name and the earliest expiration as createdAt.
    val products =
      db.rows("SELECT name, categoryId, createdAt FROM products WHERE id <> 'p1' ORDER BY categoryId") {
        listOf(it.getString(0), it.getString(1), it.getLong(2))
      }
    expectThat(products).containsExactly(
      listOf("Chips", "c4", 7000L),
      listOf("Chips", "c5", 8000L),
    )
  }

  @Test
  fun `migrates an empty database`() {
    helper.createDatabase(TEST_DB, 2).close()
    val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)
    expectThat(db.count("categories")).isEqualTo(0)
  }

  @Test
  fun `chain 1 to 3 works`() {
    helper.createDatabase(TEST_DB, 1).apply {
      execSQL("INSERT INTO categories VALUES ('c1', 'Lácteos', '')")
      execSQL(
        "INSERT INTO items (id, categoryId, identifier, productId, expirationDate) VALUES ('i1', 'c1', 'Yogur', NULL, 100)",
      )
      close()
    }
    val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_1_2, MIGRATION_2_3)
    val identifiers = db.rows("SELECT identifier FROM items") { listOf(it.getString(0)) }
    expectThat(identifiers.single().single()).isEqualTo("")
    expectThat(db.foreignKeyViolations()).isEmpty()
  }

  private companion object {
    const val TEST_DB = "migration-2-3-test"
  }
}
