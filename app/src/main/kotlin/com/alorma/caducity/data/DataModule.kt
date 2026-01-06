package com.alorma.caducity.data

import com.alorma.caducity.data.datasource.FakeNotificationConfigDataSource
import com.alorma.caducity.data.datasource.InstanceRoomMapper
import com.alorma.caducity.data.datasource.RoomProductDataSource
import com.alorma.caducity.data.datasource.RoomVariantDataSource
import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.RoomInstanceDataSource
import com.alorma.caducity.domain.InstanceDataSource
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.domain.VariantDataSource
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {

  single { get<AppDatabase>().productDao() }
  single { get<AppDatabase>().instanceDao() }
  single { get<AppDatabase>().variantDao() }

  factoryOf(::InstanceRoomMapper)

  singleOf(::RoomProductDataSource) bind ProductDataSource::class
  singleOf(::RoomInstanceDataSource) bind InstanceDataSource::class
  singleOf(::RoomVariantDataSource) bind VariantDataSource::class
  singleOf(::FakeNotificationConfigDataSource) bind NotificationConfigDataSource::class
}