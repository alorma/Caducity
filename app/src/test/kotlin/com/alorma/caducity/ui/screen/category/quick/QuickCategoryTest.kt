package com.alorma.caducity.ui.screen.category.quick

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.alorma.caducity.config.clock.AppClock
import com.alorma.caducity.config.preferences.SoonThresholdPreferences
import com.alorma.caducity.config.time.date
import com.alorma.caducity.data.datasource.RoomCategoryDataSource
import com.alorma.caducity.data.datasource.RoomPantryDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
import com.alorma.caducity.domain.model.PantryError
import com.alorma.caducity.domain.usecase.AppExpirationThresholdsImpl
import com.alorma.caducity.domain.usecase.CreateCategoryUseCase
import com.russhwolf.settings.MapSettings
import kotlin.time.Instant
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isFailure
import strikt.assertions.isSuccess

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class QuickCategoryTest {
  private val clock =
    object : AppClock {
      override fun now(): Instant = Instant.parse("2026-10-11T10:00:00Z")

      override fun nowDate(timeZone: TimeZone): LocalDate = now().date(timeZone)
    }
  private lateinit var db: AppDatabase
  private lateinit var pantry: RoomPantryDataSource
  private lateinit var create: CreateCategoryUseCase

  @Before
  fun setUp() {
    db =
      Room
        .inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    val thresholds = AppExpirationThresholdsImpl(SoonThresholdPreferences(MapSettings()))
    val itemMapper = ItemRoomMapper(clock, thresholds)
    val categoryMapper = CategoryRoomMapper(ProductRoomMapper(), itemMapper)
    pantry = RoomPantryDataSource(db, db.categoryDao(), db.itemDao(), db.productDao(), categoryMapper, clock)
    create =
      CreateCategoryUseCase(
        categoryDataSource = RoomCategoryDataSource(db.categoryDao(), db.itemDao(), categoryMapper, itemMapper),
        appClock = clock,
        expirationThresholds = thresholds,
        pantryDataSource = pantry,
      )
  }

  @After
  fun tearDown() = db.close()

  @Test
  fun `creates a category and rejects a duplicate name regardless of case and accents`() =
    runTest {
      expectThat(create.createCategory("Lactics", "")).isSuccess()
      expectThat(create.createCategory(" lÀctics ", "")).isFailure().isA<PantryError.DuplicateCategoryName>()
      expectThat(pantry.getCategories()).hasSize(1)
    }
}
