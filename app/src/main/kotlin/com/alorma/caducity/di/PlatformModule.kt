package com.alorma.caducity.di

import androidx.room.Room
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.feature.debug.AndroidDebugModeProvider
import com.alorma.caducity.feature.debug.DebugModeProvider
import com.alorma.caducity.feature.notification.notificationsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val platformModule: Module = module {
  includes(notificationsModule)

  single {
    Room.databaseBuilder(
      androidContext(),
      AppDatabase::class.java,
      "caducity_database.db"
    )
      .fallbackToDestructiveMigration(true)
      .build()
  }


  singleOf(::AndroidDebugModeProvider) {
    bind<DebugModeProvider>()
  }

}
