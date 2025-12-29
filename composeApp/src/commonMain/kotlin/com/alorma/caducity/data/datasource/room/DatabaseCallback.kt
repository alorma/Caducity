package com.alorma.caducity.data.datasource.room

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class DatabaseCallback : RoomDatabase.Callback() {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onCreate(connection: SQLiteConnection) {
    super.onCreate(connection)

    // Pre-populate database with sample data
    scope.launch {
      populateDatabase(connection)
    }
  }

  private fun populateDatabase(connection: SQLiteConnection) {
    val now = Clock.System.now().toEpochMilliseconds()
    val oneDayMs = 1.days.inWholeMilliseconds
    val threeDaysMs = 3.days.inWholeMilliseconds
    val sevenDaysMs = 7.days.inWholeMilliseconds
    val fourteenDaysMs = 14.days.inWholeMilliseconds

    // Insert sample products
    connection.execSQL("""
      INSERT INTO products (id, name, description) VALUES
      ('1', 'Milk', 'Fresh whole milk'),
      ('2', 'Eggs', 'Free-range eggs'),
      ('3', 'Bread', 'Whole wheat bread'),
      ('4', 'Cheese', 'Cheddar cheese block'),
      ('5', 'Yogurt', 'Greek yogurt')
    """.trimIndent())

    // Insert sample product instances with various expiration dates
    connection.execSQL("""
      INSERT INTO product_instances (id, productId, identifier, expirationDate) VALUES
      ('i1', '1', 'MILK-001', ${now - oneDayMs}),
      ('i2', '1', 'MILK-002', ${now + threeDaysMs}),
      ('i3', '2', 'EGGS-001', ${now - (2 * oneDayMs)}),
      ('i4', '2', 'EGGS-002', ${now + sevenDaysMs}),
      ('i5', '3', 'BREAD-001', ${now + oneDayMs}),
      ('i6', '3', 'BREAD-002', ${now + fourteenDaysMs}),
      ('i7', '4', 'CHEESE-001', ${now + (10 * oneDayMs)}),
      ('i8', '5', 'YOGURT-001', ${now + (2 * oneDayMs)}),
      ('i9', '5', 'YOGURT-002', ${now - (3 * oneDayMs)})
    """.trimIndent())
  }
}
