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

/**
 * Verifica que la infra de tests de migració (Robolectric + room-testing + esquemes com a assets)
 * funciona, amb la migració 1→2 existent. Les migracions noves (2→3) tindran els seus propis tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class MigrationInfraSmokeTest {
  @get:Rule
  val helper =
    MigrationTestHelper(
      InstrumentationRegistry.getInstrumentation(),
      AppDatabase::class.java,
    )

  @Test
  fun `migration 1 to 2 keeps rows and adds packSize`() {
    helper.createDatabase(TEST_DB, 1).apply {
      execSQL("INSERT INTO categories (id, name, description) VALUES ('c1', 'Dairy', '')")
      execSQL(
        "INSERT INTO items (id, categoryId, identifier, productId, expirationDate) " +
          "VALUES ('i1', 'c1', 'Milk', NULL, 1000)",
      )
      close()
    }

    val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

    db.query("SELECT identifier, packSize FROM items WHERE id = 'i1'").use { cursor ->
      expectThat(cursor.moveToFirst()).isEqualTo(true)
      expectThat(cursor.getString(0)).isEqualTo("Milk")
      expectThat(cursor.isNull(1)).isEqualTo(true)
    }
  }

  private companion object {
    const val TEST_DB = "migration-smoke-test"
  }
}
