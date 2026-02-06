package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.datasource.room.AppDatabase
import com.alorma.caducity.data.datasource.room.RoomEntityMapper
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryDataSource(
  database: AppDatabase,
  private val mapper: RoomEntityMapper,
) : CategoryDataSource {

  private val categoryDao = database.categoryDao()
  private val itemDao = database.itemDao()

  override fun getCategories(): Flow<ImmutableList<CategoryWithItems>> {
    val daoFlow = categoryDao.getAllCategoriesWithItems()

    return daoFlow.map { roomEntities ->
      roomEntities
        .map { mapper.mapCategoryWithItemsToModel(it) }
        .toImmutableList()
    }
  }

  override fun getCategory(categoryId: String): Flow<Result<CategoryWithItems>> {
    return categoryDao.getCategoryWithItems(categoryId)
      .map { roomEntity ->
        roomEntity?.let {
          Result.success(mapper.mapCategoryWithItemsToModel(it))
        } ?: Result.failure(NoSuchElementException("Category with id $categoryId not found"))
      }
  }

  override suspend fun createCategory(
    category: Category,
    items: ImmutableList<Item>,
  ) {
    categoryDao.insertCategory(mapper.mapCategoryToEntity(category))
    items.forEach { item ->
      itemDao.insertItem(mapper.mapItemToEntity(item, category.id))
    }
  }

  override suspend fun deleteCategory(categoryId: String) {
    categoryDao.deleteCategory(categoryId)
  }
}