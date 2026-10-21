package com.alorma.caducity.data.datasource.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alorma.caducity.domain.model.CategoryDefaults
import com.alorma.caducity.domain.model.CategoryIcon
import com.alorma.caducity.domain.model.CategoryShape
import com.alorma.caducity.domain.model.SuggestedDurations
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

/**
 * v4 → v5: `categories.soonThresholdDays` becomes `usualDurationDays` and `items.packSize` is gone.
 * - A category keeps the margin it had configured as its usual duration; without one, a category that still looks like a
 *   default one (name, shape and icon) gets the suggested duration; any other gets null.
 * - Every live pack of N becomes N single items (same product, dates and state); consumed/discarded/transformed
 *   packs stay as one item of history.
 * Both tables are rebuilt (create new, copy, drop, rename) since the columns are dropped.
 */
val MIGRATION_4_5 =
  object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
      expandPacks(db)
      rebuildTables(db)
      createItemIndices(db)
    }

    private fun expandPacks(db: SupportSQLiteDatabase) {
      class Pack(
        val id: String,
        val size: Int,
      )

      val packs = mutableListOf<Pack>()
      db
        .query(
          "SELECT id, packSize FROM items WHERE packSize > 1 " +
            "AND consumedDate IS NULL AND discardedDate IS NULL AND transformedDate IS NULL ORDER BY rowid",
        ).use { cursor ->
          while (cursor.moveToNext()) packs += Pack(cursor.getString(0), cursor.getInt(1))
        }
      packs.forEach { pack ->
        repeat(pack.size - 1) {
          db.execSQL(
            "INSERT INTO items (id, categoryId, identifier, productId, expirationDate, pausedDate, remainingDays, " +
              "createdAt, originTransformationId) " +
              "SELECT ?, categoryId, identifier, productId, expirationDate, pausedDate, remainingDays, " +
              "createdAt, originTransformationId FROM items WHERE id = ?",
            arrayOf<Any>(UUID.randomUUID().toString(), pack.id),
          )
        }
      }
    }

    private fun rebuildTables(db: SupportSQLiteDatabase) {
      db.execSQL(
        "CREATE TABLE IF NOT EXISTS `categories_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
          "`description` TEXT NOT NULL, `shape` TEXT NOT NULL DEFAULT 'COOKIE', `icon` TEXT NOT NULL DEFAULT 'eco', " +
          "`sortOrder` INTEGER NOT NULL DEFAULT 0, `usualDurationDays` INTEGER, PRIMARY KEY(`id`))",
      )
      db.execSQL(
        "INSERT INTO categories_new (id, name, description, shape, icon, sortOrder, usualDurationDays) " +
          "SELECT id, name, description, shape, icon, sortOrder, soonThresholdDays FROM categories",
      )

      class Unconfigured(
        val id: String,
        val name: String,
        val shape: String,
        val icon: String,
      )

      val unconfigured = mutableListOf<Unconfigured>()
      db.query("SELECT id, name, shape, icon FROM categories WHERE soonThresholdDays IS NULL").use { cursor ->
        while (cursor.moveToNext()) {
          unconfigured +=
            Unconfigured(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3))
        }
      }
      unconfigured.forEach { category ->
        val shape = CategoryShape.entries.firstOrNull { it.name == category.shape } ?: return@forEach
        val days = SuggestedDurations.forDefaultCategory(category.name, shape, CategoryIcon(category.icon))
        if (days != null) {
          db.execSQL(
            "UPDATE categories_new SET usualDurationDays = ? WHERE id = ?",
            arrayOf<Any>(days, category.id),
          )
        }
      }

      db.execSQL("CREATE TABLE IF NOT EXISTS `items_new` ${ITEMS_V5_COLUMNS}")
      db.execSQL(
        "INSERT INTO items_new (id, categoryId, identifier, productId, expirationDate, pausedDate, remainingDays, " +
          "consumedDate, discardedDate, createdAt, transformedDate, transformationId, originTransformationId) " +
          "SELECT id, categoryId, identifier, productId, expirationDate, pausedDate, remainingDays, " +
          "consumedDate, discardedDate, createdAt, transformedDate, transformationId, originTransformationId FROM items",
      )
      db.execSQL("DROP TABLE items")
      db.execSQL("DROP TABLE categories")
      db.execSQL("ALTER TABLE categories_new RENAME TO categories")
    }

    private fun createItemIndices(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE items_new RENAME TO items")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_categoryId` ON `items` (`categoryId`)")
      db.execSQL("CREATE INDEX IF NOT EXISTS `index_items_productId` ON `items` (`productId`)")
    }
  }

private const val ITEMS_V5_COLUMNS =
  "(`id` TEXT NOT NULL, `categoryId` TEXT NOT NULL, `identifier` TEXT NOT NULL, `productId` TEXT, " +
    "`expirationDate` INTEGER NOT NULL, `pausedDate` INTEGER, `remainingDays` INTEGER, `consumedDate` INTEGER, " +
    "`discardedDate` INTEGER, `createdAt` INTEGER, `transformedDate` INTEGER, `transformationId` TEXT, " +
    "`originTransformationId` TEXT, PRIMARY KEY(`id`), " +
    "FOREIGN KEY(`categoryId`) REFERENCES `categories`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE , " +
    "FOREIGN KEY(`productId`) REFERENCES `products`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL )"

/**
 * v5 → v6: «Es pot congelar». `categories.canFreeze` defaults to true (nothing changes for existing categories) and
 * `products.canFreeze` is null, so every product inherits its category.
 */
val MIGRATION_5_6 =
  object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE categories ADD COLUMN canFreeze INTEGER NOT NULL DEFAULT 1")
      db.execSQL("ALTER TABLE products ADD COLUMN canFreeze INTEGER")
    }
  }

/**
 * v6 → v7: unit type. Every product starts as `UNITS`; `items.amount` and `items.level` are for the measured and
 * handful types and stay null until those arrive.
 */
val MIGRATION_6_7 =
  object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
      db.execSQL("ALTER TABLE products ADD COLUMN unitType TEXT NOT NULL DEFAULT 'UNITS'")
      db.execSQL("ALTER TABLE items ADD COLUMN amount INTEGER")
      db.execSQL("ALTER TABLE items ADD COLUMN level TEXT")
    }
  }
