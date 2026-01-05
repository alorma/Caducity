package com.alorma.caducity.data

import com.alorma.caducity.data.datasource.FakeNotificationConfigDataSource
import com.alorma.caducity.data.datasource.RoomVariantDataSource
import com.alorma.caducity.domain.ProductDataSource
import com.alorma.caducity.data.datasource.RoomProductDataSource
import com.alorma.caducity.domain.NotificationConfigDataSource
import com.alorma.caducity.domain.VariantDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
  singleOf(::RoomProductDataSource) bind ProductDataSource::class
  singleOf(::RoomVariantDataSource) bind VariantDataSource::class
  singleOf(::FakeNotificationConfigDataSource) bind NotificationConfigDataSource::class
}