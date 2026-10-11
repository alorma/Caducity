package com.alorma.caducity.data.datasource.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alorma.caducity.domain.model.CategoryDefaults
import java.util.UUID

val MIGRATION_1_2 =
  object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE items ADD COLUMN packSize INTEGER")
    }
  }

/**
 * v2 → v3: category shape/icon/sortOrder, item discardedDate/createdAt, items without product
 * converted to products and `identifier` cleared. Only ADD COLUMN + UPDATE/INSERT (no table rebuilds).
 */
val MIGRATION_2_3 =
  object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE categories ADD COLUMN shape TEXT NOT NULL DEFAULT 'COOKIE'")
      db.execSQL("ALTER TABLE categories ADD COLUMN icon TEXT NOT NULL DEFAULT 'eco'")
      db.execSQL("ALTER TABLE categories ADD COLUMN sortOrder INTEGER NOT NULL DEFAULT 0")
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
      class Group(
        val categoryId: String,
        val name: String,
        val firstExpiration: Long,
      )

      val groups = mutableListOf<Group>()
      db
        .query(
          "SELECT categoryId, identifier, MIN(expirationDate) FROM items " +
            "WHERE productId IS NULL AND identifier <> '' GROUP BY categoryId, identifier",
        ).use { cursor ->
          while (cursor.moveToNext()) {
            groups += Group(cursor.getString(0), cursor.getString(1), cursor.getLong(2))
          }
        }
      groups.forEach { group ->
        val productId = UUID.randomUUID().toString()
        db.execSQL(
          "INSERT INTO products (id, categoryId, name, createdAt) VALUES (?, ?, ?, ?)",
          arrayOf<Any>(productId, group.categoryId, group.name, group.firstExpiration),
        )
        db.execSQL(
          "UPDATE items SET productId = ? WHERE productId IS NULL AND categoryId = ? AND identifier = ?",
          arrayOf<Any>(productId, group.categoryId, group.name),
        )
      }
    }
  }
