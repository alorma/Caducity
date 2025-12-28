package com.alorma.caducity.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.alorma.caducity.data.datasource.FakeNotificationConfigDataSource
import com.alorma.caducity.data.datasource.NotificationConfigDataSource
import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.data.datasource.RoomProductDataSource
import com.alorma.caducity.data.room.AppDatabase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
  single {
    Room.databaseBuilder(
      context = androidContext(),
      klass = AppDatabase::class.java,
      name = "caducity.db"
    )
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.IO)
//      .addCallback(DatabaseCallback())
      .build()
  }

  singleOf(::RoomProductDataSource) bind ProductDataSource::class
  singleOf(::FakeNotificationConfigDataSource) bind NotificationConfigDataSource::class
}