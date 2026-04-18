package com.example.freshtrack.data.repository

import com.example.freshtrack.data.local.dao.CategoryDao
import com.example.freshtrack.data.local.dao.ItemDao
import com.example.freshtrack.data.local.model.CategoryEntity
import com.example.freshtrack.data.local.model.ItemEntity
import com.example.freshtrack.data.local.model.ItemWithCategory
import kotlinx.coroutines.flow.Flow

class OfflineItemRepository(
    private val itemDao: ItemDao,
    private val categoryDao: CategoryDao
) : ItemRepository {

    override fun getAllItemsStream(): Flow<List<ItemEntity>> = itemDao.getAllItems()

    override fun getAllItemsWithCategoryStream(): Flow<List<ItemWithCategory>> {
        return itemDao.getAllItemsWithCategoryStream()
    }

    override suspend fun getItem(id: Int): ItemEntity? = itemDao.getItemById(id)

    override suspend fun insertItem(itemEntity: ItemEntity) {
        itemDao.insertItem(itemEntity)
    }

    override suspend fun updateItem(itemEntity: ItemEntity) {
        itemDao.updateItem(itemEntity)
    }

    override suspend fun deleteItem(itemEntity: ItemEntity) {
        itemDao.deleteItem(itemEntity)
    }

    override suspend fun markConsumed(id: Int) {
        itemDao.markConsumed(id)
    }

    override fun getCategoriesStream(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()
}