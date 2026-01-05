package com.alorma.caducity.data.datasource.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
  override fun migrate(database: SupportSQLiteDatabase) {
    // 1. Create variants table
    database.execSQL("""
      CREATE TABLE IF NOT EXISTS variants (
        id TEXT NOT NULL PRIMARY KEY,
        productId TEXT NOT NULL,
        name TEXT NOT NULL,
        createdAt INTEGER NOT NULL,
        FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE
      )
    """)
    database.execSQL("CREATE INDEX IF NOT EXISTS index_variants_productId ON variants(productId)")

    // 2. Migrate existing identifiers to variants
    // Get distinct identifier/productId combinations from non-consumed instances
    database.execSQL("""
      INSERT INTO variants (id, productId, name, createdAt)
      SELECT
        lower(hex(randomblob(16))),
        productId,
        identifier,
        MIN(expirationDate)
      FROM product_instances
      WHERE consumedDate IS NULL
      GROUP BY productId, identifier
    """)

    // 3. Add variantId column to product_instances
    database.execSQL("ALTER TABLE product_instances ADD COLUMN variantId TEXT")

    // 4. Populate variantId based on identifier match
    database.execSQL("""
      UPDATE product_instances
      SET variantId = (
        SELECT v.id
        FROM variants v
        WHERE v.productId = product_instances.productId
        AND v.name = product_instances.identifier
        LIMIT 1
      )
      WHERE consumedDate IS NULL
    """)

    // 5. Add index for variantId
    database.execSQL("CREATE INDEX IF NOT EXISTS index_product_instances_variantId ON product_instances(variantId)")
  }
}
