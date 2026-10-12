package com.alorma.caducity.data.datasource.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alorma.caducity.domain.model.CategoryDefaults
import com.alorma.caducity.domain.model.normalizeForSearch
import java.util.UUID

val MIGRATION_1_2 =
  object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE items ADD COLUMN packSize INTEGER")
    }
  }

/** v3 → v4: transform ingredients/results (item columns + `transformations` table). Only ADD COLUMN + CREATE TABLE. */
val MIGRATION_3_4 =
  object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE items ADD COLUMN transformedDate INTEGER")
      db.execSQL("ALTER TABLE items ADD COLUMN transformationId TEXT")
      db.execSQL("ALTER TABLE items ADD COLUMN originTransformationId TEXT")
      db.execSQL(
        "CREATE TABLE IF NOT EXISTS `transformations` " +
          "(`id` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))",
      )
    }
  }

/**
 * v2 → v3: category shape/icon/sortOrder/soonThresholdDays (null = global default), item discardedDate/createdAt, items without product
 * converted to products and `identifier` cleared. Only ADD COLUMN + UPDATE/INSERT (no table rebuilds).
 */
val MIGRATION_2_3 =
  object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE categories ADD COLUMN shape TEXT NOT NULL DEFAULT 'COOKIE'")
      db.execSQL("ALTER TABLE categories ADD COLUMN icon TEXT NOT NULL DEFAULT 'eco'")
      db.execSQL("ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
      db.execSQL("ALTER TABLE categories ADD COLUMN soonThresholdDays INTEGER")
      db.execSQL("ALTER TABLE items ADD COLUMN discardedDate INTEGER")
      db.execSQL("ALTER TABLE items ADD COLUMN createdAt INTEGER")

      assignCategoryDefaults(db)
      convertStandaloneItemsToProducts(db)
      db.execSQL("UPDATE items SET identifier = '' WHERE productId IS NOT NULL")
    }

    private fun assignCategoryDefaults(db: SupportSQLiteDatabase) {
      val categories = mutableListOf<Pair<String, String>>()
      db.query("SELECT id, name FROM categories ORDER BY rowid").use { cursor ->
        while (cursor.moveToNext()) categories += cursor.getString(0) to cursor.getString(1)
      }
      categories.forEachIndexed { index, (id, name) ->
        val defaults = CategoryDefaults.forName(name, index)
        db.execSQL(
          "UPDATE categories SET sortOrder = ?, shape = ?, icon = ? WHERE id = ?",
          arrayOf<Any>(index, defaults.shape.name, defaults.icon.id, id),
        )
      }
    }

    private fun convertStandaloneItemsToProducts(db: SupportSQLiteDatabase) {
      class Standalone(
        val id: String,
        val categoryId: String,
        val name: String,
        val expiration: Long,
      )

      // Existing products by (category, normalized name), so a standalone item named like one joins it.
      val productsByName = mutableMapOf<Pair<String, String>, String>()
      db.query("SELECT id, categoryId, name FROM products ORDER BY rowid").use { cursor ->
        while (cursor.moveToNext()) {
          productsByName.putIfAbsent(
            cursor.getString(1) to cursor.getString(2).normalizeForSearch(),
            cursor.getString(0),
          )
        }
      }

      val standalone = mutableListOf<Standalone>()
      db
        .query(
          "SELECT id, categoryId, identifier, expirationDate FROM items " +
            "WHERE productId IS NULL AND identifier <> '' ORDER BY rowid",
        ).use { cursor ->
          while (cursor.moveToNext()) {
            standalone += Standalone(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getLong(3))
          }
        }

      standalone.groupBy { it.categoryId to it.name.normalizeForSearch() }.forEach { (key, group) ->
        val productId =
          productsByName.getOrPut(key) {
            val newId = UUID.randomUUID().toString()
            db.execSQL(
              "INSERT INTO products (id, categoryId, name, createdAt) VALUES (?, ?, ?, ?)",
              arrayOf<Any>(newId, key.first, group.first().name.trim(), group.minOf { it.expiration }),
            )
            newId
          }
        group.forEach { item ->
          db.execSQL("UPDATE items SET productId = ? WHERE id = ?", arrayOf<Any>(productId, item.id))
        }
      }
    }
  }
