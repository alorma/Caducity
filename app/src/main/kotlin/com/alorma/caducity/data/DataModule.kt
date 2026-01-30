package com.alorma.caducity.data

import com.alorma.caducity.data.datasource.FakeNotificationConfigDataSource
import com.alorma.caducity.data.datasource.ItemRoomMapper
import com.alorma.caducity.data.datasource.RoomCategoryDataSource
import com.alorma.caducity.data.datasource.RoomProductDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.RoomItemDataSource
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.ItemDataSource
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.model.ItemComparator
import com.alorma.caducity.domain.model.StatusItemComparator
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {

  single { get<AppDatabase>().categoryDao() }
  single { get<AppDatabase>().itemDao() }
  single { get<AppDatabase>().productDao() }

  factoryOf(::ItemRoomMapper)

  singleOf(::StatusItemComparator) bind ItemComparator::class

  singleOf(::RoomCategoryDataSource) bind CategoryDataSource::class
  singleOf(::RoomItemDataSource) bind ItemDataSource::class
  singleOf(::RoomProductDataSource) bind ProductDataSource::class
  singleOf(::FakeNotificationConfigDataSource) bind NotificationConfigDataSource::class
}