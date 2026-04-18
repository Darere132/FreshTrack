package com.example.freshtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ItemEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = true
)
abstract class FreshTrackDatabase : RoomDatabase() {

    abstract fun itemDao(): ItemDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var Instance: FreshTrackDatabase? = null

        fun getDatabase(context: Context): FreshTrackDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    FreshTrackDatabase::class.java,
                    "freshtrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}