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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class Migration3To4Test {
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

  private fun Cursor.longOrNull(index: Int): Long? = if (isNull(index)) null else getLong(index)

  private fun Cursor.intOrNull(index: Int): Int? = if (isNull(index)) null else getInt(index)

  @Test
  fun `migrates populated database preserving data`() {
    helper.createDatabase(TEST_DB, 3).apply {
      execSQL("INSERT INTO categories (id, name, description, shape, icon, sortOrder) VALUES ('c1', 'Dairy', '', 'COOKIE', 'eco', 0)")
      execSQL("INSERT INTO products VALUES ('p1', 'c1', 'Milk', 500)")
      execSQL(
        "INSERT INTO items (id, categoryId, identifier, productId, expirationDate, pausedDate, remainingDays, " +
          "consumedDate, packSize, discardedDate, createdAt) VALUES " +
          "('i1', 'c1', '', 'p1', 1000, NULL, NULL, NULL, NULL, NULL, 10), " +
          "('i2', 'c1', '', 'p1', 2000, NULL, NULL, 1500, NULL, NULL, 20), " +
          "('i3', 'c1', '', 'p1', 3000, 2500, 4, NULL, NULL, NULL, 30), " +
          "('i4', 'c1', '', 'p1', 4000, NULL, NULL, NULL, 6, 3500, NULL)",
      )
      close()
    }

    val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

    expectThat(db.count("categories")).isEqualTo(1)
    expectThat(db.count("products")).isEqualTo(1)
    expectThat(db.count("items")).isEqualTo(4)
    expectThat(db.count("transformations")).isEqualTo(0)

    val items =
      db.rows(
        "SELECT id, expirationDate, pausedDate, remainingDays, consumedDate, packSize, discardedDate, createdAt, " +
          "transformedDate, transformationId, originTransformationId FROM items ORDER BY id",
      ) {
        listOf(
          it.getString(0),
          it.getLong(1),
          it.longOrNull(2),
          it.intOrNull(3),
          it.longOrNull(4),
          it.intOrNull(5),
          it.longOrNull(6),
          it.longOrNull(7),
          it.longOrNull(8),
          it.getString(9),
          it.getString(10),
        )
      }
    expectThat(items).containsExactly(
      listOf("i1", 1000L, null, null, null, null, null, 10L, null, null, null),
      listOf("i2", 2000L, null, null, 1500L, null, null, 20L, null, null, null),
      listOf("i3", 3000L, 2500L, 4, null, null, null, 30L, null, null, null),
      listOf("i4", 4000L, null, null, null, 6, 3500L, null, null, null, null),
    )
    expectThat(db.rows("PRAGMA foreign_key_check") { listOf(it.getString(0)) }).isEmpty()
  }

  @Test
  fun `new columns and table are writable`() {
    helper.createDatabase(TEST_DB, 3).apply {
      execSQL("INSERT INTO categories (id, name, description, shape, icon, sortOrder) VALUES ('c1', 'Dairy', '', 'COOKIE', 'eco', 0)")
      execSQL("INSERT INTO items (id, categoryId, identifier, expirationDate) VALUES ('i1', 'c1', '', 1000)")
      close()
    }
    val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

    db.execSQL("INSERT INTO transformations VALUES ('t1', 5000)")
    db.execSQL("UPDATE items SET transformedDate = 5000, transformationId = 't1' WHERE id = 'i1'")
    val row = db.rows("SELECT transformedDate, transformationId FROM items") { listOf(it.getLong(0), it.getString(1)) }
    expectThat(row.single()).containsExactly(5000L, "t1")
  }

  @Test
  fun `migrates an empty database`() {
    helper.createDatabase(TEST_DB, 3).close()
    val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)
    expectThat(db.count("items")).isEqualTo(0)
    expectThat(db.count("transformations")).isEqualTo(0)
  }

  @Test
  fun `chain 2 to 4 works`() {
    helper.createDatabase(TEST_DB, 2).apply {
      execSQL("INSERT INTO categories VALUES ('c1', 'Dairy', '')")
      execSQL("INSERT INTO items (id, categoryId, identifier, productId, expirationDate) VALUES ('i1', 'c1', 'Yogurt', NULL, 100)")
      close()
    }
    val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_2_3, MIGRATION_3_4)
    expectThat(db.count("items")).isEqualTo(1)
  }

  private companion object {
    const val TEST_DB = "migration-3-4-test"
  }
}
