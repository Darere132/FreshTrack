package com.example.freshtrack.data.repository

import com.example.freshtrack.data.local.model.CategoryEntity
import com.example.freshtrack.data.local.model.ItemEntity
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun getAllItemsStream(): Flow<List<ItemEntity>>
    suspend fun getItem(id: Int): ItemEntity?
    suspend fun insertItem(itemEntity: ItemEntity)
    suspend fun updateItem(itemEntity: ItemEntity)
    suspend fun deleteItem(itemEntity: ItemEntity)
    suspend fun markConsumed(id: Int)

    fun getCategoriesStream(): Flow<List<CategoryEntity>>
}