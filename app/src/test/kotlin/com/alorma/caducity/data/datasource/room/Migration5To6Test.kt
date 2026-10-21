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
class Migration5To6Test {
  @get:Rule
  val helper =
    MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.java,
    )

  @Test
  fun `every category can be frozen and every product inherits it`() {
    helper.createDatabase(TEST_DB, 5).apply {
      execSQL(
        "INSERT INTO categories (id, name, description, shape, icon, sortOrder) " +
          "VALUES ('c1', 'Secs', '', 'COOKIE', 'eco', 0)",
      )
      execSQL("INSERT INTO products (id, categoryId, name, createdAt) VALUES ('p1', 'c1', 'Pasta', 1)")
      close()
    }
    val db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

    db.query("SELECT canFreeze FROM categories WHERE id = 'c1'").use { c ->
      expectThat(c.moveToFirst()).isTrue()
      expectThat(c.getInt(0)).isEqualTo(1)
    }
    db.query("SELECT canFreeze FROM products WHERE id = 'p1'").use { c ->
      expectThat(c.moveToFirst()).isTrue()
      expectThat(c.isNull(0)).isTrue()
    }
  }

  private companion object {
    const val TEST_DB = "migration-5-6-test"
  }
}
