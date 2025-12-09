package com.alorma.caducity.data

import com.alorma.caducity.data.datasource.FakeProductDataSource
import com.alorma.caducity.data.datasource.ProductDataSource
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
  singleOf(::FakeProductDataSource) bind ProductDataSource::class
}