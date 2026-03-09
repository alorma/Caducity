package com.alorma.caducity.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.feature.debug.AndroidDebugModeProvider
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.feature.notification.notificationsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val MIGRATION_1_2 =
  object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
      // Add packSize column to items table
      db.execSQL("ALTER TABLE items ADD COLUMN packSize INTEGER")
    }
  }

val platformModule: Module =
  module {
    includes(notificationsModule)

    single {
      Room
        .databaseBuilder(
          androidContext(),
          AppDatabase::class.java,
          "caducity_database.db",
        ).addMigrations(MIGRATION_1_2)
        .build()
    }

    singleOf(::AndroidDebugModeProvider) {
      bind<DebugModeProvider>()
    }
  }
