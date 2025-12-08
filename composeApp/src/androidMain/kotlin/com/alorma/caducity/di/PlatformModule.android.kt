package com.alorma.caducity.di

import com.alorma.caducity.data.datasource.ProductDataSource
import com.alorma.caducity.data.datasource.RoomProductDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

actual val platformModule = module {
  singleOf(::RoomProductDataSource) bind ProductDataSource::class
}
