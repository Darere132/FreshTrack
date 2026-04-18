package com.example.freshtrack.data

import android.content.Context

interface AppContainer {
    val itemRepository: ItemRepository
}

class AppDataContainer(context: Context) : AppContainer {
    private val database = FreshTrackDatabase.getDatabase(context)
    override val itemRepository: ItemRepository by lazy {
        OfflineItemRepository(database.itemDao())
    }
}