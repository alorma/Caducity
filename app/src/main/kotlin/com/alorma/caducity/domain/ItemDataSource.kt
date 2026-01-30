package com.alorma.caducity.domain

import com.alorma.caducity.domain.model.Item
import kotlinx.coroutines.flow.Flow

interface ItemDataSource {
  fun getAllItems(): Flow<List<Item>>
}