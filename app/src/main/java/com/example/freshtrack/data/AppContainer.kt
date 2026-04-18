package com.example.freshtrack.data

import android.content.Context
import com.example.freshtrack.data.local.FreshTrackDatabase
import com.example.freshtrack.data.repository.ItemRepository
import com.example.freshtrack.data.repository.OfflineItemRepository

interface AppContainer {
    val itemRepository: ItemRepository
}

class AppDataContainer(context: Context) : AppContainer {
    private val database = FreshTrackDatabase.getDatabase(context)
    override val itemRepository: ItemRepository by lazy {
        OfflineItemRepository(database.itemDao(), database.categoryDao())
    }
}