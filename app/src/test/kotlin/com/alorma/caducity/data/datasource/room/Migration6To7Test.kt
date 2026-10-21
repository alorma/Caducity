package com.alorma.caducity.data.datasource.room

import android.app.Application
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class Migration6To7Test {
  @get:Rule
  val helper =
    MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.java,
    )

  @Test
  fun `every product is counted in units and no item has an amount or level`() {
    helper.createDatabase(TEST_DB, 6).apply {
      execSQL(
        "INSERT INTO categories (id, name, description, shape, icon, sortOrder) " +
          "VALUES ('c1', 'Dairy', '', 'COOKIE', 'eco', 0)",
      )
      execSQL("INSERT INTO products (id, categoryId, name, createdAt) VALUES ('p1', 'c1', 'Milk', 1)")
      execSQL(
        "INSERT INTO items (id, categoryId, identifier, productId, expirationDate) " +
          "VALUES ('i1', 'c1', '1', 'p1', 10)",
      )
      close()
    }
    val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

    db.query("SELECT unitType FROM products WHERE id = 'p1'").use { c ->
      expectThat(c.moveToFirst()).isTrue()
      expectThat(c.getString(0)).isEqualTo("UNITS")
    }
    db.query("SELECT amount, level FROM items WHERE id = 'i1'").use { c ->
      expectThat(c.moveToFirst()).isTrue()
      expectThat(c.isNull(0)).isTrue()
      expectThat(c.isNull(1)).isTrue()
    }
  }

  private companion object {
    const val TEST_DB = "migration-6-7-test"
  }
}
