package com.example.freshtrack.data.local.dao

import androidx.room.*
import com.example.freshtrack.data.local.model.ItemEntity
import com.example.freshtrack.data.local.model.ItemWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY expirationDate ASC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Transaction
    @Query("SELECT * FROM items ORDER BY expirationDate ASC")
    fun getAllItemsWithCategoryStream(): Flow<List<ItemWithCategory>>

    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Int): ItemEntity?

    // Jednorazové čítanie pre Worker (bez Flow)
    @Query("SELECT * FROM items WHERE isConsumed = 0")
    suspend fun getActiveItemsOnce(): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(itemEntity: ItemEntity): Long

    @Update
    suspend fun updateItem(itemEntity: ItemEntity)

    @Delete
    suspend fun deleteItem(itemEntity: ItemEntity)

    @Query("UPDATE items SET isConsumed = 1 WHERE id = :id")
    suspend fun markConsumed(id: Int)
}