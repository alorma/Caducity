package com.alorma.caducity.di

import androidx.room.Room
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.MIGRATION_1_2
import com.alorma.caducity.data.datasource.room.MIGRATION_2_3
import com.alorma.caducity.data.datasource.room.MIGRATION_3_4
import com.alorma.caducity.data.datasource.room.MIGRATION_4_5
import com.alorma.caducity.data.datasource.room.MIGRATION_5_6
import com.alorma.caducity.feature.debug.AndroidDebugModeProvider
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.feature.notification.notificationsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule: Module =
  module {
    includes(notificationsModule)

    single {
      Room
        .databaseBuilder(
          androidContext(),
          AppDatabase::class.java,
          "caducity_database.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
        .build()
    }

    singleOf(::AndroidDebugModeProvider) {
      bind<DebugModeProvider>()
    }
  }
