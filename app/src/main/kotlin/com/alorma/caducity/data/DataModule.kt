package com.alorma.caducity.data

import com.alorma.caducity.data.datasource.RoomCategoryDataSource
import com.alorma.caducity.data.datasource.RoomItemDataSource
import com.alorma.caducity.data.datasource.RoomProductDataSource
import com.alorma.caducity.data.datasource.SettingsNotificationConfigDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ProductRoomMapper
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

val dataModule =
  module {

    // DAOs
    single { get<AppDatabase>().categoryDao() }
    single { get<AppDatabase>().itemDao() }
    single { get<AppDatabase>().productDao() }

    // Specialized Mappers (each with their own responsibilities)
    factoryOf(::CategoryRoomMapper)
    factoryOf(::ProductRoomMapper)
    factoryOf(::ItemRoomMapper)

    // Item Comparator
    singleOf(::StatusItemComparator) bind ItemComparator::class

    // Data Sources
    singleOf(::RoomCategoryDataSource) bind CategoryDataSource::class
    singleOf(::RoomItemDataSource) bind ItemDataSource::class
    singleOf(::RoomProductDataSource) bind ProductDataSource::class
    singleOf(::SettingsNotificationConfigDataSource) bind NotificationConfigDataSource::class
  }
