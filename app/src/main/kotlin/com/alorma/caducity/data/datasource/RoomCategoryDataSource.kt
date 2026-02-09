package com.alorma.caducity.data.datasource

import com.alorma.caducity.data.datasource.room.dao.CategoryDao
import com.alorma.caducity.data.datasource.room.dao.ItemDao
import com.alorma.caducity.data.datasource.room.mapper.CategoryRoomMapper
import com.alorma.caducity.data.datasource.room.mapper.ItemRoomMapper
import com.alorma.caducity.domain.CategoryDataSource
import com.alorma.caducity.domain.model.Category
import com.alorma.caducity.domain.model.CategoryWithItems
import com.alorma.caducity.domain.model.Item
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryDataSource(
  private val categoryDao: CategoryDao,
  private val itemDao: ItemDao,
  private val categoryMapper: CategoryRoomMapper,
  private val itemRoomMapper: ItemRoomMapper,
) : CategoryDataSource {

  override fun getCategories(): Flow<ImmutableList<CategoryWithItems>> {
    val daoFlow = categoryDao.getAllCategoriesWithItems()

    return daoFlow.map { roomEntities ->
      roomEntities
        .map { categoryWithItemsRoomEntity ->
          categoryMapper.toModel(categoryWithItemsRoomEntity)
        }
        .toImmutableList()
    }
  }

  override fun getCategory(categoryId: String): Flow<Result<CategoryWithItems>> {
    return categoryDao.getCategoryWithItems(categoryId)
      .map { roomEntity ->
        roomEntity?.let { categoryWithItemsRoomEntity ->
          Result.success(categoryMapper.toModel(categoryWithItemsRoomEntity))
        } ?: Result.failure(NoSuchElementException("Category with id $categoryId not found"))
      }
  }

  override suspend fun createCategory(
    category: Category,
    items: ImmutableList<Item>,
  ) {
    categoryDao.insertCategory(categoryMapper.toEntity(category))
    items.forEach { item ->
      itemDao.insertItem(itemRoomMapper.toEntity(item, category.id))
    }
  }

  override suspend fun deleteCategory(categoryId: String) {
    categoryDao.deleteCategory(categoryId)
  }
}
