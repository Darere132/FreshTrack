package com.example.freshtrack.data

import kotlinx.coroutines.flow.Flow

class OfflineItemRepository(
    private val itemDao: ItemDao
) : ItemRepository {

    override fun getAllItemsStream(): Flow<List<ItemEntity>> = itemDao.getAllItems()

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
}